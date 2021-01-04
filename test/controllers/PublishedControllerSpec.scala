/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.ZonedDateTime

import base.BaseSpec
import mocks.MockPublishedService
import models.PublishedProcess
import models.errors.{BadRequestError, InternalServerError, NotFoundError}
import models.ocelot.ProcessJson
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.ContentTypes
import play.api.libs.json.JsObject
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class PublishedControllerSpec extends BaseSpec with GuiceOneAppPerSuite with ProcessJson {

  private trait Test extends MockPublishedService {

    val validId: String = "oct90005"

    lazy val target: PublishedController = new PublishedController(mockPublishedService, stubControllerComponents())

    lazy val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/")
  }

  "Invoking the controller get action" when {

    "the request is valid" should {

      trait ValidGetTest extends Test {

        val expectedProcess: JsObject = validOnePageJson.as[JsObject]
        val returnedPublishedProcess: PublishedProcess =
          PublishedProcess(validId, 1, ZonedDateTime.now(), validOnePageJson.as[JsObject], "user", processCode = "processCode")

        MockPublishedService
          .getById(validId)
          .returns(Future.successful(Right(returnedPublishedProcess)))

      }

      "return an Ok response" in new ValidGetTest {

        private val result = target.get(validId)(getRequest)

        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidGetTest {

        private val result = target.get(validId)(getRequest)

        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return expected content" in new ValidGetTest {
        private val result = target.get(validId)(getRequest)
        contentAsJson(result).as[JsObject] shouldBe expectedProcess
      }
    }

    "the request has an invalid process identifier" should {

      trait InvalidIdGetTest extends Test {

        val invalidId: String = "oct2002"

        MockPublishedService
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

        MockPublishedService
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

        MockPublishedService
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

  "Invoking the controller getByProcessCode action" when {

    "the request is valid" should {

      trait ValidGetTest extends Test {

        val expectedProcess: JsObject = validOnePageJson.as[JsObject]
        val returnedPublishedProcess: PublishedProcess =
          PublishedProcess(validId, 1, ZonedDateTime.now(), validOnePageJson.as[JsObject], "user", processCode = "processCode")

        MockPublishedService
          .getByProcessCode(validId)
          .returns(Future.successful(Right(returnedPublishedProcess)))

      }

      "return an Ok response" in new ValidGetTest {

        private val result = target.getByProcessCode(validId)(getRequest)

        status(result) shouldBe OK
      }

      "return content as JSON" in new ValidGetTest {

        private val result = target.getByProcessCode(validId)(getRequest)

        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return expected content" in new ValidGetTest {
        private val result = target.getByProcessCode(validId)(getRequest)
        contentAsJson(result).as[JsObject] shouldBe expectedProcess
      }
    }

    "the request has an invalid process identifier" should {

      trait InvalidIdGetTest extends Test {

        val invalidId: String = "oct2002"

        MockPublishedService
          .getByProcessCode(invalidId)
          .returns(Future.successful(Left(BadRequestError)))

      }

      "return a bad request response" in new InvalidIdGetTest {

        private val result = target.getByProcessCode(invalidId)(getRequest)

        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON" in new InvalidIdGetTest {

        private val result = target.getByProcessCode(invalidId)(getRequest)

        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return the error code for BadRequestError" in new InvalidIdGetTest {

        private val result = target.getByProcessCode(invalidId)(getRequest)

        val json: JsObject = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe BadRequestError.code
      }
    }

    "the process identified in the request does not exists" should {

      trait NotFoundTest extends Test {

        MockPublishedService
          .getByProcessCode(validId)
          .returns(Future.successful(Left(NotFoundError)))
      }

      "return a resource not found response" in new NotFoundTest {

        private val result = target.getByProcessCode(validId)(getRequest)

        status(result) shouldBe NOT_FOUND
      }

      "return content as JSON" in new NotFoundTest {

        private val result = target.getByProcessCode(validId)(getRequest)

        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return the error code NOT_FOUND" in new NotFoundTest {

        private val result = target.getByProcessCode(validId)(getRequest)

        val json: JsObject = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe NotFoundError.code
      }
    }

    "an internal service error is raised by the service" should {

      trait InternalServiceErrorTest extends Test {

        MockPublishedService
          .getByProcessCode(validId)
          .returns(Future.successful(Left(InternalServerError)))
      }

      "return an internal server error response" in new InternalServiceErrorTest {

        private val result = target.getByProcessCode(validId)(getRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return content as JSON" in new InternalServiceErrorTest {

        private val result = target.getByProcessCode(validId)(getRequest)

        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return the error code for InternalServerError" in new InternalServiceErrorTest {

        private val result = target.getByProcessCode(validId)(getRequest)

        val json: JsObject = contentAsJson(result).as[JsObject]
        (json \ "code").as[String] shouldBe InternalServerError.code
      }
    }
  }
}
