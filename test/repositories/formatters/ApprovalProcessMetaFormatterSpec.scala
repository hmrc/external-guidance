/*
 * Copyright 2020 HM Revenue & Customs
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

package repositories.formatters

import java.time.{LocalDate, ZonedDateTime}

import base.UnitSpec
import models.{ApprovalProcessJson, ApprovalProcessMeta}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import repositories.formatters.ApprovalProcessMetaFormatter.mongoFormat
import utils.Constants._

class ApprovalProcessMetaFormatterSpec extends UnitSpec with ApprovalProcessJson {

  private val invalidJson = Json.parse("{}")
  private val dateLong = 1583193600000L
  private val validString = s"""
    |  {
    |    "id" : "oct90001",
    |    "title" : "This is the title",
    |    "status" : "$StatusSubmitted",
    |    "dateSubmitted" : {"$$date": $dateLong},
    |    "lastModified" : {"$$date": $dateLong},
    |    "ocelotDateSubmitted" : 1,
    |    "ocelotVersion" : 1,
    |    "reviewType" : "$ReviewType2i",
    |    "processCode" : "processCode"
    |  }
    """.stripMargin
  private val validMetaJson = Json.parse(validString)

  private val validStringWithoutLastModified = s"""
    |  {
    |    "id" : "oct90001",
    |    "title" : "This is the title",
    |    "status" : "$StatusSubmitted",
    |    "dateSubmitted" : {"$$date": $dateLong},
    |    "reviewType" : "$ReviewType2i"
    |  }
    """.stripMargin
  private val validMetaJsonWithoutLastModified = Json.parse(validStringWithoutLastModified)

  "Deserializing a JSON payload into an instance of ApprovalProcessMeta" should {

    "Result in a successful conversion for valid JSON" in {

      validMetaJson.validate[ApprovalProcessMeta] match {
        case JsSuccess(result, _) =>
          val dateToCompare = LocalDate.of(2020, 3, 3)
          result.id shouldBe "oct90001"
          result.title shouldBe "This is the title"
          result.status shouldBe StatusSubmitted
          result.reviewType shouldBe ReviewType2i
          result.ocelotDateSubmitted shouldBe 1
          result.ocelotVersion shouldBe 1
          result.dateSubmitted shouldBe dateToCompare
        case JsError(errors) => fail(s"Unable to parse valid Json $errors")
      }
    }

    "Generate a lastModified date based on current time when converting valid JSON that does not have a last modified date" in {
      val baseDateTime: ZonedDateTime = ZonedDateTime.now().minusSeconds(1)
      validMetaJsonWithoutLastModified.validate[ApprovalProcessMeta] match {
        case JsSuccess(result, _) if result.lastModified.isAfter(baseDateTime) => succeed
        case JsSuccess(result, _) => fail(s"Deserializing valid JSON did not create correct process ${result.lastModified} : $baseDateTime")
        case JsError(errors) => fail(s"Unable to parse valid Json $errors")
      }
    }

    "Result in a failure when for invalid JSON" in {

      invalidJson.validate[ApprovalProcessMeta] match {
        case e: JsError => succeed
        case _ => fail("Invalid JSON payload should not have been successfully deserialized")
      }
    }

  }

  "Serializing an approval process meta into JSON" should {

    "Generate the expected JSON" in {
      val result: JsValue = Json.toJson(approvalProcessMeta)
      result shouldBe validMetaJson.as[JsValue]
    }
  }

}
