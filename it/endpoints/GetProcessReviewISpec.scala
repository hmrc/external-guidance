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

import play.api.http.{ContentTypes, Status}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSResponse
import stubs.AuditStub
import support.IntegrationSpec

class GetProcessReviewISpec extends IntegrationSpec {

  "Calling the approval 2i Review endpoint" should {

    lazy val request = buildRequest(s"/external-guidance/approval/oct90005/2i-review")
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

    "return the corresponding list as JSON in the response" in {
      val json = response.body[JsValue]
      json match {
        case JsObject(_) => succeed
        case _ => fail()
      }
    }
  }
}
