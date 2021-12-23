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

import base.BaseSpec
import mocks.{MockTimescalesRepository, MockPublishedService}
import core.models.RequestOutcome
//import core.models.{RequestOutcome, MongoDateTimeFormats}
import core.models.errors._
import play.api.libs.json.{JsValue, Json, JsObject}
import mocks.MockAppConfig
import scala.concurrent.Future
import core.models.ocelot.Process
import java.time.ZonedDateTime
import models.{TimescalesResponse, UpdateDetails, TimescalesUpdate}
import mocks.MockApprovalService

class TimescalesServiceSpec extends BaseSpec {
  val jsonWithNoTsTable: JsObject = Json.parse(
  """{
    |  "meta": {
    |    "title": "Customer wants to make a cup of tea",
    |    "id": "oct90001",
    |    "ocelot": 1,
    |    "lastAuthor": "000000",
    |    "lastUpdate": 1500298931016,
    |    "version": 4,
    |    "filename": "oct90001.js",
    |    "titlePhrase": 8,
    |    "processCode": "cup-of-tea"
    |  },
    |  "howto": [],
    |  "contacts": [],
    |  "links": [],
    |  "flow": {
    |    "start": {
    |      "type": "PageStanza",
    |      "url": "/feeling-bad",
    |      "next": ["3"],
    |      "stack": true
    |    },
    |    "3": {
    |      "type": "InstructionStanza",
    |      "text": 1,
    |      "next": [
    |        "2"
    |      ],
    |      "stack": true
    |    },
    |    "2": {
    |      "type": "InstructionStanza",
    |      "text": 0,
    |      "next": [
    |        "end"
    |      ],
    |      "stack": true
    |    },
    |    "end": {
    |      "type": "EndStanza"
    |    }
    |  },
    |  "phrases": [
    |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
    |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
    |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
    |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
    |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
    |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
    |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
    |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
    |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
    |  ]
    |}
  """.stripMargin
  ).as[JsObject]

  val jsonWithBlankTsTable: JsObject = Json.parse(
  """{
    |  "meta": {
    |    "title": "Customer wants to make a cup of tea",
    |    "id": "oct90001",
    |    "ocelot": 1,
    |    "lastAuthor": "000000",
    |    "lastUpdate": 1500298931016,
    |    "version": 4,
    |    "filename": "oct90001.js",
    |    "titlePhrase": 8,
    |    "processCode": "cup-of-tea"
    |  },
    |  "howto": [],
    |  "contacts": [],
    |  "links": [],
    |  "flow": {
    |    "start": {
    |      "type": "PageStanza",
    |      "url": "/feeling-bad",
    |      "next": ["3"],
    |      "stack": true
    |    },
    |    "3": {
    |      "type": "InstructionStanza",
    |      "text": 1,
    |      "next": [
    |        "2"
    |      ],
    |      "stack": true
    |    },
    |    "2": {
    |      "type": "InstructionStanza",
    |      "text": 0,
    |      "next": [
    |        "end"
    |      ],
    |      "stack": true
    |    },
    |    "end": {
    |      "type": "EndStanza"
    |    }
    |  },
    |  "phrases": [
    |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
    |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
    |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
    |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
    |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
    |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
    |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
    |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
    |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
    |  ],
    |  "timescales" : {
    |      "First" : 0,
    |      "Second" : 0,
    |      "Third" : 0
    |  }
    |}
  """.stripMargin
  ).as[JsObject]

  val jsonWithUpdatedTsTable: JsObject = Json.parse(
  """
    |{
    |  "meta": {
    |    "title": "Customer wants to make a cup of tea",
    |    "id": "oct90001",
    |    "ocelot": 1,
    |    "lastAuthor": "000000",
    |    "lastUpdate": 1500298931016,
    |    "version": 4,
    |    "filename": "oct90001.js",
    |    "titlePhrase": 8,
    |    "processCode": "cup-of-tea"
    |  },
    |  "howto": [],
    |  "contacts": [],
    |  "links": [],
    |  "flow": {
    |    "start": {
    |      "type": "PageStanza",
    |      "url": "/feeling-bad",
    |      "next": ["3"],
    |      "stack": true
    |    },
    |    "3": {
    |      "type": "InstructionStanza",
    |      "text": 1,
    |      "next": [
    |        "2"
    |      ],
    |      "stack": true
    |    },
    |    "2": {
    |      "type": "InstructionStanza",
    |      "text": 0,
    |      "next": [
    |        "end"
    |      ],
    |      "stack": true
    |    },
    |    "end": {
    |      "type": "EndStanza"
    |    }
    |  },
    |  "phrases": [
    |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
    |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
    |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
    |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
    |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
    |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
    |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
    |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
    |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
    |  ],
    |  "timescales" : {
    |      "First" : 1,
    |      "Second" : 2,
    |      "Third" : 3
    |  }
    |}
  """.stripMargin
  ).as[JsObject]

  private trait Test extends MockTimescalesRepository
    with MockPublishedService
    with MockApprovalService {

    lazy val target: TimescalesService = new TimescalesService(mockTimescalesRepository, MockAppConfig)
    val lastUpdateTime: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, MongoDateTimeFormats.localZoneID)
    val timescalesJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
    val timescalesJsonWithDeletion: JsValue = Json.parse("""{"Second": 2, "Third": 3, "Fourth": 4}""")

    val timescales: Map[String, Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
    val credId: String = "234324234"
    val user: String = "User Blah"
    val email: String = "user@blah.com"
    val timescalesUpdate = TimescalesUpdate(timescalesJson, lastUpdateTime, credId, user, email)
    val updateDetail = UpdateDetails(lastUpdateTime, "234324234", "User Blah", "user@blah.com")
    val timescalesResponse = TimescalesResponse(timescales.size, Some(updateDetail))

    val expected: RequestOutcome[TimescalesUpdate] = Right(timescalesUpdate)
  }

  "Calling save method" when {

    "the JSON is valid" should {
      "return TimescalesResponse" in new Test{

        MockTimescalesRepository
          .get(mockTimescalesRepository.CurrentTimescalesID)
          .returns(Future.successful(expected))

        MockTimescalesRepository
          .save(timescalesJson, lastUpdateTime, credId, user, email)
          .returns(Future.successful(expected))

        MockPublishedService
          .getTimescalesInUse()
          .returns(Future.successful(Right(Nil)))

        MockApprovalService
          .getTimescalesInUse()
          .returns(Future.successful(Right(Nil)))

        whenReady(target.save(timescalesJson, credId, user, email, Nil)) {
          case Right(response) if response == timescalesResponse => succeed
          case _ => fail
        }
      }
    }

    "the JSON is valid but update contains deletions of inuse timescales" should {
      "return TimescalesResponse" in new Test{
        val timescalesWithRetainedDefn: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3, "Fourth": 4}""")

        MockTimescalesRepository
          .get(mockTimescalesRepository.CurrentTimescalesID)
          .returns(Future.successful(expected))

        MockTimescalesRepository
          .save(timescalesWithRetainedDefn, lastUpdateTime, credId, user, email)
          .returns(Future.successful(expected))

        MockPublishedService
          .getTimescalesInUse()
          .returns(Future.successful(Right(List("First"))))

        MockApprovalService
          .getTimescalesInUse()
          .returns(Future.successful(Right(List("First"))))

        whenReady(target.save(timescalesJsonWithDeletion, credId, user, email, List("First"))) {
          case Right(response) if response.lastUpdate.map(_.retainedDeletions).contains(List("First")) => succeed
          case Right(response) => fail
          case Left(_) => fail
        }
      }
    }

    "the JSON is valid but published service call fails" should {
      "return TimescalesResponse" in new Test{

        MockTimescalesRepository
          .get(mockTimescalesRepository.CurrentTimescalesID)
          .returns(Future.successful(Left(DatabaseError)))

        MockTimescalesRepository
          .save(timescalesJson, lastUpdateTime, credId, user, email)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.save(timescalesJsonWithDeletion, credId, user, email, Nil)) {
          case Right(response) => fail
          case Left(_) => succeed
        }
      }
    }

    "the timescales JSON is not valid" should {
      "return Validation error" in new Test {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")

        whenReady(target.save(invalidTs, credId, user, email, Nil)) {
          case Right(_) => fail
          case Left(ValidationError) => succeed
          case err => fail
        }
      }
    }

    "the JSON is invalid" should {
      "not call the scratch repository" in new Test {
        MockTimescalesRepository.save(Json.parse("""{"Hello": "World"}"""), lastUpdateTime, credId, user, email).never()

        target.save(Json.parse("""{"Hello": "World"}"""), credId, user, email, Nil)
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test {
        MockTimescalesRepository
          .get(mockTimescalesRepository.CurrentTimescalesID)
          .returns(Future.successful(expected))

        MockPublishedService
          .getTimescalesInUse()
          .returns(Future.successful(Right(Nil)))

        MockTimescalesRepository
          .save(timescalesJson, lastUpdateTime, credId, user, email)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.save(timescalesJson, credId, user, email, Nil)) {
          case result @ Left(_) => result shouldBe Left(InternalServerError)
          case _ => fail
        }
      }
    }
  }

  "Calling get method" should {

    "return the timescales" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Right(timescalesUpdate)))

      whenReady(target.get) { result =>
        result shouldBe Right(timescales)
      }
    }

    "return Seed defnitions if no DB data found" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.get) { result =>
        result shouldBe Right(timescales)
      }
    }

    "return an internal error when a database error occurs" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.get) { result =>
        result shouldBe Left(InternalServerError)
      }
    }
  }

  "Calling details method" should {
    "Return complete details if timescales exist" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Right(timescalesUpdate)))

      whenReady(target.details()) { result =>
        result shouldBe Right(timescalesResponse)
      }
    }

    "Return details with no datetime if only seed timescales exist" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.details()) { result =>
        result shouldBe Right(TimescalesResponse(timescales.size, None))
      }

    }

    "return an internal error when a database error occurs" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.details()) { result =>
        result shouldBe Left(InternalServerError)
      }
    }

  }

  "Calling updateProcessTimescaleTable method" should {

    "Update table using mongo timescale defns" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Right(timescalesUpdate)))

      whenReady(target.updateProcessTimescaleTable(jsonWithBlankTsTable)) { result =>
        result match {
          case Right(json) => (json.as[Process]).timescales shouldBe timescales
          case _ => fail
        }
      }
    }

    "Update table using mongo timescale defns where json contains no timescale table" in new Test {
      whenReady(target.updateProcessTimescaleTable(jsonWithNoTsTable)) { result =>
        result match {
          case Right(json) => (json.as[Process]).timescales shouldBe Map()
          case _ => fail
        }
      }
    }

    "Update table using mongo timescale defns where json is not a valid Process" in new Test {
      val update = Json.parse("{}").as[JsObject]
      whenReady(target.updateProcessTimescaleTable(update)) { result =>
        result match {
          case Right(_) => fail
          case Left(err) => err shouldBe ValidationError
        }
      }
    }

    "Update table using seed timescale defns when no DB data found" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.updateProcessTimescaleTable(jsonWithBlankTsTable)) { result =>
        result match {
          case Right(json) => (json.as[Process]).timescales shouldBe timescales
          case _ => fail
        }
      }
    }


    "return an internal error if a database error occurs" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.updateProcessTimescaleTable(jsonWithBlankTsTable)) { result =>
        result shouldBe Left(InternalServerError)
      }
    }
  }

}
