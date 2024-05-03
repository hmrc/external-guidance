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
import controllers.actions.FakeAllRolesAction
import core.models.errors.{BadRequestError, InternalServerError, NotFoundError}
import core.models.ocelot.ProcessJson
import mocks.MockArchiveService
import models.{ArchivedProcess, ProcessSummary}
import play.api.http.ContentTypes
import play.api.libs.json._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.ZonedDateTime
import scala.concurrent.Future

class ArchivedControllerSpec extends BaseSpec with ProcessJson {

  private trait Test extends MockArchiveService {

    val when: ZonedDateTime = ZonedDateTime.now
    val validId: String = when.toInstant.toEpochMilli().toString
    val processSummary = ProcessSummary(validId, "process_code", 1, "author", None, when, "Actioner", "Archived")

    lazy val target: ArchivedController = new ArchivedController(
      mockArchiveService,
      stubControllerComponents(),
      FakeAllRolesAction
    )

    lazy val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/")
  }

  "Invoking the controller get action" when {

    "the request is valid" should {

      trait ValidGetTest extends Test {

        val time: ZonedDateTime = ZonedDateTime.now()

        val returnedProcess: ArchivedProcess =
          ArchivedProcess(validId.toLong, time, validOnePageJson.as[JsObject], "user", processCode = "processCode")

        MockArchiveService
          .getById(validId.toString)
          .returns(Future.successful(Right(returnedProcess)))

      }

      "return an Ok response" in new ValidGetTest {

        private val result = target.get(validId)(getRequest)

        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidGetTest {

        private val result = target.get(validId)(getRequest)

        contentType(result) shouldBe Some(ContentTypes.JSON)
      }
    }

    "the request has an invalid process identifier" should {

      trait InvalidIdGetTest extends Test {

        val invalidId: String = "oct2002"

        MockArchiveService
          .getById(invalidId)
          .returns(Future.successful(Left(BadRequestError)))

      }

      "return a bad request response" in new InvalidIdGetTest {

        private val result = target.get(invalidId)(getRequest)

        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidIdGetTest {

        private val result = target.get(invalidId)(getRequest)

        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return the error code for BadRequestError" in new InvalidIdGetTest {

        private val result = target.get(invalidId)(getRequest)

        val json: JsObject = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe BadRequestError.code
      }
    }

    "the process identified in the request does not exists" should {

      trait NotFoundTest extends Test {

        MockArchiveService
          .getById(validId)
          .returns(Future.successful(Left(NotFoundError)))
      }

      "return a resource not found response" in new NotFoundTest {

        private val result = target.get(validId)(getRequest)

        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundTest {

        private val result = target.get(validId)(getRequest)

        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return the error code NOT_FOUND" in new NotFoundTest {

        private val result = target.get(validId)(getRequest)

        val json: JsObject = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe NotFoundError.code
      }
    }

    "an internal service error is raised by the service" should {

      trait InternalServiceErrorTest extends Test {

        MockArchiveService
          .getById(validId)
          .returns(Future.successful(Left(InternalServerError)))
      }

      "return an internal server error response" in new InternalServiceErrorTest {

        private val result = target.get(validId)(getRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new InternalServiceErrorTest {

        private val result = target.get(validId)(getRequest)

        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return the error code for InternalServerError" in new InternalServiceErrorTest {

        private val result = target.get(validId)(getRequest)

        val json: JsObject = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe InternalServerError.code
      }
    }
  }

  "Calling the list action" when {

    "the request is valid" should {

      trait ValidListTest extends Test {
        implicit val formats: OFormat[ProcessSummary] = Json.format[ProcessSummary]
        MockArchiveService
          .list
          .returns(Future.successful(Right(Json.toJson(List(processSummary)).as[JsArray])))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
      }

      "return an OK response" in new ValidListTest {
        private val result = target.list(request)
        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidListTest {
        private val result = target.list(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }
    }

    "a downstream error occurs" should {

      trait ErrorGetTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        MockArchiveService
          .list
          .returns(Future.successful(Left(InternalServerError)))

        lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
      }

      "return a internal server error response" in new ErrorGetTest {
        private val result = target.list(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new ErrorGetTest {
        private val result = target.list(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of INTERNAL_SERVER_ERROR" in new ErrorGetTest {
        private val result = target.list(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe expectedErrorCode
      }
    }
  }

}
