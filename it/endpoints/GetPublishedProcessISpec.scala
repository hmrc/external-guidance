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

import core.models.errors.NotFoundError
import play.api.http.{ContentTypes, Status}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.{WSRequest, WSResponse}
import stubs.AuditStub
import support.IntegrationSpec

/**
  * Note at present this test class is dependent on the process "ext90002" being
  * present in the Mongo collection publishedProcesses of the local database
  * external-guidance.
  *
  * When the save process functionality has been developed this dependency
  * should be removed.
  */
class GetPublishedProcessISpec extends IntegrationSpec {

  "Calling the published endpoint with a valid process id" should {

    val processId: String = "this-is-the-process-code"

    lazy val request: WSRequest = buildRequest(s"/external-guidance/published/$processId")

    lazy val response: WSResponse = {
      AuditStub.audit()
      await(request.get)
    }

    "return an OK status" ignore {

      response.status shouldBe Status.OK

    }

    "return content as JSON" ignore {

      response.contentType shouldBe ContentTypes.JSON

    }

  }

  "Calling the published endpoint with an unknown process id" should {

    val unknownProcessId: String = "unk10000"

    lazy val request: WSRequest = buildRequest(s"/external-guidance/published/$unknownProcessId")

    lazy val response: WSResponse = {
      AuditStub.audit()
      await(request.get)
    }

    "return a not found status" in {

      response.status shouldBe Status.NOT_FOUND

    }

    "return content as JSON" in {

      response.contentType shouldBe ContentTypes.JSON

    }

    "return the error code NOT_FOUND" in {

      val json: JsObject = response.body[JsValue].as[JsObject]

      (json \ "code").as[String] shouldBe NotFoundError.code
    }

  }

}
