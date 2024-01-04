/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.libs.json.{JsObject, JsValue}
import services.TimescalesService
import models.TimescalesResponse

import scala.concurrent.Future

trait MockTimescalesService extends MockFactory {
  val mockTimescalesService: TimescalesService = mock[TimescalesService]

  object MockTimescalesService {

    def save(timescales: JsValue, credId: String, user: String, email: String, inUse: List[String]): CallHandler[Future[RequestOutcome[TimescalesResponse]]] =
      (mockTimescalesService
        .save(_: JsValue, _: String, _: String, _: String, _: List[String]))
        .expects(timescales, credId, user, email, inUse)

    def updateProcessTimescaleTable(js: JsObject): CallHandler[Future[RequestOutcome[JsObject]]] =
        (mockTimescalesService
          .updateProcessTimescaleTableAndDetails(_: JsObject))
          .expects(js)

    def get(): CallHandler[Future[RequestOutcome[(Map[String, Int], Long)]]] =
      (mockTimescalesService
        .get _)
        .expects()

    def details(): CallHandler[Future[RequestOutcome[TimescalesResponse]]] =
      (mockTimescalesService
        .details _)
        .expects()
    }
}
