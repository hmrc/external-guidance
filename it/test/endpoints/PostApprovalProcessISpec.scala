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

package endpoints

import data.ExamplePayloads
import play.api.http.{ContentTypes, Status}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSResponse
import stubs.{AuthStub, AuditStub}
import support.IntegrationSpec

class PostApprovalProcessISpec extends IntegrationSpec {

  override def beforeAll(): Unit = {
    super.beforeAll()

    lazy val request = buildRequest(s"/test-only/processes/approval/trn90099")
    AuditStub.audit()
    AuthStub.authorise()
    await(request.delete())
  }

  "Calling the approval POST endpoint with a valid payload" should {

    val processToSave: JsValue = ExamplePayloads.validProcessWithCallouts
    val idToSave = (processToSave \ "meta" \ "id").as[String]

    lazy val request = buildRequest("/external-guidance/approval/2i-review")
    lazy val response: WSResponse = {
      AuditStub.audit()
      await(request.post(processToSave))
    }

    "return a CREATED status code" in {
      response.status shouldBe Status.CREATED
    }

    "return content as JSON" in {
      response.contentType shouldBe ContentTypes.JSON
    }

    "return an id property in the response" in {
      val json = response.body[JsValue].as[JsObject]
      json.keys should contain("id")
    }

    "return an id value based on the content of the JSON payload" in {
      val json = response.body[JsValue].as[JsObject]
      val id = (json \ "id").as[String]
      id shouldBe idToSave
    }
  }

}
