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
import java.time.ZonedDateTime
import models.LabelledData
import repositories.HmrcLabelledDataRepository

import scala.concurrent.Future

trait MockHmrcLabelledRepository extends MockFactory {
  val mockHmrcLabelledRepository: HmrcLabelledDataRepository = mock[HmrcLabelledDataRepository]

  object MockHmrcLabelledRepository {

    def save(id: String, data: JsValue, when: ZonedDateTime, credId: String, user: String, email: String): CallHandler[Future[RequestOutcome[LabelledData]]] =
      (mockHmrcLabelledRepository
        .save(_: String, _: JsValue, _: ZonedDateTime, _: String, _: String, _: String))
        .expects(id, data, *, credId, user, email)

    def get(id: String): CallHandler[Future[RequestOutcome[LabelledData]]] =
      (mockHmrcLabelledRepository
        .get(_: String))
        .expects(id)
    }
}
