/*
 * Copyright 2023 HM Revenue & Customs
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

import java.util.UUID
import core.models.RequestOutcome
import models.{ApprovalProcessPageReview, ApprovalProcessReview}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import repositories.ApprovalProcessReviewRepository

import scala.concurrent.Future

trait MockApprovalProcessReviewRepository extends MockFactory {

  val mockApprovalProcessReviewRepository: ApprovalProcessReviewRepository = mock[ApprovalProcessReviewRepository]

  object MockApprovalProcessReviewRepository {

    def save(review: ApprovalProcessReview): CallHandler[Future[RequestOutcome[UUID]]] = {
      (mockApprovalProcessReviewRepository
        .save(_: ApprovalProcessReview))
        .expects(*)
    }

    def getByIdVersionAndType(id: String, reviewType: String, version: Int = 1): CallHandler[Future[RequestOutcome[ApprovalProcessReview]]] = {
      (mockApprovalProcessReviewRepository
        .getByIdVersionAndType(_: String, _: Int, _: String))
        .expects(id, version, reviewType)
    }

    def updatePageReview(id: String,
                         version: Int,
                         pageUrl: String,
                         reviewType: String,
                         reviewInfo: ApprovalProcessPageReview
                        ): CallHandler[Future[RequestOutcome[Unit]]] = {
      (mockApprovalProcessReviewRepository
        .updatePageReview(_: String, _: Int, _: String, _: String, _: ApprovalProcessPageReview))
        .expects(id, version, pageUrl, reviewType, reviewInfo)
    }

    def updateReview(id: String,
                     version: Int,
                     reviewType: String,
                     updateUser: String,
                     result: String
                    ): CallHandler[Future[RequestOutcome[Unit]]] = {
      (mockApprovalProcessReviewRepository
        .updateReview(_: String, _: Int, _: String, _: String, _: String))
        .expects(id, version, reviewType, updateUser, result)

    }

  }

}
