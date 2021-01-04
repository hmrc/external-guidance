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

import models._
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import services.ReviewService

import scala.concurrent.Future

trait MockReviewService extends MockFactory {
  val mockReviewService: ReviewService = mock[ReviewService]

  object MockReviewService {

    def approvalReviewInfo(id: String, reviewType: String): CallHandler[Future[RequestOutcome[ProcessReview]]] = {
      (mockReviewService
        .approvalReviewInfo(_: String, _: String))
        .expects(id, reviewType)
    }

    def twoEyeReviewComplete(id: String, statusInfo: ApprovalProcessStatusChange): CallHandler[Future[RequestOutcome[AuditInfo]]] = {
      (mockReviewService
        .twoEyeReviewComplete(_: String, _: ApprovalProcessStatusChange))
        .expects(id, statusInfo)
    }

    def approvalPageInfo(id: String, pageUrl: String, reviewType: String): CallHandler[Future[RequestOutcome[ApprovalProcessPageReview]]] = {
      (mockReviewService
        .approvalPageInfo(_: String, _: String, _: String))
        .expects(id, pageUrl, reviewType)
    }

    def factCheckComplete(id: String, statusInfo: ApprovalProcessStatusChange): CallHandler[Future[RequestOutcome[AuditInfo]]] = {
      (mockReviewService
        .factCheckComplete(_: String, _: ApprovalProcessStatusChange))
        .expects(id, statusInfo)
    }

    def approvalPageComplete(id: String,
                             pageUrl: String,
                             reviewType: String,
                             reviewInfo: ApprovalProcessPageReview
                             ): CallHandler[Future[RequestOutcome[Unit]]] = {
      (mockReviewService
        .approvalPageComplete(_: String, _: String, _: String, _: ApprovalProcessPageReview))
        .expects(id, pageUrl, reviewType, reviewInfo)
    }

    def checkProcessInCorrectStateForCompletion(id: String,
                                                reviewType: String
                                               ): CallHandler[Future[RequestOutcome[ApprovalProcess]]] = {
      (mockReviewService
        .checkProcessInCorrectStateForCompletion(_: String, _: String))
        .expects(id, reviewType)
    }

  }
}
