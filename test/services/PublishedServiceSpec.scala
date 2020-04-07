/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import scala.concurrent.Future

import play.api.libs.json.{Json, JsObject}

import models.RequestOutcome
import models.errors._

import base.UnitSpec
import mocks.MockPublishedRepository

class PublishedServiceSpec extends UnitSpec {

  private trait Test extends MockPublishedRepository {

    val validId: String = "ext90005"
    val invalidId: String = "ext9005"

    val process: JsObject = Json.obj()

    lazy val target: PublishedService = new PublishedService(mockPublishedRepository)
  }

  "The method getById of class PublishedService" should {

    "Return a JsObject representing an Ocelot process when the input identifies a valid process" in new Test {

      val expected: RequestOutcome[JsObject] = Right(process)

      MockPublishedRepository
        .getById(validId)
        .returns(Future.successful(expected))

      whenReady(target.getById(validId)) {
        result => result shouldBe expected
      }
    }

    "Return a bad request response when the input identifier is invalid" in new Test {

      val expected: RequestOutcome[JsObject] = Left(Errors(BadRequestError))

      whenReady(target.getById(invalidId)) {
        result => result shouldBe expected
      }
    }

    "Return a not found response when no process has the identifier input to the method" in new Test {

      val expected: RequestOutcome[JsObject] = Left(Errors(NotFoundError))

      MockPublishedRepository
        .getById(validId)
        .returns(Future.successful(expected))

      whenReady(target.getById(validId)) {
        result => result shouldBe expected
      }
    }

    "Return an internal server error when the repository reports a database error" in new Test {

      val repositoryError: RequestOutcome[JsObject] = Left(Errors(DatabaseError))

      val expected: RequestOutcome[JsObject] = Left(Errors(InternalServiceError))

      MockPublishedRepository
        .getById(validId)
        .returns(Future.successful(repositoryError))

      whenReady(target.getById(validId)) {
        result => result shouldBe expected
      }
    }
  }

}
