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
import models.{LabelledData, Rates}
import core.models.RequestOutcome
import play.api.libs.json.{JsValue, Json}
import mocks.MockAppConfig
import java.time.{Instant, ZonedDateTime}
import models.{TimescalesResponse, UpdateDetails, TimescalesUpdate}
import core.models.MongoDateTimeFormats.localZoneID

class RatesServiceSpec extends BaseSpec {

  private trait Test extends MockLabelledDataRepository {

    lazy val target: RatesService = new RatesService(mockLabelledDataRepository, new Rates(), MockAppConfig)
    val lastUpdateTime: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, localZoneID)
    val lastUpdateInstant: Instant = lastUpdateTime.toInstant()
    val timescalesJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
    val timescalesJsonWithDeletion: JsValue = Json.parse("""{"Second": 2, "Third": 3, "Fourth": 4}""")

    val timescales: Map[String, Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
    val timeScalesWithVersion = (Map("First" -> 1, "Second" -> 2, "Third" -> 3), lastUpdateTime.toInstant.toEpochMilli)
    val timeScalesWithZeroVersion = (Map("First" -> 1, "Second" -> 2, "Third" -> 3), 0L)
    val credId: String = "234324234"
    val user: String = "User Blah"
    val email: String = "user@blah.com"
    val timescalesUpdate = TimescalesUpdate(timescalesJson, lastUpdateTime, credId, user, email)
    val updateDetail = UpdateDetails(lastUpdateTime, "234324234", "User Blah", "user@blah.com")
    val timescalesResponse = TimescalesResponse(timescales.size, Some(updateDetail))
    val ratesJsonString =
      """
     |{
     |  "Legacy": {
     |      "TopRate":
     |      {
     |          "2016": 45,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 45
     |      }
     |  },
     |  "TaxNicScot": {
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
     |          "2016": 0.45,
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
    val ratesJson = Json.parse(ratesJsonString)
    val labelledData = LabelledData(Rates, ratesJson, lastUpdateTime.toInstant(), credId, user, email)
    val expected: RequestOutcome[LabelledData] = Right(labelledData)
    val seedRates =  target.seedRates().getOrElse(fail())
    val seedRatesJson = target.seedRatesAsJson().getOrElse(fail())
  }

  "Calling seedRates" when {

    "Using conf release" should {
      "return a valid 4d map" in new Test {
        seedRates.size shouldBe 6
        seedRates("Legacy").size shouldBe 256
      }

      "data can be converted to 2d map" in new Test {
        target.toMap(seedRates).size shouldBe 7763
      }

      "2d map can be converted to 4d map" in new Test {
        target.unMap(target.toMap(seedRates)).fold(fail()){nmp =>
          nmp.size shouldBe 6
          nmp("Legacy").size shouldBe 256
        }
      }

      "Round trip; json to 2d map to json" in new Test {
        seedRatesJson.asOpt[Map[String, Map[String, Map[String, BigDecimal]]]].map{mp =>
          val twoDMap = target.toMap(mp)
          target.unMap(twoDMap).fold(fail()){nmp =>
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
          target.toMap(mp).size shouldBe 7763
        }
      }
    }
  }

  "Calling save method" when {

    // "the JSON is valid" should {
    //   "return LabelledDataUpdateStatus" in new Test{
    //     val json = data.ExampleLabelledData.rates
    //     override val labelledData = LabelledData(Rates, json, lastUpdateTime.toInstant(), credId, user, email)
    //     override val expected: RequestOutcome[LabelledData] = Right(labelledData)

    //     val expectedStatus = LabelledDataUpdateStatus(1, Some(UpdateDetails(lastUpdateTime, credId, user, email, Nil)))

    //     MockLabelledDataRepository
    //       .get(Rates)
    //       .returns(Future.successful(expected))

    //     MockLabelledDataRepository
    //       .save(Rates, json, lastUpdateInstant, credId, user, email)
    //       .returns(Future.successful(expected))

    //     whenReady(target.save(json, credId, user, email, Nil)) {
    //       case Right(response) =>
    //         if (response == expectedStatus) succeed
    //       case _ => fail()
    //     }
    //   }
    // }

  //   "the JSON is valid but update contains deletions of inuse timescales" should {
  //     "return TimescalesResponse" in new Test{
  //       val timescalesWithRetainedDefn: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3, "Fourth": 4}""")

  //       MockTimescalesRepository
  //         .get(mockTimescalesRepository.CurrentTimescalesID)
  //         .returns(Future.successful(expected))

  //       MockTimescalesRepository
  //         .save(timescalesWithRetainedDefn, lastUpdateTime, credId, user, email)
  //         .returns(Future.successful(expected))

  //       MockPublishedService
  //         .getTimescalesInUse()
  //         .returns(Future.successful(Right(List("First"))))

  //       MockApprovalReviewService
  //         .getTimescalesInUse()
  //         .returns(Future.successful(Right(List("First"))))

  //       whenReady(target.save(timescalesJsonWithDeletion, credId, user, email, List("First"))) {
  //         case Right(response) if response.lastUpdate.map(_.retainedDeletions).contains(List("First")) => succeed
  //         case Right(response) => fail()
  //         case Left(_) => fail()
  //       }
  //     }
  //   }

  //   "the JSON is valid but published service call fails" should {
  //     "return TimescalesResponse" in new Test{

  //       MockTimescalesRepository
  //         .get(mockTimescalesRepository.CurrentTimescalesID)
  //         .returns(Future.successful(Left(DatabaseError)))

  //       MockTimescalesRepository
  //         .save(timescalesJson, lastUpdateTime, credId, user, email)
  //         .returns(Future.successful(Left(DatabaseError)))

  //       whenReady(target.save(timescalesJsonWithDeletion, credId, user, email, Nil)) {
  //         case Right(response) => fail()
  //         case Left(_) => succeed
  //       }
  //     }
  //   }

  //   "the timescales JSON is not valid" should {
  //     "return Validation error" in new Test {
  //       val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")

  //       whenReady(target.save(invalidTs, credId, user, email, Nil)) {
  //         case Right(_) => fail()
  //         case Left(ValidationError) => succeed
  //         case err => fail()
  //       }
  //     }
  //   }

  //   "the JSON is invalid" should {
  //     "not call the scratch repository" in new Test {
  //       MockTimescalesRepository.save(Json.parse("""{"Hello": "World"}"""), lastUpdateTime, credId, user, email).never()

  //       target.save(Json.parse("""{"Hello": "World"}"""), credId, user, email, Nil)
  //     }
  //   }

  //   "a database error occurs" should {
  //     "return a internal error" in new Test {
  //       MockTimescalesRepository
  //         .get(mockTimescalesRepository.CurrentTimescalesID)
  //         .returns(Future.successful(expected))

  //       MockPublishedService
  //         .getTimescalesInUse()
  //         .returns(Future.successful(Right(Nil)))

  //       MockTimescalesRepository
  //         .save(timescalesJson, lastUpdateTime, credId, user, email)
  //         .returns(Future.successful(Left(DatabaseError)))

  //       whenReady(target.save(timescalesJson, credId, user, email, Nil)) {
  //         case result @ Left(_) => result shouldBe Left(InternalServerError)
  //         case _ => fail()
  //       }
  //     }
  //   }
  }

  // "Calling get method" should {

  //   "return the timescales" in new Test {
  //     MockTimescalesRepository
  //       .get("1")
  //       .returns(Future.successful(Right(timescalesUpdate)))

  //     whenReady(target.get()) { result =>
  //       result shouldBe Right(timeScalesWithVersion)
  //     }
  //   }

  //   "return Seed defnitions if no DB data found" in new Test {
  //     MockTimescalesRepository
  //       .get("1")
  //       .returns(Future.successful(Left(NotFoundError)))

  //     whenReady(target.get()) { result =>
  //       result shouldBe Right(timeScalesWithZeroVersion)
  //     }
  //   }

  //   "return an internal error when a database error occurs" in new Test {
  //     MockTimescalesRepository
  //       .get("1")
  //       .returns(Future.successful(Left(DatabaseError)))

  //     whenReady(target.get()) { result =>
  //       result shouldBe Left(InternalServerError)
  //     }
  //   }
  // }

  // "Calling details method" should {
  //   "Return complete details if timescales exist" in new Test {
  //     MockTimescalesRepository
  //       .get("1")
  //       .returns(Future.successful(Right(timescalesUpdate)))

  //     whenReady(target.details()) { result =>
  //       result shouldBe Right(timescalesResponse)
  //     }
  //   }

  //   "Return details with no datetime if only seed timescales exist" in new Test {
  //     MockTimescalesRepository
  //       .get("1")
  //       .returns(Future.successful(Left(NotFoundError)))

  //     whenReady(target.details()) { result =>
  //       result shouldBe Right(TimescalesResponse(timescales.size, None))
  //     }

  //   }

  //   "return an internal error when a database error occurs" in new Test {
  //     MockTimescalesRepository
  //       .get("1")
  //       .returns(Future.successful(Left(DatabaseError)))

  //     whenReady(target.details()) { result =>
  //       result shouldBe Left(InternalServerError)
  //     }
  //   }

  // }

  // "Calling updateProcessTimescaleTable method" should {

  //   "Update table using mongo timescale defns" in new Test {
  //     MockTimescalesRepository
  //       .get("1")
  //       .returns(Future.successful(Right(timescalesUpdate)))

  //     whenReady(target.updateProcessTimescaleTableAndDetails(jsonWithBlankTsTable)) { result =>
  //       result match {
  //         case Right(json) =>
  //           val process = json.as[Process]
  //           process.meta.timescalesVersion shouldBe Some(timescalesUpdate.when.toInstant().toEpochMilli())
  //           process.timescales shouldBe timescales
  //         case _ => fail()
  //       }
  //     }
  //   }

  //   "Update table using mongo timescale defns where json contains no timescale table" in new Test {
  //     whenReady(target.updateProcessTimescaleTableAndDetails(jsonWithNoTsTable)) { result =>
  //       result match {
  //         case Right(json) => (json.as[Process]).timescales shouldBe Map()
  //         case _ => fail()
  //       }
  //     }
  //   }

  //   "Update table using mongo timescale defns where json is not a valid Process" in new Test {
  //     val update = Json.parse("{}").as[JsObject]
  //     whenReady(target.updateProcessTimescaleTableAndDetails(update)) { result =>
  //       result match {
  //         case Right(_) => fail()
  //         case Left(err) => err shouldBe ValidationError
  //       }
  //     }
  //   }

  //   "Update table using seed timescale defns when no DB data found" in new Test {
  //     MockTimescalesRepository
  //       .get("1")
  //       .returns(Future.successful(Left(NotFoundError)))

  //     whenReady(target.updateProcessTimescaleTableAndDetails(jsonWithBlankTsTable)) { result =>
  //       result match {
  //         case Right(json) => (json.as[Process]).timescales shouldBe timescales
  //         case _ => fail()
  //       }
  //     }
  //   }


  //   "return an internal error if a database error occurs" in new Test {
  //     MockTimescalesRepository
  //       .get("1")
  //       .returns(Future.successful(Left(DatabaseError)))

  //     whenReady(target.updateProcessTimescaleTableAndDetails(jsonWithBlankTsTable)) { result =>
  //       result shouldBe Left(InternalServerError)
  //     }
  //   }
  // }

}
