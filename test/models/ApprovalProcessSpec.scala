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
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

class ApprovalProcessSpec extends BaseSpec with ApprovalProcessJson {

  import models.Approval.format

  private val invalidJson = Json.parse("{}")


  "Deserializing a JSON payload into an instance of Approval" should {

    "Result in a successful conversion for valid JSON" in {

      validApprovalProcessJson.validate[Approval] match {
        case JsSuccess(result, _) if result == approvalProcess => succeed
        case JsSuccess(result, _) => fail("Deserializing valid JSON did not create correct process")
        case JsError(errs) => fail("Unable to parse valid Json")
      }
    }

    "Result in a successful conversion for valid JSON that does not have an _id" in {

      validApprovalProcessWithoutAnIdJson.validate[Approval] match {
        case JsSuccess(result, _) if result == approvalProcess => succeed
        case JsSuccess(_, _) => fail("Deserializing valid JSON did not create correct process")
        case err => fail("Unable to parse valid Json")
      }
    }

    "Result in a failure when for invalid JSON" in {

      invalidJson.validate[Approval] match {
        case e: JsError => succeed
        case _ => fail("Invalid JSON payload should not have been successfully deserialized")
      }
    }

  }

  "Serializing an approval process into JSON" should {

    "Generate the expected JSON" in {
      val result: JsValue = Json.toJson(approvalProcess)
      result shouldBe validApprovalProcessJson.as[JsValue]
    }
  }

}
