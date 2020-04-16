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

import base.UnitSpec
import data.ExamplePayloads
import mocks.MockSubmittedRepository
import models.RequestOutcome
import models.errors._
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class SubmittedServiceSpec extends UnitSpec with MockFactory {

  // TODO Do we need to test for invalid JSON content being sent to save method

  private trait Test extends MockSubmittedRepository {

    val validId: String = "oct90001"
    val invalidId: String = "ext9005"

    val process: JsObject = ExamplePayloads.simpleValidProcess.as[JsObject]
    val invalidProcess: JsObject = Json.obj("idx"-> invalidId)

    lazy val service: SubmittedService = new SubmittedService(mockSubmittedRepository)
  }

  "The method getById of class SubmittedService" should {

    "Return a JsObject representing the submitted ocelot process when the input identifies a valid process" in new Test {

      val expected: RequestOutcome[JsObject] = Right(process)

      MockSubmittedRepository
        .getById(validId)
        .returns(Future.successful(expected))

      whenReady(service.getById(validId)) { result =>
        result shouldBe expected
      }
    }

    "Return a not found response when no process has the identifier input to the method" in new Test {

      val expected: RequestOutcome[JsObject] = Left(Errors(NotFoundError))

      MockSubmittedRepository
        .getById(validId)
        .returns(Future.successful(expected))

      whenReady(service.getById(validId)) { result =>
        result shouldBe expected
      }
    }

    "Return an internal server error when the repository reports a database error" in new Test {

      val repositoryError: RequestOutcome[JsObject] = Left(Errors(DatabaseError))
      val expected: RequestOutcome[JsObject] = Left(Errors(InternalServiceError))

      MockSubmittedRepository
        .getById(validId)
        .returns(Future.successful(repositoryError))

      whenReady(service.getById(validId)) { result =>
        result shouldBe expected
      }
    }
  }

  "Calling save method" when {

    "the id and JSON are valid" should {
      "return valid Id" in new Test {

        val expected: RequestOutcome[String] = Right(validId)

        MockSubmittedRepository
          .save(validId, process)
          .returns(Future.successful(expected))

        whenReady(service.save(process)) {
          case Right(id) => id shouldBe validId
          case _ => fail
        }
      }
    }

    "the id cannot be found in the JSON" should {
      "not call the repository" in new Test {
        MockSubmittedRepository
          .save(invalidId, invalidProcess)
          .never()

        service.save(invalidProcess)
      }

      "return a bad request error" in new Test {
        val expected: RequestOutcome[String] = Left(Errors(BadRequestError))

        whenReady(service.save(invalidProcess)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test {
        val repositoryResponse: RequestOutcome[String] = Left(Errors(DatabaseError))
        val expected: RequestOutcome[String] = Left(Errors(InternalServiceError))

        MockSubmittedRepository
          .save(validId, process)
          .returns(Future.successful(repositoryResponse))

        whenReady(service.save(process)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }
  }

}
