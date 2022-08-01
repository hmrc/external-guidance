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

package services

import java.util.UUID
import base.BaseSpec
import mocks.MockScratchRepository
import core.services._
import core.models.errors._
import core.models.ocelot.errors._
import core.models.RequestOutcome
import core.models.ocelot.ProcessJson
import play.api.libs.json.{Json, JsObject}
import mocks.MockAppConfig
import scala.concurrent.{ExecutionContext, Future}
import mocks.MockTimescalesService

class ScratchServiceSpec extends BaseSpec {

  private trait Test extends MockScratchRepository with MockTimescalesService {
    implicit def executionContext: ExecutionContext = ExecutionContext.global
    val pageBuilder = new ValidatingPageBuilder(new PageBuilder(new Timescales(new DefaultTodayProvider)))
    lazy val target: ScratchService = new ScratchService(mockScratchRepository,
                                                         pageBuilder,
                                                         mockTimescalesService,
                                                         MockAppConfig)
  }

    "Calling save" should {
      "Detect missing timescale defn Error" in new Test with ProcessJson {

        val id: UUID = UUID.fromString("bf8bf6bb-0894-4df6-8209-2467bc9af6ae")
        val expected: RequestOutcome[UUID] = Right(id)
        val json: JsObject = validOnePageWithTimescalesJson.as[JsObject]

        MockScratchRepository
          .save(json)
          .returns(Future.successful(expected))

        MockTimescalesService.get().returns(Future.successful(Right(Map())))

        whenReady(target.save(json)) { result =>
          result shouldBe Left(Error(Error.UnprocessableEntity, List(MissingTimescaleDefinition("RepayReimb")), None))
        }
      }
    }

    "return valid UUID when the JSON is valid" in new Test with ProcessJson {

      val id: UUID = UUID.fromString("bf8bf6bb-0894-4df6-8209-2467bc9af6ae")
      val expected: RequestOutcome[UUID] = Right(id)
      val json: JsObject = validOnePageJson.as[JsObject]

      MockScratchRepository
        .save(json)
        .returns(Future.successful(expected))

      whenReady(target.save(json)) { result =>
        result shouldBe expected
      }
    }

    "the JSON is valid but the process is not" should {
      "return Unsupportable entity error" in new Test with ProcessJson {
        val expectedError = Error(DuplicatePageUrl("4","/feeling-bad"))
        val expected: RequestOutcome[Error] = Left(expectedError)
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

      "return an HTTP 422 error" in new Test {
        val process: JsObject = Json.obj()
        MockScratchRepository
          .save(process)
          .never()

        whenReady(target.save(process)) {
          case result @ Left(err) if err.code == Error.UnprocessableEntity => succeed
          case _ => fail
        }
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test with ProcessJson {
        val repositoryResponse: RequestOutcome[UUID] = Left(DatabaseError)
        val expected: RequestOutcome[UUID] = Left(InternalServerError)
        val json: JsObject = validOnePageJson.as[JsObject]

        MockScratchRepository
          .save(json)
          .returns(Future.successful(repositoryResponse))

        whenReady(target.save(json)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
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
        val expected: RequestOutcome[JsObject] = Left(NotFoundError)
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
        val expected: RequestOutcome[JsObject] = Left(BadRequestError)
        val id: String = "Some invalid ID"

        whenReady(target.getById(id)) { result =>
          result shouldBe expected
        }
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test {
        val repositoryResponse: RequestOutcome[JsObject] = Left(DatabaseError)
        val expected: RequestOutcome[JsObject] = Left(InternalServerError)
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
