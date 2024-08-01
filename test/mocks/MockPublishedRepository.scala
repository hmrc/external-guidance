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

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsObject
import repositories.PublishedRepository
import core.models.RequestOutcome
import models.{LabelledDataId, PublishedProcess}
import org.scalatest.TestSuite

import scala.concurrent.Future

trait MockPublishedRepository extends TestSuite with MockFactory {

  val mockPublishedRepository: PublishedRepository = mock[PublishedRepository]

  object MockPublishedRepository {

    def getById(id: String): CallHandler[Future[RequestOutcome[PublishedProcess]]] =
      (mockPublishedRepository
        .getById(_: String))
        .expects(id)

    def getByProcessCode(processCode: String): CallHandler[Future[RequestOutcome[PublishedProcess]]] =
      (mockPublishedRepository
        .getByProcessCode(_: String))
        .expects(processCode)

    def save(id: String, user: String, processCode: String, process: JsObject, version: Int): CallHandler[Future[RequestOutcome[String]]] =
      (mockPublishedRepository
        .save(_: String, _: String, _: String, _: JsObject, _: Int))
        .expects(id, user, processCode, process, version)

    def delete(id: String): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockPublishedRepository
        .delete(_: String))
        .expects(id)

    def getDataInUse(dataId: LabelledDataId): CallHandler[Future[RequestOutcome[List[String]]]] =
      (mockPublishedRepository
        .getDataInUse(_: LabelledDataId))
        .expects(dataId)

    def list(): CallHandler[Future[RequestOutcome[List[PublishedProcess]]]] =
      (mockPublishedRepository
        .list _)
        .expects()
  }
}
