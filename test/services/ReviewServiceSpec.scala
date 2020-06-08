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

import java.time.LocalDateTime
import java.util.UUID

import base.UnitSpec
import data.ReviewData
import mocks.{MockApprovalProcessReviewRepository, MockApprovalRepository, MockPublishedService}
import models._
import models.errors._
import org.scalamock.scalatest.MockFactory
import utils.Constants._

import scala.concurrent.Future

class ReviewServiceSpec extends UnitSpec with MockFactory with ReviewData with ApprovalProcessJson {

  private trait Test extends MockApprovalRepository with MockApprovalProcessReviewRepository with MockPublishedService {

    lazy val service: ReviewService = new ReviewService(mockPublishedService, mockApprovalRepository, mockApprovalProcessReviewRepository)
  }

  "Calling the approvalReviewInfo method" when {
    "there are entries to return" should {
      "return an ApprovalProcessReview object containing appropriate page info" in new Test {

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType(validId, ReviewType2i)
          .returns(Future.successful(Right(approvalProcessReview)))

        whenReady(service.approvalReviewInfo(validId, ReviewType2i)) {
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

        whenReady(service.approvalReviewInfo(validId, ReviewTypeFactCheck)) {
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

        whenReady(service.approvalReviewInfo(validId, ReviewType2i)) {
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
          .getByIdVersionAndType(validId, ReviewTypeFactCheck)
          .returns(Future.successful(Left(Errors(NotFoundError))))

        whenReady(service.approvalReviewInfo(validId, ReviewTypeFactCheck)) {
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
          .getByIdVersionAndType(validId, ReviewType2i)
          .returns(Future.successful(Left(Errors(DatabaseError))))

        whenReady(service.approvalReviewInfo(validId, ReviewType2i)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }
  }

  "Calling the twoEyeReviewComplete method" when {
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

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }

      "the status is published" should {
        "indicate the process status was updated and published in the database" in new Test {

          val expected: RequestOutcome[Unit] = Right(())
          val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalRepository
            .changeStatus("validId", StatusPublished, "user id")
            .returns(Future.successful(expected))

          MockPublishedService
            .save("validId", approvalProcess.process)
            .returns(Future.successful(Right("validId")))

          whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
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

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the getById fails to find the process with the expected status" should {
        "return a stale data response" in new Test {

          val expected: RequestOutcome[Unit] = Left(Errors(StaleDataError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess.copy(meta = approvalProcess.meta.copy(status = StatusSubmittedForFactCheck)))))

          MockApprovalRepository
            .changeStatus("validId", StatusPublished, "userId")
            .returns(Future.successful(expected))
            .never()

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
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

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[Unit] = Left(Errors(DatabaseError))
        val expected: RequestOutcome[Unit] = Left(Errors(InternalServiceError))
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(repositoryError))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

    "the process fails to be published" should {
      "indicate a database error" in new Test {

        val expectedChangeStatusResponse: RequestOutcome[Unit] = Right(())
        val expected: RequestOutcome[Unit] = Left(Errors(InternalServiceError))
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(expectedChangeStatusResponse))

        MockPublishedService
          .save("validId", approvalProcess.process)
          .returns(Future.successful(Left(Errors(InternalServiceError))))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

  }

  "Calling the approvalPageInfo method" when {

    trait PageInfoTest extends Test {
      val processReview: ApprovalProcessReview = ApprovalProcessReview(
        UUID.randomUUID(),
        "validId",
        1,
        ReviewType2i,
        "This is the title",
        List(ApprovalProcessPageReview("1", "/pageUrl", Some("result1")), ApprovalProcessPageReview("2", "/pageUrl2", Some("NotStarted")))
      )

    }

    "the ID identifies a valid process" when {
      "the pageUrl exists in the process" should {
        "return a populated PageReview" in new PageInfoTest {

          val expected: RequestOutcome[ApprovalProcessPageReview] = Right(ApprovalProcessPageReview("2", "/pageUrl2", Some("NotStarted")))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck)
            .returns(Future.successful(Right(processReview)))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewTypeFactCheck)) { result =>
            result shouldBe expected
          }
        }
      }
      "the pageUrl does not exist in the process" should {
        "return a NotFound error" in new PageInfoTest {

          val expected: RequestOutcome[PageReview] = Left(Errors(NotFoundError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck)
            .returns(Future.successful(Right(processReview)))

          whenReady(service.approvalPageInfo("validId", "/pageUrl26", ReviewTypeFactCheck)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the ID cannot be matched to an approval process" when {
      "the getById fails to find the process" should {
        "return a not found response" in new PageInfoTest {

          val expected: RequestOutcome[Unit] = Left(Errors(NotFoundError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Left(Errors(NotFoundError))))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewType2i)
            .never()

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewType2i)) { result =>
            result shouldBe expected
          }
        }
      }

      "the getById method returns a DatabaseError" should {
        "return an InternalServiceError" in new PageInfoTest {

          val expected: RequestOutcome[Unit] = Left(Errors(InternalServiceError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Left(Errors(DatabaseError))))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck)
            .never()

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewTypeFactCheck)) { result =>
            result shouldBe expected
          }
        }
      }
      "the approval2iReviewPageInfo fails to find the process review info" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[Unit] = Left(Errors(NotFoundError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewType2i)
            .returns(Future.successful(Left(Errors(NotFoundError))))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewType2i)) { result =>
            result shouldBe expected
          }
        }
      }

      "the review repository reports a database error" should {
        "return an internal server error" in new PageInfoTest {

          val expected: RequestOutcome[ApprovalProcessReview] = Left(Errors(InternalServiceError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck)
            .returns(Future.successful(Left(Errors(DatabaseError))))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewTypeFactCheck)) { result =>
            result shouldBe expected
          }
        }
      }
    }
  }

  "Calling the approvalPageComplete method" when {

    trait PageCompleteTest extends Test {

      val pageUrl = "/pageUrl"
      val validId = "oct90001"

      val pageReview: ApprovalProcessPageReview =
        ApprovalProcessPageReview(validId, pageUrl, Some("result1"), "Failed", None, LocalDateTime.now, Some("user"))
    }

    "the ID identifies a valid process" when {
      "the pageUrl is found in the process review info" should {
        "indicate the process status was updated in the database" in new PageCompleteTest {

          val expected: RequestOutcome[Unit] = Right(())

          MockApprovalRepository
            .getById(validId)
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .updatePageReview(validId, 1, pageUrl, ReviewType2i, pageReview)
            .returns(Future.successful(expected))

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewType2i, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the ID cannot be matched to a submitted process" when {
      "the getById fails to find the process" should {
        "return a not found response" in new PageCompleteTest {

          val expected: RequestOutcome[Unit] = Left(Errors(NotFoundError))

          MockApprovalRepository
            .getById(validId)
            .returns(Future.successful(Left(Errors(NotFoundError))))

          MockApprovalProcessReviewRepository
            .updatePageReview(validId, 1, pageUrl, ReviewType2i, pageReview)
            .never()

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewType2i, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
      "the updatePageReview fails to find the pageUrl" should {
        "return a not found response" in new PageCompleteTest {

          val expected: RequestOutcome[Unit] = Left(Errors(NotFoundError))

          MockApprovalRepository
            .getById(validId)
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .updatePageReview(validId, 1, pageUrl, ReviewTypeFactCheck, pageReview)
            .returns(Future.successful(Left(Errors(NotFoundError))))

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewTypeFactCheck, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "A problem with the database server" when {
      "the getById returns a DatabaseError" should {
        "return an internal server error" in new PageCompleteTest {

          val repositoryError: RequestOutcome[ApprovalProcess] = Left(Errors(DatabaseError))
          val expected: RequestOutcome[Unit] = Left(Errors(InternalServiceError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(repositoryError))

          MockApprovalProcessReviewRepository
            .updatePageReview(validId, 1, pageUrl, ReviewTypeFactCheck, pageReview)
            .never()

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewTypeFactCheck, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
      "the updatePageReview returns a DatabaseError" should {
        "return an internal server error" in new PageCompleteTest {

          val repositoryError: RequestOutcome[Unit] = Left(Errors(DatabaseError))
          val expected: RequestOutcome[Unit] = Left(Errors(InternalServiceError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .updatePageReview(validId, 1, pageUrl, ReviewType2i, pageReview)
            .returns(Future.successful(repositoryError))

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewType2i, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
    }
  }

  "Calling the factCheckComplete method" when {

    val factCheckProcess = approvalProcess.copy(meta = approvalProcess.meta.copy(status = StatusSubmittedForFactCheck))

    "the ID identifies a valid process" when {
      "the status is submitted for 2i review" should {
        "indicate the process status was updated in the database" in new Test {

          val expected: RequestOutcome[Unit] = Right(())

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess)))

          MockApprovalRepository
            .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(expected))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
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

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the getById fails to find the process with the expected status" should {
        "return a stale data response" in new Test {

          val expected: RequestOutcome[Unit] = Left(Errors(StaleDataError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the changeStatus fails to find the process" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[Unit] = Left(Errors(NotFoundError))

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess)))

          MockApprovalRepository
            .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(Left(Errors(NotFoundError))))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[Unit] = Left(Errors(DatabaseError))
        val expected: RequestOutcome[Unit] = Left(Errors(InternalServiceError))
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusWithDesignerForUpdate)

        MockApprovalRepository
          .getById("validId")
          .returns(Future.successful(Right(factCheckProcess)))

        MockApprovalRepository
          .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
          .returns(Future.successful(repositoryError))

        whenReady(service.factCheckComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

  }

}
