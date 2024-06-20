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

package services

import base.BaseSpec
import mocks.MockLabelledDataRepository
import core.models.RequestOutcome
import core.models.errors._
import play.api.libs.json.{JsValue, Json, JsObject}
import mocks.MockAppConfig
import scala.concurrent.Future
import core.models.ocelot.Process
import java.time.{ZoneId, ZonedDateTime}
import models.{Timescales, LabelledData, UpdateDetails, LabelledDataUpdateStatus}
import core.models.MongoDateTimeFormats.localZoneID

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

  private trait Test extends MockLabelledDataRepository {

    lazy val target: TimescalesService = new TimescalesService(mockLabelledDataRepository, MockAppConfig)
    val lastUpdateTime: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, localZoneID)
    val lastUpdateTimeUTC: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, ZoneId.of("UTC"))
    val timescalesJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
    val timescalesJsonWithDeletion: JsValue = Json.parse("""{"Second": 2, "Third": 3, "Fourth": 4}""")

    val timescales: Map[String, Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
    val timeScalesWithVersion = (Map("First" -> 1, "Second" -> 2, "Third" -> 3), lastUpdateTime.toInstant.toEpochMilli)
    val timeScalesWithZeroVersion = (Map("First" -> 1, "Second" -> 2, "Third" -> 3), 0L)
    val credId: String = "234324234"
    val user: String = "User Blah"
    val email: String = "user@blah.com"
    val labelledData = LabelledData(Timescales, timescalesJson, lastUpdateTime.toInstant(), credId, user, email)
    val updateDetail = UpdateDetails(lastUpdateTimeUTC, "234324234", "User Blah", "user@blah.com")
    val labelledDataUpdateStatus = LabelledDataUpdateStatus(timescales.size, Some(updateDetail))

    val expected: RequestOutcome[LabelledData] = Right(labelledData)
  }

  "Calling save method" when {

    "the JSON is valid" should {
      "return LabelledDataUpdateStatus" in new Test{

        MockLabelledDataRepository
          .get(Timescales)
          .returns(Future.successful(expected))

        MockLabelledDataRepository
          .save(Timescales, timescalesJson, lastUpdateTime.toInstant(), credId, user, email)
          .returns(Future.successful(expected))

        whenReady(target.save(timescalesJson, credId, user, email, Nil)) {
          case Right(response) if response == labelledDataUpdateStatus => succeed
          case Right(response) =>
            println(labelledDataUpdateStatus)
            println(response)
            fail()
          case _ => fail()
        }
      }
    }

    "the JSON is valid but update contains deletions of inuse timescales" should {
      "return TimescalesResponse" in new Test{
        val timescalesWithRetainedDefn: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3, "Fourth": 4}""")

        MockLabelledDataRepository
          .get(Timescales)
          .returns(Future.successful(expected))

        MockLabelledDataRepository
          .save(Timescales, timescalesWithRetainedDefn, lastUpdateTime.toInstant(), credId, user, email)
          .returns(Future.successful(expected))

        whenReady(target.save(timescalesJsonWithDeletion, credId, user, email, List("First"))) {
          case Right(response) if response.lastUpdate.map(_.retainedDeletions).contains(List("First")) => succeed
          case Right(response) => fail()
          case Left(_) => fail()
        }
      }
    }

    "the JSON is valid but published service call fails" should {
      "return TimescalesResponse" in new Test{

        MockLabelledDataRepository
          .get(Timescales)
          .returns(Future.successful(Left(DatabaseError)))

        MockLabelledDataRepository
          .save(Timescales, timescalesJson, lastUpdateTime.toInstant(), credId, user, email)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.save(timescalesJsonWithDeletion, credId, user, email, Nil)) {
          case Right(response) => fail()
          case Left(_) => succeed
        }
      }
    }

    "the timescales JSON is not valid" should {
      "return Validation error" in new Test {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")

        whenReady(target.save(invalidTs, credId, user, email, Nil)) {
          case Right(_) => fail()
          case Left(ValidationError) => succeed
          case err => fail()
        }
      }
    }

    "the JSON is invalid" should {
      "not call the scratch repository" in new Test {
        MockLabelledDataRepository.save(Timescales, Json.parse("""{"Hello": "World"}"""), lastUpdateTime.toInstant(), credId, user, email).never()

        target.save(Json.parse("""{"Hello": "World"}"""), credId, user, email, Nil)
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test {
        MockLabelledDataRepository
          .get(Timescales)
          .returns(Future.successful(expected))

        MockLabelledDataRepository
          .save(Timescales, timescalesJson, lastUpdateTime.toInstant(), credId, user, email)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.save(timescalesJson, credId, user, email, Nil)) {
          case result @ Left(_) => result shouldBe Left(InternalServerError)
          case _ => fail()
        }
      }
    }
  }

  "Calling get method" should {

    "return the timescales" in new Test {
      MockLabelledDataRepository
        .get(Timescales)
        .returns(Future.successful(Right(labelledData)))

      whenReady(target.get()) { result =>
        result shouldBe Right(timeScalesWithVersion)
      }
    }

    "return Seed defnitions if no DB data found" in new Test {
      MockLabelledDataRepository
        .get(Timescales)
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.get()) { result =>
        result shouldBe Right(timeScalesWithZeroVersion)
      }
    }

    "return an internal error when a database error occurs" in new Test {
      MockLabelledDataRepository
        .get(Timescales)
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.get()) { result =>
        result shouldBe Left(InternalServerError)
      }
    }
  }

  "Calling details method" should {
    "Return complete details if timescales exist" in new Test {
      MockLabelledDataRepository
        .get(Timescales)
        .returns(Future.successful(Right(labelledData)))

      whenReady(target.details()) { result =>
        result shouldBe Right(labelledDataUpdateStatus)
      }
    }

    "Return details with no datetime if only seed timescales exist" in new Test {
      MockLabelledDataRepository
        .get(Timescales)
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.details()) { result =>
        result shouldBe Right(LabelledDataUpdateStatus(timescales.size, None))
      }

    }

    "return an internal error when a database error occurs" in new Test {
      MockLabelledDataRepository
        .get(Timescales)
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.details()) { result =>
        result shouldBe Left(InternalServerError)
      }
    }

  }

  "Calling updateProcessTimescaleTable method" should {

    "Update table using mongo timescale defns" in new Test {
      val process: Process = jsonWithBlankTsTable.as[Process]
      MockLabelledDataRepository
        .get(Timescales)
        .returns(Future.successful(Right(labelledData)))

      whenReady(target.updateProcessTable(jsonWithBlankTsTable, process)) { result =>
        result match {
          case Right((json, p)) =>
            p.meta.timescalesVersion shouldBe Some(labelledData.when.toEpochMilli())
            p.timescales shouldBe timescales
          case _ => fail()
        }
      }
    }

    "Update table using mongo timescale defns where json contains no timescale table" in new Test {
      val process: Process = jsonWithNoTsTable.as[Process]
      whenReady(target.updateProcessTable(jsonWithNoTsTable, process)) { result =>
        result match {
          case Right((json, p)) => p.timescales shouldBe Map()
          case _ => fail()
        }
      }
    }

    "Update table using seed timescale defns when no DB data found" in new Test {
      val process: Process = jsonWithBlankTsTable.as[Process]
      MockLabelledDataRepository
        .get(Timescales)
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.updateProcessTable(jsonWithBlankTsTable, process)) { result =>
        result match {
          case Right((json, p)) => p.timescales shouldBe timescales
          case _ => fail()
        }
      }
    }


    "return an internal error if a database error occurs" in new Test {
      val process: Process = jsonWithBlankTsTable.as[Process]
      MockLabelledDataRepository
        .get(Timescales)
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.updateProcessTable(jsonWithBlankTsTable, process)) { result =>
        result shouldBe Left(InternalServerError)
      }
    }
  }

}
