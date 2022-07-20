/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import core.models.ocelot._
import core.models.errors.Error
import models.errors._
import core.models.ocelot.errors._
import core.models.RequestOutcome
import core.models.ocelot.Process
import play.api.libs.json._
import config.AppConfig
import scala.concurrent.{Future, ExecutionContext}

package object services {

  def guidancePagesAndProcess(pb: ValidatingPageBuilder, jsObject: JsObject, timescalesService: TimescalesService)
                   (implicit c: AppConfig, ec: ExecutionContext): Future[RequestOutcome[(Process, Seq[Page], JsObject)]] =
    jsObject.validate[Process].fold(errs => Future.successful(Left(Error(processErrs(GuidanceError.fromJsonValidationErrors(errs))))),
      incomingProcess => {
        // Transform process if fake welsh, secured process or timescales are indicated
        val (p, js) = fakeWelshTextIfRequired _ tupled securedProcessIfRequired(incomingProcess, Some(jsObject))
        pb.pagesWithValidation(p, p.startPageId).fold(
          errs => Future.successful(Left(Error(processErrs(errs)))),
          pages => {
            // If valid process, collect list of timescale ids from process flow and phrases
            val timescaleIds = (pb.pageBuilder.timescales.referencedNonPhraseIds(incomingProcess.flow) ++
                                pb.pageBuilder.timescales.referencedIds(incomingProcess.phrases)).distinct
            timescaleIds match {
              case Nil => Future.successful(Right((p, pages, js.fold(Json.toJsObject(p))(json => json))))
              case _ =>
                timescalesService.get().flatMap{
                  case Left(err) => Future.successful(Left(err))
                  case Right(timescales) if timescaleIds.forall(id => timescales.contains(id)) =>
                    // All timescales used in process are currently available from the timescales service
                    val updatedProcess = p.copy(timescales = timescaleIds.map(id => (id, 0)).toMap)
                    Future.successful(Right((updatedProcess, pages, Json.toJsObject(updatedProcess))))
                  case Right(timescales) =>
                    Future.successful(Left(Error(processErrs(timescaleIds.filterNot(timescales.contains).map(MissingTimescaleDefinition)))))
                }
            }
          }
        )
      }
    )

  private[services] def fakeWelshTextIfRequired(process: Process, jsObject: Option[JsObject])(implicit c: AppConfig): (Process,  Option[JsObject]) =
    if (process.passPhrase.isDefined || c.fakeWelshInUnauthenticatedGuidance) {
      val fakedWelshProcess = process.copy(phrases = process.phrases.map(p => if (p.welsh.trim.isEmpty) Phrase(p.english, s"Welsh: ${p.english}") else p))
      (fakedWelshProcess, None)
    } else (process, jsObject)

  private[services] def securedProcessIfRequired(p: Process, jsObject: Option[JsObject]): (Process, Option[JsObject]) =
    p.valueStanzaPassPhrase.fold((p, jsObject)){passPhrase =>
      // Add optional passphrase to process meta section
      val securedProcess = p.copy(meta = p.meta.copy(passPhrase = Some(passPhrase)))
      (securedProcess, None)
    }
}
