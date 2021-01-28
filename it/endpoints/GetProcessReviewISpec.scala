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

package endpoints

import data.ExamplePayloads
import play.api.http.{ContentTypes, Status}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSResponse
import stubs.{AuditStub, AuthStub}
import support.IntegrationSpec

class GetProcessReviewISpec extends IntegrationSpec {

  def populateDatabase(processToSave: JsValue): String = {
    lazy val request = buildRequest("/external-guidance/approval/2i-review")

    val result = await(request.post(processToSave))
    val json = result.body[JsValue].as[JsObject]
    (json \ "id").as[String]
  }

  val processToSave: JsValue = ExamplePayloads.validProcessWithCallouts
  lazy val id: String = populateDatabase(processToSave)

  "Calling the approval 2i Review endpoint" should {

    lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-review")
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

    "return the corresponding data as JSON in the response" in {
      val json = response.body[JsValue]
      json match {
        case JsObject(_) => succeed
        case _ => fail()
      }
    }
  }

  "Calling the approval 2i Review Page Info endpoint" should {

    val pageUrl: String = "/example-page-2"
    lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-page-review$pageUrl")
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

    "return the corresponding data as JSON in the response" in {
      val json = response.body[JsValue]
      json match {
        case JsObject(_) => succeed
        case _ => fail()
      }
    }
  }

  "Calling the approval 2i all pages completed endpoint when all pages are not completed" should {

    lazy val request = buildRequest( s"/external-guidance/approval/$id/2i-review/confirm")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.authorise()
      await(request.get())
    }

    "return bad request" in {
      response.status shouldBe Status.BAD_REQUEST
    }

  }

  "Calling the approval 2i review endpoint without authorization" should {

    lazy val request = buildRequest("/external-guidance/approval/oct90001/2i-review")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.unauthorised()
      await(request.get())
    }

    "return unauthorized" in {
      response.status shouldBe Status.UNAUTHORIZED
    }
  }

  "Calling the approval 2i review page information endpoint without authorization" should {

    lazy val request = buildRequest(s"/external-guidance/approval/oct90001/2i-page-review/page-1")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.unauthorised()
      await(request.get())
    }

    "return unauthorized" in {
      response.status shouldBe Status.UNAUTHORIZED
    }
  }

  "Calling the approval 2i review all pages completed endpoint without authorization" should {

    lazy val request = buildRequest("/external-guidance/approval/oct90001/2i-review/confirm")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.unauthorised()
      await(request.get())
    }

    "return unauthorized" in {
      response.status shouldBe Status.UNAUTHORIZED
    }
  }
}
