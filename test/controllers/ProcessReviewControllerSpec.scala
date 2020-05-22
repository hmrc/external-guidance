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

import data.ReviewData._
import mocks.MockReviewService
import models.errors._
import models.{ApprovalProcessReview, ReviewProcessJson}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class ProcessReviewControllerSpec extends WordSpec with Matchers with ScalaFutures with GuiceOneAppPerSuite with MockFactory with ReviewProcessJson {

  private trait Test extends MockReviewService {
    val invalidId: String = "ext95"
    lazy val controller: ProcessReviewController = new ProcessReviewController(mockReviewService, stubControllerComponents())
  }
  "Calling the approval2iReviewInfo action" when {

    "the request is valid" should {

      trait ValidTest extends Test {
        MockReviewService
          .approval2iReviewInfo(validId)
          .returns(Future.successful(Right(reviewInfo)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an OK response" in new ValidTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "confirm returned content is a JSON object" in new ValidTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        val dataReturned: ApprovalProcessReview = contentAsJson(result).as[ApprovalProcessReview]
        dataReturned.id shouldBe validId
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
          .approval2iReviewInfo(validId)
          .returns(Future.successful(Left(Errors(NotFoundError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new NotFoundTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND_ERROR" in new NotFoundTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the requested process is no longer SubmittedFor2iReview" should {

      trait StaleDataTest extends Test {
        val expectedErrorCode = "STALE_DATA_ERROR"
        MockReviewService
          .approval2iReviewInfo(validId)
          .returns(Future.successful(Left(Errors(StaleDataError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new StaleDataTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new StaleDataTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND_ERROR" in new StaleDataTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .approval2iReviewInfo(validId)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a internal server error response" in new ErrorTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorTest {
        private val result = controller.approval2iReviewInfo(validId)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the approvalReviewComplete action" when {

    "the request is valid" should {

      trait ValidTest extends Test {
        MockReviewService
          .changeStatus(validId, statusChangeInfo)
          .returns(Future.successful(Right(true)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return a NO_CONTENT response" in new ValidTest {
        private val result = controller.approvalReviewComplete(validId)(request)
        status(result) shouldBe NO_CONTENT
      }

      "return no content" in new ValidTest {
        private val result = controller.approvalReviewComplete(validId)(request)
        contentType(result) shouldBe None
      }

    }

    "the request is invalid" should {

      trait InvalidTest extends Test {
        MockReviewService
          .changeStatus(validId, statusChangeInfo)
          .returns(Future.successful(Right(true)))
          .never()

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidStatusChangeJson)
      }

      "return a bad request response" in new InvalidTest {
        private val result = controller.approvalReviewComplete(invalidId)(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidTest {
        private val result = controller.approvalReviewComplete(invalidId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

    }

    "the request contains an unknown ID" should {

      trait NotFoundTest extends Test {
        val expectedErrorCode = "NOT_FOUND_ERROR"
        MockReviewService
          .changeStatus(validId, statusChangeInfo)
          .returns(Future.successful(Left(Errors(NotFoundError))))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return an not found response" in new NotFoundTest {
        private val result = controller.approvalReviewComplete(validId)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundTest {
        private val result = controller.approvalReviewComplete(validId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND_ERROR" in new NotFoundTest {
        private val result = controller.approvalReviewComplete(validId)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .changeStatus(validId, statusChangeInfo)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return a internal server error response" in new ErrorTest {
        private val result = controller.approvalReviewComplete(validId)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorTest {
        private val result = controller.approvalReviewComplete(validId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorTest {
        private val result = controller.approvalReviewComplete(validId)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }
}
