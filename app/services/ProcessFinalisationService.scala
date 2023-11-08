/*
 * Copyright 2023 HM Revenue & Customs
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

package services

import javax.inject.{Inject, Singleton}
import core.models.errors.Error
import core.models.ocelot.errors._
import core.models._
import core.models.ocelot.{Phrase, SecuredProcess, Process, Page}
import core.models.ocelot.stanzas.{Value, Stanza}
import play.api.libs.json._
import scala.concurrent.{Future, ExecutionContext}
import config.AppConfig
import play.api.Logging
import core.services.EncrypterService

@Singleton
class ProcessFinalisationService @Inject() (
    appConfig: AppConfig,
    vpb: ValidatingPageBuilder, 
    timescalesService: TimescalesService,
    encrypter: EncrypterService) extends Logging {


  def guidancePagesAndProcess(jsObject: JsObject, checkLevel: GuidanceCheckLevel = Strict)
                             (implicit c: AppConfig, ec: ExecutionContext): Future[RequestOutcome[(Process, Seq[Page], JsObject)]] =
    jsObject.validate[Process].fold(errs => Future.successful(Left(Error(GuidanceError.fromJsonValidationErrors(errs)))),
      incomingProcess => {
        // Transform process if fake welsh, secured process or timescales are indicated
        val (p, js) = fakeWelshTextIfRequired _ tupled securedProcessIfRequired(incomingProcess, Some(jsObject))
        vpb.pagesWithValidation(p, p.startPageId, checkLevel).fold(
          errs => Future.successful(Left(Error(errs))),
          pages => {
            // If valid process, collect list of timescale ids from process flow and phrases
            val timescaleIds = (vpb.pageBuilder.timescales.referencedNonPhraseIds(incomingProcess.flow) ++
              vpb.pageBuilder.timescales.referencedIds(incomingProcess.phrases)).distinct
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
                    Future.successful(Left(Error(timescaleIds.filterNot(timescales.contains).map(MissingTimescaleDefinition))))
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
      val encryptedPhrase = encrypter.encrypt(passPhrase)                                          // Encrypt password
      val securedProcess = p.copy(meta = p.meta.copy(encryptedPassPhrase = Some(encryptedPhrase)), // Add optional passphrase to process meta section
                                  flow = updateFlowPassPhrase(p, encryptedPhrase))
      (securedProcess, None)
    }

  private[services] def updateFlowPassPhrase(p: Process, encryptedPassPhrase: String): Map[String, Stanza] = 
    p.passphraseValueStanza.fold(p.flow){
      case (id, valueStanza) =>
        val updatedValues = valueStanza.values.map{
          case v: Value if v.label == SecuredProcess.PassPhraseLabelName => v.copy(value = encryptedPassPhrase)
          case v => v
        }
        p.flow.filterNot(p => p._1 == id) ++ Map(id -> valueStanza.copy(values = updatedValues))
    }
}


