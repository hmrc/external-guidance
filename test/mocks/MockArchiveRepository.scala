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

package mocks

import core.models.RequestOutcome
import models.PublishedProcess
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import repositories.ArchiveRepository

import scala.concurrent.Future

trait MockArchiveRepository extends MockFactory {

  val mockArchiveRepository: ArchiveRepository = mock[ArchiveRepository]

  object MockArchiveRepository {
    def archive(id: String, user: String, processCode: String, process: PublishedProcess): CallHandler[Future[RequestOutcome[String]]] = {
      (mockArchiveRepository.archive(_: String, _: String, _: String, _: PublishedProcess))
        .expects(id, user, processCode, process)
    }

    // def getByProcessCode(processCode: String): CallHandler[Future[List[JsObject]]] = {
    //   (mockArchiveRepository.getByProcessCode(_: String)).expects(processCode)
    // }
  }

}