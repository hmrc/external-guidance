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

import java.util.UUID

import base.UnitSpec
import mocks.MockScratchRepository
import models.RequestOutcome
import models.ocelot.errors._
import models.errors._
import models.ocelot.ProcessJson
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class ScratchServiceSpec extends UnitSpec {

  private trait Test extends MockScratchRepository {
    lazy val target: ScratchService = new ScratchService(mockScratchRepository, new PageBuilder)
  }

  "Calling save method" when {

    "the JSON is valid" should {
      "return valid UUID" in new Test with ProcessJson {

        val id: UUID = UUID.fromString("bf8bf6bb-0894-4df6-8209-2467bc9af6ae")
        val expected: RequestOutcome[UUID] = Right(id)
        val process: JsObject = validOnePageJson.as[JsObject]
        MockScratchRepository
          .save(process)
          .returns(Future.successful(expected))

        whenReady(target.save(process)) {
          case Right(uuid) => uuid.toString shouldBe id.toString
          case _ => fail
        }
      }
    }

    "the JSON is valid but the process is not" should {
      "return Unsupportable entity error" in new Test with ProcessJson {
        val errorDetails = DuplicatePageUrl("4","/feeling-bad").details
        val expectedError = Error("UNSUPPORTABLE_ENTITY", None, Some(List(errorDetails)))
        val expected: RequestOutcome[Errors] = Left(Errors(expectedError))
        val process: JsObject = data.ProcessData.invalidOnePageJson.as[JsObject]

        whenReady(target.save(process)) {
          case Right(_) => fail
          case err if err == expected => succeed
          case err => fail
        }
      }
    }

    "the JSON is invalid" should {
      "not call the scratch repository" in new Test {
        val process: JsObject = Json.obj()
        MockScratchRepository.save(process).never()
        target.save(process)
      }

      "return a validation error" in new Test {
        val expected: RequestOutcome[UUID] = Left(Errors(ValidationError))
        val process: JsObject = Json.obj()
        MockScratchRepository
          .save(process)
          .returns(Future.successful(expected))

        whenReady(target.save(process)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test with ProcessJson {
        val repositoryResponse: RequestOutcome[UUID] = Left(Errors(DatabaseError))
        val expected: RequestOutcome[UUID] = Left(Errors(InternalServiceError))
        val process: JsObject = validOnePageJson.as[JsObject]
        MockScratchRepository
          .save(process)
          .returns(Future.successful(repositoryResponse))

        whenReady(target.save(process)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }
  }

  "Calling getById method" when {

    "the ID is valid and known" should {
      "return the corresponding scratch process" in new Test {
        val expected: RequestOutcome[JsObject] = Right(Json.obj())
        val id: UUID = UUID.randomUUID()
        MockScratchRepository
          .getById(id)
          .returns(Future.successful(expected))

        whenReady(target.getById(id.toString)) { result =>
          result shouldBe expected
        }
      }
    }

    "the ID is valid but unknown" should {
      "return a not found error" in new Test {
        val expected: RequestOutcome[JsObject] = Left(Errors(NotFoundError))
        val id: UUID = UUID.randomUUID()
        MockScratchRepository
          .getById(id)
          .returns(Future.successful(expected))

        whenReady(target.getById(id.toString)) { result =>
          result shouldBe expected
        }
      }
    }

    "the ID is invalid" should {
      "return a bad request error" in new Test {
        val expected: RequestOutcome[JsObject] = Left(Errors(BadRequestError))
        val id: String = "Some invalid ID"

        whenReady(target.getById(id)) { result =>
          result shouldBe expected
        }
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test {
        val repositoryResponse: RequestOutcome[JsObject] = Left(Errors(DatabaseError))
        val expected: RequestOutcome[JsObject] = Left(Errors(InternalServiceError))
        val id: UUID = UUID.randomUUID()
        MockScratchRepository
          .getById(id)
          .returns(Future.successful(repositoryResponse))

        whenReady(target.getById(id.toString)) { result =>
          result shouldBe expected
        }
      }
    }
  }

}
