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

import java.util.UUID

import data.ExamplePayloads
import play.api.http.{ContentTypes, Status}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSResponse
import stubs.AuditStub
import support.IntegrationSpec

class GetScratchProcessISpec extends IntegrationSpec {

  "Calling the scratch GET endpoint with a valid ID" should {

    def populateDatabase(): String = {
      lazy val request = buildRequest("/external-guidance/scratch")
      val result = await(request.post(ExamplePayloads.simpleValidProcess))
      val json = result.body[JsValue].as[JsObject]
      (json \ "id").as[String]
    }

    lazy val id = populateDatabase()
    lazy val request = buildRequest(s"/external-guidance/scratch/$id")
    lazy val response: WSResponse = {
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
      json shouldBe ExamplePayloads.simpleValidProcess
    }
  }

  "Calling the scratch GET endpoint with a unknown ID" should {

    lazy val id = UUID.randomUUID().toString
    lazy val request = buildRequest(s"/external-guidance/scratch/$id")
    lazy val response: WSResponse = {
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
}
