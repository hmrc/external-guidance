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

package services

import java.time.ZonedDateTime
import core.services.{Timescales, PageBuilder}
import base.BaseSpec
import data.ReviewData
import mocks.{MockAppConfig, MockApprovalsRepository, MockPublishedRepository, MockPublishedService}
import models._
import core.models.errors._
import core.models.ocelot.ProcessJson
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsArray, JsObject, Json, OFormat}
import models.Constants._
import core.models.RequestOutcome
import scala.concurrent.Future
import core.services.{EncrypterService, DefaultTodayProvider}
import mocks.MockTimescalesService

class ApprovalReviewServiceSpec extends BaseSpec with ReviewData with MockFactory with ApprovalProcessJson {

  private trait Test extends MockApprovalsRepository
    with MockPublishedRepository
    with MockPublishedService
    with MockTimescalesService
    with ApprovalProcessJson
    with ProcessJson {

    val invalidId: String = "ext9005"

    val invalidProcess: JsObject = Json.obj("idx" -> invalidId)
    val fsService = new ProcessFinalisationService(
                          MockAppConfig, 
                          new ValidatingPageBuilder(
                            new PageBuilder(new Timescales(new DefaultTodayProvider))
                          ),
                          mockTimescalesService,
                          new EncrypterService(MockAppConfig))
    val service = new ApprovalReviewService(mockApprovalsRepository,
                          mockPublishedRepository,
                          mockPublishedService,
                          fsService
                          )(ec, MockAppConfig)

    val publishedProcessSuccessResponse: RequestOutcome[PublishedProcess] =
      Right(PublishedProcess(validId, 1, ZonedDateTime.now(), JsObject.empty, "publishedBy", approvalProcess.meta.processCode))
    val publishedProcessFailureResponse: RequestOutcome[PublishedProcess] =
      Right(PublishedProcess("random-id", 1, ZonedDateTime.now(), JsObject.empty, "publishedBy", approvalProcess.meta.processCode))

  }

  "Calling the getById method" when {
    "the ID identifies a valid process" should {
      "return a JSON representing the submitted ocelot process" in new Test {

        val returnFromRepo: RequestOutcome[Approval] = Right(approvalProcessWithValidProcess)
        val expected: RequestOutcome[JsObject] = Right(approvalProcessWithValidProcess.process)

        MockApprovalsRepository
          .getById(validId)
          .returns(Future.successful(returnFromRepo))

        whenReady(service.getById(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the ID cannot be matched to a submitted process" should {
      "return a not found response" in new Test {

        val expected: RequestOutcome[Approval] = Left(NotFoundError)

        MockApprovalsRepository
          .getById(validId)
          .returns(Future.successful(expected))

        whenReady(service.getById(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[Approval] = Left(DatabaseError)
        val expected: RequestOutcome[JsObject] = Left(InternalServerError)

        MockApprovalsRepository
          .getById(validId)
          .returns(Future.successful(repositoryError))

        whenReady(service.getById(validId)) { result =>
          result shouldBe expected
        }
      }
    }
  }

  "Calling the getByProcessCode method" when {
    "the ID identifies a valid process" should {
      "return a JSON representing the submitted ocelot process" in new Test {

        val returnFromRepo: RequestOutcome[Approval] = Right(approvalProcess)
        val expected: RequestOutcome[JsObject] = Right(approvalProcess.process)

        MockApprovalsRepository
          .getByProcessCode(validId)
          .returns(Future.successful(returnFromRepo))

        whenReady(service.getByProcessCode(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the ID cannot be matched to a submitted process" should {
      "return a not found response" in new Test {

        val expected: RequestOutcome[Approval] = Left(NotFoundError)

        MockApprovalsRepository
          .getByProcessCode(validId)
          .returns(Future.successful(expected))

        whenReady(service.getByProcessCode(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[Approval] = Left(DatabaseError)
        val expected: RequestOutcome[JsObject] = Left(InternalServerError)

        MockApprovalsRepository
          .getByProcessCode(validId)
          .returns(Future.successful(repositoryError))

        whenReady(service.getByProcessCode(validId)) { result =>
          result shouldBe expected
        }
      }
    }
  }

  "Calling the save method" when {

    "the id and JSON are valid" should {
      "return valid Id" in new Test {

        val expected: RequestOutcome[String] = Right(validId)
        MockPublishedRepository
          .getByProcessCode("cup-of-tea")
          .returns(Future.successful(publishedProcessSuccessResponse))

        MockApprovalsRepository
          .createOrUpdate(approvalProcess)
          .returns(Future.successful(expected))

        MockApprovalsRepository
          .getById("oct90001")
          .returns(Future.successful(Right(approvalProcess)))

        whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
          case Right(id) => id shouldBe validId
          case Left(err) => fail(s"Failed with $err")
        }
      }
    }

    "the processCode exists for another process in the publishedCollection" should {
      "return a DuplicateKeyException" in new Test {

        MockPublishedRepository
          .getByProcessCode("cup-of-tea")
          .returns(Future.successful(publishedProcessFailureResponse))

        whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
          case Left(DuplicateKeyError) => succeed
          case _ => fail()
        }
      }
    }

    "the processCode exists for another process in the approvedCollection" should {
      "return a DuplicateKeyException" in new Test {

        val expected: RequestOutcome[String] = Left(DuplicateKeyError)
        MockPublishedRepository
          .getByProcessCode("cup-of-tea")
          .returns(Future.successful(publishedProcessSuccessResponse))

        MockApprovalsRepository
          .createOrUpdate(approvalProcess)
          .returns(Future.successful(expected))

        whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
          case Left(DuplicateKeyError) => succeed
          case _ => fail()
        }
      }
    }

    "the process is saved" when {
      "the save of review data fails" should {
        "return an internal error" in new Test {

          val expected: RequestOutcome[String] = Left(InternalServerError)

          MockPublishedRepository
            .getByProcessCode("cup-of-tea")
            .returns(Future.successful(publishedProcessSuccessResponse))

          MockApprovalsRepository
            .createOrUpdate(approvalProcess)
            .returns(Future.successful(Left(InternalServerError)))

          // MockApprovalsRepository
          //   .getById("oct90001")
          //   .returns(Future.successful(Right(approvalProcess)))

          whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
            case result @ Left(_) => result shouldBe expected
            case _ => fail()
          }
        }
      }
    }

    "the process is saved" when {
      // "the subsequent get of the process fails with a NotFoundError" should {
      //   "return an internal error" in new Test {

      //     val expected: RequestOutcome[String] = Left(NotFoundError)

      //     MockPublishedRepository
      //       .getByProcessCode("cup-of-tea")
      //       .returns(Future.successful(publishedProcessSuccessResponse))

      //     MockApprovalsRepository
      //       .createOrUpdate(approvalProcess)
      //       .returns(Future.successful(Right(validId)))

      //     MockApprovalsRepository
      //       .getById("oct90001")
      //       .returns(Future.successful(Left(NotFoundError)))

      //     whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
      //       case result @ Left(_) => result shouldBe expected
      //       case _ => fail()
      //     }
      //   }
      // }

      // "the subsequent get of the process fails with a DatabaseError" should {
      //   "return an internal error" in new Test {

      //     val expected: RequestOutcome[String] = Left(InternalServerError)

      //     MockPublishedRepository
      //       .getByProcessCode("cup-of-tea")
      //       .returns(Future.successful(publishedProcessSuccessResponse))

      //     MockApprovalsRepository
      //       .createOrUpdate(approvalProcess)
      //       .returns(Future.successful(Right(validId)))

      //     MockApprovalsRepository
      //       .getById("oct90001")
      //       .returns(Future.successful(Left(DatabaseError)))

      //     whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
      //       case result @ Left(_) => result shouldBe expected
      //       case _ => fail()
      //     }
      //   }
      // }
    }

    "the JSON is invalid" should {
      "not call the repository" in new Test {
        MockApprovalsRepository
          .createOrUpdate(approvalProcess)
          .never()

        service.save(invalidProcess, ReviewType2i, StatusSubmittedFor2iReview)
      }

      "return a HTTP 422 error" in new Test {
        whenReady(service.save(invalidProcess, ReviewType2i, StatusSubmittedFor2iReview)) {
          case result @ Left(err) if err.code == Error.UnprocessableEntity => succeed
          case _ => fail()
        }
      }
    }

    "a database error occurs" should {
      "return an internal error" in new Test {
        val repositoryResponse: RequestOutcome[String] = Left(DatabaseError)
        val expected: RequestOutcome[String] = Left(InternalServerError)

        MockPublishedRepository
          .getByProcessCode("cup-of-tea")
          .returns(Future.successful(publishedProcessSuccessResponse))

        MockApprovalsRepository
          .createOrUpdate(approvalProcess)
          .returns(Future.successful(repositoryResponse))

        whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail()
        }
      }
    }
  }

  "Calling the approvalSummaryList method" when {
    "there are entries to return" should {
      "return a List of approval processes" in new Test {
        implicit val formats: OFormat[ApprovalProcessSummary] = Json.format[ApprovalProcessSummary]

        val expected: RequestOutcome[List[ApprovalProcessSummary]] = Right(List(approvalProcessSummary))

        MockApprovalsRepository
          .approvalSummaryList(List("FactChecker"))
          .returns(Future.successful(expected))

        whenReady(service.approvalSummaryList(List("FactChecker"))) {
          case Right(jsonList) =>
            val list: List[ApprovalProcessSummary] = jsonList.as[List[ApprovalProcessSummary]]
            list.size shouldBe 1
            val entry = list.head
            entry.id shouldBe approvalProcessSummary.id
            entry.title shouldBe approvalProcessSummary.title
            entry.status shouldBe approvalProcessSummary.status
            entry.reviewType shouldBe approvalProcessSummary.reviewType
          case _ => fail()
        }
      }
    }

    "there are no processes in the database" should {
      "return an empty list" in new Test {

        val expected: RequestOutcome[JsArray] = Right(JsArray())
        val returnedList: RequestOutcome[List[ApprovalProcessSummary]] = Right(List())

        MockApprovalsRepository
          .approvalSummaryList(List("FactChecker"))
          .returns(Future.successful(returnedList))

        whenReady(service.approvalSummaryList(List("FactChecker"))) { result =>
          result shouldBe expected
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[List[ApprovalProcessSummary]] = Left(DatabaseError)
        val expected: RequestOutcome[List[ApprovalProcessSummary]] = Left(InternalServerError)

        MockApprovalsRepository
          .approvalSummaryList(List("FactChecker"))
          .returns(Future.successful(repositoryError))

        whenReady(service.approvalSummaryList(List("FactChecker"))) { result =>
          result shouldBe expected
        }
      }
    }
  }

  private trait ReviewCompleteTest extends Test {
    val pageUrl = "/pageUrl"
    val pageReview: ApprovalProcessPageReview =
      ApprovalProcessPageReview(validId, pageUrl, "title", Some("result1"), "Failed", ZonedDateTime.now, Some("user"))

    // val approvalProcessReviewComplete: ApprovalProcessReview = ApprovalProcessReview(
    //   UUID.randomUUID(),
    //   "validId",
    //   1,
    //   ReviewType2i,
    //   "This is the title",
    //   List(
    //     ApprovalProcessPageReview("1", "/pageUrl", "pageTitle1", Some("Yes"), ReviewCompleteStatus),
    //     ApprovalProcessPageReview("2", "/pageUrl2", "pageTitle2", Some("No"), ReviewCompleteStatus)
    //   )
    // )

    val auditInfo: AuditInfo = AuditInfo("user id","oct90001","This is the title",1,"7903088",1589467563758L,6)
    // val processReviewComplete: ApprovalReview = ApprovalReview(
    //   List(
    //     PageReview("1", "pageTitle1", "/pageUrl", ReviewCompleteStatus, Some("Yes")),
    //     PageReview("2", "pageTitle2", "/pageUrl2", ReviewCompleteStatus, Some("No"))
    //   ),
    //   approvalProcessReviewComplete.lastUpdated,
    // )
    // val processReviewIncomplete: ApprovalProcessReview = ApprovalProcessReview(
    //   UUID.randomUUID(),
    //   "validId",
    //   1,
    //   ReviewType2i,
    //   "This is the title",
    //   List(
    //     ApprovalProcessPageReview("1", "/pageUrl", "/pageUrl", Some("Yes"), InitialPageReviewStatus),
    //     ApprovalProcessPageReview("2", "/pageUrl2", "/pageUrl2", Some("No"), ReviewCompleteStatus)
    //   )
    // )

  }
  "Calling the approvalReviewInfo method" when {
    "there are entries to return" should {
      "return an ApprovalProcessReview object containing appropriate page info" in new Test {

        MockPublishedService
          .getByProcessCode(approvalProcess.meta.processCode)
          .returns(Future.successful(Right(PublishedProcess(validId, 1, ZonedDateTime.now(), Json.obj(), "", approvalProcess.meta.processCode))))

        MockApprovalsRepository
          .getById(validId)
          .returns(Future.successful(Right(approvalProcess)))

        whenReady(service.approvalReviewInfo(validId, ReviewType2i)) {
          case Right(entry) =>
            entry.ocelotId shouldBe validId
            entry.pages.size shouldBe 1
          case _ => fail()
        }
      }
    }

    "there are entries to return but another published process has the same process code" should {
      "return a duplicate key error" in new Test {

        MockPublishedService
          .getByProcessCode(approvalProcess.meta.processCode)
          .returns(Future.successful(Right(PublishedProcess("anotherId", 1, ZonedDateTime.now(), Json.obj(), "", approvalProcess.meta.processCode))))

        MockApprovalsRepository
          .getById(validId)
          .returns(Future.successful(Right(approvalProcess)))

        whenReady(service.approvalReviewInfo(validId, ReviewType2i)) {
          case Left(DuplicateKeyError) => succeed
          case _ => fail()
        }
      }
    }

    "the process cannot be found" should {
      "return a NotFoundError" in new Test {

        val expected: RequestOutcome[String] = Left(NotFoundError)

        MockApprovalsRepository
          .getById(validId)
          .returns(Future.successful(Left(NotFoundError)))

        whenReady(service.approvalReviewInfo(validId, ReviewTypeFactCheck)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail()
        }
      }
    }

    "there is an error retrieving the process" should {
      "return an InternalServerError" in new Test {

        val expected: RequestOutcome[String] = Left(InternalServerError)

        MockApprovalsRepository
          .getById(validId)
          .returns(Future.successful(Left(DatabaseError)))

        whenReady(service.approvalReviewInfo(validId, ReviewType2i)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail()
        }
      }
    }

    // "the review info cannot be found" should {
    //   "return a NotFoundError" in new Test {

    //     val expected: RequestOutcome[String] = Left(NotFoundError)

    //     MockPublishedService
    //       .getByProcessCode(approvalProcess.meta.processCode)
    //       .returns(Future.successful(Right(PublishedProcess(validId, 1, ZonedDateTime.now(), Json.obj(), "", approvalProcess.meta.processCode))))

    //     MockApprovalsRepository
    //       .getById(validId)
    //       .returns(Future.successful(Right(approvalProcess)))

    //     whenReady(service.approvalReviewInfo(validId, ReviewTypeFactCheck)) {
    //       case result @ Left(_) => result shouldBe expected
    //       case _ => fail()
    //     }
    //   }
    // }

    // "there is a database error when retrieving the review info" should {
    //   "return an InternalServerError" in new Test {

    //     val expected: RequestOutcome[String] = Left(InternalServerError)

    //     MockPublishedService
    //       .getByProcessCode(approvalProcess.meta.processCode)
    //       .returns(Future.successful(Right(PublishedProcess(validId, 1, ZonedDateTime.now(), Json.obj(), "", approvalProcess.meta.processCode))))

    //     MockApprovalsRepository
    //       .getById(validId)
    //       .returns(Future.successful(Right(approvalProcess)))

    //     whenReady(service.approvalReviewInfo(validId, ReviewType2i)) {
    //       case result @ Left(_) => result shouldBe expected
    //       case _ => fail()
    //     }
    //   }
    // }
  }

  "Calling the twoEyeReviewComplete method" when {
    "the ID identifies a valid process" when {
      "the status is submitted for 2i review" should {
        "indicate the process status was updated in the database" in new ReviewCompleteTest {

          val expected: RequestOutcome[AuditInfo] = Right(auditInfo)

          MockApprovalsRepository
            .getById(validId)
            .returns(Future.successful(Right(approvalProcessWithValidProcess)))

          MockApprovalsRepository
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

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcessWithValidProcess)))

          MockApprovalsRepository
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

      "the status is published" should {
        "confirm that the correct record has been deleted" in new ReviewCompleteTest {

          val expected: RequestOutcome[AuditInfo] = Right(auditInfo)
          val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcessWithValidProcess)))

          MockApprovalsRepository
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

          val expected: RequestOutcome[Approval] = Left(IncompleteDataError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(incompleteApprovalProcess)))

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the ID cannot be matched to a submitted process" when {
      "the getById fails to find the process" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[Approval] = Left(NotFoundError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the getById fails to find the process with the expected status" should {
        "return a stale data response" in new Test {

          val expected: RequestOutcome[Approval] = Left(StaleDataError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess.copy(meta = approvalProcess.meta.copy(status = StatusSubmittedForFactCheck)))))

          whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      // "the delete fails to delete the details" should {
      //   "return a not found response" in new ReviewCompleteTest {

      //     val expected: RequestOutcome[Approval] = Left(DatabaseError)

      //     MockApprovalsRepository
      //       .getById("validId")
      //       .returns(Future.successful(Right(approvalProcess)))

      //     MockApprovalsRepository
      //       .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
      //       .returns(Future.successful(Right(())))

      //     whenReady(service.twoEyeReviewComplete("validId", statusChange2iReviewInfo)) { result =>
      //       result shouldBe expected
      //     }
      //   }
      // }
      "the changeStatus fails to find the process" should {
        "return a not found response" in new ReviewCompleteTest {

          val expected: RequestOutcome[Approval] = Left(NotFoundError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalsRepository
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
        val expected: RequestOutcome[Approval] = Left(InternalServerError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalsRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalsRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(repositoryError))

        MockPublishedService
          .save("validId", publishedStatusChangeInfo.userId, "processCode", approvalProcess.process)
          .returns(Future.successful(Right("validId")))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }
    "the approval repository reports an unknown error" should {
      "return an internal server error" in new ReviewCompleteTest {

        val repositoryError: RequestOutcome[Unit] = Left(InternalServerError)
        val expected: RequestOutcome[Approval] = Left(InternalServerError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalsRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalsRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(repositoryError))

        MockPublishedService
          .save("validId", publishedStatusChangeInfo.userId, "processCode", approvalProcess.process)
          .returns(Future.successful(Right("validId")))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

    "the process fails to be published" should {
      "indicate a database error" in new ReviewCompleteTest {

        val expected: RequestOutcome[Approval] = Left(InternalServerError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalsRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockPublishedService
          .save("validId", publishedStatusChangeInfo.userId, "processCode", approvalProcess.process)
          .returns(Future.successful(Left(InternalServerError)))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }

      "confirm the record is retained" in new ReviewCompleteTest {

        val expected: RequestOutcome[Approval] = Left(InternalServerError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalsRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockPublishedService
          .save("validId", publishedStatusChangeInfo.userId, "processCode", approvalProcess.process)
          .returns(Future.successful(Left(InternalServerError)))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

    "the process fails to be published due to a duplicate processCode in the published repository" should {
      "indicate a duplicate key error" in new ReviewCompleteTest {

        val expectedChangeStatusResponse: RequestOutcome[Unit] = Right(())
        val expected: RequestOutcome[Approval] = Left(DuplicateKeyError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        MockApprovalsRepository
          .getById("validId")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalsRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(expectedChangeStatusResponse))

        MockPublishedService
          .save("validId", publishedStatusChangeInfo.userId, "processCode", approvalProcess.process)
          .returns(Future.successful(Left(DuplicateKeyError)))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

    "the saved process fails to be validate" should {
      "indicate a bad request" in new ReviewCompleteTest {

        val expectedChangeStatusResponse: RequestOutcome[Unit] = Right(())
        val expected: RequestOutcome[Approval] = Left(BadRequestError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusPublished)

        private val invalidApprovalProcess: Approval = approvalProcess.copy(process = Json.obj("xx" -> "yy"))

        MockApprovalsRepository
          .getById("validId")
          .returns(Future.successful(Right(invalidApprovalProcess)))

        MockApprovalsRepository
          .changeStatus("validId", StatusPublished, "user id")
          .returns(Future.successful(expectedChangeStatusResponse))

        MockPublishedService
          .save("validId", publishedStatusChangeInfo.userId, "processCode", invalidApprovalProcess.process)
          .returns(Future.successful(Right("validId")))

        whenReady(service.twoEyeReviewComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }
  }

  "Calling the approvalPageInfo method" when {

    trait PageInfoTest extends Test {
    }

    "the ID identifies a valid process" when {
      "the pageUrl exists in the process" should {
        "return a populated PageReview" in new PageInfoTest {

          val expected: RequestOutcome[ApprovalProcessPageReview] =
            Right(ApprovalProcessPageReview("2", "/pageUrl2", "title", None, updateDate = currentDateTime))

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(incompleteApprovalProcess)))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewTypeFactCheck)) { result =>
            result shouldBe expected
          }
        }
      }
      "the pageUrl does not exist in the process" should {
        "return a NotFound error" in new PageInfoTest {

          val expected: RequestOutcome[PageReview] = Left(NotFoundError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

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

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewType2i)) { result =>
            result shouldBe expected
          }
        }
      }

      "the getById method returns a DatabaseError" should {
        "return an InternalServerError" in new PageInfoTest {

          val expected: RequestOutcome[Unit] = Left(InternalServerError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Left(DatabaseError)))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewTypeFactCheck)) { result =>
            result shouldBe expected
          }
        }
      }
      "the approvalPageInfo fails to find the process review info" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[Unit] = Left(NotFoundError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.approvalPageInfo("validId", "/pageUrl2", ReviewType2i)) { result =>
            result shouldBe expected
          }
        }
      }

      "the approvals repository reports a database error" should {
        "return an internal server error" in new PageInfoTest {

          val expected: RequestOutcome[ApprovalReview] = Left(InternalServerError)

          MockApprovalsRepository
            .getById("validId")
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

      val pageReview: ApprovalProcessPageReview =
        ApprovalProcessPageReview(validId, pageUrl, "title", Some("result1"), "Failed", ZonedDateTime.now, Some("user"))
    }

    "the ID identifies a valid process" when {
      "the pageUrl is found in the process review info" should {
        "indicate the process status was updated in the database" in new PageCompleteTest {

          val expected: RequestOutcome[Unit] = Right(())

          MockApprovalsRepository
            .getById(validId)
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalsRepository
            .updatePageReview(validId, pageUrl, ReviewType2i, pageReview)
            .returns(Future.successful(Right(())))

          MockApprovalsRepository
            .changeStatus(validId, StatusInProgress, pageReview.updateUser.get)
            .returns(Future.successful(Right(())))

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewType2i, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
      "the changeStatus fails to update the status" should {
        "return an ok response" in new PageCompleteTest {

          val expected: RequestOutcome[Unit] = Right(())

          MockApprovalsRepository
            .getById(validId)
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalsRepository
            .updatePageReview(validId, pageUrl, ReviewType2i, pageReview)
            .returns(Future.successful(Right(())))

          MockApprovalsRepository
            .changeStatus(validId, StatusInProgress, pageReview.updateUser.get)
            .returns(Future.successful(Left(NotFoundError)))

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

          MockApprovalsRepository
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

          MockApprovalsRepository
            .getById(validId)
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalsRepository
            .updatePageReview(validId, pageUrl, ReviewTypeFactCheck, pageReview)
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

          val repositoryError: RequestOutcome[Approval] = Left(DatabaseError)
          val expected: RequestOutcome[Unit] = Left(InternalServerError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(repositoryError))

          whenReady(service.approvalPageComplete(validId, pageUrl, ReviewTypeFactCheck, pageReview)) { result =>
            result shouldBe expected
          }
        }
      }
      "the updatePageReview returns a DatabaseError" should {
        "return an internal server error" in new PageCompleteTest {

          val expected: RequestOutcome[Unit] = Left(InternalServerError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalsRepository
            .updatePageReview(validId, pageUrl, ReviewType2i, pageReview)
            .returns(Future.successful(Left(DatabaseError)))

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
    val incompleteFactCheckProcess = incompleteApprovalProcess.copy(meta = factCheckMeta)

    "the ID identifies a valid process" when {
      "the status is submitted for fact check" should {
        "indicate the process status was updated in the database" in new ReviewCompleteTest {

          val expected: RequestOutcome[AuditInfo] = Right(auditInfo)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess)))

          MockApprovalsRepository
            .updateReview("validId", 1, ReviewTypeFactCheck, "user id", StatusWithDesignerForUpdate)
            .returns(Future.successful(Right(())))

          MockApprovalsRepository
            .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(Right(())))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }

      "all the pages have not been reviewed" should {
        "indicate that the data was incomplete" in new ReviewCompleteTest {

          val expected: RequestOutcome[Approval] = Left(IncompleteDataError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(incompleteFactCheckProcess)))

          MockApprovalsRepository
            .updateReview("validId", 1, ReviewTypeFactCheck, "user id", StatusWithDesignerForUpdate)
            .returns(Future.successful(Right(())))

          MockApprovalsRepository
            .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
            .returns(Future.successful(Right(())))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
    }

    "the ID cannot be matched to a submitted process" when {
      "the getById fails to find the process" should {
        "return a not found response" in new Test {

          val expected: RequestOutcome[Approval] = Left(NotFoundError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the getById fails to find the process with the expected status" should {
        "return a stale data response" in new Test {

          val expected: RequestOutcome[Approval] = Left(StaleDataError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess.copy(meta = factCheckProcess.meta.copy(status = StatusPublished)))))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the updateReview fails to update the details" should {
        "return a not found response" in new ReviewCompleteTest {

          val expected: RequestOutcome[Approval] = Left(NotFoundError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess)))

          MockApprovalsRepository
            .updateReview("validId", 1, ReviewTypeFactCheck, "user id", StatusWithDesignerForUpdate)
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.factCheckComplete("validId", statusChange2iReviewInfo)) { result =>
            result shouldBe expected
          }
        }
      }
      "the changeStatus fails to find the process" should {
        "return a not found response" in new ReviewCompleteTest {

          val expected: RequestOutcome[Approval] = Left(NotFoundError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(factCheckProcess)))

          MockApprovalsRepository
            .updateReview("validId", 1, ReviewTypeFactCheck, "user id", StatusWithDesignerForUpdate)
            .returns(Future.successful(Right(())))

          MockApprovalsRepository
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
        val expected: RequestOutcome[Approval] = Left(InternalServerError)
        val publishedStatusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusWithDesignerForUpdate)

        MockApprovalsRepository
          .getById("validId")
          .returns(Future.successful(Right(factCheckProcess)))

        MockApprovalsRepository
          .updateReview("validId", 1, ReviewTypeFactCheck, "user id", StatusWithDesignerForUpdate)
          .returns(Future.successful(Right(())))

        MockApprovalsRepository
          .changeStatus("validId", StatusWithDesignerForUpdate, "user id")
          .returns(Future.successful(repositoryError))

        whenReady(service.factCheckComplete("validId", publishedStatusChangeInfo)) { result =>
          result shouldBe expected
        }
      }
    }

  }

  // "Invoking private method getReviewInfo" when {
  //   "the getByIdAndVersion method returns a valid result" should {
  //     "return a valid ProcessReview" in new ReviewCompleteTest {

  //       val expected: RequestOutcome[ProcessReview] = Right(processReviewComplete)

  //      whenReady(service.getReviewInfo(validId, ReviewType2i, 1)) { result =>
  //         result shouldBe expected
  //       }
  //     }
  //   }
  //   "the getByIdAndVersion method returns a NotFoundError" should {
  //     "return a NotFoundError" in new ReviewCompleteTest {

  //       val expected: RequestOutcome[ProcessReview] = Left(NotFoundError)

  //       whenReady(service.getReviewInfo(validId, ReviewType2i, 1)) { result =>
  //         result shouldBe expected
  //       }
  //     }
  //   }
  //   "the getByIdAndVersion method returns a DatabaseError" should {
  //     "return an InternalServerError" in new ReviewCompleteTest {

  //       val expected: RequestOutcome[ProcessReview] = Left(InternalServerError)

  //       whenReady(service.getReviewInfo(validId, ReviewType2i, 1)) { result =>
  //         result shouldBe expected
  //       }
  //     }
  //   }
  // }

  "Invoking method checkProcessInCorrectStateForCompletion" when {
    "the getApprovalProcessToUpdate method returns a valid result" when {
      "the process review in not complete" when {
        "return an INCOMPLETE_DATA_ERROR" in new ReviewCompleteTest {

          val expected: RequestOutcome[Approval] = Left(IncompleteDataError)

          MockApprovalsRepository
            .getById("validId")
            .returns(Future.successful(Right(incompleteApprovalProcess)))

         whenReady(service.checkProcessInCorrectStateForCompletion(validId, ReviewType2i)) { result =>
            result shouldBe expected
          }
        }
      }
    }
  }

}
