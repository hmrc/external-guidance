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

import java.time.ZonedDateTime

import base.UnitSpec
import mocks.MockPublishedRepository
import models.errors._
import models.ocelot.ProcessJson
import models.{PublishedProcess, RequestOutcome}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class PublishedServiceSpec extends UnitSpec {

  private trait Test extends MockPublishedRepository with ProcessJson {

    val validId: String = "ext90005"
    val invalidId: String = "ext9005"
    val invalidProcess: JsObject = Json.obj("idx" -> invalidId)
    val publishedProcess: PublishedProcess =
      PublishedProcess(validId, 1, ZonedDateTime.now(), validOnePageJson.as[JsObject], "user", processCode = "processCode")

    lazy val target: PublishedService = new PublishedService(mockPublishedRepository)
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

      val expected: RequestOutcome[JsObject] = Left(InternalServiceError)

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

      val expected: RequestOutcome[JsObject] = Left(InternalServiceError)

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
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }

    "a database error occurs" should {
      "return an internal error" in new Test {
        val repositoryResponse: RequestOutcome[String] = Left(DatabaseError)
        val expected: RequestOutcome[String] = Left(InternalServiceError)

        MockPublishedRepository
          .save(validId, "userId", "processCode", validOnePageJson.as[JsObject])
          .returns(Future.successful(repositoryResponse))

        whenReady(target.save(validId, "userId", "processCode", validOnePageJson.as[JsObject])) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }
  }

}
