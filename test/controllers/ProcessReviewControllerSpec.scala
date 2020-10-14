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

import controllers.actions.{FakeFactCheckerIdentifierAction, FakeTwoEyeReviewerIdentifierAction}
import data.ReviewData
import mocks.MockReviewService
import models._
import models.errors._
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.Constants._

import scala.concurrent.Future

class ProcessReviewControllerSpec extends WordSpec
  with Matchers with ScalaFutures with GuiceOneAppPerSuite with MockFactory with ReviewData with ApprovalProcessJson {

  private trait Test extends MockReviewService {
    val invalidId: String = "ext95"
    val approvalProcessCompleted: ApprovalProcess = approvalProcess.copy(process = createProcess)
    val auditInfo = AuditInfo("ID",
                              approvalProcessCompleted.id,
                              approvalProcessCompleted.meta.title,
                              approvalProcessCompleted.version,
                              "author", 111111, approvalProcessCompleted.version)
    val approvalProcessContainingInvalidOcelotProcess: ApprovalProcess = approvalProcess.copy()
    val reviewUpdate: ApprovalProcessPageReview = ApprovalProcessPageReview("id", "/pageUrl", "Title", None, "status")

    lazy val controller: ProcessReviewController = new ProcessReviewController(
      FakeFactCheckerIdentifierAction,
      FakeTwoEyeReviewerIdentifierAction,
      mockReviewService,
      stubControllerComponents())
  }
  "Calling the approval2iReviewInfo action" when {

    "the request is valid" should {

      trait ValidTest extends Test {
        MockReviewService
          .approvalReviewInfo(validProcessIdForReview, ReviewType2i)
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
        val expectedErrorCode = "BAD_REQUEST"
        MockReviewService
          .approvalReviewInfo(invalidId, ReviewType2i)
          .returns(Future.successful(Left(BadRequestError)))

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
        val expectedErrorCode = "NOT_FOUND"
        MockReviewService
          .approvalReviewInfo(validProcessIdForReview, ReviewType2i)
          .returns(Future.successful(Left(NotFoundError)))

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

      "return a error code of NOT_FOUND" in new NotFoundTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the requested process is no longer SubmittedFor2iReview" should {

      trait StaleDataTest extends Test {
        val expectedErrorCode = "STALE_DATA_ERROR"
        MockReviewService
          .approvalReviewInfo(validProcessIdForReview, ReviewType2i)
          .returns(Future.successful(Left(StaleDataError)))

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

      "return a error code of NOT_FOUND" in new StaleDataTest {
        private val result = controller.approval2iReviewInfo(validProcessIdForReview)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .approvalReviewInfo(validProcessIdForReview, ReviewType2i)
          .returns(Future.successful(Left(InternalServerError)))

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

      "return an OK response" in new Test {

        MockReviewService
          .twoEyeReviewComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Right(auditInfo)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)

        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/json")
      }

    }

    "the request is invalid" should {

      trait InvalidTest extends Test {
        MockReviewService
          .twoEyeReviewComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Right(auditInfo)))
          .never()

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidStatusChangeJson)
      }

      "return a bad request response" in new InvalidTest {
        private val result = controller.approval2iReviewComplete(invalidId)(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

    }

    "the request contains an unknown ID" should {

      trait NotFoundTest extends Test {
        val expectedErrorCode = "NOT_FOUND"
        MockReviewService
          .twoEyeReviewComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(NotFoundError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return an not found error response" in new NotFoundTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the request contains a duplicate process code" should {

      trait DuplicateKeyTest extends Test {
        val expectedErrorCode = "DUPLICATE_KEY_ERROR"
        MockReviewService
          .twoEyeReviewComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(DuplicateKeyError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return a duplicate key error response" in new DuplicateKeyTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a stale data error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "STALE_DATA_ERROR"
        MockReviewService
          .twoEyeReviewComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(StaleDataError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return a stale data error response" in new ErrorTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .twoEyeReviewComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(InternalServerError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return a internal server error response" in new ErrorTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
    "a bad request error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "BAD_REQUEST"
        MockReviewService
          .twoEyeReviewComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(BadRequestError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
      }

      "return a bad request error response" in new ErrorTest {
        private val result = controller.approval2iReviewComplete(validProcessIdForReview)(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the approval2iReviewConfirmAllPagesReviewed action" when {

    "the request is valid" should {

      trait ValidTest extends Test {
        val approvalProcess: ApprovalProcess =
          ApprovalProcess(validProcessIdForReview, ApprovalProcessMeta(validProcessIdForReview, "title", processCode = "processCode"), Json.obj())

        MockReviewService
          .checkProcessInCorrectStateForCompletion(validProcessIdForReview, ReviewType2i)
          .returns(Future.successful(Right(approvalProcess)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a NO_CONTENT response" in new ValidTest {
        private val result = controller.approval2iReviewConfirmAllPagesReviewed(validProcessIdForReview)(request)
        status(result) shouldBe NO_CONTENT
      }

      "return no content" in new ValidTest {
        private val result = controller.approval2iReviewConfirmAllPagesReviewed(validProcessIdForReview)(request)
        contentType(result) shouldBe None
      }

    }

    "the requested process has pages that have not been reviewed" should {

      trait NotFoundTest extends Test {

        MockReviewService
          .checkProcessInCorrectStateForCompletion(validProcessIdForReview, ReviewType2i)
          .returns(Future.successful(Left(IncompleteDataError)))

        val expectedErrorCode = "INCOMPLETE_DATA_ERROR"

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new NotFoundTest {
        private val result = controller.approval2iReviewConfirmAllPagesReviewed(validProcessIdForReview)(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }

    }

    "the request contains an unknown ID" should {

      trait NotFoundTest extends Test {

        MockReviewService
          .checkProcessInCorrectStateForCompletion(validProcessIdForReview, ReviewType2i)
          .returns(Future.successful(Left(NotFoundError)))

        val expectedErrorCode = "NOT_FOUND"

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new NotFoundTest {
        private val result = controller.approval2iReviewConfirmAllPagesReviewed(validProcessIdForReview)(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }

    }

    "the process requested is no longer in a status to be completed" should {

      trait NotFoundTest extends Test {

        MockReviewService
          .checkProcessInCorrectStateForCompletion(validProcessIdForReview, ReviewType2i)
          .returns(Future.successful(Left(StaleDataError)))

        val expectedErrorCode = "STALE_DATA_ERROR"

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new NotFoundTest {
        private val result = controller.approval2iReviewConfirmAllPagesReviewed(validProcessIdForReview)(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }

    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .checkProcessInCorrectStateForCompletion(validProcessIdForReview, ReviewType2i)
          .returns(Future.successful(Left(InternalServerError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a internal server error response" in new ErrorTest {
        private val result = controller.approval2iReviewConfirmAllPagesReviewed(validProcessIdForReview)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorTest {
        private val result = controller.approval2iReviewConfirmAllPagesReviewed(validProcessIdForReview)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorTest {
        private val result = controller.approval2iReviewConfirmAllPagesReviewed(validProcessIdForReview)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the approval2iReviewPageInfo action" when {

    "the request is valid" should {

      trait ValidTest extends Test {

        val pageUrl: String = "/pageUrl"
        val pageReview: ApprovalProcessPageReview = ApprovalProcessPageReview("id", pageUrl, "Title", None, "status")

        MockReviewService
          .approvalPageInfo(validProcessIdForReview, pageUrl, ReviewType2i)
          .returns(Future.successful(Right(pageReview)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")

      }

      "return an OK response" in new ValidTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, "pageUrl")(request)
        status(result) shouldBe OK
        contentType(result) shouldBe Some(ContentTypes.JSON)
        val dataReturned: ApprovalProcessPageReview = contentAsJson(result).as[ApprovalProcessPageReview]
        dataReturned shouldBe pageReview
      }
    }

    "the request is invalid" should {

      trait InvalidTest extends Test {
        val expectedErrorCode = "BAD_REQUEST"
        val pageUrl: String = "/pageUrl"
        MockReviewService
          .approvalPageInfo(validProcessIdForReview, pageUrl, ReviewType2i)
          .returns(Future.successful(Left(BadRequestError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a bad request response" in new InvalidTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, "pageUrl")(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the request contains an unknown ID" should {

      trait NotFoundTest extends Test {
        val expectedErrorCode = "NOT_FOUND"
        val pageUrl: String = "/pageUrl"
        MockReviewService
          .approvalPageInfo(validProcessIdForReview, pageUrl, ReviewType2i)
          .returns(Future.successful(Left(NotFoundError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new NotFoundTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, "pageUrl")(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the requested process is no longer SubmittedFor2iReview" should {

      trait StaleDataTest extends Test {
        val expectedErrorCode = "STALE_DATA_ERROR"
        val pageUrl: String = "/pageUrl"
        MockReviewService
          .approvalPageInfo(validProcessIdForReview, pageUrl, ReviewType2i)
          .returns(Future.successful(Left(StaleDataError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new StaleDataTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, "pageUrl")(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val pageUrl: String = "/pageUrl"
        MockReviewService
          .approvalPageInfo(validProcessIdForReview, pageUrl, ReviewType2i)
          .returns(Future.successful(Left(InternalServerError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a internal server error response" in new ErrorTest {
        private val result = controller.approval2iReviewPageInfo(validProcessIdForReview, "pageUrl")(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }

    }
  }

  "Calling the approval2iReviewPageComplete action" when {

    "the request is valid" should {

      trait ValidTest extends Test {

        MockReviewService
          .approvalPageComplete("id", "/pageUrl", ReviewType2i, reviewUpdate)
          .returns(Future.successful(Right(())))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(reviewUpdate))
      }

      "return a NO_CONTENT response" in new ValidTest {
        private val result = controller.approval2iReviewPageComplete(reviewUpdate.id, "pageUrl")(request)
        status(result) shouldBe NO_CONTENT
      }

      "return no content" in new ValidTest {
        private val result = controller.approval2iReviewPageComplete(reviewUpdate.id, "pageUrl")(request)
        contentType(result) shouldBe None
      }

    }

    "the request is invalid" should {

      trait InvalidTest extends Test {
        MockReviewService
          .approvalPageComplete("id", "/pageUrl", ReviewType2i, reviewUpdate)
          .never()

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.obj())
      }

      "return a bad request response" in new InvalidTest {
        private val result = controller.approval2iReviewPageComplete(reviewUpdate.id, "pageUrl")(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidTest {
        private val result = controller.approval2iReviewPageComplete(reviewUpdate.id, "pageUrl")(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

    }

    "the request contains an unknown ID" should {

      trait NotFoundTest extends Test {
        val expectedErrorCode = "NOT_FOUND"
        MockReviewService
          .approvalPageComplete("id", "/pageUrl", ReviewType2i, reviewUpdate)
          .returns(Future.successful(Left(NotFoundError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(reviewUpdate))
        val result: Future[Result] = controller.approval2iReviewPageComplete(reviewUpdate.id, "pageUrl")(request)
      }

      "return an not found response" in new NotFoundTest {
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundTest {
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND" in new NotFoundTest {
        val json: JsObject = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .approvalPageComplete("id", "/pageUrl", ReviewType2i, reviewUpdate)
          .returns(Future.successful(Left(InternalServerError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(reviewUpdate))
      }

      "return a internal server error response" in new ErrorTest {
        val result: Future[Result] = controller.approval2iReviewPageComplete(reviewUpdate.id, "pageUrl")(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorTest {
        val result: Future[Result] = controller.approval2iReviewPageComplete(reviewUpdate.id, "pageUrl")(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorTest {
        val result: Future[Result] = controller.approval2iReviewPageComplete(reviewUpdate.id, "pageUrl")(request)
        val data: JsObject = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the approvalFactCheckInfo action" when {

    "the request is valid" should {

      trait ValidTest extends Test {
        MockReviewService
          .approvalReviewInfo(validProcessIdForReview, ReviewTypeFactCheck)
          .returns(Future.successful(Right(processReviewInfo.copy(reviewType = ReviewTypeFactCheck))))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an OK response" in new ValidTest {
        private val result = controller.approvalFactCheckInfo(validProcessIdForReview)(request)
        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidTest {
        private val result = controller.approvalFactCheckInfo(validProcessIdForReview)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "confirm returned content is a JSON object" in new ValidTest {
        private val result = controller.approvalFactCheckInfo(validProcessIdForReview)(request)
        val dataReturned: ProcessReview = contentAsJson(result).as[ProcessReview]
        dataReturned.ocelotId shouldBe validProcessIdForReview
      }
    }
  }

  "Calling the approvalFactCheckComplete action" when {

    "the request is valid" should {

      "correctly return an OK response" in new Test {

        MockReviewService
          .factCheckComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Right(auditInfo)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
        private val result = controller.approvalFactCheckComplete(validProcessIdForReview)(request)
        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/json")
      }

      "correctly return a BAD_REQUEST if the response contains an invalid process" in new Test {

        MockReviewService
          .factCheckComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(BadRequestError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
        private val result = controller.approvalFactCheckComplete(validProcessIdForReview)(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some("application/json")
      }
    }

    "the request is invalid" should {

      "corectly return a bad request response" in new Test {
        MockReviewService
          .factCheckComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(BadRequestError)))
          .never()

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidStatusChangeJson)
        private val result = controller.approvalFactCheckComplete(invalidId)(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

    }

    "the request contains an unknown ID" should {

      "return an not found response" in new Test {
        val expectedErrorCode = "NOT_FOUND"
        MockReviewService
          .factCheckComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(NotFoundError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
        private val result = controller.approvalFactCheckComplete(validProcessIdForReview)(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a stale data error occurs" should {

      "return a stale data error response" in new Test {
        val expectedErrorCode = "STALE_DATA_ERROR"
        MockReviewService
          .factCheckComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(StaleDataError)))

        val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
        private val result = controller.approvalFactCheckComplete(validProcessIdForReview)(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }


    "a downstream error occurs" should {

      "return a internal server error response" in new Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockReviewService
          .factCheckComplete(validProcessIdForReview, statusChangeInfo)
          .returns(Future.successful(Left(InternalServerError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(statusChangeJson)
        private val result = controller.approvalFactCheckComplete(validProcessIdForReview)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the approvalFactCheckPageInfo action" when {

    "the request is valid" should {

      "return an OK response" in new Test {
        val pageUrl: String = "/pageUrl"
        val pageReview: ApprovalProcessPageReview = ApprovalProcessPageReview("2", pageUrl, "title", Some("result2"))

        MockReviewService
          .approvalPageInfo(validProcessIdForReview, pageUrl, ReviewTypeFactCheck)
          .returns(Future.successful(Right(pageReview)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
        private val result = controller.approvalFactCheckPageInfo(validProcessIdForReview, "pageUrl")(request)
        status(result) shouldBe OK
        contentType(result) shouldBe Some(ContentTypes.JSON)
        val dataReturned: ApprovalProcessPageReview = contentAsJson(result).as[ApprovalProcessPageReview]
        dataReturned shouldBe pageReview
      }
    }

    "the requested process is no longer SubmittedForFactCheck" should {

      "return an not found response" in new Test {
        val expectedErrorCode = "STALE_DATA_ERROR"
        val pageUrl: String = "/pageUrl"
        MockReviewService
          .approvalPageInfo(validProcessIdForReview, pageUrl, ReviewTypeFactCheck)
          .returns(Future.successful(Left(StaleDataError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
        private val result = controller.approvalFactCheckPageInfo(validProcessIdForReview, "pageUrl")(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(ContentTypes.JSON)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

  }

  "Calling the approvalFactCheckPageComplete action" when {

    "the request is valid" should {

      "return a NO_CONTENT response" in new Test {
        MockReviewService
          .approvalPageComplete("id", "/pageUrl", ReviewTypeFactCheck, reviewUpdate)
          .returns(Future.successful(Right(())))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(reviewUpdate))
        private val result = controller.approvalFactCheckPageComplete(reviewUpdate.id, "pageUrl")(request)
        status(result) shouldBe NO_CONTENT
        contentType(result) shouldBe None
      }

    }

  }

  def createProcess: JsObject = {
    Json
      .parse(
        s"""
           |  {
           |    "meta": {
           |      "id": "trn90087",
           |      "title": "External Guidance Automated Test Process",
           |      "ocelot": 3,
           |      "lastAuthor": "7903088",
           |      "lastUpdate": 1589467563758,
           |      "filename": "trn90087.js",
           |      "version": 6
           |    },
           |    "flow": {
           |      "1": {
           |        "type": "CalloutStanza",
           |        "text": 0,
           |        "noteType": "Title",
           |        "next": [
           |          "end"
           |        ],
           |        "stack": false
           |      },
           |      "end": {
           |        "type": "EndStanza"
           |      }
           |    },
           |    "phrases": [
           |    [
           |      "External Guidance Testing process",
           |      "Welsh - External Guidance Testing process"
           |    ]
           |    ],
           |    "contacts": [],
           |    "howto": [],
           |    "links": [
           |    {
           |      "dest": "13",
           |      "title": "Ocelot roles",
           |      "window": false,
           |      "leftbar": false,
           |      "always": false,
           |      "popup": false,
           |      "id": 0
           |    }
           |    ]
           |  }
    """.stripMargin
      )
      .as[JsObject]
  }
}
