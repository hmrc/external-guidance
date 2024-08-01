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

import core.models.ocelot.errors.GuidanceError
import core.models.RequestOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsObject, JsValue}
import services.RatesService
import models.LabelledDataUpdateStatus
import core.models.ocelot.Process
import org.scalatest.TestSuite

import scala.concurrent.Future

trait MockRatesService extends TestSuite with MockFactory {
  val mockRatesService: RatesService = mock[RatesService]

  object MockRatesService {

    def save(rates: JsValue, credId: String, user: String, email: String, inUse: List[String]): CallHandler[Future[RequestOutcome[LabelledDataUpdateStatus]]] =
      (mockRatesService
        .save(_: JsValue, _: String, _: String, _: String, _: List[String]))
        .expects(rates, credId, user, email, inUse)

    def updateProcessTable(js: JsObject, p: Process): CallHandler[Future[RequestOutcome[(JsObject, Process)]]] =
        (mockRatesService
          .updateProcessTable(_: JsObject, _: Process))
          .expects(js, p)

    def get(): CallHandler[Future[RequestOutcome[(Map[String, BigDecimal], Long)]]] =
      (mockRatesService
        .get _)
        .expects()

    def details(): CallHandler[Future[RequestOutcome[LabelledDataUpdateStatus]]] =
      (mockRatesService
        .details _)
        .expects()

    def expandDataIds(ids: List[String]): CallHandler[List[String]] =
      (mockRatesService
        .expandDataIds(_: List[String]))
        .expects(ids)

    def missingIdError(id: String): CallHandler[GuidanceError] =
      (mockRatesService
        .missingIdError(_: String))
        .expects(id)

    def getNativeAsJson: CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockRatesService
        .getNativeAsJson _)
        .expects()
  }
}
