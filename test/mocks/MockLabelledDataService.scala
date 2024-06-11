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
import play.api.libs.json.JsObject
import services.LabelledDataService

import scala.concurrent.{ExecutionContext, Future}

trait MockLabelledDataService extends MockFactory {
  val mockLabelledDataService: LabelledDataService = mock[LabelledDataService]

  object MockLabelledDataService {
    val timescaleService =  new MockTimescalesService{}
    val ratesService = new MockRatesService{}

    def updateProcessLabelledDataTablesAndVersions(js: JsObject): CallHandler[Future[RequestOutcome[JsObject]]] =
        (mockLabelledDataService
          .updateProcessLabelledDataTablesAndVersions(_: JsObject))
          .expects(js)

    def buildLabelledDataTables(pages: Seq[Page], process: Process, js: Option[JsObject]): CallHandler[Future[RequestOutcome[(Process, Seq[Page], JsObject)]]] =
      (mockLabelledDataService
        .buildLabelledDataTables(_: Seq[Page], _: Process, _: Option[JsObject])(_: ExecutionContext))
        .expects(pages, process, js, *)
  }
}
