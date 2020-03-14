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

package services

import java.util.UUID

import base.BaseSpec
import mocks.MockScratchRepository
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class ScratchServiceSpec extends BaseSpec {

  private trait Test extends MockScratchRepository {
    lazy val target: ScratchService = new ScratchService(mockScratchRepository)
  }

  "calling saveScratch method with a Process" should {
    "return valid UUID" in new Test {

      val expected: Future[UUID] = Future.successful(UUID.randomUUID())
      val process: JsObject = Json.obj()
      MockScratchRepository
        .save(process)
        .returns(expected)

      target.save(process) mustBe expected
    }
  }
}
