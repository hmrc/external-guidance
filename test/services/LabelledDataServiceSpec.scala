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
import mocks.{MockTimescalesService, MockRatesService}
import java.time.LocalDate
import core.models.errors._
import play.api.libs.json.{Json, JsObject, JsValue}
import mocks.MockAppConfig
import scala.concurrent.Future
import core.models.ocelot.{Process, ProcessJson}
import models._

class LabelledDataServiceSpec extends BaseSpec {
  
  private trait Test extends MockTimescalesService with MockRatesService with ProcessJson {

    val today: LocalDate = LocalDate.now
    val timescales: core.services.Timescales = new core.services.Timescales(new core.services.TodayProvider{
                                        def now = today
                                        def year: String = now.getYear().toString
                                     })
    val rates: core.services.Rates = new core.services.Rates()
    lazy val target: LabelledDataService = new LabelledDataService(mockTimescalesService, timescales, mockRatesService, rates, MockAppConfig)
  }

  "Calling updateProcessLabelledDataTablesAndVersions method" when {

    "the JSON is invalid" should {
      "return ValidationError" in new Test{

        whenReady(target.updateProcessLabelledDataTablesAndVersions(Json.parse("""{"Hello": "World"}""").as[JsObject])) {
          case Left(ValidationError) => succeed
          case _ => fail()
        }
      }
    }

    "the JSON is valid but timescales service fails with DatabaseError" should {
      "return DatabaseError" in new Test{
        MockTimescalesService
          .updateProcessTable(validOnePageJson.as[JsObject], validOnePageJson.as[Process])
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.updateProcessLabelledDataTablesAndVersions(validOnePageJson.as[JsObject])) {
          case Left(DatabaseError) => succeed
          case _ => fail()
        }
      }
    }

    "the JSON is valid but rates service fails with DatabaseError" should {
      "return DatabaseError" in new Test{
        MockTimescalesService
          .updateProcessTable(validOnePageJson.as[JsObject], validOnePageJson.as[Process])
          .returns(Future.successful(Right((validOnePageJson.as[JsObject], validOnePageJson.as[Process]))))

        MockRatesService
          .updateProcessTable(validOnePageJson.as[JsObject], validOnePageJson.as[Process])
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.updateProcessLabelledDataTablesAndVersions(validOnePageJson.as[JsObject])) {
          case Left(DatabaseError) => succeed
          case _ => fail()
        }
      }
    }
  }

  "Calling save method" when {
    "called with Timescales arguments" should {
      "Call the correct data provider" in new Test {
        MockTimescalesService
          .save(validOnePageJson.as[JsValue], "12345566", "Someone", "Someone@blah.com", Nil)
          .returns(Future.successful(Right(LabelledDataUpdateStatus(5, None))))
        whenReady(target.save(Timescales, validOnePageJson.as[JsValue], "12345566", "Someone", "Someone@blah.com", Nil)) {
          case Right(LabelledDataUpdateStatus(5, None)) => succeed
          case _ => fail()
        }
      }
    }

    "called with Rates arguments" should {
      "Call the correct data provider" in new Test {
        MockRatesService
          .save(validOnePageJson.as[JsValue], "12345566", "Someone", "Someone@blah.com", Nil)
          .returns(Future.successful(Right(LabelledDataUpdateStatus(5, None))))
        whenReady(target.save(Rates, validOnePageJson.as[JsValue], "12345566", "Someone", "Someone@blah.com", Nil)) {
          case Right(LabelledDataUpdateStatus(5, None)) => succeed
          case _ => fail()
        }
      }
    }
  }

  "Calling details method" when {
    "called with Timescales arguments" should {
      "Call the correct data provider" in new Test {
        MockTimescalesService
          .details()
          .returns(Future.successful(Right(LabelledDataUpdateStatus(5, None))))
        whenReady(target.details(Timescales)) {
          case Right(LabelledDataUpdateStatus(5, None)) => succeed
          case _ => fail()
        }
      }
    }

    "called with Rates arguments" should {
      "Call the correct data provider" in new Test {
        MockRatesService
          .details()
          .returns(Future.successful(Right(LabelledDataUpdateStatus(5, None))))
        whenReady(target.details(Rates)) {
          case Right(LabelledDataUpdateStatus(5, None)) => succeed
          case _ => fail()
        }
      }
    }
  }

  "Calling GET method" when {
    "called with Timescales arguments" should {
      "Call the correct data provider" in new Test {
        val dummyJson = validOnePageJson.as[JsValue]
        MockTimescalesService
          .getNativeAsJson()
          .returns(Future.successful(Right(dummyJson)))
        whenReady(target.get(Timescales)) {
          case Right(dummyJson) => succeed
          case _ => fail()
        }
      }
    }

    "called with Rates arguments" should {
      "Call the correct data provider" in new Test {
        val dummyJson = validOnePageJson.as[JsValue]
        MockRatesService
          .getNativeAsJson()
          .returns(Future.successful(Right(dummyJson)))
        whenReady(target.get(Rates)) {
          case Right(dummyJson) => succeed
          case _ => fail()
        }
      }
    }
  }

}
