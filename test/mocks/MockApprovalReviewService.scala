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

import core.models._
import models._
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsValue, JsObject}
import services.ApprovalReviewService
import models.Constants._

import scala.concurrent.Future

trait MockApprovalReviewService extends MockFactory {
  val mockApprovalReviewService: ApprovalReviewService = mock[ApprovalReviewService]

  object MockApprovalReviewService {

    def getById(id: String): CallHandler[Future[RequestOutcome[JsObject]]] =
      (mockApprovalReviewService
        .getById(_: String))
        .expects(id)

    def getByProcessCode(processCode: String): CallHandler[Future[RequestOutcome[JsObject]]] =
      (mockApprovalReviewService
        .getByProcessCode(_: String))
        .expects(processCode)

    def save(
        process: JsObject,
        reviewType: String = ReviewType2i,
        status: String = StatusSubmitted,
        checkLevel: GuidanceCheckLevel = Strict
    ): CallHandler[Future[RequestOutcome[String]]] =
      (mockApprovalReviewService
        .save(_: JsObject, _: String, _: String, _: GuidanceCheckLevel))
        .expects(process, reviewType, status, checkLevel)

    def approvalSummaryList(roles: List[String]): CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockApprovalReviewService.approvalSummaryList(_: List[String]))
        .expects(roles)

    def getDataInUse(dataId: LabelledDataId): CallHandler[Future[RequestOutcome[List[String]]]] =
      (mockApprovalReviewService
        .getDataInUse(_: LabelledDataId))
        .expects(dataId)

    // Review service

    def approvalReviewInfo(id: String, reviewType: String): CallHandler[Future[RequestOutcome[ProcessReview]]] = {
      (mockApprovalReviewService
        .approvalReviewInfo(_: String, _: String))
        .expects(id, reviewType)
    }

    def twoEyeReviewComplete(id: String, statusInfo: ApprovalProcessStatusChange): CallHandler[Future[RequestOutcome[AuditInfo]]] = {
      (mockApprovalReviewService
        .twoEyeReviewComplete(_: String, _: ApprovalProcessStatusChange))
        .expects(id, statusInfo)
    }

    def approvalPageInfo(id: String, pageUrl: String, reviewType: String): CallHandler[Future[RequestOutcome[ApprovalProcessPageReview]]] = {
      (mockApprovalReviewService
        .approvalPageInfo(_: String, _: String, _: String))
        .expects(id, pageUrl, reviewType)
    }

    def factCheckComplete(id: String, statusInfo: ApprovalProcessStatusChange): CallHandler[Future[RequestOutcome[AuditInfo]]] = {
      (mockApprovalReviewService
        .factCheckComplete(_: String, _: ApprovalProcessStatusChange))
        .expects(id, statusInfo)
    }

    def list(): CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockApprovalReviewService.list _)
        .expects()

    def approvalPageComplete(id: String,
                             pageUrl: String,
                             reviewType: String,
                             reviewInfo: ApprovalProcessPageReview
                             ): CallHandler[Future[RequestOutcome[Unit]]] = {
      (mockApprovalReviewService
        .approvalPageComplete(_: String, _: String, _: String, _: ApprovalProcessPageReview))
        .expects(id, pageUrl, reviewType, reviewInfo)
    }

    def checkProcessInCorrectStateForCompletion(id: String,
                                                reviewType: String
                                               ): CallHandler[Future[RequestOutcome[Approval]]] = {
      (mockApprovalReviewService
        .checkProcessInCorrectStateForCompletion(_: String, _: String))
        .expects(id, reviewType)
    }


  }
}
