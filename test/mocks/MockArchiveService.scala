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
import models.ArchivedProcess
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import services.ArchiveService
import play.api.libs.json.JsValue
import scala.concurrent.Future

trait MockArchiveService extends MockFactory {

  val mockArchiveService: ArchiveService = mock[ArchiveService]

  object MockArchiveService {

    def getById(id: String): CallHandler[Future[RequestOutcome[ArchivedProcess]]] =
      (mockArchiveService
        .getById(_: String))
        .expects(id)


    def list: CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockArchiveService.list _).expects

  }

}
