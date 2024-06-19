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
import java.time.LocalDate
import mocks.MockLabelledDataRepository
import core.services.{TodayProvider, DefaultTodayProvider}
import core.models.errors._
import core.models.ocelot.Process
import models.{LabelledData, Rates, LabelledDataUpdateStatus}
import core.models.RequestOutcome
import play.api.libs.json.{JsValue, Json}
import mocks.MockAppConfig
import models.UpdateDetails
import scala.concurrent.Future
import data.RatesTestData
import java.time.ZonedDateTime

class RatesServiceSpec extends BaseSpec with RatesTestData {

  private trait Test extends MockLabelledDataRepository {
    val today: LocalDate = LocalDate.of(2020, 6, 24)
    val earlyYearToday: LocalDate = LocalDate.of(2018, 2, 12)
    val taxStartForNow = LocalDate.of(2020, 4, 6)
    val taxYearForNow = taxStartForNow.getYear
    val earlyTodayProvider: TodayProvider = new TodayProvider{
                              def now = earlyYearToday
                              def year: String = now.getYear().toString
                            }

    lazy val target: RatesService = new RatesService(mockLabelledDataRepository, new core.services.Rates(), new DefaultTodayProvider, MockAppConfig)
    lazy val earlyRatesService: RatesService = new RatesService(mockLabelledDataRepository, new core.services.Rates(), earlyTodayProvider, MockAppConfig)

    val seedRates =  target.seedRates().getOrElse(fail())
    val seedRatesTwoDimMap = target.twoDimMapFromFour(seedRates)
    val seedRatesJson = target.seedRatesAsJson().getOrElse(fail())

    val labelledSeedData = LabelledData(Rates, seedRatesJson, lastUpdateTime.toInstant(), credId, user, email)
    val ratesWithVersion = (ratesTwoDimMap, lastUpdateInstant.toEpochMilli)
    val nativeRatesWithVersion = (ratesFourDimMap, lastUpdateInstant.toEpochMilli)
    val ratesWithZeroVersion = (seedRatesTwoDimMap, 0L)
    val nativeRatesWithZeroVersion = (seedRates, 0L)

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
        val labelledData = LabelledData(Rates, ratesJson, lastUpdateTime.toInstant(), credId, user, email)
        val expected: RequestOutcome[LabelledData] = Right(labelledData)

        val expectedStatus = LabelledDataUpdateStatus(1, Some(UpdateDetails(lastUpdateTime, credId, user, email, Nil)))

        MockLabelledDataRepository
          .get(Rates)
          .returns(Future.successful(expected))

        MockLabelledDataRepository
          .save(Rates, ratesJson, lastUpdateInstant, credId, user, email)
          .returns(Future.successful(expected))

        whenReady(target.save(ratesJson, credId, user, email, Nil)) {
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

  "Calling getNative method" should {

    "return the rates" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Right(labelledData)))

      whenReady(target.getNative()) { result =>
        result shouldBe Right(nativeRatesWithVersion)
      }
    }

    "return Seed defnitions if no DB data found" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.getNative()) { result =>
        result shouldBe Right(nativeRatesWithZeroVersion)
      }
    }

    "return an internal error when a database error occurs" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.getNative()) { result =>
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

    "return an internal error when a json returned from db is invalid" in new Test {
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Right(LabelledData(Rates, Json.parse("""{"Hello": "World"}"""), ZonedDateTime.now.toInstant(), "","",""))))

      whenReady(target.details()) { result =>
        result shouldBe Left(InternalServerError)
      }
    }
  }

  "Calling updateProcessTable method" should {

    "Update table using mongo rates defns" in new Test {
      val process: Process = jsonWithBlankRatesTable.as[Process]

      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Right(labelledData)))

      whenReady(target.updateProcessTable(jsonWithBlankRatesTable, process)) { result =>
        result match {
          case Right((json, p)) =>
            p.meta.ratesVersion shouldBe Some(labelledData.when.toEpochMilli())
            p.rates shouldBe rates
          case _ => fail()
        }
      }
    }

    "Update table using mongo rates defns where json contains no rates table" in new Test {
      val process: Process = jsonWithNoRatesTable.as[Process]

      whenReady(target.updateProcessTable(jsonWithNoRatesTable, process)) { result =>
        result match {
          case Right((json, p)) => p.rates shouldBe Map()
          case _ => fail()
        }
      }
    }

    "Update table using seed rates defns when no DB data found" in new Test {
      val process: Process = jsonWithBlankRatesTable.as[Process]
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.updateProcessTable(jsonWithBlankRatesTable, process)) { result =>
        result match {
          case Right((json, p)) => p.rates shouldBe rates
          case _ => fail()
        }
      }
    }


    "return an internal error if a database error occurs" in new Test {
      val process: Process = jsonWithBlankRatesTable.as[Process]
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.updateProcessTable(jsonWithBlankRatesTable, process)) { result =>
        result shouldBe Left(InternalServerError)
      }
    }

    "return an internal error when rate table entry cannot be resolved" in new Test {
    // |      "Legacy!higherrate!2016" : 0,
    // |      "Legacy!basicrate!2022" : 0,
    // |      "TaxNic!CTC!2016" : 0

      val process: Process = jsonWithBlankRatesTable.as[Process]
      val updatedProcess = process.copy(rates = rates + ("TaxNic!CND" -> BigDecimal(0)))
      MockLabelledDataRepository
        .get(Rates)
        .returns(Future.successful(Right(labelledData)))

      whenReady(target.updateProcessTable(jsonWithBlankRatesTable, updatedProcess)) { result =>
        result shouldBe Left(InternalServerError)
      }
    }

  }

  "Calling finaliseIds method" should {
    "Return empty string if passed empty string" in new Test {
      target.finaliseIds(Nil) shouldBe Nil
    }

    "Return ids unchanged if they terminate in a 4 digit value (year)" in new Test {
      target.finaliseIds(List("Legacy!higherrate!2024", "Legacy!basicrate!2020")) shouldBe List("Legacy!higherrate!2024", "Legacy!basicrate!2020")
    }

    "Replace shortIds (do not terminate in a 4 digit value (year)) with fully qualified ids" in new Test {
      earlyRatesService.finaliseIds(List("Legacy!higherrate", "Legacy!basicrate!2020")) shouldBe List("Legacy!higherrate!2018", "Legacy!basicrate!2020")
    }

  }
}
