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
import core.services.Rates
import core.models.errors._
import core.models.ocelot.Process
import models.{LabelledData, Rates, LabelledDataUpdateStatus}
import core.models.RequestOutcome
import play.api.libs.json.{JsValue, Json, JsObject}
import mocks.MockAppConfig
import java.time.{Instant, ZonedDateTime, ZoneId}
import models.UpdateDetails
import scala.concurrent.Future

class RatesServiceSpec extends BaseSpec {
  val jsonWithNoRatesTable: JsObject = Json.parse(
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

  val jsonWithBlankRatesTable: JsObject = Json.parse(
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
    |  "rates" : {
    |      "Legacy!higherrate!2016" : 0,
    |      "Legacy!basicrate!2022" : 0,
    |      "TaxNic!CTC!2016" : 0
    |  }
    |}
  """.stripMargin
  ).as[JsObject]

  val jsonWithUpdatedRatesTable: JsObject = Json.parse(
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
    |  "rates" : {
    |      "Legacy!higherrate!2016" : 0.4,
    |      "Legacy!basicrate!2022" : 0.2,
    |      "TaxNic!CTC!2016" : 0
    |  }
    |}
  """.stripMargin
  ).as[JsObject]

  private trait Test extends MockLabelledDataRepository {

    lazy val target: RatesService = new RatesService(mockLabelledDataRepository, new Rates(), MockAppConfig)
    val lastUpdateTime: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, ZoneId.of("UTC"))
    val lastUpdateInstant: Instant = lastUpdateTime.toInstant()
    val rates: Map[String, BigDecimal] = Map("Legacy!higherrate!2016" -> 0.4, "Legacy!basicrate!2022" -> 0.2, "TaxNic!CTC!2016" -> 0)
    val credId: String = "234324234"
    val user: String = "User Blah"
    val email: String = "user@blah.com"
    val ratesJsonString =
      """
     |{
     |  "Legacy": {
     |      "higherrate":
     |      {
     |          "2016": 0.4,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 45
     |      },
     |      "basicrate":
     |      {
     |          "2016": 45,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 0.2
     |      }
     |  },
     |  "TaxNic": {
     |      "BasicRate":
     |      {
     |          "2016": 20,
     |          "2017": 20,
     |          "2018": 20,
     |          "2019": 20,
     |          "2020": 20,
     |          "2021": 20,
     |          "2022": 20
     |      },
     |      "CTC":
     |      {
     |          "2016": 0,
     |          "2017": 0,
     |          "2018": 0,
     |          "2019": 0,
     |          "2020": 0,
     |          "2021": 0,
     |          "2022": 0.567
     |      }
     |  }
     |}
    """.stripMargin
    val ratesJsonWithDeletionString =
      """
     |{
     |  "Legacy": {
     |      "higherrate":
     |      {
     |          "2016": 0.4,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 45
     |      },
     |      "basicrate":
     |      {
     |          "2016": 45,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 0.2
     |      }
     |  },
     |  "TaxNic": {
     |      "BasicRate":
     |      {
     |          "2019": 20,
     |          "2020": 20,
     |          "2021": 20,
     |          "2022": 20
     |      },
     |      "CTC":
     |      {
     |          "2017": 0,
     |          "2018": 0,
     |          "2019": 0,
     |          "2020": 0,
     |          "2021": 0,
     |          "2022": 0.567
     |      }
     |  }
     |}
    """.stripMargin
    val ratesJsonWithDeletionAndRetainedString =
      """
     |{
     |  "Legacy": {
     |      "higherrate":
     |      {
     |          "2016": 0.4,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 45
     |      },
     |      "basicrate":
     |      {
     |          "2016": 45,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 0.2
     |      }
     |  },
     |  "TaxNic": {
     |      "BasicRate":
     |      {
     |          "2019": 20,
     |          "2020": 20,
     |          "2021": 20,
     |          "2022": 20
     |      },
     |      "CTC":
     |      {
     |          "2016": 0,
     |          "2017": 0,
     |          "2018": 0,
     |          "2019": 0,
     |          "2020": 0,
     |          "2021": 0,
     |          "2022": 0.567
     |      }
     |  }
     |}
    """.stripMargin

    val ratesFourDimMap: Map[String, Map[String, Map[String, BigDecimal]]] =
      Map(
        ("Legacy" -> Map(("higherrate" -> Map(
                            ("2016" -> 0.4),
                            ("2017" -> 45),
                            ("2018" -> 45),
                            ("2019" -> 45),
                            ("2020" -> 45),
                            ("2021" -> 45),
                            ("2022" -> 45)
                          )
                         ),
                         ("basicrate" -> Map(
                            ("2016" -> 45),
                            ("2017" -> 45),
                            ("2018" -> 45),
                            ("2019" -> 45),
                            ("2020" -> 45),
                            ("2021" -> 45),
                            ("2022" -> 0.2)
                          )
                         )
                        )
      ),
      ("TaxNic" ->  Map(("BasicRate" -> Map(
                                  ("2016" -> 20),
                                  ("2017" -> 20),
                                  ("2018" -> 20),
                                  ("2019" -> 20),
                                  ("2020" -> 20),
                                  ("2021" -> 20),
                                  ("2022" -> 20)
                                )
                              ),
                              ("CTC" -> Map(
                                   ("2016" -> 0),
                                   ("2017" -> 0),
                                   ("2018" -> 0),
                                   ("2019" -> 0),
                                   ("2020" -> 0),
                                   ("2021" -> 0),
                                   ("2022" -> 0.567)
                                )
                              )
                            )
        )
      )

    val ratesTwoDimMap: Map[String, BigDecimal] = Map(
       ("Legacy!higherrate!2016" -> 0.4),
       ("Legacy!higherrate!2017" -> 45),
       ("Legacy!higherrate!2018" -> 45),
       ("Legacy!higherrate!2019" -> 45),
       ("Legacy!higherrate!2020" -> 45),
       ("Legacy!higherrate!2021" -> 45),
       ("Legacy!higherrate!2022" -> 45),
       ("Legacy!basicrate!2016" -> 45),
       ("Legacy!basicrate!2017" -> 45),
       ("Legacy!basicrate!2018" -> 45),
       ("Legacy!basicrate!2019" -> 45),
       ("Legacy!basicrate!2020" -> 45),
       ("Legacy!basicrate!2021" -> 45),
       ("Legacy!basicrate!2022" -> 0.2),
       ("TaxNic!BasicRate!2016" -> 20),
       ("TaxNic!BasicRate!2017" -> 20),
       ("TaxNic!BasicRate!2018" -> 20),
       ("TaxNic!BasicRate!2019" -> 20),
       ("TaxNic!BasicRate!2020" -> 20),
       ("TaxNic!BasicRate!2021" -> 20),
       ("TaxNic!BasicRate!2022" -> 20),
       ("TaxNic!CTC!2016" -> 0),
       ("TaxNic!CTC!2017" -> 0),
       ("TaxNic!CTC!2018" -> 0),
       ("TaxNic!CTC!2019" -> 0),
       ("TaxNic!CTC!2020" -> 0),
       ("TaxNic!CTC!2021" -> 0),
       ("TaxNic!CTC!2022" -> 0.567)
    )

    val ratesJson = Json.parse(ratesJsonString)
    val labelledData = LabelledData(Rates, ratesJson, lastUpdateTime.toInstant(), credId, user, email)
    val expected: RequestOutcome[LabelledData] = Right(labelledData)
    val labelledDataStatus = LabelledDataUpdateStatus(ratesTwoDimMap.size, Some(UpdateDetails(lastUpdateTime, credId, user, email)))

    val ratesJsonWithDeletion = Json.parse(ratesJsonWithDeletionString)
    val labelledDataWithDeletion = LabelledData(Rates, ratesJsonWithDeletion, lastUpdateTime.toInstant(), credId, user, email)
    val expectedWithDeletion: RequestOutcome[LabelledData] = Right(labelledDataWithDeletion)

    val ratesJsonWithDeletionAndRetained = Json.parse(ratesJsonWithDeletionAndRetainedString)
    val labelledDataWithDeletionAndRetained = LabelledData(Rates, ratesJsonWithDeletionAndRetained, lastUpdateTime.toInstant(), credId, user, email)
    val expectedWithDeletionAndRetained: RequestOutcome[LabelledData] = Right(labelledDataWithDeletionAndRetained)

    val seedRates =  target.seedRates().getOrElse(fail())
    val seedRatesTwoDimMap = target.twoDimMapFromFour(seedRates)
    val seedRatesJson = target.seedRatesAsJson().getOrElse(fail())

    val labelledSeedData = LabelledData(Rates, seedRatesJson, lastUpdateTime.toInstant(), credId, user, email)
    val ratesWithVersion = (ratesTwoDimMap, lastUpdateInstant.toEpochMilli)
    val ratesWithZeroVersion = (seedRatesTwoDimMap, 0L)

  }

  "Calling seedRates" when {

    "Using conf release" should {
      "return a valid 4d map" in new Test {
        seedRates.size shouldBe 6
        seedRates("Legacy").size shouldBe 256
      }

      "data can be converted to 2d map" in new Test {
        target.twoDimMapFromFour(seedRates).size shouldBe 7763
      }

      "2d map can be converted to 4d map" in new Test {
        target.fourDimMapFromTwo(target.twoDimMapFromFour(seedRates)).fold(fail()){nmp =>
          nmp.size shouldBe 6
          nmp("Legacy").size shouldBe 256
        }
      }

      "Round trip; json to 2d map to json" in new Test {
        seedRatesJson.asOpt[Map[String, Map[String, Map[String, BigDecimal]]]].map{mp =>
          val twoDMap = target.twoDimMapFromFour(mp)
          target.fourDimMapFromTwo(twoDMap).fold(fail()){nmp =>
            nmp.size shouldBe 6
            nmp("Legacy").size shouldBe 256
            Json.toJson(nmp) shouldBe seedRatesJson
          }
        }
      }
    }

    "Using test release" should {
      "return a valid 4 dim map" in new Test {
        target.seedRates("test/data/rates.json").fold(fail()){mp =>
          mp.size shouldBe 6
        }
      }

      "data can be converted to 2 dim map" in new Test {
        target.seedRates("test/data/rates.json").fold(fail()){mp =>
          target.twoDimMapFromFour(mp).size shouldBe 7763
        }
      }
    }
  }

  "Calling save method" when {

    "the JSON is valid" should {
      "return LabelledDataUpdateStatus" in new Test{
        val json = data.ExampleLabelledData.rates
        override val labelledData = LabelledData(Rates, json, lastUpdateTime.toInstant(), credId, user, email)
        override val expected: RequestOutcome[LabelledData] = Right(labelledData)

        val expectedStatus = LabelledDataUpdateStatus(1, Some(UpdateDetails(lastUpdateTime, credId, user, email, Nil)))

        MockLabelledDataRepository
          .get(Rates)
          .returns(Future.successful(expected))

        MockLabelledDataRepository
          .save(Rates, json, lastUpdateInstant, credId, user, email)
          .returns(Future.successful(expected))

        whenReady(target.save(json, credId, user, email, Nil)) {
          case Right(response) =>
            if (response == expectedStatus) succeed
          case _ => fail()
        }
      }
    }

    "the JSON is valid but update contains deletions of inuse rates" should {
      "return LabelledDataUpdateStatus" in new Test{
        MockLabelledDataRepository
          .get(Rates)
          .returns(Future.successful(expected))

        MockLabelledDataRepository
          .save(Rates, ratesJsonWithDeletionAndRetained, lastUpdateInstant, credId, user, email)
          .returns(Future.successful(expectedWithDeletionAndRetained))

        whenReady(target.save(ratesJsonWithDeletion, credId, user, email, List("TaxNic!CTC!2016"))) {
          case Right(response) if response.lastUpdate.map(_.retainedDeletions).contains(List("TaxNic!CTC!2016")) => succeed
          case Right(response) => fail()
          case Left(_) => fail()
        }
      }
    }

   "the JSON is valid but published service call fails" should {
      "return LabelledDataUpdateStatus" in new Test{

        MockLabelledDataRepository
          .get(Rates)
          .returns(Future.successful(Left(DatabaseError)))

        MockLabelledDataRepository
          .save(Rates, ratesJson, lastUpdateInstant, credId, user, email)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.save(ratesJsonWithDeletion, credId, user, email, Nil)) {
          case Right(response) => fail()
          case Left(_) => succeed
        }
      }
    }

    "the rates JSON is not valid" should {
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
        MockLabelledDataRepository.save(Rates, Json.parse("""{"Hello": "World"}"""), lastUpdateInstant, credId, user, email).never()

        target.save(Json.parse("""{"Hello": "World"}"""), credId, user, email, Nil)
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test {
        MockLabelledDataRepository
          .get(Rates)
          .returns(Future.successful(expected))

        MockLabelledDataRepository
          .save(Rates, ratesJson, lastUpdateInstant, credId, user, email)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.save(ratesJson, credId, user, email, Nil)) {
          case result @ Left(_) => result shouldBe Left(InternalServerError)
          case _ => fail()
        }
      }
    }
  }

  "Calling get method" should {

    "return the rates" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Right(labelledData)))

      whenReady(target.get()) { result =>
        result shouldBe Right(ratesWithVersion)
      }
    }

    "return Seed defnitions if no DB data found" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.get()) { result =>
        result shouldBe Right(ratesWithZeroVersion)
      }
    }

    "return an internal error when a database error occurs" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.get()) { result =>
        result shouldBe Left(InternalServerError)
      }
    }
  }

  "Calling details method" should {
    "Return complete details if rates exist" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Right(labelledData)))

      whenReady(target.details()) { result =>
        result shouldBe Right(labelledDataStatus)
      }
    }

    "Return details with no datetime if only seed rates exist" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.details()) { result =>
        result shouldBe Right(LabelledDataUpdateStatus(seedRatesTwoDimMap.size, None))
      }

    }

    "return an internal error when a database error occurs" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.details()) { result =>
        result shouldBe Left(InternalServerError)
      }
    }

  }

  "Calling updateProcessRatesTableAndDetails method" should {

    "Update table using mongo rates defns" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Right(labelledData)))

      whenReady(target.updateProcessRatesTableAndDetails(jsonWithBlankRatesTable)) { result =>
        result match {
          case Right(json) =>
            val process = json.as[Process]
            process.meta.ratesVersion shouldBe Some(labelledData.when.toEpochMilli())
            process.rates shouldBe rates
          case _ => fail()
        }
      }
    }

    "Update table using mongo rates defns where json contains no rates table" in new Test {
      whenReady(target.updateProcessRatesTableAndDetails(jsonWithNoRatesTable)) { result =>
        result match {
          case Right(json) => (json.as[Process]).rates shouldBe Map()
          case _ => fail()
        }
      }
    }

    "Update table using mongo rates defns where json is not a valid Process" in new Test {
      val update = Json.parse("{}").as[JsObject]
      whenReady(target.updateProcessRatesTableAndDetails(update)) { result =>
        result match {
          case Right(_) => fail()
          case Left(err) => err shouldBe ValidationError
        }
      }
    }

    "Update table using seed rates defns when no DB data found" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.updateProcessRatesTableAndDetails(jsonWithBlankRatesTable)) { result =>
        result match {
          case Right(json) => (json.as[Process]).rates shouldBe rates
          case _ => fail()
        }
      }
    }


    "return an internal error if a database error occurs" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.updateProcessRatesTableAndDetails(jsonWithBlankRatesTable)) { result =>
        result shouldBe Left(InternalServerError)
      }
    }
  }
}
