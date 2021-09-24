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

import mocks.{MockTimescalesRepository, MockTimescalesService}
import core.models.errors.{InternalServerError, ValidationError}
import core.models.MongoDateTimeFormats
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import controllers.actions.FakeAllRolesAction
import java.time.ZonedDateTime
import play.api.mvc._
import scala.concurrent.Future
import models.{UpdateDetails, TimescalesDetail, TimescalesUpdate}

class TimescalesControllerSpec extends WordSpec with Matchers with ScalaFutures with GuiceOneAppPerSuite {

  trait Test extends MockTimescalesService with MockTimescalesRepository {
    val timescaleJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
    lazy val target: TimescalesController = new TimescalesController(mockTimescalesService, stubControllerComponents(), FakeAllRolesAction)
    val lastUpdateTime: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, MongoDateTimeFormats.localZoneID)
    val timescales: Map[String, Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
    val credId: String = FakeAllRolesAction.credential
    val user: String = FakeAllRolesAction.name
    val email: String = FakeAllRolesAction.email
    val timescalesUpdate = TimescalesUpdate(timescaleJson, lastUpdateTime, credId, user, email)
    val updateDetail = UpdateDetails(lastUpdateTime, credId, user, email)
    val timescaleDetails = TimescalesDetail(timescales.size, Some(updateDetail))
  }

  "Calling the save action" when {

    trait ValidSaveTest extends Test {
      MockTimescalesService.save(timescaleJson, credId, user, email).returns(Future.successful(Right(timescaleDetails)))
      lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(timescaleJson)
    }

    "the request is valid" should {

      "return an Accepted response" in new ValidSaveTest {
        private val result = target.save()(request)
        status(result) shouldBe ACCEPTED
      }
    }

    "the request is valid but the timescales are invalid" should {

      trait InvalidSaveTest extends Test {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")
        MockTimescalesService.save(invalidTs, credId, user, email).returns(Future.successful(Left(ValidationError)))
        lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(invalidTs)
      }

      "return Bad request response with invalid timescales" in new InvalidSaveTest {
        private val result = target.save()(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return content as JSON with invalid timescales" in new InvalidSaveTest {
        private val result = target.save()(request)
        contentType(result) shouldBe Some(ContentTypes.JSON)
      }

      "return an error code of VALIDATION_ERROR with invalid timescales" in new InvalidSaveTest {
        private val result = target.save()(request)
        private val data = contentAsJson(result).as[JsObject]
        (data \ "code").as[String] shouldBe "VALIDATION_ERROR"
      }

    }

    "a downstream error occurs" should {

      trait ErrorSaveTest extends Test {
        val expectedErrorCode = "INTERNAL_SERVER_ERROR"
        val process: JsObject = Json.obj()
        MockTimescalesService.save(process, credId, user, email).returns(Future.successful(Left(InternalServerError)))
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

  "Calling the details action" when {

    "the request is valid" should {

      trait ValidDetailsTest extends Test {
        MockTimescalesService.details.returns(Future.successful(Right(timescaleDetails)))
        lazy val request: FakeRequest[AnyContent] = FakeRequest()
      }

      "return a no content response" in new ValidDetailsTest {
        private val result = target.details(request)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(timescaleDetails)
      }
    }

    "the request is valid but the timescales are invalid" should {

      trait InvalidSaveTest extends Test {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")
        MockTimescalesService.save(invalidTs, credId, user, email).returns(Future.successful(Left(ValidationError)))
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
        MockTimescalesService.save(process, credId, user, email).returns(Future.successful(Left(InternalServerError)))
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
