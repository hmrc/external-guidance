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
import mocks.MockTimescalesRepository
import core.models.RequestOutcome
import core.models.errors._
import play.api.libs.json.{JsValue, Json, JsObject}
import mocks.MockAppConfig
import scala.concurrent.Future
import core.models.ocelot.Process

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

  private trait Test extends MockTimescalesRepository {
    lazy val target: TimescalesService = new TimescalesService(mockTimescalesRepository, MockAppConfig)
    val timescales: Map[String, Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
    val timescaleJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
  }

  "Calling save method" when {

    "the JSON is valid" should {
      "return Unit" in new Test{

        val expected: RequestOutcome[Unit] = Right(())

        MockTimescalesRepository
          .save(timescaleJson)
          .returns(Future.successful(expected))

        whenReady(target.save(timescaleJson)) {
          case result => result shouldBe expected
        }
      }
    }

    "the timescales JSON is not valid" should {
      "return Validation error" in new Test {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")

        whenReady(target.save(invalidTs)) {
          case Right(_) => fail
          case Left(ValidationError) => succeed
          case err => fail
        }
      }
    }

    "the JSON is invalid" should {
      "not call the scratch repository" in new Test {
        MockTimescalesRepository.save(Json.parse("""{"Hello": "World"}""")).never()

        target.save(Json.parse("""{"Hello": "World"}"""))
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test {
        MockTimescalesRepository
          .save(timescaleJson)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.save(timescaleJson)) {
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
        .returns(Future.successful(Right(timescaleJson)))

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

  "Calling updateTimescaleTable method" should {

    "Update table using mongo timescale defns" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Right(timescaleJson)))

      whenReady(target.updateTimescaleTable(jsonWithBlankTsTable)) { result =>
        result match {
          case Right(json) => (json.as[Process]).timescales shouldBe timescales
          case _ => fail
        }
      }
    }

    "Update table using mongo timescale defns where json contains no timescale table" in new Test {
      whenReady(target.updateTimescaleTable(jsonWithNoTsTable)) { result =>
        result match {
          case Right(json) => (json.as[Process]).timescales shouldBe Map()
          case _ => fail
        }
      }
    }

    "Update table using mongo timescale defns where json is not a valid Process" in new Test {
      whenReady(target.updateTimescaleTable(Json.parse("{}").as[JsObject])) { result =>
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

      whenReady(target.updateTimescaleTable(jsonWithBlankTsTable)) { result =>
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

      whenReady(target.updateTimescaleTable(jsonWithBlankTsTable)) { result =>
        result shouldBe Left(InternalServerError)
      }
    }
  }

}