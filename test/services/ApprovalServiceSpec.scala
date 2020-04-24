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
import mocks.MockApprovalRepository
import models.RequestOutcome
import models.ocelot.ProcessJson
import models.errors._
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class ApprovalServiceSpec extends UnitSpec with MockFactory {

  private trait Test extends MockApprovalRepository with ProcessJson {

    val validId: String = "oct90001"
    val invalidId: String = "ext9005"

    val process: JsObject = validOnePageJson.as[JsObject]
    val invalidProcess: JsObject = Json.obj("idx" -> invalidId)

    lazy val service: ApprovalService = new ApprovalService(mockApprovalRepository)
  }

  "Calling the getById method" when {
    "the ID identifies a valid process" should {
      "return a JSON representing the submitted ocelot process" in new Test {

        val expected: RequestOutcome[JsObject] = Right(process)

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(expected))

        whenReady(service.getById(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the ID cannot be matched to a submitted process" should {
      "return a not found response" in new Test {

        val expected: RequestOutcome[JsObject] = Left(Errors(NotFoundError))

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(expected))

        whenReady(service.getById(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[JsObject] = Left(Errors(DatabaseError))
        val expected: RequestOutcome[JsObject] = Left(Errors(InternalServiceError))

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(repositoryError))

        whenReady(service.getById(validId)) { result =>
          result shouldBe expected
        }
      }
    }
  }

  "Calling the save method" when {

    "the id and JSON are valid" should {
      "return valid Id" in new Test {

        val expected: RequestOutcome[String] = Right(validId)

        MockApprovalRepository
          .update(validId, process)
          .returns(Future.successful(expected))

        whenReady(service.save(process)) {
          case Right(id) => id shouldBe validId
          case _ => fail
        }
      }
    }

    "the JSON is invalid" should {
      "not call the repository" in new Test {
        MockApprovalRepository
          .update(invalidId, invalidProcess)
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

        MockApprovalRepository
          .update(validId, process)
          .returns(Future.successful(repositoryResponse))

        whenReady(service.save(process)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }
  }

}
