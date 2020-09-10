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

import java.util.UUID

import base.BaseSpec
import models.{ApprovalProcessJson, ApprovalProcessReview}
import play.api.libs.json._
import repositories.formatters.ApprovalProcessReviewFormatter.mongoFormat

class ApprovalProcessReviewFormatterSpec extends BaseSpec with ApprovalProcessJson {

  private val invalidJson = Json.parse("{}")

  "Deserializing a JSON payload into an instance of ApprovalProcessReview" should {

    "Result in a successful conversion for valid JSON" in {

      validApprovalProcessReviewJson.validate[ApprovalProcessReview] match {
        case JsSuccess(result, _) if result.id == UUID.fromString(validReviewId) => succeed
        case JsSuccess(_, _) => fail("Deserializing valid JSON did not create correct process")
        case JsError(err) => fail(s"Unable to parse valid Json: $err")
      }
    }

    "Result in a successful conversion for valid JSON that does not have an _id" in {

      validApprovalProcessReviewWithNoIdJson.validate[ApprovalProcessReview] match {
        case JsSuccess(result, _) if result.ocelotId == validId => succeed
        case JsSuccess(_, _) => fail("Deserializing valid JSON did not create correct process")
        case JsError(err) => fail(s"Unable to parse valid Json: $err")
      }
    }

    "Result in a failure when for invalid JSON" in {

      invalidJson.validate[ApprovalProcessReview] match {
        case e: JsError => succeed
        case _ => fail("Invalid JSON payload should not have been successfully deserialized")
      }
    }

  }

  "Serializing an approval process into JSON" should {

    "Generate the expected JSON" in {
      val result: JsObject = Json.toJson(approvalProcessReview).as[JsObject]
      result("ocelotId") shouldBe JsString(validId)
    }
  }

}
