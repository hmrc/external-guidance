/*
 * Copyright 2019 HM Revenue & Customs
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

package endpoints

import data.ExamplePayloads._
import models.ApprovalProcessSummary
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSResponse
import stubs.AuditStub
import support.IntegrationSpec

class PostProcessReviewISpec extends IntegrationSpec {

  "Calling the approvalReviewComplete POST endpoint with a valid payload" should {

    def populateDatabase(processToSave: JsValue): String = {
      lazy val request = buildRequest("/external-guidance/approval")

      val result = await(request.post(processToSave))
      val json = result.body[JsValue].as[JsObject]
      (json \ "id").as[String]
    }

    val processToSave: JsValue = simpleValidProcess
    lazy val id = populateDatabase(processToSave)
    lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-review")
    lazy val response: WSResponse = {
      AuditStub.audit()
      await(request.post(statusChangeJson))
    }

    "return a NO_CONTENT status code" in {
      response.status shouldBe NO_CONTENT
    }

    "status should be set to SubmittedForFactCheck" in {
      lazy val request = buildRequest(s"/external-guidance/approval")
      lazy val response: WSResponse = {
        AuditStub.audit()
        await(request.get())
      }
      val list: List[ApprovalProcessSummary] = response.body[JsValue].as[List[ApprovalProcessSummary]]
      val updatedEntry = list.find(p => p.id == id)
      updatedEntry shouldBe 'defined
      updatedEntry.get.status shouldBe "SubmittedForFactCheck"
    }

  }

  "Calling the approvalReviewComplete POST endpoint with an id that doesn't exist" should {

    lazy val request = buildRequest(s"/external-guidance/approval/xyzinvalid/2i-review")
    lazy val response: WSResponse = {
      AuditStub.audit()
      await(request.post(statusChangeJson))
    }

    "return a NOT_FOUND status code" in {
      response.status shouldBe NOT_FOUND
    }

  }
}
