/*
 * Copyright 2020 HM Revenue & Customs
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

import data.ReviewData
import mocks.MockReviewService
import models.errors._
import models.{ApprovalProcessPageReview, ProcessReview}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.Constants._

import scala.concurrent.Future

class ProcessReviewControllerSpec extends WordSpec with Matchers with ScalaFutures with GuiceOneAppPerSuite with MockFactory with ReviewData {

  private trait Test extends MockReviewService {
    val invalidId: String = "ext95"
    val reviewUpdate: ApprovalProcessPageReview = ApprovalProcessPageReview("id", "/pageUrl", None, "status")
    lazy val controller: ProcessReviewController = new ProcessReviewController(mockReviewService, stubControllerComponents())
  }
  "Calling the approval2iReviewInfo action" when {

    "the request is valid" should {

      trait ValidTest extends Test {
        MockReviewService
          .approval2iReviewInfo(validProcessIdForReview)
          .returns(Future.successful(Right(processReviewInfo)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an OK response" in new ValidTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "confirm returned content is a JSON object" in new ValidTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        val dataReturned: ProcessReview = contentAsJson(result).as[ProcessReview]
        dataReturned.ocelotId shouldBe validProcessIdForReview
      }
    }

    "the request is invalid" should {

      trait InvalidTest extends Test {
        val expectedErrorCode = "BAD_REQUEST_ERROR"
        MockReviewService
          .approval2iReviewInfo(invalidId)
          .returns(Future.successful(Left(Errors(BadRequestError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a bad request response" in new InvalidTest {
        private val result = controller.approval2iReviewInfo(invalidId)(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidTest {
        private val result = controller.approval2iReviewInfo(invalidId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of BAD_REQUEST" in new InvalidTest {
        private val result = controller.approval2iReviewInfo(invalidId)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the request contains an unknown ID" should {

      trait NotFoundTest extends Test {
        val expectedErrorCode = "NOT_FOUND_ERROR"
        MockReviewService
          .approval2iReviewInfo(validProcessIdForReview)
          .returns(Future.successful(Left(Errors(NotFoundError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new NotFoundTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND_ERROR" in new NotFoundTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the requested process is no longer SubmittedFor2iReview" should {

      trait StaleDataTest extends Test {
        val expectedErrorCode = "STALE_DATA_ERROR"
        MockReviewService
          .approval2iReviewInfo(validProcessIdForReview)
          .returns(Future.successful(Left(Errors(StaleDataError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new StaleDataTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new StaleDataTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND_ERROR" in new StaleDataTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .approval2iReviewInfo(validProcessIdForReview)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a internal server error response" in new ErrorTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the approval2iReviewComplete action" when {

    "the request is valid" should {

      trait ValidTest extends Test {
        MockReviewService
          .changeStatus(validProcessIdForReview, StatusSubmittedFor2iReview, statusChangeInfo)
          .returns(Future.successful(Right(())))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return a NO_CONTENT response" in new ValidTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        status(result) shouldBe NO_CONTENT
      }

      "return no content" in new ValidTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        contentType(result) shouldBe None
      }

    }

    "the request is invalid" should {

      trait InvalidTest extends Test {
        MockReviewService
          .changeStatus(validProcessIdForReview, StatusSubmittedFor2iReview, statusChangeInfo)
          .returns(Future.successful(Right(())))
          .never()

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidStatusChangeJson)
      }

      "return a bad request response" in new InvalidTest {
        private val result = controller.approval2iReviewComplete(invalidId)(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidTest {
        private val result = controller.approval2iReviewComplete(invalidId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

    }

    "the request contains an unknown ID" should {

      trait NotFoundTest extends Test {
        val expectedErrorCode = "NOT_FOUND_ERROR"
        MockReviewService
          .changeStatus(validProcessIdForReview, StatusSubmittedFor2iReview, statusChangeInfo)
          .returns(Future.successful(Left(Errors(NotFoundError))))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return an not found response" in new NotFoundTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND_ERROR" in new NotFoundTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .changeStatus(validProcessIdForReview, StatusSubmittedFor2iReview, statusChangeInfo)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return a internal server error response" in new ErrorTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the approval2iReviewPageInfo action" when {

    "the request is valid" should {

      trait ValidTest extends Test {

        val pageUrl: String = "/pageUrl"
        val pageReview: ApprovalProcessPageReview = ApprovalProcessPageReview("id", pageUrl, None, "status")

        MockReviewService
          .approval2iReviewPageInfo(validProcessIdForReview, pageUrl)
          .returns(Future.successful(Right(pageReview)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")

      }

      "return an OK response" in new ValidTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "confirm returned content is a JSON object" in new ValidTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        val dataReturned: ApprovalProcessPageReview = contentAsJson(result).as[ApprovalProcessPageReview]
        dataReturned shouldBe pageReview
      }
    }

    "the request is invalid" should {

      trait InvalidTest extends Test {
        val expectedErrorCode = "BAD_REQUEST_ERROR"
        val pageUrl: String = "/pageUrl"
        MockReviewService
          .approval2iReviewPageInfo(validProcessIdForReview, pageUrl)
          .returns(Future.successful(Left(Errors(BadRequestError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a bad request response" in new InvalidTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of BAD_REQUEST" in new InvalidTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the request contains an unknown ID" should {

      trait NotFoundTest extends Test {
        val expectedErrorCode = "NOT_FOUND_ERROR"
        val pageUrl: String = "/pageUrl"
        MockReviewService
          .approval2iReviewPageInfo(validProcessIdForReview, pageUrl)
          .returns(Future.successful(Left(Errors(NotFoundError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new NotFoundTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND_ERROR" in new NotFoundTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the requested process is no longer SubmittedFor2iReview" should {

      trait StaleDataTest extends Test {
        val expectedErrorCode = "STALE_DATA_ERROR"
        val pageUrl: String = "/pageUrl"
        MockReviewService
          .approval2iReviewPageInfo(validProcessIdForReview, pageUrl)
          .returns(Future.successful(Left(Errors(StaleDataError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new StaleDataTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new StaleDataTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND_ERROR" in new StaleDataTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val pageUrl: String = "/pageUrl"
        MockReviewService
          .approval2iReviewPageInfo(validProcessIdForReview, pageUrl)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a internal server error response" in new ErrorTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, pageUrl)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the approval2iReviewPageComplete action" when {

    "the request is valid" should {

      trait ValidTest extends Test {

        MockReviewService
          .approval2iReviewPageComplete("id", "/pageUrl", reviewUpdate)
          .returns(Future.successful(Right(())))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(reviewUpdate))
      }

      "return a NO_CONTENT response" in new ValidTest {
        private val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        status(result) shouldBe NO_CONTENT
      }

      "return no content" in new ValidTest {
        private val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        contentType(result) shouldBe None
      }

    }

    "the request is invalid" should {

      trait InvalidTest extends Test {
        MockReviewService
          .approval2iReviewPageComplete("id", "/pageUrl", reviewUpdate)
          .never()

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.obj())
      }

      "return a bad request response" in new InvalidTest {
        private val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidTest {
        private val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

    }

    "the request contains an unknown ID" should {

      trait NotFoundTest extends Test {
        val expectedErrorCode = "NOT_FOUND_ERROR"
        MockReviewService
          .approval2iReviewPageComplete("id", "/pageUrl", reviewUpdate)
          .returns(Future.successful(Left(Errors(NotFoundError))))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(reviewUpdate))
      }

      "return an not found response" in new NotFoundTest {
        val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundTest {
        val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND_ERROR" in new NotFoundTest {
        val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .approval2iReviewPageComplete("id", "/pageUrl", reviewUpdate)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(reviewUpdate))
      }

      "return a internal server error response" in new ErrorTest {
        val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorTest {
        val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorTest {
        val result = controller.approval2iReviewPageComplete(reviewUpdate.id, reviewUpdate.pageUrl)(request)
        val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

}
