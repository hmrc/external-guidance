/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import base.BaseSpec
import play.api.libs.json._
import models.ApprovalReview.format
import Constants.ReviewCompleteStatus

class ApprovalReviewSpec extends BaseSpec with ApprovalProcessJson {

  private val invalidJson = Json.parse("{}")

  "Deserializing a JSON payload into an instance of ApprovalReview" should {

    "Result in a successful conversion for valid JSON with nulls" in {
      validApprovalProcessReviewJson.validate[ApprovalReview] match {
        case JsSuccess(result, _) if result.pages(0).id == "1" => succeed
        case JsSuccess(_, _) => fail("Deserializing valid JSON did not create correct process")
        case JsError(err) => fail(s"Unable to parse valid Json: $err")
      }
    }

    "Result in a successful conversion for valid JSON without nulls" in {
      validApprovalProcessReviewJson2.validate[ApprovalReview] match {
        case JsSuccess(result, _) if result.pages(0).id == "1" => succeed
        case JsSuccess(_, _) => fail("Deserializing valid JSON did not create correct process")
        case JsError(err) => fail(s"Unable to parse valid Json: $err")
      }
    }

    "Result in a failure when for invalid JSON" in {

      invalidJson.validate[ApprovalReview] match {
        case e: JsError => succeed
        case _ => fail("Invalid JSON payload should not have been successfully deserialized")
      }
    }

  }

  "Serializing an approval process into JSON" should {

    "Generate the expected JSON" in {
      val result: JsObject = Json.toJson(approvalProcessReview).as[JsObject]
      result("result") shouldBe JsString(ReviewCompleteStatus)
    }
  }

}
