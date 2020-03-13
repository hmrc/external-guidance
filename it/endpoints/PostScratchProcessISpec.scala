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

import data.ExamplePayloads
import play.api.http.Status
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSResponse
import stubs.AuditStub
import support.IntegrationSpec

class PostScratchProcessISpec extends IntegrationSpec {

  "Calling the scratch POST endpoint" should {

    lazy val request = buildRequest("/external-guidance/scratch")
    lazy val response: WSResponse = {
      await(request.post(ExamplePayloads.simpleValidProcess))
    }

    "return a CREATED status code" in {
      AuditStub.audit()
      response.status shouldBe Status.CREATED
    }

    "return a valid payload" in {
      AuditStub.audit()
      val expectedId: String = "265e0178-cbe1-42ab-8418-7120ce6d0925"
      val json = response.body[JsValue].as[JsObject]
      (json \ "id").as[String] shouldBe expectedId
    }
  }
}
