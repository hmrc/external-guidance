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

package mocks

import models.{PublishedProcess, RequestOutcome}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsObject
import repositories.PublishedRepository

import scala.concurrent.Future

trait MockPublishedRepository extends MockFactory {

  val mockPublishedRepository: PublishedRepository = mock[PublishedRepository]

  object MockPublishedRepository {

    def getById(id: String): CallHandler[Future[RequestOutcome[PublishedProcess]]] = {
      (mockPublishedRepository
        .getById(_: String))
        .expects(id)
    }

    def save(id: String, process: JsObject): CallHandler[Future[RequestOutcome[String]]] = {
      (mockPublishedRepository
        .save(_: String, _: JsObject))
        .expects(id, process)
    }

  }

}
