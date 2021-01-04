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

import models.{ApprovalProcess, ApprovalProcessSummary, RequestOutcome}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import repositories.ApprovalRepository

import scala.concurrent.Future

trait MockApprovalRepository extends MockFactory {

  val mockApprovalRepository: ApprovalRepository = mock[ApprovalRepository]

  object MockApprovalRepository {

    def update(process: ApprovalProcess): CallHandler[Future[RequestOutcome[String]]] = {
      (mockApprovalRepository
        .update(_: ApprovalProcess))
        .expects(*)
    }

    def getById(id: String): CallHandler[Future[RequestOutcome[ApprovalProcess]]] = {
      (mockApprovalRepository
        .getById(_: String))
        .expects(*)
    }

    def getByProcessCode(processCode: String): CallHandler[Future[RequestOutcome[ApprovalProcess]]] = {
      (mockApprovalRepository
        .getByProcessCode(_: String))
        .expects(*)
    }

    def approvalSummaryList(roles: List[String]): CallHandler[Future[RequestOutcome[List[ApprovalProcessSummary]]]] = {
      (mockApprovalRepository.approvalSummaryList (_: List[String]))
        .expects(roles)
    }

    def changeStatus(id: String, status: String, user: String): CallHandler[Future[RequestOutcome[Unit]]] = {
      (mockApprovalRepository
        .changeStatus(_: String, _: String, _: String))
        .expects(id, status, user)
    }

  }

}
