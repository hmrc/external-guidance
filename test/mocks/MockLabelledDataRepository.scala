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

package mocks

import core.models.RequestOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue

import java.time.Instant
import models.{LabelledData, LabelledDataId}
import org.scalatest.TestSuite
import repositories.LabelledDataRepository

import scala.concurrent.Future

trait MockLabelledDataRepository extends TestSuite with MockFactory {
  val mockLabelledDataRepository: LabelledDataRepository = mock[LabelledDataRepository]

  object MockLabelledDataRepository {

    def save(id: LabelledDataId, data: JsValue, when: Instant, credId: String, user: String, email: String): CallHandler[Future[RequestOutcome[LabelledData]]] =
      (mockLabelledDataRepository
        .save(_: LabelledDataId, _: JsValue, _: Instant, _: String, _: String, _: String))
        .expects(id, data, *, credId, user, email)

    def get(id: LabelledDataId): CallHandler[Future[RequestOutcome[LabelledData]]] =
      (mockLabelledDataRepository
        .get(_: LabelledDataId))
        .expects(id)
    }
}
