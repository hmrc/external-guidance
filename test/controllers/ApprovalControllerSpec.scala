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

import base.ControllerBaseSpec
import mocks._
import controllers.actions.PrivilegedActionProvider
import mocks.MockApprovalService
import models.errors.{BadRequestError, Error, InternalServiceError, NotFoundError, ProcessError, ValidationError}
import models.ocelot.errors.DuplicatePageUrl
import models.{ApprovalProcess, ApprovalProcessJson}
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, ~}
import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolment, Enrolments}
import play.api.http.ContentTypes
import play.api.http.Status.UNPROCESSABLE_ENTITY
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.formatters.ApprovalProcessFormatter
import services.toProcessErr
import utils.Constants._

import scala.concurrent.{ExecutionContext, Future}

class ApprovalControllerSpec extends ControllerBaseSpec with MockApprovalService with MockAuthConnector with ApprovalProcessJson {

  private trait Test extends MockApprovalService {
    val invalidId: String = "ext95"
    val invalidProcess: JsObject = Json.obj("id" -> "ext0093")
    implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

    val actionProvider = new PrivilegedActionProvider(MockAppConfig, bodyParser, mockAuthConnector, config, env)

    lazy val controller: ApprovalController = new ApprovalController(actionProvider, mockApprovalService, stubControllerComponents(), MockAppConfig)
  }

  "Calling the saveFor2iReview action" when {

    "the request is valid" should {

      trait Valid2iSaveTest extends Test {
        val expectedId: String = validId
        MockApprovalService
          .save(validApprovalProcessJson)
          .returns(Future.successful(Right(expectedId)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(validApprovalProcessJson)
      }

      "return a created response" in new Valid2iSaveTest {
        private val result = controller.saveFor2iReview()(request)
        status(result) shouldBe CREATED
      }

      "return content as JSON" in new Valid2iSaveTest {
        private val result = controller.saveFor2iReview()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a String assigned to an attribute labelled id" in new Valid2iSaveTest {
        private val result = controller.saveFor2iReview()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "id").as[String] shouldBe expectedId
      }
    }

    "the request is invalid" should {

      trait InvalidSaveTest extends Test {
        val expectedErrorCode = "BAD_REQUEST"
        MockApprovalService
          .save(invalidProcess)
          .returns(Future.successful(Left(BadRequestError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidProcess)
      }

      "return a bad request response" in new InvalidSaveTest {
        private val result = controller.saveFor2iReview()(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidSaveTest {
        private val result = controller.saveFor2iReview()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of BAD_REQUEST" in new InvalidSaveTest {
        private val result = controller.saveFor2iReview()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the request is invalid with a ValidationError" should {

      trait InvalidSaveTest extends Test {
        val expectedErrorCode = "BAD_REQUEST"
        MockApprovalService
          .save(validApprovalProcessJson)
          .returns(Future.successful(Left(ValidationError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(validApprovalProcessJson)
      }

      "return a bad request response" in new InvalidSaveTest {
        private val result = controller.saveFor2iReview()(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidSaveTest {
        private val result = controller.saveFor2iReview()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of BAD_REQUEST" in new InvalidSaveTest {
        private val result = controller.saveFor2iReview()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the request is invalid with a UnprocessableEntity" should {

      trait InvalidSaveTest extends Test {
        val processError: ProcessError = toProcessErr(DuplicatePageUrl("4", "/feeling-bad"))
        val expectedError = Error(List(processError))
        MockApprovalService
          .save(validApprovalProcessJson)
          .returns(Future.successful(Left(expectedError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(validApprovalProcessJson)
      }

      "return a bad request response" in new InvalidSaveTest {
        private val result = controller.saveFor2iReview()(request)
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      "return content as JSON" in new InvalidSaveTest {
        private val result = controller.saveFor2iReview()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of BAD_REQUEST" in new InvalidSaveTest {
        private val result = controller.saveFor2iReview()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe Error.UnprocessableEntity
      }
    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockApprovalService
          .save(invalidProcess)
          .returns(Future.successful(Left(InternalServiceError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidProcess)
      }

      "return a internal server error response" in new ErrorSaveTest {
        private val result = controller.saveFor2iReview()(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorSaveTest {
        private val result = controller.saveFor2iReview()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorSaveTest {
        private val result = controller.saveFor2iReview()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the saveForFactCheck action" when {

    "the request is valid" should {

      trait ValidFactCheckSaveTest extends Test {
        val expectedId: String = validId
        MockApprovalService
          .save(validApprovalProcessJson, ReviewTypeFactCheck)
          .returns(Future.successful(Right(expectedId)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(validApprovalProcessJson)
      }

      "return a created response" in new ValidFactCheckSaveTest {
        private val result = controller.saveForFactCheck()(request)
        status(result) shouldBe CREATED
      }

      "return content as JSON" in new ValidFactCheckSaveTest {
        private val result = controller.saveForFactCheck()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a String assigned to an attribute labelled id" in new ValidFactCheckSaveTest {
        private val result = controller.saveForFactCheck()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "id").as[String] shouldBe expectedId
      }
    }

    "the request is invalid" should {

      trait InvalidSaveTest extends Test {
        val expectedErrorCode = "BAD_REQUEST"
        MockApprovalService
          .save(invalidProcess, ReviewTypeFactCheck)
          .returns(Future.successful(Left(BadRequestError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidProcess)
      }

      "return a bad request response" in new InvalidSaveTest {
        private val result = controller.saveForFactCheck()(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidSaveTest {
        private val result = controller.saveForFactCheck()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of BAD_REQUEST" in new InvalidSaveTest {
        private val result = controller.saveForFactCheck()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockApprovalService
          .save(invalidProcess, ReviewTypeFactCheck)
          .returns(Future.successful(Left(InternalServiceError)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidProcess)
      }

      "return a internal server error response" in new ErrorSaveTest {
        private val result = controller.saveForFactCheck()(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorSaveTest {
        private val result = controller.saveForFactCheck()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorSaveTest {
        private val result = controller.saveForFactCheck()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the get action" when {

    "the request is valid" should {

      trait ValidGetTest extends Test {
        MockApprovalService
          .getById(validId)
          .returns(Future.successful(Right(validApprovalProcessJson)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an OK response" in new ValidGetTest {
        private val result = controller.get(validId)(request)
        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidGetTest {
        private val result = controller.get(validId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "confirm returned content is a JSON object" in new ValidGetTest {
        private val result = controller.get(validId)(request)
        val processReturned: ApprovalProcess = contentAsJson(result).as[ApprovalProcess](ApprovalProcessFormatter.mongoFormat)
        processReturned.id shouldBe approvalProcess.id
      }
    }

    "the request is invalid" should {

      trait InvalidGetTest extends Test {
        val expectedErrorCode = "BAD_REQUEST"
        MockApprovalService
          .getById(invalidId)
          .returns(Future.successful(Left(BadRequestError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an bad request response" in new InvalidGetTest {
        private val result = controller.get(invalidId)(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidGetTest {
        private val result = controller.get(invalidId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of BAD_REQUEST" in new InvalidGetTest {
        private val result = controller.get(invalidId)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the request contains an unknown ID" should {

      trait NotFoundGetTest extends Test {
        val expectedErrorCode = "NOT_FOUND"
        MockApprovalService
          .getById(validId)
          .returns(Future.successful(Left(NotFoundError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new NotFoundGetTest {
        private val result = controller.get(validId)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundGetTest {
        private val result = controller.get(validId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND" in new NotFoundGetTest {
        private val result = controller.get(validId)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorGetTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockApprovalService
          .getById(validId)
          .returns(Future.successful(Left(InternalServiceError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a internal server error response" in new ErrorGetTest {
        private val result = controller.get(validId)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorGetTest {
        private val result = controller.get(validId)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorGetTest {
        private val result = controller.get(validId)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the approvalSummaryList action" when {

    "the request is valid" should {

      trait ValidListTest extends Test {
        MockApprovalService
          .approvalSummaryList(List("FactChecker", "2iReviewer"))
          .returns(Future.successful(Right(Json.toJson(List(approvalProcessSummary)).as[JsArray])))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an OK response" in new ValidListTest {
      val enrolments: Enrolments = Enrolments(Set(Enrolment(key = "FactChecker")))
      val authResult = new ~(new ~(new ~(Some(Credentials("id", "type")), Some(Name(Some("name"), None))), Some("email")), enrolments)

      MockAuthConnector.authorize().returns(Future.successful(authResult))
        private val result = controller.approvalSummaryList()(request)
        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidListTest {
        private val result = controller.approvalSummaryList()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }
    }

    "a downstream error occurs" should {

      trait ErrorGetTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockApprovalService
          .approvalSummaryList(List("FactChecker", "2iReviewer"))
          .returns(Future.successful(Left(InternalServiceError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      }

      "return a internal server error response" in new ErrorGetTest {
        private val result = controller.approvalSummaryList()(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorGetTest {
        private val result = controller.approvalSummaryList()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorGetTest {
        private val result = controller.approvalSummaryList()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }
}
