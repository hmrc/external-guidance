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
  def guidancePages(pageBuilder: PageBuilder, jsObject: JsObject)
                   (implicit c: AppConfig, spd: SecuredProcessBuilder): RequestOutcome[(Process, Seq[Page], JsObject)] =
    jsObject.validate[Process].fold(errs => Left(Error(GuidanceError.fromJsonValidationErrors(errs))),
      incomingProcess => {
        // Transform process if fake welsh required and/or secured process is indicated
        val (p, js) = fakeWelshTextIfRequired _ tupled securedProcessIfRequired(incomingProcess, jsObject)
        pageBuilder.pagesWithValidation(p, p.startPageId).fold(
          errs => Left(Error(errs)),
          pages => Right((p, pages, js))
        )
      }
    )

  private[services] def fakeWelshTextIfRequired(process: Process, jsObject: JsObject)(implicit c: AppConfig): (Process, JsObject) =
    if (process.passPhrase.isDefined || c.fakeWelshInUnauthenticatedGuidance) {
      val fakedWelshProcess = process.copy(phrases = process.phrases.map(p => if (p.welsh.trim.isEmpty) Phrase(p.english, s"Welsh, ${p.english}") else p))
      (fakedWelshProcess, Json.toJsObject(fakedWelshProcess))
    } else (process, jsObject)

  private[services] def securedProcessIfRequired(process: Process, jsObject: JsObject)(implicit spb: SecuredProcessBuilder): (Process, JsObject) =
    process.passPhrase.fold((process, jsObject)){_ =>
      val securedProcess = spb.secure(process)
      (securedProcess, Json.toJsObject(securedProcess))
    }
}
