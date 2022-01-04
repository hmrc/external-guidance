/*
 * Copyright 2022 HM Revenue & Customs
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

import java.util.UUID

import mocks.MockScratchService
import core.models.errors.{BadRequestError, Error, InternalServerError, NotFoundError, ProcessError, ValidationError}
import core.models.ocelot.errors._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import core.models.errors.ProcessError.toProcessErr

import scala.concurrent.Future
import mocks.MockTimescalesService

class ScratchControllerSpec extends AnyWordSpec with Matchers with ScalaFutures with GuiceOneAppPerSuite {

  private trait Test extends MockScratchService with MockTimescalesService {
    val id: String = "7a2f7eb3-6f0d-4d7f-a9b9-44a7137820ad"
    lazy val target: ScratchController = new ScratchController(mockScratchService, mockTimescalesService, stubControllerComponents())
  }

  "Calling the save action" when {

    "the request is valid" should {

      trait ValidSaveTest extends Test {
        val expectedId: UUID = UUID.fromString(id)
        val process: JsObject = Json.obj()
        MockScratchService.save(process).returns(Future.successful(Right(expectedId)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return a created response" in new ValidSaveTest {
        private val result = target.save()(request)
        status(result) shouldBe CREATED
      }

      "return content as JSON" in new ValidSaveTest {
        private val result = target.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a UUID assigned to an attribute labelled id" in new ValidSaveTest {
        private val result = target.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "id").as[String] shouldBe expectedId.toString
      }
    }

    "the request is invalid" should {

      trait InvalidSaveTest extends Test {
        val expectedErrorCode = "BAD_REQUEST"
        val process: JsObject = Json.obj()
        MockScratchService.save(process).returns(Future.successful(Left(BadRequestError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return a bad request response" in new InvalidSaveTest {
        private val result = target.save()(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidSaveTest {
        private val result = target.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of BAD_REQUEST" in new InvalidSaveTest {
        private val result = target.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the request is valid but the process is invalid" should {

      trait InvalidSaveTest extends Test {
        val processError: ProcessError = toProcessErr(DuplicatePageUrl("4", "/feeling-bad"))
        val expectedError = Error(List(processError))
        val process: JsObject = data.ProcessData.invalidOnePageJson.as[JsObject]
        MockScratchService.save(process).returns(Future.successful(Left(expectedError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return a unsupportable entity response" in new InvalidSaveTest {
        private val result = target.save()(request)
        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      "return content as JSON" in new InvalidSaveTest {
        private val result = target.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of UNPROCESSABLE_ENTITY" in new InvalidSaveTest {
        private val result = target.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe Error.UnprocessableEntity
      }

    }

    "the request is valid but the process returns ValidationError" should {

      trait InvalidSaveTest extends Test {
        val processError: ProcessError = toProcessErr(DuplicatePageUrl("4", "/feeling-bad"))
        val expectedError = BadRequestError
        val process: JsObject = data.ProcessData.invalidOnePageJson.as[JsObject]
        MockScratchService.save(process).returns(Future.successful(Left(ValidationError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return a unsupportable entity response" in new InvalidSaveTest {
        private val result = target.save()(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidSaveTest {
        private val result = target.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of UNPROCESSABLE_ENTITY" in new InvalidSaveTest {
        private val result = target.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe "BAD_REQUEST"
      }

    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val process: JsObject = Json.obj()
        MockScratchService.save(process).returns(Future.successful(Left(InternalServerError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(process)
      }

      "return a internal server error response" in new ErrorSaveTest {
        private val result = target.save()(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorSaveTest {
        private val result = target.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorSaveTest {
        private val result = target.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

  "Calling the get action" when {

    "the request is valid" should {

      trait ValidGetTest extends Test {
        val expectedId: UUID = UUID.fromString(id)
        val expectedProcess: JsObject = Json.obj()
        MockScratchService.getById(id).returns(Future.successful(Right(expectedProcess)))
        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an OK response" in new ValidGetTest {
        MockTimescalesService.updateProcessTimescaleTable(expectedProcess).returns(Future.successful(Right(expectedProcess)))
        private val result = target.get(id)(request)
        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidGetTest {
        MockTimescalesService.updateProcessTimescaleTable(expectedProcess).returns(Future.successful(Right(expectedProcess)))
        private val result = target.get(id)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "confirm returned content is a JSON object" in new ValidGetTest {
        MockTimescalesService.updateProcessTimescaleTable(expectedProcess).returns(Future.successful(Right(expectedProcess)))
        private val result = target.get(id)(request)
        contentAsJson(result).as[JsObject] shouldBe expectedProcess
      }
    }

    "the request is invalid" should {

      trait InvalidGetTest extends Test {
        val expectedErrorCode = "BAD_REQUEST"
        MockScratchService.getById(id).returns(Future.successful(Left(BadRequestError)))
        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an bad request response" in new InvalidGetTest {
        private val result = target.get(id)(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidGetTest {
        private val result = target.get(id)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of BAD_REQUEST" in new InvalidGetTest {
        private val result = target.get(id)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "the request contains an unknown ID" should {

      trait NotFoundGetTest extends Test {
        val expectedErrorCode = "NOT_FOUND"
        MockScratchService.getById(id).returns(Future.successful(Left(NotFoundError)))
        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an not found response" in new NotFoundGetTest {
        private val result = target.get(id)(request)
        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundGetTest {
        private val result = target.get(id)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return a error code of NOT_FOUND" in new NotFoundGetTest {
        private val result = target.get(id)(request)
        private val json = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe expectedErrorCode
      }
    }

    "a downstream error occurs" should {

      trait ErrorGetTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockScratchService.getById(id).returns(Future.successful(Left(InternalServerError)))
        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return a internal server error response" in new ErrorGetTest {
        private val result = target.get(id)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorGetTest {
        private val result = target.get(id)(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorGetTest {
        private val result = target.get(id)(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }
}
