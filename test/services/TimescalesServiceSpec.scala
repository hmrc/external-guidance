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

package services

import base.BaseSpec
import mocks.MockTimescalesRepository
import core.models.RequestOutcome
import core.models.errors._
import play.api.libs.json.{JsValue, Json}
import mocks.MockAppConfig
import scala.concurrent.Future

class TimescalesServiceSpec extends BaseSpec {

  private trait Test extends MockTimescalesRepository {
    lazy val target: TimescalesService = new TimescalesService(mockTimescalesRepository, MockAppConfig)

    val timescales: Map[String, Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
    val timescaleJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
  }

  "Calling save method" when {

    "the JSON is valid" should {
      "return Unit" in new Test{

        val expected: RequestOutcome[Unit] = Right(())

        MockTimescalesRepository
          .save(timescaleJson)
          .returns(Future.successful(expected))

        whenReady(target.save(timescaleJson)) {
          case result => result shouldBe expected
        }
      }
    }

    "the timescales JSON is not valid" should {
      "return Validation error" in new Test {
        val invalidTs: JsValue =  Json.parse("""{"Hello": "World"}""")

        whenReady(target.save(invalidTs)) {
          case Right(_) => fail
          case Left(ValidationError) => succeed
          case err => fail
        }
      }
    }

    "the JSON is invalid" should {
      "not call the scratch repository" in new Test {
        MockTimescalesRepository.save(Json.parse("""{"Hello": "World"}""")).never()

        target.save(Json.parse("""{"Hello": "World"}"""))
      }
    }

    "a database error occurs" should {
      "return a internal error" in new Test {
        MockTimescalesRepository
          .save(timescaleJson)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(target.save(timescaleJson)) {
          case result @ Left(_) => result shouldBe Left(InternalServerError)
          case _ => fail
        }
      }
    }

  }

  "Calling get method" should {

    "return the corresponding scratch process" in new Test {
      val expected: RequestOutcome[JsValue] = Right(timescaleJson)
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(expected))

      whenReady(target.get) { result =>
        result shouldBe expected
      }
    }

    "return Seed defnitions if no DB data found" in new Test {
      val expected: RequestOutcome[JsValue] = Right(timescaleJson)
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Left(NotFoundError)))

      whenReady(target.get) { result =>
        result shouldBe expected
      }
    }


    "return an internal error if a database error occurs" in new Test {
      MockTimescalesRepository
        .get("1")
        .returns(Future.successful(Left(DatabaseError)))

      whenReady(target.get) { result =>
        result shouldBe Left(InternalServerError)
      }
    }
  }

}
