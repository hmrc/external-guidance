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
import play.api.http.Status.{OK, BAD_REQUEST}
import play.api.libs.json.JsObject
import play.api.libs.ws.WSResponse
import repositories.PublishedRepository
import stubs.{AuditStub, AuthStub}
import support.IntegrationSpec

class ArchiveISpec extends IntegrationSpec {

  "Calling the archive GET endpoint with a valid id" should {

    val testId = "trn90099"
    val processCode = "this-is-the-process-code"
    val processToSave: JsObject = ExamplePayloads.validProcessWithCallouts.as[JsObject]

    val published: PublishedRepository = app.injector.instanceOf[PublishedRepository]

    published.save(testId, "user", processCode, processToSave)

    lazy val setup = buildRequest("/external-guidance/approval/2i-review")
    lazy val _: WSResponse = {
      AuditStub.audit()
      await(setup.post(processToSave))
    }

    "return an OK" in {
      AuthStub.authorise()
      AuditStub.audit()
      val request = buildRequest(s"/external-guidance/archive/$testId").get()

      val response = await(request)

      response.status shouldBe OK
    }

    "return a BAD REQUEST" in {
      AuthStub.authorise()
      AuditStub.audit()
      val request = buildRequest(s"/external-guidance/archive/FAKE123").get()

      await(request).status shouldBe BAD_REQUEST
    }
  }
}
