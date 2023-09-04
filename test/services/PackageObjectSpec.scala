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
import core.models.errors.Error
import core.models.errors.Error._
import mocks.mockAppConfig
import core.models.ocelot._
import core.models.ocelot.errors.LanguageLinkIdsDiffer
import play.api.libs.json._
import core.services._
import mocks.MockAppConfig
import mocks.MockTimescalesService

import scala.concurrent.{ExecutionContext, Future}

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

    val jsonWithDiffLangIds: JsValue = Json.parse(
      """
        |{
        |  "meta": {
        |    "id": "oct90001",
        |    "title": "Customer wants to make a cup of tea",
        |    "ocelot": 1,
        |    "lastAuthor": "000000",
        |    "lastUpdate": 1500298931016,
        |    "version": 4,
        |    "filename": "oct90001.js",
        |    "titlePhrase": 8,
        |    "processCode": "cup-of-tea"
        |  },
        |  "flow": {
        |    "start": {
        |      "type": "PageStanza",
        |      "url": "/feeling-bad",
        |      "next": ["33"],
        |      "stack": true
        |    },
        |    "33": {
        |      "next": [
        |        "3"
        |      ],
        |      "noteType": "Title",
        |      "stack": false,
        |      "text": 1,
        |      "type": "CalloutStanza"
        |    },
        |    "3": {
        |      "type": "InstructionStanza",
        |      "text": 1,
        |      "next": [
        |        "2"
        |      ],
        |      "stack": true
        |    },
        |    "2": {
        |      "type": "InstructionStanza",
        |      "text": 0,
        |      "next": [
        |        "end"
        |      ],
        |      "stack": true
        |    },
        |    "end": {
        |      "type": "EndStanza"
        |    }
        |  },
        |  "phrases": [
        |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
        |    ["Do you have a tea bag [link:Change:start]?", "Welsh: Do you have a tea bag [link:Change:99]?"],
        |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
        |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
        |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
        |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
        |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
        |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
        |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
        |  ],
        |  "links": [],
        |  "timescales": {}
        |}
    """.stripMargin
    )

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

    "detect mismatched English and Welsh Link ids" in new Test {

      MockTimescalesService.get().returns(Future.successful(Right(Map())))

      whenReady(guidancePagesAndProcess(validatingPageBuilder, jsonWithDiffLangIds.as[JsObject], mockTimescalesService)(MockAppConfig, ec)){
        case Left(Error(List(LanguageLinkIdsDiffer("", "1")))) =>
          succeed
        case Right(_) =>
          fail()

      }
    }
  }

}
