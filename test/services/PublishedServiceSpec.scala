/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.ZonedDateTime
import base.BaseSpec
import mocks.{MockApprovalRepository, MockArchiveRepository, MockPublishedRepository}
import core.models.errors._
import core.models.ocelot.ProcessJson
import core.models.RequestOutcome
import models.PublishedProcess
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class PublishedServiceSpec extends BaseSpec {

  private trait Test extends MockPublishedRepository with MockArchiveRepository with MockApprovalRepository with ProcessJson {

    val validId: String = "ext90005"
    val invalidId: String = "ext9005"
    val invalidProcess: JsObject = Json.obj("idx" -> invalidId)
    val publishedProcess: PublishedProcess =
      PublishedProcess(validId, 1, ZonedDateTime.now(), validOnePageJson.as[JsObject], "user", processCode = "processCode")

    lazy val target: PublishedService = new PublishedService(mockPublishedRepository, mockArchiveRepository, mockApprovalRepository)
  }

  "The method getById of class PublishedService" should {

    "Return a JsObject representing an Ocelot process when the input identifies a valid process" in new Test {

      val expected: RequestOutcome[PublishedProcess] = Right(publishedProcess)

      MockPublishedRepository
        .getById(validId)
        .returns(Future.successful(expected))

      whenReady(target.getById(validId)) { result =>
        result shouldBe expected
      }
    }

    "Return a bad request response when the input identifier is invalid" in new Test {

      val expected: RequestOutcome[PublishedProcess] = Left(BadRequestError)

      whenReady(target.getById(invalidId)) { result =>
        result shouldBe expected
      }
    }

    "Return a not found response when no process has the identifier input to the method" in new Test {

      val expected: RequestOutcome[PublishedProcess] = Left(NotFoundError)

      MockPublishedRepository
        .getById(validId)
        .returns(Future.successful(expected))

      whenReady(target.getById(validId)) { result =>
        result shouldBe expected
      }
    }

    "Return an internal server error when the repository reports a database error" in new Test {

      val repositoryError: RequestOutcome[PublishedProcess] = Left(DatabaseError)

      val expected: RequestOutcome[JsObject] = Left(InternalServerError)

      MockPublishedRepository
        .getById(validId)
        .returns(Future.successful(repositoryError))

      whenReady(target.getById(validId)) { result =>
        result shouldBe expected
      }
    }
  }

  "The method getByProcessCode of class PublishedService" should {

    "Return a JsObject representing an Ocelot process when the input identifies a valid process" in new Test {

      val expected: RequestOutcome[PublishedProcess] = Right(publishedProcess)

      MockPublishedRepository
        .getByProcessCode(validId)
        .returns(Future.successful(expected))

      whenReady(target.getByProcessCode(validId)) { result =>
        result shouldBe expected
      }
    }

    "Return a not found response when the input identifier is invalid" in new Test {
      val expected: RequestOutcome[PublishedProcess] = Left(NotFoundError)
      MockPublishedRepository
        .getByProcessCode(invalidId)
        .returns(Future.successful(expected))


      whenReady(target.getByProcessCode(invalidId)) { result =>
        result shouldBe expected
      }
    }

    "Return a not found response when no process has the identifier input to the method" in new Test {

      val expected: RequestOutcome[PublishedProcess] = Left(NotFoundError)

      MockPublishedRepository
        .getByProcessCode(validId)
        .returns(Future.successful(expected))

      whenReady(target.getByProcessCode(validId)) { result =>
        result shouldBe expected
      }
    }

    "Return an internal server error when the repository reports a database error" in new Test {

      val repositoryError: RequestOutcome[PublishedProcess] = Left(DatabaseError)

      val expected: RequestOutcome[JsObject] = Left(InternalServerError)

      MockPublishedRepository
        .getByProcessCode(validId)
        .returns(Future.successful(repositoryError))

      whenReady(target.getByProcessCode(validId)) { result =>
        result shouldBe expected
      }
    }
  }

  "Calling the save method" when {

    "the id and JSON are valid" should {
      "return valid Id" in new Test {

        val expected: RequestOutcome[String] = Right(validId)

        MockPublishedRepository
          .save(validId, "userId", "processCode", validOnePageJson.as[JsObject])
          .returns(Future.successful(expected))

        whenReady(target.save(validId, "userId", "processCode", validOnePageJson.as[JsObject])) {
          case Right(id) => id shouldBe validId
          case _ => fail
        }
      }
    }

    "the processCode already exists for another process" should {
      "return a DuplicateKeyError" in new Test {

        val expected: RequestOutcome[String] = Left(DuplicateKeyError)

        MockPublishedRepository
          .save(validId, "userId", "processCode", validOnePageJson.as[JsObject])
          .returns(Future.successful(expected))

        whenReady(target.save(validId, "userId", "processCode", validOnePageJson.as[JsObject])) {
          case Left(DuplicateKeyError) => succeed
          case _ => fail
        }
      }
    }

    "calling the archive method" when {
      "the id retrieves a document" should {
        "move it to the archive" in new Test {

          val expected: RequestOutcome[String] = Right(validId)

          MockPublishedRepository
            .getById(validId)
            .returns(Future.successful(Right(publishedProcess)))

          MockArchiveRepository
            .archive(validId, "userId", "processCode", publishedProcess)
            .returns(Future.successful(Right(validId)))

          MockApprovalRepository
            .changeStatus(validId, "Archived", "userId")
            .returns(Future.successful(Right(validId)))

          MockPublishedRepository
            .delete(validId)
            .returns(Future.successful(Right(validId)))

          whenReady(target.archive(validId, "userId")) { outcome =>
            if (outcome == expected) succeed else fail
          }
        }
      }

      "the id fails to retrieve a document" should {
        "return an error" in new Test {
          MockPublishedRepository
            .getById(validId)
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(target.archive(validId, "userId")) { outcome =>
            if (outcome.left.get == BadRequestError) succeed else fail
          }
        }
      }


      "the JSON is invalid" should {

        "not call the repository" in new Test {

          MockPublishedRepository
            .save(validId, "userId", "processCode", validOnePageJson.as[JsObject])
            .never()

          target.save(validId, "userId", "processCode", invalidProcess)
        }

        "return a bad request error" in new Test {
          val expected: RequestOutcome[String] = Left(BadRequestError)

          whenReady(target.save(validId, "userId", "processCode", invalidProcess)) {
            case result@Left(_) => result shouldBe expected
            case _ => fail
          }
        }
      }

      "a database error occurs" should {
        "return an internal error" in new Test {
          val repositoryResponse: RequestOutcome[String] = Left(DatabaseError)
          val expected: RequestOutcome[String] = Left(InternalServerError)

          MockPublishedRepository
            .save(validId, "userId", "processCode", validOnePageJson.as[JsObject])
            .returns(Future.successful(repositoryResponse))

          whenReady(target.save(validId, "userId", "processCode", validOnePageJson.as[JsObject])) {
            case result@Left(_) => result shouldBe expected
            case _ => fail
          }
        }
      }
    }
  }

}
