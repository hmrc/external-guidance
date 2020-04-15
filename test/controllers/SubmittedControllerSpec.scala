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

import data.ExamplePayloads
import mocks.MockSubmittedService
import models.errors.{BadRequestError, Errors, InternalServiceError, NotFoundError}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubmittedService

import scala.concurrent.Future

class SubmittedControllerSpec extends WordSpec with Matchers with ScalaFutures with GuiceOneAppPerSuite with MockFactory {

  private trait Test extends MockSubmittedService {
    val validId: String = "ext90005"
    val invalidId: String = "ext95"
    val process: JsObject = ExamplePayloads.simpleValidProcess.as[JsObject]
    val invalidProcess: JsObject = Json.obj("id"-> "ext0093")

    val mockService: SubmittedService = mock[SubmittedService]

    lazy val controller: SubmittedController = new SubmittedController(mockSubmittedService, stubControllerComponents())
  }

  "Calling the save action" when {

    "the request is valid" should {

      trait ValidSaveTest extends Test {
        val expectedId: String = validId
        MockSubmittedService
          .save(process)
          .returns(Future.successful(Right(expectedId)))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return a created response" in new ValidSaveTest {
        private val result = controller.save()(request)
        status(result) shouldBe CREATED
      }

      "return content as JSON" in new ValidSaveTest {
        private val result = controller.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a UUID assigned to an attribute labelled id" in new ValidSaveTest {
        private val result = controller.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "id").as[String] shouldBe expectedId
      }
    }

    "the request is invalid" should {

      trait InvalidSaveTest extends Test {
        val expectedErrorCode = "BAD_REQUEST_ERROR"
        MockSubmittedService
          .save(invalidProcess)
          .returns(Future.successful(Left(Errors(BadRequestError))))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidProcess)
      }

      "return a bad request response" in new InvalidSaveTest {
        private val result = controller.save()(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidSaveTest {
        private val result = controller.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of BAD_REQUEST" in new InvalidSaveTest {
        private val result = controller.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockSubmittedService
          .save(invalidProcess)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidProcess)
      }

      "return a internal server error response" in new ErrorSaveTest {
        private val result = controller.save()(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorSaveTest {
        private val result = controller.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorSaveTest {
        private val result = controller.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the get action" when {

    "the request is valid" should {

      trait ValidGetTest extends Test {
        val expectedProcess: JsObject = Json.obj("_id" -> validId)
        MockSubmittedService
          .getById(validId)
          .returns(Future.successful(Right(expectedProcess)))

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
        contentAsJson(result).as[JsObject] shouldBe expectedProcess
      }
    }

    "the request is invalid" should {

      trait InvalidGetTest extends Test {
        val expectedErrorCode = "BAD_REQUEST_ERROR"
        MockSubmittedService
          .getById(invalidId)
          .returns(Future.successful(Left(Errors(BadRequestError))))

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
        val expectedErrorCode = "NOT_FOUND_ERROR"
        MockSubmittedService
          .getById(validId)
          .returns(Future.successful(Left(Errors(NotFoundError))))

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

      "return a error code of NOT_FOUND_ERROR" in new NotFoundGetTest {
        private val result = controller.get(validId)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorGetTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockSubmittedService
          .getById(validId)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

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
}
