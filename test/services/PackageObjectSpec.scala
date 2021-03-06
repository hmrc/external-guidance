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

package services

import base.BaseSpec
import mocks.mockAppConfig
import core.models.ocelot._
import play.api.libs.json._

class PackageObjectSpec extends BaseSpec with ProcessJson {
  "Faking welsh text" should {
    "Add fake welsh to all passphrase protected guidance" in {
      val json = validOnePageProcessWithPassPhrase.as[JsObject]
      val process: Process = validOnePageProcessWithPassPhrase.as[Process]
      val withMissingWelsh = process.copy(phrases = process.phrases.map(p => Phrase(p.english, "")))
      val (fakedProcess, _) = fakeWelshTextIfRequired(withMissingWelsh, json)(mockAppConfig)

      fakedProcess.phrases shouldBe process.phrases
    }

    "Not Add fake welsh to unauthenticated guidance when not configured to" in {
      val process: Process = validOnePageJson.as[Process]
      val withMissingWelsh = process.copy(phrases = process.phrases.map(p => Phrase(p.english, "")))
      val jsObjectWithMissingwelsh = Json.toJsObject(withMissingWelsh)
      val configFakeWelshFalse = mockAppConfig.copy(fakeWelshInUnauthenticatedGuidance = false)
      val (fakedProcess, _) = fakeWelshTextIfRequired(withMissingWelsh, jsObjectWithMissingwelsh)(configFakeWelshFalse)

      fakedProcess.phrases shouldBe withMissingWelsh.phrases
    }

    "Add fake welsh to unauthenticated guidance when configured to" in {
      val process: Process = validOnePageJson.as[Process]
      val jsObject = validOnePageJson.as[JsObject]
      val withMissingWelsh = process.copy(phrases = process.phrases.map(p => Phrase(p.english, "")))
      val (fakedProcess, _) = fakeWelshTextIfRequired(withMissingWelsh, jsObject)(mockAppConfig)

      fakedProcess.phrases shouldBe process.phrases
    }

    "Return the original process and JsObject unchanged if welsh already exists within the process" in {
      val process: Process = validOnePageJson.as[Process]
      val jsObject = validOnePageJson.as[JsObject]
      val configFakeWelshFalse = mockAppConfig.copy(fakeWelshInUnauthenticatedGuidance = false)
      val (fakedProcess, fakedJsObject) = fakeWelshTextIfRequired(process, jsObject)(configFakeWelshFalse)

      fakedProcess shouldBe process
      fakedJsObject shouldBe jsObject
    }

  }
}
