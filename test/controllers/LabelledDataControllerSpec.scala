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

package controllers

import base.BaseSpec
import core.models.errors.{InternalServerError, ValidationError, DatabaseError, NotFoundError}
import mocks.{MockLabelledDataRepository, MockTimescalesService, MockRatesService, MockLabelledDataService}
import controllers.actions.FakeAllRolesAction
import mocks.{MockApprovalReviewService, MockPublishedService}
import models.{LabelledDataUpdateStatus, LabelledData, UpdateDetails, Timescales, Rates}
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import java.time.{ZoneId, ZonedDateTime}
import scala.concurrent.Future
import data.RatesTestData

class LabelledDataControllerSpec extends BaseSpec {

  trait Test extends MockTimescalesService
             with MockRatesService
             with MockLabelledDataService
             with MockLabelledDataRepository
             with MockPublishedService
             with MockApprovalReviewService {
    lazy val target: LabelledDataController = new LabelledDataController(mockLabelledDataService,
                                                                     mockPublishedService,
                                                                     mockApprovalReviewService,
                                                                     stubControllerComponents(),
                                                                     FakeAllRolesAction)

  }

  "Using Timescales" when {
    trait TimescalesTest extends Test {
      val timescaleJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
      val lastUpdateTime: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, ZoneId.of("UTC"))
      val timescales: Map[String, Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
      val timescalesJsonWithDeletion: JsValue = Json.parse("""{"Second": 2, "Third": 3, "Fourth": 4}""")
      val credId: String = FakeAllRolesAction.credential
      val user: String = FakeAllRolesAction.name
      val email: String = FakeAllRolesAction.email
      val timescalesUpdate = LabelledData(Timescales, timescaleJson, lastUpdateTime.toInstant, credId, user, email)
      val updateDetail = UpdateDetails(lastUpdateTime, credId, user, email, List("First"))
      val labelledDataUpdateStatus = LabelledDataUpdateStatus(timescales.size, Some(updateDetail))
      val labelledDataUpdateStatusWithRetention = LabelledDataUpdateStatus(timescales.size, Some(updateDetail))
    }

    "Calling the save action" when {

      trait ValidSaveTest extends TimescalesTest {
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(timescaleJson)
      }

      "the request is valid" should {

        "return an Accepted response" in new ValidSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          MockLabelledDataService.save(Timescales, timescaleJson, credId, user, email, Nil).returns(Future.successful(Right(labelledDataUpdateStatus)))
          private val result = target.save(Timescales)(request)
          status(result) shouldBe ACCEPTED
        }
      }

      "the request is valid and contains deletions of in use timescales" should {

        "Identify the retained in-use timescales in response" in new TimescalesTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(List("First"))))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          MockLabelledDataService.save(Timescales, timescalesJsonWithDeletion, credId, user, email, List("First")).returns(Future.successful(Right(labelledDataUpdateStatusWithRetention)))
          lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(timescalesJsonWithDeletion)
          val result = target.save(Timescales)(request)
          status(result) shouldBe ACCEPTED
        }
      }

      "the request is valid but the timescales are invalid" should {

        trait InvalidSaveTest extends TimescalesTest {
          val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")
          MockLabelledDataService.save(Timescales, invalidTs, credId, user, email, List("First")).returns(Future.successful(Left(ValidationError)))
          lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidTs)
        }

        "return Bad request response with invalid timescales" in new InvalidSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(List("First"))))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))

          private val result = target.save(Timescales)(request)
          status(result) shouldBe BAD_REQUEST
        }

        "return content as JSON with invalid timescales" in new InvalidSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(List("First"))))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))

          private val result = target.save(Timescales)(request)
          contentType(result) shouldBe Some(ContentTypes.JSON)
        }

        "return an error code of VALIDATION_ERROR with invalid timescales" in new InvalidSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(List("First"))))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          private val result = target.save(Timescales)(request)
          private val data = contentAsJson(result).as[JsObject]
          (data \ "code").as[String] shouldBe "VALIDATION_ERROR"
        }

      }

      "a downstream error occurs" should {

        trait ErrorSaveTest extends TimescalesTest {
          val expectedErrorCode = "INTERNAL_SERVER_ERROR"
          val process: JsObject = Json.obj()
          MockLabelledDataService.save(Timescales, process, credId, user, email, Nil).returns(Future.successful(Left(InternalServerError)))
          lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
        }

        "return a internal server error response" in new ErrorSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))

          private val result = target.save(Timescales)(request)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "return content as JSON" in new ErrorSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          private val result = target.save(Timescales)(request)
          contentType(result) shouldBe Some(ContentTypes.JSON)
        }

        "return an error code of INTERNAL_SERVER_ERROR" in new ErrorSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          private val result = target.save(Timescales)(request)
          private val data = contentAsJson(result).as[JsObject]
          (data \ "code").as[String] shouldBe expectedErrorCode
        }
      }
    }

    "Calling the details action" when {

      "the request is valid" should {

        trait ValidDetailsTest extends TimescalesTest {
          MockLabelledDataService.details(Timescales).returns(Future.successful(Right(labelledDataUpdateStatus)))
          lazy val request: FakeRequest[AnyContent] = FakeRequest()
        }

        "return a no content response" in new ValidDetailsTest {
          private val result = target.details(Timescales)(request)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(labelledDataUpdateStatus)
        }
      }

      "the request is valid but the timescales are invalid" should {

        trait InvalidSaveTest extends TimescalesTest {
          val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")
          MockLabelledDataService.save(Timescales, invalidTs, credId, user, email, Nil).returns(Future.successful(Left(ValidationError)))
          lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidTs)
        }

        "return a unsupportable entity response" in new InvalidSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))

          private val result = target.save(Timescales)(request)
          status(result) shouldBe BAD_REQUEST
        }

        "return content as JSON" in new InvalidSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          private val result = target.save(Timescales)(request)
          contentType(result) shouldBe Some(ContentTypes.JSON)
        }

        "return an error code of BAD_REQUEST" in new InvalidSaveTest {
          MockPublishedService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          MockApprovalReviewService.getDataInUse(Timescales).returns(Future.successful(Right(Nil)))
          private val result = target.save(Timescales)(request)
          private val data = contentAsJson(result).as[JsObject]
          (data \ "code").as[String] shouldBe "VALIDATION_ERROR"
        }

      }

      "a downstream error occurs" should {

        trait ErrorSaveTest extends TimescalesTest {
          val expectedErrorCode = "INTERNAL_SERVER_ERROR"
          val process: JsObject = Json.obj()
          MockLabelledDataService.details(Timescales).returns(Future.successful(Left(InternalServerError)))
          lazy val request: FakeRequest[AnyContent] = FakeRequest()
        }

        "return a internal server error response" in new ErrorSaveTest {
          val result = target.details(Timescales)(request)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "Calling the data retrieval action " when {

      "the request is valid" should {

        trait ValidGetTest extends TimescalesTest {
          val ts: Map[String, Int] = Map("blah" -> 32, "another" -> 56)
          MockLabelledDataService.get(Timescales).returns(Future.successful(Right(Json.toJson(ts))))
          lazy val request: FakeRequest[AnyContent] = FakeRequest()
        }

        "return a valid response" in new ValidGetTest {
          private val result = target.get(Timescales)(request)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(ts)
        }
      }
    }
  }

  trait RatesTest extends Test with RatesTestData {
  }

  "Calling the save action" when {

    trait ValidSaveTest extends RatesTest {
      MockLabelledDataService.save(Rates, ratesJson, credId, user, email, Nil).returns(Future.successful(Right(labelledDataStatus)))
      lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(ratesJson)
    }

    "the request is valid" should {

      "return an Accepted response" in new ValidSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        private val result = target.save(Rates)(request)
        status(result) shouldBe ACCEPTED
      }
    }

    "the request is valid and contains deletions of in use rates" should {

      "Identify the retained in-use rates in response" in new RatesTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(List("Legacy!higherrate!2016"))))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))

        MockLabelledDataService.save(Rates, ratesJsonWithDeletion, credId, user, email, List("Legacy!higherrate!2016")).returns(Future.successful(Right(expectedStatusWithDeletionAndRetained)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(ratesJsonWithDeletion)
        val result = target.save(Rates)(request)
        status(result) shouldBe ACCEPTED
      }
    }

    "the request is valid but the rates are invalid" should {

      trait InvalidSaveTest extends RatesTest {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")
        MockLabelledDataService.save(Rates, invalidTs, credId, user, email, List("Legacy!higherrate!2016")).returns(Future.successful(Left(ValidationError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidTs)
      }

      "return Bad request response with invalid rates" in new InvalidSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(List("Legacy!higherrate!2016"))))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))

        private val result = target.save(Rates)(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON with invalid rates" in new InvalidSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(List("Legacy!higherrate!2016"))))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))

        private val result = target.save(Rates)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of VALIDATION_ERROR with invalid timescales" in new InvalidSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(List("Legacy!higherrate!2016"))))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        private val result = target.save(Rates)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe "VALIDATION_ERROR"
      }

    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends RatesTest {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val process: JsObject = Json.obj()
        MockLabelledDataService.save(Rates, process, credId, user, email, Nil).returns(Future.successful(Left(InternalServerError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return a internal server error response" in new ErrorSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))

        private val result = target.save(Rates)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        private val result = target.save(Rates)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        private val result = target.save(Rates)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "Failures retrieving in-use rates" should {
      trait ErrorSaveTest extends RatesTest {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val process: JsObject = Json.obj()
        MockLabelledDataService.save(Rates, process, credId, user, email, Nil).returns(Future.successful(Left(InternalServerError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return ISE if published in-use rates are unavailable" in new ErrorSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Left(DatabaseError)))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))

        private val result = target.save(Rates)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR

      }

      "return ISE if approvals in-use rates are unavailable" in new ErrorSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Left(DatabaseError)))

        private val result = target.save(Rates)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR

      }
    }
  }

  "Calling the details action" when {

    "the request is valid" should {

      trait ValidDetailsTest extends RatesTest {
        MockLabelledDataService.details(Rates).returns(Future.successful(Right(labelledDataStatus)))
        lazy val request: FakeRequest[AnyContent] = FakeRequest()
      }

      "return a no content response" in new ValidDetailsTest {
        private val result = target.details(Rates)(request)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(labelledDataStatus)
      }
    }

    "the request is valid but the timescales are invalid" should {

      trait InvalidSaveTest extends RatesTest {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")
        MockLabelledDataService.save(Rates, invalidTs, credId, user, email, Nil).returns(Future.successful(Left(ValidationError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidTs)
      }

      "return a unsupportable entity response" in new InvalidSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))

        private val result = target.save(Rates)(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        private val result = target.save(Rates)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of BAD_REQUEST" in new InvalidSaveTest {
        MockPublishedService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getDataInUse(Rates).returns(Future.successful(Right(Nil)))
        private val result = target.save(Rates)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe "VALIDATION_ERROR"
      }

    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends RatesTest {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val process: JsObject = Json.obj()
        MockLabelledDataService.details(Rates).returns(Future.successful(Left(InternalServerError)))
        lazy val request: FakeRequest[AnyContent] = FakeRequest()
      }

      "return a internal server error response" in new ErrorSaveTest {
        val result = target.details(Rates)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "Calling the data retrieval action " when {

    "the request is valid" should {

      trait ValidGetTest extends RatesTest {
        val nativeRates: Map[String, Map[String, Map[String, BigDecimal]]] = Map("blah" -> Map("again" -> Map("2010" -> 2.3)))
        MockLabelledDataService.get(Rates).returns(Future.successful(Right(Json.toJson(nativeRates))))
        lazy val request: FakeRequest[AnyContent] = FakeRequest()
      }

      "return a valid response" in new ValidGetTest {
        private val result = target.get(Rates)(request)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(nativeRates)
      }
    }

    "the request is invalid" should {

      trait InValidGetTest extends RatesTest {
        MockLabelledDataService.get(Rates).returns(Future.successful(Left(NotFoundError)))
        lazy val request: FakeRequest[AnyContent] = FakeRequest()
      }

      "return a valid response" in new InValidGetTest {
        private val result = target.get(Rates)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
