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
import models.PublishedProcess
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsValue, JsObject}
import services.PublishedService

import scala.concurrent.Future

trait MockPublishedService extends MockFactory {

  val mockPublishedService: PublishedService = mock[PublishedService]

  object MockPublishedService {

    def getById(id: String): CallHandler[Future[RequestOutcome[PublishedProcess]]] =
      (mockPublishedService
        .getById(_: String))
        .expects(id)

    def getByProcessCode(processCode: String): CallHandler[Future[RequestOutcome[PublishedProcess]]] =
      (mockPublishedService
        .getByProcessCode(_: String))
        .expects(processCode)

    def save(id: String, user: String, processCode: String, jsonProcess: JsObject): CallHandler[Future[RequestOutcome[String]]] =
      (mockPublishedService
        .save(_: String, _: String, _: String, _: JsObject))
        .expects(id, user, processCode, jsonProcess)

    def archive(id: String, user: String): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockPublishedService
        .archive(_: String, _: String))
        .expects(id, user)

    def getTimescalesInUse(): CallHandler[Future[RequestOutcome[List[String]]]] =
      (mockPublishedService
        .getTimescalesInUse _)
        .expects()

    def getRatesInUse(): CallHandler[Future[RequestOutcome[List[String]]]] =
      (mockPublishedService
        .getRatesInUse _)
        .expects()

    def list: CallHandler[Future[RequestOutcome[JsValue]]] =
      (() => mockPublishedService
        .list)
        .expects()

  }

}
