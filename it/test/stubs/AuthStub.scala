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

package stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.Json
import support.WireMockMethods

object AuthStub extends WireMockMethods {

  private val authoriseUri: String = "/auth/authorise"
  private val internalId: String = "id"
  private val credId: String = "5001002"
  private val name: String = "Harold"
  private val lastName: String = "Walker"
  private val email: String = "harold.walker@worldmail.com"

  def authorise(): StubMapping =
    when(method = POST, uri = authoriseUri)
      .thenReturn(
        status = Status.OK,
        body = Json.obj(
          "internalId" -> internalId,
          "optionalCredentials" -> Json.obj(
            "providerId" -> credId,
            "providerType" -> "PrivilegedApplication"
          ),
          "optionalName" -> Json.obj(
            "name" -> name,
            "lastName" -> lastName
          ),
          "email" -> email,
          "authorisedEnrolments" -> Json.arr(Json.obj("key" -> "FactChecker", "state" -> "activated"), Json.obj("key" -> "2iReviewer", "state" -> "activated"))
        )
      )

  def incompleteAuthorisation(): StubMapping =
    when(method = POST, uri = authoriseUri)
      .thenReturn(
        status = Status.OK,
        body = Json.obj(
          "internalId" -> internalId,
          "optionalCredentials" -> Json.obj(
            "providerId" -> credId,
            "providerType" -> "PrivilegedApplication"
          ),
          "email" -> email,
          "authorisedEnrolments" -> Json.arr(Json.obj("key" -> "FactChecker", "state" -> "activated"))
        )
      )

  def unauthorised(): StubMapping =
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = Status.UNAUTHORIZED)
}

