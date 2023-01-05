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

import base.BaseSpec
import mocks.mockAppConfig
import core.models.ocelot._
import play.api.libs.json._
import core.services._
import mocks.MockAppConfig
import mocks.MockTimescalesService
import scala.concurrent.{Future, ExecutionContext}

class PackageObjectSpec extends BaseSpec with ProcessJson {
  "Faking welsh text" should {
    "Add fake welsh to all passphrase protected guidance" in {
      val json = Some(validOnePageProcessWithPassPhrase.as[JsObject])
      val process: Process = validOnePageProcessWithPassPhrase.as[Process]
      val withMissingWelsh = process.copy(phrases = process.phrases.map(p => Phrase(p.english, "")))
      val (fakedProcess, _) = fakeWelshTextIfRequired(withMissingWelsh, json)(mockAppConfig)

      fakedProcess.phrases shouldBe process.phrases
    }

    "Not Add fake welsh to unauthenticated guidance when not configured to" in {
      val process: Process = validOnePageJson.as[Process]
      val withMissingWelsh = process.copy(phrases = process.phrases.map(p => Phrase(p.english, "")))
      val jsObjectWithMissingwelsh = Some(Json.toJsObject(withMissingWelsh))
      val configFakeWelshFalse = mockAppConfig.copy(fakeWelshInUnauthenticatedGuidance = false)
      val (fakedProcess, _) = fakeWelshTextIfRequired(withMissingWelsh, jsObjectWithMissingwelsh)(configFakeWelshFalse)

      fakedProcess.phrases shouldBe withMissingWelsh.phrases
    }

    "Add fake welsh to unauthenticated guidance when configured to" in {
      val process: Process = validOnePageJson.as[Process]
      val jsObject = Some(validOnePageJson.as[JsObject])
      val withMissingWelsh = process.copy(phrases = process.phrases.map(p => Phrase(p.english, "")))
      val (fakedProcess, _) = fakeWelshTextIfRequired(withMissingWelsh, jsObject)(mockAppConfig)

      fakedProcess.phrases shouldBe process.phrases
    }

    "Return the original process and JsObject unchanged if welsh already exists within the process" in {
      val process: Process = validOnePageJson.as[Process]
      val jsObject = Some(validOnePageJson.as[JsObject])
      val configFakeWelshFalse = mockAppConfig.copy(fakeWelshInUnauthenticatedGuidance = false)
      val (fakedProcess, fakedJsObject) = fakeWelshTextIfRequired(process, jsObject)(configFakeWelshFalse)

      fakedProcess shouldBe process
      fakedJsObject shouldBe jsObject
    }

  }

  trait Test extends MockTimescalesService {
    implicit val ec: ExecutionContext = ExecutionContext.global
    val timescales = new Timescales(new DefaultTodayProvider)
    val validatingPageBuilder = new ValidatingPageBuilder(new PageBuilder(timescales))
  }

  "guidancePagesAndProcess" should {
    "Add a complete timescales table to process and json" in new Test {
      val process: Process = rawOcelotTimescalesJson.as[Process]

      process.timescales shouldBe Map()

      MockTimescalesService.get().returns(Future.successful(Right(Map("JRSProgChaseCB" -> 0, "CHBFLCertabroad" -> 0, "JRSRefCB" -> 0))))

      whenReady(guidancePagesAndProcess(validatingPageBuilder, rawOcelotTimescalesJson.as[JsObject], mockTimescalesService)(MockAppConfig, ec)){
        case Left(err) => fail(s"Failed with $err")
        case Right((updatedProcess, pages, updatedJsObject)) =>
          updatedProcess.timescales shouldBe Map("JRSProgChaseCB" -> 0, "CHBFLCertabroad" -> 0, "JRSRefCB" -> 0)

          (updatedJsObject.as[Process]).timescales shouldBe Map("JRSProgChaseCB" -> 0, "CHBFLCertabroad" -> 0, "JRSRefCB" -> 0)
      }
    }
  }
}
