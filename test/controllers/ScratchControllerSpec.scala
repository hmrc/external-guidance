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

import java.util.UUID

import mocks.MockScratchService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.ContentTypes
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.AnyContentAsEmpty

import scala.concurrent.Future

class ScratchControllerSpec extends WordSpec with Matchers with ScalaFutures with GuiceOneAppPerSuite {

  private trait Test extends MockScratchService {
    val expectedId: UUID = UUID.randomUUID()
    val uuid: String = expectedId.toString
    val unknownUuid: String = UUID.randomUUID().toString
    val expectedJsObj: JsObject = Json.obj()
    MockScratchService.save().returns(Future.successful(expectedId))
    MockScratchService.getByUuid(uuid).returns(Future.successful(Some(expectedJsObj)))
    MockScratchService.getByUuid(unknownUuid).returns(Future.successful(None))
    lazy val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.obj())
    lazy val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")

    lazy val target: ScratchController = new ScratchController(mockScratchService, stubControllerComponents())
  }

  "Calling the save action" should {

    "return a created response" in new Test {
      private val result = target.save()(request)
      status(result) shouldBe CREATED
    }

    "return content as JSON" in new Test {
      private val result = target.save()(request)
      contentType(result) shouldBe Some(ContentTypes.JSON)
    }

    "return a UUID assigned to an attribute labelled id" in new Test {
      private val result = target.save()(request)
      val data = contentAsJson(result).as[JsObject]
      (data \ "id").as[String] shouldBe expectedId.toString
    }

  }

  "Calling the get action" should {

    "return an Ok response" in new Test {
      private val result = target.get(uuid)(getRequest)
      status(result) shouldBe OK
    }

    "return content as JSON" in new Test {
      private val result = target.get(uuid)(getRequest)
      contentType(result) shouldBe Some(ContentTypes.JSON)
    }

    "confirm returned content is a jason object" in new Test {
      private val result = target.get(uuid)(getRequest)
      contentAsJson(result).as[JsObject] shouldBe Json.obj()
    }

    "return a NOT_FOUND response" in new Test {
      private val result = target.get(unknownUuid)(getRequest)
      status(result) shouldBe NOT_FOUND
    }

  }

}
