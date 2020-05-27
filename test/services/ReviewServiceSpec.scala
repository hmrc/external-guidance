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

package services

import base.UnitSpec
import data.ReviewData._
import mocks.MockApprovalRepository
import models.RequestOutcome
import models.errors._
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future

class ReviewServiceSpec extends UnitSpec with MockFactory {

  private trait Test extends MockApprovalRepository {

    lazy val service: ReviewService = new ReviewService(mockApprovalRepository)
  }

  "Calling the approval2iReviewInfo method" when {
    "there are entries to return" should {
      "return an ApprovalProcessReview object containing appropriate page info" in new Test {

        whenReady(service.approval2iReviewInfo(validId)) {
          case Right(entry) =>
            entry.id shouldBe processReviewInfo.id
            entry.title shouldBe processReviewInfo.title
            entry.pages.size shouldBe processReviewInfo.pages.size
          case _ => fail
        }
      }
    }

  }

  "Calling the changeStatus method" when {
    "the ID identifies a valid process" should {
      "return to indicate the process status was updated in the database" in new Test {

        val expected: RequestOutcome[Unit] = Right(())

        MockApprovalRepository
          .changeStatus("validId", statusChangeInfo)
          .returns(Future.successful(expected))

        whenReady(service.changeStatus("validId", statusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

    "the ID cannot be matched to a submitted process" should {
      "return a not found response" in new Test {

        val expected: RequestOutcome[Unit] = Left(Errors(NotFoundError))

        MockApprovalRepository
          .changeStatus("validId", statusChangeInfo)
          .returns(Future.successful(expected))

        whenReady(service.changeStatus("validId", statusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[Unit] = Left(Errors(DatabaseError))
        val expected: RequestOutcome[Unit] = Left(Errors(InternalServiceError))

        MockApprovalRepository
          .changeStatus("validId", statusChangeInfo)
          .returns(Future.successful(repositoryError))

        whenReady(service.changeStatus("validId", statusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }
  }
}
