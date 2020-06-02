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

import java.util.UUID

import models.{ApprovalProcessReview, RequestOutcome}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import repositories.ApprovalProcessReviewRepository
import utils.Constants._

import scala.concurrent.Future

trait MockApprovalProcessReviewRepository extends MockFactory {

  val mockApprovalProcessReviewRepository: ApprovalProcessReviewRepository = mock[ApprovalProcessReviewRepository]

  object MockApprovalProcessReviewRepository {

    def save(review: ApprovalProcessReview): CallHandler[Future[RequestOutcome[UUID]]] = {
      (mockApprovalProcessReviewRepository
        .save(_: ApprovalProcessReview))
        .expects(*)
    }

    def getByIdVersionAndType(id: String, version: Int = 1, reviewType: String = ReviewType2i): CallHandler[Future[RequestOutcome[ApprovalProcessReview]]] = {
      (mockApprovalProcessReviewRepository
        .getByIdVersionAndType(_: String, _: Int, _: String))
        .expects(id, version, reviewType)
    }

  }

}
