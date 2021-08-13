/*
 * Copyright 2021 HM Revenue & Customs
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

import core.services._
import core.models.ocelot._
import core.models.errors.Error
import core.models.ocelot.errors._
import core.models.RequestOutcome
import core.models.ocelot.Process
import play.api.libs.json._
import config.AppConfig

package object services {

  def guidancePagesAndProcess(pb: ValidatingPageBuilder, jsObject: JsObject)
                   (implicit c: AppConfig): RequestOutcome[(Process, Seq[Page], JsObject)] =
    jsObject.validate[Process].fold(errs => Left(Error(GuidanceError.fromJsonValidationErrors(errs))),
      incomingProcess => {
        // Transform process if fake welsh and/or secured process is indicated
        val (p, js) = fakeWelshTextIfRequired _ tupled securedProcessIfRequired(incomingProcess, Some(jsObject))
        pb.pagesWithValidation(p, p.startPageId).fold(
          errs => Left(Error(errs)),
          pages => {
            val timescaleIds = pages.toList.flatMap(p => pb.pageBuilder.timescales.referencedIds(p))
            val (process, jsObject) = addTimescaleTableIfRequired(p, js, timescaleIds)
            Right((process, pages, jsObject.fold(Json.toJsObject(process))(json => json)))
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

  private[services] def addTimescaleTableIfRequired(p: Process, jsObject: Option[JsObject], timescaleIds: List[String]): (Process, Option[JsObject]) =
    timescaleIds match {
      case Nil => (p, jsObject)
      case _ =>
        val updatedProcess = p.copy(timescales = timescaleIds.map(id => (id, 0)).toMap)
        (updatedProcess, None)
    }
}
