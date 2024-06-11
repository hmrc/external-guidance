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
import models.{Approval, ApprovalProcessPageReview, ApprovalProcessSummary}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import repositories.ApprovalsRepository

import scala.concurrent.Future

trait MockApprovalsRepository extends MockFactory {

  val mockApprovalsRepository: ApprovalsRepository = mock[ApprovalsRepository]

  object MockApprovalsRepository {

    def createOrUpdate(process: Approval): CallHandler[Future[RequestOutcome[String]]] =
      (mockApprovalsRepository
        .createOrUpdate(_: Approval))
        .expects(*)

    def getById(id: String): CallHandler[Future[RequestOutcome[Approval]]] =
      (mockApprovalsRepository
        .getById(_: String))
        .expects(*)

    def getByProcessCode(processCode: String): CallHandler[Future[RequestOutcome[Approval]]] =
      (mockApprovalsRepository
        .getByProcessCode(_: String))
        .expects(*)

    def approvalSummaryList(roles: List[String]): CallHandler[Future[RequestOutcome[List[ApprovalProcessSummary]]]] =
      (mockApprovalsRepository.approvalSummaryList (_: List[String]))
        .expects(roles)

    def changeStatus(id: String, status: String, user: String): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockApprovalsRepository
        .changeStatus(_: String, _: String, _: String))
        .expects(id, status, user)

    def delete(id: String): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockApprovalsRepository
        .delete(_: String))
        .expects(id)

    def getTimescalesInUse(): CallHandler[Future[RequestOutcome[List[String]]]] =
      (mockApprovalsRepository
        .getTimescalesInUse _)
        .expects()

    def getRatesInUse(): CallHandler[Future[RequestOutcome[List[String]]]] =
      (mockApprovalsRepository
        .getRatesInUse _)
        .expects()

    def updatePageReview(id: String, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockApprovalsRepository
        .updatePageReview(_: String, _: String, _: String, _: ApprovalProcessPageReview))
        .expects(id, pageUrl, reviewType, reviewInfo)

    def updateReview(id: String, reviewType: String, updateUser: String, result: String): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockApprovalsRepository
        .updateReview(_: String, _: String, _: String, _: String))
        .expects(id, reviewType, updateUser, result)



  }

}
