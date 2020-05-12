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

import models.RequestOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsArray, JsObject}
import services.ApprovalService

import scala.concurrent.Future

trait MockApprovalService extends MockFactory {
  val mockApprovalService: ApprovalService = mock[ApprovalService]

  object MockApprovalService {

    def getById(id: String): CallHandler[Future[RequestOutcome[JsObject]]] = {
      (mockApprovalService
        .getById(_: String))
        .expects(id)
    }

    def save(process: JsObject): CallHandler[Future[RequestOutcome[String]]] = {
      (mockApprovalService
        .save(_: JsObject))
        .expects(process)
    }

    def approvalSummaryList(): CallHandler[Future[RequestOutcome[JsArray]]] = {
      (mockApprovalService.approvalSummaryList: () => Future[RequestOutcome[JsArray]])
        .expects()
    }

  }
}
