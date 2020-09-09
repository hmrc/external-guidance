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
import utils.Constants._

import scala.concurrent.Future

trait MockApprovalService extends MockFactory {
  val mockApprovalService: ApprovalService = mock[ApprovalService]

  object MockApprovalService {

    def getById(id: String): CallHandler[Future[RequestOutcome[JsObject]]] = {
      (mockApprovalService
        .getById(_: String))
        .expects(id)
    }

    def getByProcessCode(processCode: String): CallHandler[Future[RequestOutcome[JsObject]]] = {
      (mockApprovalService
        .getByProcessCode(_: String))
        .expects(processCode)
    }

    def save(
        process: JsObject,
        reviewType: String = ReviewType2i,
        status: String = StatusSubmitted
    ): CallHandler[Future[RequestOutcome[String]]] = {
      (mockApprovalService
        .save(_: JsObject, _: String, _: String))
        .expects(process, reviewType, status)
    }

    def approvalSummaryList(roles: List[String]): CallHandler[Future[RequestOutcome[JsArray]]] = {
      (mockApprovalService.approvalSummaryList(_: List[String]))
        .expects(roles)
    }
  }
}
