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
import data.ReviewData
import mocks.{MockApprovalProcessReviewRepository, MockApprovalRepository, MockPublishedService}
import models.errors._
import models.{ApprovalProcessJson, ApprovalProcessStatusChange, RequestOutcome}
import org.scalamock.scalatest.MockFactory
import utils.Constants._

import scala.concurrent.Future

class ReviewServiceSpec extends UnitSpec with MockFactory with ReviewData with ApprovalProcessJson {

  private trait Test extends MockApprovalRepository with MockApprovalProcessReviewRepository with MockPublishedService {

    lazy val service: ReviewService = new ReviewService(mockPublishedService, mockApprovalRepository, mockApprovalProcessReviewRepository)
  }

  "Calling the approval2iReviewInfo method" when {
    "there are entries to return" should {
      "return an ApprovalProcessReview object containing appropriate page info" in new Test {

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType(validId)
          .returns(Future.successful(Right(approvalProcessReview)))

        whenReady(service.approval2iReviewInfo(validId)) {
          case Right(entry) =>
            entry.ocelotId shouldBe validId
            entry.title shouldBe approvalProcessReview.title
            entry.pages.size shouldBe 1
          case _ => fail
        }
      }
    }

    "the process cannot be found" should {
      "return a NotFoundError" in new Test {

        val expected: RequestOutcome[String] = Left(Errors(NotFoundError))

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Left(Errors(NotFoundError))))

        whenReady(service.approval2iReviewInfo(validId)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }

    "there is an error retrieving the process" should {
      "return an InternalServiceError" in new Test {

        val expected: RequestOutcome[String] = Left(Errors(InternalServiceError))

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Left(Errors(DatabaseError))))

        whenReady(service.approval2iReviewInfo(validId)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }

    "the review info cannot be found" should {
      "return a NotFoundError" in new Test {

        val expected: RequestOutcome[String] = Left(Errors(NotFoundError))

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType(validId)
          .returns(Future.successful(Left(Errors(NotFoundError))))

        whenReady(service.approval2iReviewInfo(validId)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }

    "there is a database error when retrieving the review info" should {
      "return an InternalServiceError" in new Test {

        val expected: RequestOutcome[String] = Left(Errors(InternalServiceError))

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType(validId)
          .returns(Future.successful(Left(Errors(DatabaseError))))

        whenReady(service.approval2iReviewInfo(validId)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }
  }

  "Calling the changeStatus method" when {
    "the ID identifies a valid process" when {
      "the status is submitted for 2i review" should {
        "indicate the process status was updated in the database" in new Test {

          val expected: RequestOutcome[Unit] = Right(())

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalRepository
            .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(expected))

          whenReady(service.changeStatus("validId", StatusSubmittedFor2iReview, statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }

      "the status is approved for publishing" should {
        "indicate the process status was updated and published in the database" in new Test {

          val expected: RequestOutcome[Unit] = Right(())
          val publishedStatusChangeInfo = ApprovalProcessStatusChange("user id", "user name", StatusApprovedForPublishing)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalRepository
            .changeStatus("validId", StatusPublished, "user id")
            .returns(Future.successful(expected))

          MockPublishedService
            .save("validId", approvalProcess.process)
            .returns(Future.successful(Right("validId")))

          whenReady(service.changeStatus("validId", StatusSubmittedFor2iReview, publishedStatusChangeInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the ID cannot be matched to a submitted process" when {
      "the getById fails to find the process" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[Unit] = Left(Errors(NotFoundError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Left(Errors(NotFoundError))))

          MockApprovalRepository
            .changeStatus("validId", StatusPublished, "userId")
            .returns(Future.successful(expected))
            .never()

          whenReady(service.changeStatus("validId", StatusSubmittedFor2iReview, statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the getById fails to find the process with the expected status" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[Unit] = Left(Errors(NotFoundError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalRepository
            .changeStatus("validId", StatusPublished, "userId")
            .returns(Future.successful(expected))
            .never()

          whenReady(service.changeStatus("validId", StatusSubmittedForFactCheck, statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the changeStatus fails to find the process" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[Unit] = Left(Errors(NotFoundError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalRepository
            .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(Left(Errors(NotFoundError))))

          whenReady(service.changeStatus("validId", StatusSubmittedFor2iReview, statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[Unit] = Left(Errors(DatabaseError))
        val expected: RequestOutcome[Unit] = Left(Errors(InternalServiceError))
        val publishedStatusChangeInfo = ApprovalProcessStatusChange("user id", "user name", StatusApprovedForPublishing)

        MockApprovalRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(repositoryError))

        whenReady(service.changeStatus("validId", StatusSubmittedFor2iReview, publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

    "the process fails to be published" should {
      "indicate a database error" in new Test {

        val expectedChangeStatusResponse: RequestOutcome[Unit] = Right(())
        val expected: RequestOutcome[Unit] = Left(Errors(InternalServiceError))
        val publishedStatusChangeInfo = ApprovalProcessStatusChange("user id", "user name", StatusApprovedForPublishing)

        MockApprovalRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(expectedChangeStatusResponse))

        MockPublishedService
          .save("validId", approvalProcess.process)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

        whenReady(service.changeStatus("validId", StatusSubmittedFor2iReview, publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

  }
}
