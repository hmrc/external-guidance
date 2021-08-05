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

import mocks.MockTimescalesService
//import core.models.errors.{BadRequestError, Error, InternalServerError, ProcessError, ValidationError}
import core.models.errors.{InternalServerError, ValidationError}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
//import core.services.toProcessErr

import scala.concurrent.Future

class TimescalesControllerSpec extends WordSpec with Matchers with ScalaFutures with GuiceOneAppPerSuite {

  private trait Test extends MockTimescalesService {
    val timescaleJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
    lazy val target: TimescalesController = new TimescalesController(mockTimescalesService, stubControllerComponents())
  }

  "Calling the save action" when {

    "the request is valid" should {

      trait ValidSaveTest extends Test {
        MockTimescalesService.save(timescaleJson).returns(Future.successful(Right(())))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(timescaleJson)
      }

      "return a no content response" in new ValidSaveTest {
        private val result = target.save()(request)
        status(result) shouldBe NO_CONTENT
      }
    }

    "the request is valid but the timescales are invalid" should {

      trait InvalidSaveTest extends Test {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")
        MockTimescalesService.save(invalidTs).returns(Future.successful(Left(ValidationError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidTs)
      }

      "return a unsupportable entity response" in new InvalidSaveTest {
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
        (data \ "code").as[String] shouldBe "VALIDATION_ERROR"
      }

    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val process: JsObject = Json.obj()
        MockTimescalesService.save(process).returns(Future.successful(Left(InternalServerError)))
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
}
