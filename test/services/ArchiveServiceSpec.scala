/*
 * Copyright 2023 HM Revenue & Customs
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
import mocks.MockArchiveRepository
import core.models.errors._
import core.models.ocelot.ProcessJson
import core.models.RequestOutcome
import models.{ProcessSummary, ArchivedProcess}
import play.api.libs.json.{JsObject, Json, OFormat, JsArray}

import scala.concurrent.Future

class ArchiveServiceSpec extends BaseSpec {

  private trait Test extends MockArchiveRepository with ProcessJson {

    val when: ZonedDateTime = ZonedDateTime.now
    val validId: String = when.toInstant.toEpochMilli().toString
    val processSummary = ProcessSummary(validId, "process_code", 1, "author", None, when, "Actioner", "Archived")
    val invalidId: String = "ext9005"
    val invalidProcess: JsObject = Json.obj("idx" -> invalidId)
    val process: ArchivedProcess =
      ArchivedProcess(validId.toLong, ZonedDateTime.now(), validOnePageJson.as[JsObject], "user", processCode = "processCode")

    lazy val target: ArchiveService = new ArchiveService(mockArchiveRepository)
  }

  "The method getById of class ArchiveService" should {

    "Return a JsObject representing an Ocelot process when the input identifies a valid process" in new Test {

      val expected: RequestOutcome[ArchivedProcess] = Right(process)

      MockArchiveRepository
        .getById(validId)
        .returns(Future.successful(expected))

      whenReady(target.getById(validId)) { result =>
        result shouldBe expected
      }
    }

    "Return a bad request response when the input identifier is invalid" in new Test {

      val expected: RequestOutcome[ArchivedProcess] = Left(BadRequestError)

      whenReady(target.getById(invalidId)) { result =>
        result shouldBe expected
      }
    }

    "Return a not found response when no process has the identifier input to the method" in new Test {

      val expected: RequestOutcome[ArchivedProcess] = Left(NotFoundError)

      MockArchiveRepository
        .getById(validId)
        .returns(Future.successful(expected))

      whenReady(target.getById(validId)) { result =>
        result shouldBe expected
      }
    }

    "Return an internal server error when the repository reports a database error" in new Test {

      val repositoryError: RequestOutcome[ArchivedProcess] = Left(DatabaseError)

      val expected: RequestOutcome[JsObject] = Left(InternalServerError)

      MockArchiveRepository
        .getById(validId)
        .returns(Future.successful(repositoryError))

      whenReady(target.getById(validId)) { result =>
        result shouldBe expected
      }
    }
  }

  "Calling the list method" when {
    "there are entries to return" should {
      "return a List of approval processes" in new Test {
        implicit val formats: OFormat[ProcessSummary] = Json.format[ProcessSummary]

        val expected: RequestOutcome[List[ProcessSummary]] = Right(List(processSummary))

        MockArchiveRepository
          .processSummaries()
          .returns(Future.successful(expected))

        whenReady(target.list) {
          case Right(jsonList) =>
            val list: List[ProcessSummary] = jsonList.as[List[ProcessSummary]]
            list.size shouldBe 1
            val entry = list.head
            entry.id shouldBe processSummary.id
            entry.status shouldBe processSummary.status
          case _ => fail()
        }
      }
    }

    "there are no processes in the database" should {
      "return an empty list" in new Test {

        val expected: RequestOutcome[JsArray] = Right(JsArray())
        val returnedList: RequestOutcome[List[ProcessSummary]] = Right(List())

        MockArchiveRepository
          .processSummaries()
          .returns(Future.successful(returnedList))

        whenReady(target.list) { result =>
          result shouldBe expected
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[List[ProcessSummary]] = Left(DatabaseError)
        val expected: RequestOutcome[List[ProcessSummary]] = Left(InternalServerError)

        MockArchiveRepository
          .processSummaries()
          .returns(Future.successful(repositoryError))

        whenReady(target.list) { result =>
          result shouldBe expected
        }
      }
    }
  }

}
