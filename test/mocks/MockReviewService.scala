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

import models.{ApprovalProcessReview, RequestOutcome}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import services.ReviewService

import scala.concurrent.Future

trait MockReviewService extends MockFactory {
  val mockReviewService: ReviewService = mock[ReviewService]

  object MockReviewService {
    def approval2iReviewInfo(id: String): CallHandler[Future[RequestOutcome[ApprovalProcessReview]]] = {
      (mockReviewService
        .approval2iReviewInfo(_: String))
        .expects(id)
    }

  }
}
