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
import core.models.ocelot.{Page, Process}
import play.api.libs.json.{JsObject, JsValue}
import services.LabelledDataService
import models.{LabelledDataId, LabelledDataUpdateStatus}
import org.scalatest.TestSuite

import scala.concurrent.{ExecutionContext, Future}

trait MockLabelledDataService extends TestSuite with MockFactory {
  val mockLabelledDataService: LabelledDataService = mock[LabelledDataService]

  object MockLabelledDataService {
    val timescaleService: MockTimescalesService =  new MockTimescalesService{}
    val ratesService: MockRatesService = new MockRatesService{}

    def updateProcessLabelledDataTablesAndVersions(js: JsObject): CallHandler[Future[RequestOutcome[JsObject]]] =
        (mockLabelledDataService
          .updateProcessLabelledDataTablesAndVersions(_: JsObject))
          .expects(js)

    def addLabelledDataTables(pages: Seq[Page], process: Process, js: Option[JsObject]): CallHandler[Future[RequestOutcome[(Process, Seq[Page], JsObject)]]] =
      (mockLabelledDataService
        .addLabelledDataTables(_: Seq[Page], _: Process, _: Option[JsObject])(_: ExecutionContext))
        .expects(pages, process, js, *)

    def save(dataId: LabelledDataId, rates: JsValue, credId: String, user: String, email: String, inUse: List[String]): CallHandler[Future[RequestOutcome[LabelledDataUpdateStatus]]] =
      (mockLabelledDataService
        .save(_: LabelledDataId, _: JsValue, _: String, _: String, _: String, _: List[String]))
        .expects(dataId, rates, credId, user, email, inUse)

    def get(dataId: LabelledDataId): CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockLabelledDataService
        .get(_: LabelledDataId))
        .expects(dataId)

    def details(dataId: LabelledDataId): CallHandler[Future[RequestOutcome[LabelledDataUpdateStatus]]] =
      (mockLabelledDataService
        .details(_: LabelledDataId))
        .expects(dataId)

  }
}
