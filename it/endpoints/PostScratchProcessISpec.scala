/*
 * Copyright 2022 HM Revenue & Customs
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
import stubs.AuditStub
import support.IntegrationSpec

class PostScratchProcessISpec extends IntegrationSpec {

  "Calling the scratch POST endpoint with a valid payload" should {

    lazy val request = buildRequest("/external-guidance/scratch")
    lazy val response: WSResponse = {
      AuditStub.audit()
      await(request.post(ExamplePayloads.simpleValidProcess))
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

    "return an id value in the format of a UUID" in {
      val uuidFormat = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
      val json = response.body[JsValue].as[JsObject]
      val id = (json \ "id").as[String]
      id.matches(uuidFormat) shouldBe true
    }
  }
}
