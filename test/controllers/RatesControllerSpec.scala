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
import mocks.{MockLabelledDataRepository, MockRatesService}
import controllers.actions.FakeAllRolesAction
import mocks.{MockApprovalReviewService, MockPublishedService}
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import data.RatesTestData
import scala.concurrent.Future

class RatesControllerSpec extends BaseSpec with RatesTestData {

  trait Test extends MockRatesService with MockLabelledDataRepository with MockPublishedService with MockApprovalReviewService {
    lazy val target: RatesController = new RatesController(mockRatesService,
                                                           mockPublishedService,
                                                           mockApprovalReviewService,
                                                           stubControllerComponents(),
                                                           FakeAllRolesAction)
  }

  "Calling the save action" when {

    trait ValidSaveTest extends Test {
      MockRatesService.save(ratesJson, credId, user, email, Nil).returns(Future.successful(Right(labelledDataStatus)))
      lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(ratesJson)
    }

    "the request is valid" should {

      "return an Accepted response" in new ValidSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))
        private val result = target.save()(request)
        status(result) shouldBe ACCEPTED
      }
    }

    "the request is valid and contains deletions of in use rates" should {

      "Identify the retained in-use rates in response" in new Test {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(List("Legacy!higherrate!2016"))))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))

        MockRatesService.save(ratesJsonWithDeletion, credId, user, email, List("Legacy!higherrate!2016")).returns(Future.successful(Right(expectedStatusWithDeletionAndRetained)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(ratesJsonWithDeletion)
        val result = target.save()(request)
        status(result) shouldBe ACCEPTED
      }
    }

    "the request is valid but the rates are invalid" should {

      trait InvalidSaveTest extends Test {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")
        MockRatesService.save(invalidTs, credId, user, email, List("Legacy!higherrate!2016")).returns(Future.successful(Left(ValidationError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidTs)
      }

      "return Bad request response with invalid rates" in new InvalidSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(List("Legacy!higherrate!2016"))))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))

        private val result = target.save()(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON with invalid rates" in new InvalidSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(List("Legacy!higherrate!2016"))))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))

        private val result = target.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of VALIDATION_ERROR with invalid timescales" in new InvalidSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(List("Legacy!higherrate!2016"))))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))
        private val result = target.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe "VALIDATION_ERROR"
      }

    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val process: JsObject = Json.obj()
        MockRatesService.save(process, credId, user, email, Nil).returns(Future.successful(Left(InternalServerError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return a internal server error response" in new ErrorSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))

        private val result = target.save()(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))
        private val result = target.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))
        private val result = target.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "Failures retrieving in-use rates" should {
      trait ErrorSaveTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val process: JsObject = Json.obj()
        MockRatesService.save(process, credId, user, email, Nil).returns(Future.successful(Left(InternalServerError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return ISE if published in-use rates are unavailable" in new ErrorSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Left(DatabaseError)))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))

        private val result = target.save()(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR

      }

      "return ISE if approvals in-use rates are unavailable" in new ErrorSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Left(DatabaseError)))

        private val result = target.save()(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR

      }
    }
  }

  "Calling the details action" when {

    "the request is valid" should {

      trait ValidDetailsTest extends Test {
        MockRatesService.details().returns(Future.successful(Right(labelledDataStatus)))
        lazy val request: FakeRequest[AnyContent] = FakeRequest()
      }

      "return a no content response" in new ValidDetailsTest {
        private val result = target.details(request)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(labelledDataStatus)
      }
    }

    "the request is valid but the timescales are invalid" should {

      trait InvalidSaveTest extends Test {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")
        MockRatesService.save(invalidTs, credId, user, email, Nil).returns(Future.successful(Left(ValidationError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidTs)
      }

      "return a unsupportable entity response" in new InvalidSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))

        private val result = target.save()(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))
        private val result = target.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of BAD_REQUEST" in new InvalidSaveTest {
        MockPublishedService.getRatesInUse().returns(Future.successful(Right(Nil)))
        MockApprovalReviewService.getRatesInUse().returns(Future.successful(Right(Nil)))
        private val result = target.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe "VALIDATION_ERROR"
      }

    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val process: JsObject = Json.obj()
        MockRatesService.details().returns(Future.successful(Left(InternalServerError)))
        lazy val request: FakeRequest[AnyContent] = FakeRequest()
      }

      "return a internal server error response" in new ErrorSaveTest {
        val result = target.details()(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "Calling the data retrieval action " when {

    "the request is valid" should {

      trait ValidGetTest extends Test {
        val rates: Map[String, Map[String, Map[String, BigDecimal]]] = Map("blah" -> Map("again" -> Map("2010" -> 2.3)))
        MockRatesService.getNative().returns(Future.successful(Right((rates, 0L))))
        lazy val request: FakeRequest[AnyContent] = FakeRequest()
      }

      "return a valid response" in new ValidGetTest {
        private val result = target.get(request)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson((rates, 0L))
      }
    }

    "the request is invalid" should {

      trait InValidGetTest extends Test {
        MockRatesService.getNative().returns(Future.successful(Left(NotFoundError)))
        lazy val request: FakeRequest[AnyContent] = FakeRequest()
      }

      "return a valid response" in new InValidGetTest {
        private val result = target.get(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

  }
}
