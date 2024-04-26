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
import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.api.libs.ws.WSResponse
import stubs.{AuditStub, AuthStub}
import support.IntegrationSpec

class GetApprovalProcessISpec extends IntegrationSpec {

  override def beforeAll(): Unit = {
    super.beforeAll()

    lazy val request = buildRequest(s"/test-only/processes/approval/trn90099")
    AuditStub.audit()
    AuthStub.authorise()
    await(request.delete())
  }

  "Calling the approval GET endpoint with a valid ID" should {

    def populateDatabase(processToSave: JsValue): String = {
      lazy val request = buildRequest("/external-guidance/approval/2i-review")

      val result = await(request.post(processToSave))
      val json = result.body[JsValue].as[JsObject]
      (json \ "id").as[String]
    }

    val processToSave: JsValue = ExamplePayloads.validProcessWithCallouts
    lazy val id = populateDatabase(processToSave)
    lazy val request = buildRequest(s"/external-guidance/approval/$id")
    lazy val response: WSResponse = {
      AuthStub.authorise()
      AuditStub.audit()
      await(request.get())
    }

    "return a OK status code" in {
      response.status shouldBe Status.OK
    }

    "return content as JSON" in {
      response.contentType shouldBe ContentTypes.JSON
    }

    "return the corresponding JSON in the response" in {
      val json = response.body[JsValue].as[JsObject]
      json shouldBe ExamplePayloads.validProcessWithCallouts
    }
  }

  "Calling the approval GET endpoint with a unknown ID" should {

    lazy val id = "oeh12345"
    lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-review")
    lazy val response: WSResponse = {
      AuthStub.authorise()
      AuditStub.audit()
      await(request.get())
    }

    "return a not found status code" in {
      response.status shouldBe Status.NOT_FOUND
    }

    "return content as JSON" in {
      response.contentType shouldBe ContentTypes.JSON
    }

    "return the not found error response" in {
      val json = response.body[JsValue].as[JsObject]
      (json \ "code").as[String] shouldBe "NOT_FOUND"
    }
  }

  "Calling the approval GET process code endpoint with a valid ID" should {

    val processCode = "this-is-the-process-code"
    lazy val request = buildRequest(s"/external-guidance/approval/code/$processCode")
    lazy val response: WSResponse = {
      AuthStub.authorise()
      AuditStub.audit()
      await(request.get())
    }

    "return a OK status code" in {
      response.status shouldBe Status.OK
    }

    "return content as JSON" in {
      response.contentType shouldBe ContentTypes.JSON
    }

    "return the corresponding JSON in the response" in {
      val json = response.body[JsValue].as[JsObject]
      json shouldBe ExamplePayloads.validProcessWithCallouts
    }
  }

  "Calling the approval GET process code endpoint with a unknown ID" should {

    lazy val processCode = "oeh12345"
    lazy val request = buildRequest(s"/external-guidance/approval/code/$processCode")
    lazy val response: WSResponse = {
      AuthStub.authorise()
      AuditStub.audit()
      await(request.get())
    }

    "return a not found status code" in {
      response.status shouldBe Status.NOT_FOUND
    }

    "return content as JSON" in {
      response.contentType shouldBe ContentTypes.JSON
    }

    "return the not found error response" in {
      val json = response.body[JsValue].as[JsObject]
      (json \ "code").as[String] shouldBe "NOT_FOUND"
    }
  }

  "Calling the approval summaries endpoint" should {

    lazy val request = buildRequest(s"/external-guidance/approval")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.authorise()

      await(request.get())
    }

    "return a OK status code" in {
      response.status shouldBe Status.OK
    }

    "return content as JSON" in {
      response.contentType shouldBe ContentTypes.JSON
    }

    "return the corresponding list as JSON in the response" in {
      val json = response.body[JsValue]
      json match {
        case JsArray(_) => succeed
        case _ => fail()
      }
    }
  }

  "An unauthorized call to the approval summaries endpoint" should {

    lazy val request = buildRequest( "/external-guidance/approval")
    lazy val response : WSResponse = {

      AuditStub.audit()
      AuthStub.unauthorised()

      await(request.get())
    }

    "return the unauthorized status" in {
      response.status shouldBe Status.UNAUTHORIZED
    }
  }

}
