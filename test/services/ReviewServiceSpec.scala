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

import java.time.ZonedDateTime
import java.util.UUID

import base.UnitSpec
import data.ReviewData
import mocks.{MockApprovalProcessReviewRepository, MockApprovalRepository, MockPublishedService}
import models._
import models.errors._
import org.scalamock.scalatest.MockFactory
import org.scalatest.PrivateMethodTester
import utils.Constants._

import scala.concurrent.Future

class ReviewServiceSpec extends UnitSpec with MockFactory with ReviewData with ApprovalProcessJson {

  private trait Test extends MockApprovalRepository with MockApprovalProcessReviewRepository with MockPublishedService with PrivateMethodTester {

    lazy val service: ReviewService = new ReviewService(mockPublishedService, mockApprovalRepository, mockApprovalProcessReviewRepository)
  }

  private trait ReviewCompleteTest extends Test {
    val approvalProcessReviewComplete: ApprovalProcessReview = ApprovalProcessReview(
      UUID.randomUUID(),
      "validId",
      1,
      ReviewType2i,
      "This is the title",
      List(
        ApprovalProcessPageReview("1", "/pageUrl", "pageTitle1", Some("Yes"), ReviewCompleteStatus),
        ApprovalProcessPageReview("2", "/pageUrl2", "pageTitle2", Some("No"), ReviewCompleteStatus)
      )
    )

    val auditInfo: AuditInfo = AuditInfo("user id","oct90001","This is the title",1,"7903088",1589467563758L,6)
    val processReviewComplete: ProcessReview = ProcessReview(
      approvalProcessReviewComplete.id,
      approvalProcessReviewComplete.ocelotId,
      approvalProcessReviewComplete.version,
      approvalProcessReviewComplete.reviewType,
      approvalProcessReviewComplete.title,
      approvalProcessReviewComplete.lastUpdated,
      List(
        PageReview("1", "pageTitle1", "/pageUrl", ReviewCompleteStatus, Some("Yes")),
        PageReview("2", "pageTitle2", "/pageUrl2", ReviewCompleteStatus, Some("No"))
      )
    )
    val processReviewIncomplete: ApprovalProcessReview = ApprovalProcessReview(
      UUID.randomUUID(),
      "validId",
      1,
      ReviewType2i,
      "This is the title",
      List(
        ApprovalProcessPageReview("1", "/pageUrl", "/pageUrl", Some("Yes"), InitialPageReviewStatus),
        ApprovalProcessPageReview("2", "/pageUrl2", "/pageUrl2", Some("No"), ReviewCompleteStatus)
      )
    )

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

        val expected: RequestOutcome[String] = Left(NotFoundError)

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Left(NotFoundError)))

        whenReady(service.approvalReviewInfo(validId, ReviewTypeFactCheck)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }

    "there is an error retrieving the process" should {
      "return an InternalServiceError" in new Test {

        val expected: RequestOutcome[String] = Left(InternalServiceError)

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(service.approvalReviewInfo(validId, ReviewType2i)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }

    "the review info cannot be found" should {
      "return a NotFoundError" in new Test {

        val expected: RequestOutcome[String] = Left(NotFoundError)

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType(validId, ReviewTypeFactCheck)
          .returns(Future.successful(Left(NotFoundError)))

        whenReady(service.approvalReviewInfo(validId, ReviewTypeFactCheck)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail
        }
      }
    }

    "there is a database error when retrieving the review info" should {
      "return an InternalServiceError" in new Test {

        val expected: RequestOutcome[String] = Left(InternalServiceError)

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType(validId, ReviewType2i)
          .returns(Future.successful(Left(DatabaseError)))

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
        "indicate the process status was updated in the database" in new ReviewCompleteTest {

          val expected: RequestOutcome[AuditInfo] = Right(auditInfo)

          MockApprovalRepository
            .getById(validId)
            .returns(Future.successful(Right(approvalProcessWithValidProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType(validId, ReviewType2i, approvalProcessWithValidProcess.version)
            .returns(Future.successful(Right(approvalProcessReviewComplete)))

          MockApprovalProcessReviewRepository
            .updateReview(validId, approvalProcessWithValidProcess.version, ReviewType2i, statusChange2iReviewInfo.userId, statusChange2iReviewInfo.status)
            .returns(Future.successful(Right(())))

          MockApprovalRepository
            .changeStatus(validId, StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(Right(())))

          whenReady(service.twoEyeReviewComplete(validId, statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }

      "the status is published" should {
        "indicate the process status was updated and published in the database" in new ReviewCompleteTest {

          val expected: RequestOutcome[AuditInfo] = Right(auditInfo)
          val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcessWithValidProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewType2i, approvalProcessWithValidProcess.version)
            .returns(Future.successful(Right(approvalProcessReviewComplete)))

          MockApprovalProcessReviewRepository
            .updateReview("validId", approvalProcessWithValidProcess.version, ReviewType2i, statusChange2iReviewInfo.userId, StatusPublished)
            .returns(Future.successful(Right(())))

          MockApprovalRepository
            .changeStatus("validId", StatusPublished, "user id")
            .returns(Future.successful(Right(())))

          MockPublishedService
            .save("validId", publishedStatusChangeInfo.userId, "processCode", approvalProcessWithValidProcess.process)
            .returns(Future.successful(Right("validId")))

          whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
            result shouldBe expected
          }
        }
      }

      "all the pages have not been reviewed" should {
        "indicate that the data was incomplete" in new ReviewCompleteTest {

          val expected: RequestOutcome[ApprovalProcess] = Left(IncompleteDataError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewType2i, approvalProcess.version)
            .returns(Future.successful(Right(processReviewIncomplete)))

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the ID cannot be matched to a submitted process" when {
      "the getById fails to find the process" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[ApprovalProcess] = Left(NotFoundError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the getById fails to find the process with the expected status" should {
        "return a stale data response" in new Test {

          val expected: RequestOutcome[ApprovalProcess] = Left(StaleDataError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess.copy(meta = approvalProcess.meta.copy(status = StatusSubmittedForFactCheck)))))

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the updateReview fails to update the details" should {
        "return a not found response" in new ReviewCompleteTest {

          val expected: RequestOutcome[ApprovalProcess] = Left(DatabaseError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewType2i, approvalProcess.version)
            .returns(Future.successful(Right(approvalProcessReviewComplete)))

          MockApprovalProcessReviewRepository
            .updateReview("validId", approvalProcess.version, ReviewType2i, statusChange2iReviewInfo.userId, statusChange2iReviewInfo.status)
            .returns(Future.successful(Left(DatabaseError)))

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the changeStatus fails to find the process" should {
        "return a not found response" in new ReviewCompleteTest {

          val expected: RequestOutcome[ApprovalProcess] = Left(NotFoundError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .updateReview("validId", approvalProcess.version, ReviewType2i, statusChange2iReviewInfo.userId, statusChange2iReviewInfo.status)
            .returns(Future.successful(Right(())))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewType2i, approvalProcess.version)
            .returns(Future.successful(Right(approvalProcessReviewComplete)))

          MockApprovalRepository
            .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the approval repository reports a database error" should {
      "return an internal server error" in new ReviewCompleteTest {

        val repositoryError: RequestOutcome[Unit] = Left(DatabaseError)
        val expected: RequestOutcome[ApprovalProcess] = Left(InternalServiceError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType("validId", ReviewType2i, approvalProcess.version)
          .returns(Future.successful(Right(approvalProcessReviewComplete)))

        MockApprovalProcessReviewRepository
          .updateReview("validId", approvalProcess.version, ReviewType2i, statusChange2iReviewInfo.userId, StatusPublished)
          .returns(Future.successful(Right(())))

        MockApprovalRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(repositoryError))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }
    "the approval repository reports an unknown error" should {
      "return an internal server error" in new ReviewCompleteTest {

        val repositoryError: RequestOutcome[Unit] = Left(InternalServiceError)
        val expected: RequestOutcome[ApprovalProcess] = Left(InternalServiceError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType("validId", ReviewType2i, approvalProcess.version)
          .returns(Future.successful(Right(approvalProcessReviewComplete)))

        MockApprovalProcessReviewRepository
          .updateReview("validId", approvalProcess.version, ReviewType2i, statusChange2iReviewInfo.userId, StatusPublished)
          .returns(Future.successful(Right(())))

        MockApprovalRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(repositoryError))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

    "the process fails to be published" should {
      "indicate a database error" in new ReviewCompleteTest {

        val expectedChangeStatusResponse: RequestOutcome[Unit] = Right(())
        val expected: RequestOutcome[ApprovalProcess] = Left(InternalServiceError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType("validId", ReviewType2i, approvalProcess.version)
          .returns(Future.successful(Right(approvalProcessReviewComplete)))

        MockApprovalProcessReviewRepository
          .updateReview("validId", approvalProcess.version, ReviewType2i, statusChange2iReviewInfo.userId, StatusPublished)
          .returns(Future.successful(Right(())))

        MockApprovalRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(expectedChangeStatusResponse))

        MockPublishedService
          .save("validId", publishedStatusChangeInfo.userId, "processCode", approvalProcess.process)
          .returns(Future.successful(Left(InternalServiceError)))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

  }

  "Calling the approvalPageInfo method" when {

    trait PageInfoTest extends Test {
      val currentDateTime: ZonedDateTime = ZonedDateTime.now()
      val processReview: ApprovalProcessReview = ApprovalProcessReview(
        UUID.randomUUID(),
        "validId",
        1,
        ReviewType2i,
        "This is the title",
        List(
          ApprovalProcessPageReview("1", "/pageUrl", "/pageUrl", Some("result1")),
          ApprovalProcessPageReview("2", "/pageUrl2", "/pageUrl2", Some("NotStarted"), updateDate = currentDateTime))
      )

    }

    "the ID identifies a valid process" when {
      "the pageUrl exists in the process" should {
        "return a populated PageReview" in new PageInfoTest {

          val expected: RequestOutcome[ApprovalProcessPageReview] =
            Right(ApprovalProcessPageReview("2", "/pageUrl2", "/pageUrl2", Some("NotStarted"), updateDate = currentDateTime))

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

          val expected: RequestOutcome[PageReview] = Left(NotFoundError)

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

          val expected: RequestOutcome[Unit] = Left(NotFoundError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewType2i)) { result =>
            result shouldBe expected
          }
        }
      }

      "the getById method returns a DatabaseError" should {
        "return an InternalServiceError" in new PageInfoTest {

          val expected: RequestOutcome[Unit] = Left(InternalServiceError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Left(DatabaseError)))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewTypeFactCheck)) { result =>
            result shouldBe expected
          }
        }
      }
      "the approval2iReviewPageInfo fails to find the process review info" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[Unit] = Left(NotFoundError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewType2i)
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewType2i)) { result =>
            result shouldBe expected
          }
        }
      }

      "the review repository reports a database error" should {
        "return an internal server error" in new PageInfoTest {

          val expected: RequestOutcome[ApprovalProcessReview] = Left(InternalServiceError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck)
            .returns(Future.successful(Left(DatabaseError)))

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
        ApprovalProcessPageReview(validId, pageUrl, "title", Some("result1"), "Failed", None, ZonedDateTime.now, Some("user"))
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

          MockApprovalRepository
            .changeStatus(validId, StatusInProgress, pageReview.updateUser.get)
            .returns(Future.successful(Right(())))

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewType2i, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the ID cannot be matched to a submitted process" when {
      "the getById fails to find the process" should {
        "return a not found response" in new PageCompleteTest {

          val expected: RequestOutcome[Unit] = Left(NotFoundError)

          MockApprovalRepository
            .getById(validId)
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewType2i, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
      "the updatePageReview fails to find the pageUrl" should {
        "return a not found response" in new PageCompleteTest {

          val expected: RequestOutcome[Unit] = Left(NotFoundError)

          MockApprovalRepository
            .getById(validId)
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .updatePageReview(validId, 1, pageUrl, ReviewTypeFactCheck, pageReview)
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewTypeFactCheck, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "A problem with the database server" when {
      "the getById returns a DatabaseError" should {
        "return an internal server error" in new PageCompleteTest {

          val repositoryError: RequestOutcome[ApprovalProcess] = Left(DatabaseError)
          val expected: RequestOutcome[Unit] = Left(InternalServiceError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(repositoryError))

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewTypeFactCheck, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
      "the updatePageReview returns a DatabaseError" should {
        "return an internal server error" in new PageCompleteTest {

          val repositoryError: RequestOutcome[Unit] = Left(DatabaseError)
          val expected: RequestOutcome[Unit] = Left(InternalServiceError)

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

    val factCheckMeta = approvalProcessWithValidProcess.meta.copy(status = StatusSubmitted, reviewType = ReviewTypeFactCheck)
    val factCheckProcess = approvalProcessWithValidProcess.copy(meta = factCheckMeta)

    "the ID identifies a valid process" when {
      "the status is submitted for fact check" should {
        "indicate the process status was updated in the database" in new ReviewCompleteTest {

          val expected: RequestOutcome[AuditInfo] = Right(auditInfo)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck, approvalProcessWithValidProcess.version)
            .returns(Future.successful(Right(approvalProcessReviewComplete)))

          MockApprovalProcessReviewRepository
            .updateReview("validId",
                          approvalProcessWithValidProcess.version,
                          ReviewTypeFactCheck,
                          statusChange2iReviewInfo.userId,
                          statusChange2iReviewInfo.status)
            .returns(Future.successful(Right(())))

          MockApprovalRepository
            .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(Right(())))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }

      "all the pages have not been reviewed" should {
        "indicate that the data was incomplete" in new ReviewCompleteTest {

          val expected: RequestOutcome[ApprovalProcess] = Left(IncompleteDataError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck, approvalProcess.version)
            .returns(Future.successful(Right(processReviewIncomplete)))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the ID cannot be matched to a submitted process" when {
      "the getById fails to find the process" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[ApprovalProcess] = Left(NotFoundError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the getById fails to find the process with the expected status" should {
        "return a stale data response" in new Test {

          val expected: RequestOutcome[ApprovalProcess] = Left(StaleDataError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess.copy(meta = factCheckProcess.meta.copy(status = StatusPublished)))))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck)
            .returns(Future.successful(Right(approvalProcessReview.copy(reviewType = ReviewTypeFactCheck))))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the updateReview fails to update the details" should {
        "return a not found response" in new ReviewCompleteTest {

          val expected: RequestOutcome[ApprovalProcess] = Left(NotFoundError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck, approvalProcess.version)
            .returns(Future.successful(Right(approvalProcessReviewComplete)))

          MockApprovalProcessReviewRepository
            .updateReview("validId", factCheckProcess.version, ReviewTypeFactCheck, statusChange2iReviewInfo.userId, statusChange2iReviewInfo.status)
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the changeStatus fails to find the process" should {
        "return a not found response" in new ReviewCompleteTest {

          val expected: RequestOutcome[ApprovalProcess] = Left(NotFoundError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType("validId", ReviewTypeFactCheck, approvalProcess.version)
            .returns(Future.successful(Right(approvalProcessReviewComplete)))

          MockApprovalProcessReviewRepository
            .updateReview("validId", approvalProcess.version, ReviewTypeFactCheck, statusChange2iReviewInfo.userId, statusChange2iReviewInfo.status)
            .returns(Future.successful(Right(())))

          MockApprovalRepository
            .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new ReviewCompleteTest {

        val repositoryError: RequestOutcome[Unit] = Left(DatabaseError)
        val expected: RequestOutcome[ApprovalProcess] = Left(InternalServiceError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusWithDesignerForUpdate)

        MockApprovalRepository
          .getById("validId")
          .returns(Future.successful(Right(factCheckProcess)))

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType("validId", ReviewTypeFactCheck, approvalProcess.version)
          .returns(Future.successful(Right(approvalProcessReviewComplete)))

        MockApprovalProcessReviewRepository
          .updateReview("validId", approvalProcess.version, ReviewTypeFactCheck, statusChange2iReviewInfo.userId, statusChange2iReviewInfo.status)
          .returns(Future.successful(Right(())))

        MockApprovalRepository
          .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
          .returns(Future.successful(repositoryError))

        whenReady(service.factCheckComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

  }

  "Invoking private method getReviewInfo" when {
    "the getByIdAndVersion method returns a valid result" should {
      "return a valid ProcessReview" in new ReviewCompleteTest {

        val expected: RequestOutcome[ProcessReview] = Right(processReviewComplete)

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType(validId, ReviewType2i)
          .returns(Future.successful(Right(approvalProcessReviewComplete)))

        private val getReviewInfoPrivateMethod = PrivateMethod[Future[RequestOutcome[ProcessReview]]]('getReviewInfo)

        whenReady(service invokePrivate getReviewInfoPrivateMethod(validId, ReviewType2i, 1)) { result =>
          result shouldBe expected
        }
      }
    }
    "the getByIdAndVersion method returns a NotFoundError" should {
      "return a NotFoundError" in new ReviewCompleteTest {

        val expected: RequestOutcome[ProcessReview] = Left(NotFoundError)

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType(validId, ReviewType2i)
          .returns(Future.successful(Left(NotFoundError)))

        private val getReviewInfoPrivateMethod = PrivateMethod[Future[RequestOutcome[ProcessReview]]]('getReviewInfo)

        whenReady(service invokePrivate getReviewInfoPrivateMethod(validId, ReviewType2i, 1)) { result =>
          result shouldBe expected
        }
      }
    }
    "the getByIdAndVersion method returns a DatabaseError" should {
      "return an InternalServiceError" in new ReviewCompleteTest {

        val expected: RequestOutcome[ProcessReview] = Left(InternalServiceError)

        MockApprovalProcessReviewRepository
          .getByIdVersionAndType(validId, ReviewType2i)
          .returns(Future.successful(Left(DatabaseError)))

        private val getReviewInfoPrivateMethod = PrivateMethod[Future[RequestOutcome[ProcessReview]]]('getReviewInfo)

        whenReady(service invokePrivate getReviewInfoPrivateMethod(validId, ReviewType2i, 1)) { result =>
          result shouldBe expected
        }
      }
    }
  }

  "Invoking method checkProcessInCorrectStateForCompletion" when {
    "the getContentToUpdate method returns a valid result" when {
      "the getReviewInfo method returns an error" when {
        "return a valid ProcessReview" in new ReviewCompleteTest {

          val expected: RequestOutcome[ApprovalProcess] = Left(NotFoundError)

          MockApprovalRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .getByIdVersionAndType(validId, ReviewType2i)
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.checkProcessInCorrectStateForCompletion(validId, ReviewType2i)) { result =>
            result shouldBe expected
          }
        }
      }
    }
  }
}
