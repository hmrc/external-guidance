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
import java.util.UUID
import core.services.{PageBuilder, Timescales}
import base.BaseSpec
import mocks.{MockAppConfig, MockApprovalProcessReviewRepository, MockApprovalRepository, MockPublishedRepository}
import models._
import core.models.errors._
import core.models.ocelot.{Process, ProcessJson}
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsArray, JsObject, Json, OFormat}
import models.Constants._
import core.models.RequestOutcome

import scala.concurrent.Future
import core.services.{DefaultTodayProvider, EncrypterService}
import mocks.MockTimescalesService

class ApprovalServiceSpec extends BaseSpec with MockFactory {

  private trait Test extends MockApprovalRepository
    with MockApprovalProcessReviewRepository
    with MockPublishedRepository
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
    val service = new ApprovalService(mockApprovalRepository,
                          mockApprovalProcessReviewRepository,
                          mockPublishedRepository,
                          fsService
                          )(ec, MockAppConfig)

    val processReview: ApprovalProcessReview =
      ApprovalProcessReview(
        UUID.randomUUID(),
        "oct90001",
        1,
        ReviewType2i,
        "title",
        List()
      )

    val publishedProcessSuccessResponse: RequestOutcome[PublishedProcess] =
      Right(PublishedProcess(validId, 1, ZonedDateTime.now(), JsObject.empty, "publishedBy", approvalProcess.meta.processCode))
    val publishedProcessFailureResponse: RequestOutcome[PublishedProcess] =
      Right(PublishedProcess("random-id", 1, ZonedDateTime.now(), JsObject.empty, "publishedBy", approvalProcess.meta.processCode))


    val validPublishedId: String = "ext90005"
    val publishedProcess: PublishedProcess =
      PublishedProcess(validPublishedId, 1, ZonedDateTime.now(), validOnePageJson.as[JsObject], "user", processCode = "processCode")

  }

  "Calling the getById method" when {
    "the ID identifies a valid process" should {
      "return a JSON representing the submitted ocelot process" in new Test {

        val returnFromRepo: RequestOutcome[ApprovalProcess] = Right(approvalProcessWithValidProcess)
        val expected: RequestOutcome[JsObject] = Right(approvalProcessWithValidProcess.process)

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(returnFromRepo))

        whenReady(service.getById(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the ID cannot be matched to a submitted process" should {
      "return a not found response" in new Test {

        val expected: RequestOutcome[ApprovalProcess] = Left(NotFoundError)

        MockApprovalRepository
          .getById(validId)
          .returns(Future.successful(expected))

        whenReady(service.getById(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[ApprovalProcess] = Left(DatabaseError)
        val expected: RequestOutcome[JsObject] = Left(InternalServerError)

        MockApprovalRepository
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

        val returnFromRepo: RequestOutcome[ApprovalProcess] = Right(approvalProcess)
        val expected: RequestOutcome[JsObject] = Right(approvalProcess.process)

        MockApprovalRepository
          .getByProcessCode(validId)
          .returns(Future.successful(returnFromRepo))

        whenReady(service.getByProcessCode(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the ID cannot be matched to a submitted process" should {
      "return a not found response" in new Test {

        val expected: RequestOutcome[ApprovalProcess] = Left(NotFoundError)

        MockApprovalRepository
          .getByProcessCode(validId)
          .returns(Future.successful(expected))

        whenReady(service.getByProcessCode(validId)) { result =>
          result shouldBe expected
        }
      }
    }

    "the repository reports a database error" should {
      "return an internal server error" in new Test {

        val repositoryError: RequestOutcome[ApprovalProcess] = Left(DatabaseError)
        val expected: RequestOutcome[JsObject] = Left(InternalServerError)

        MockApprovalRepository
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

        MockApprovalRepository
          .update(approvalProcess)
          .returns(Future.successful(expected))

        MockApprovalRepository
          .getById("oct90001")
          .returns(Future.successful(Right(approvalProcess)))

        MockApprovalProcessReviewRepository
          .save(processReview)
          .returns(Future.successful(Right(processReview.id)))

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

        MockApprovalRepository
          .update(approvalProcess)
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

          MockApprovalRepository
            .update(approvalProcess)
            .returns(Future.successful(Right(validId)))

          MockApprovalRepository
            .getById("oct90001")
            .returns(Future.successful(Right(approvalProcess)))

          MockApprovalProcessReviewRepository
            .save(processReview)
            .returns(Future.successful(Left(DatabaseError)))

          whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
            case result @ Left(_) => result shouldBe expected
            case _ => fail()
          }
        }
      }
    }

    "the process is saved" when {
      "the subsequent get of the process fails with a NotFoundError" should {
        "return an internal error" in new Test {

          val expected: RequestOutcome[String] = Left(NotFoundError)

          MockPublishedRepository
            .getByProcessCode("cup-of-tea")
            .returns(Future.successful(publishedProcessSuccessResponse))

          MockApprovalRepository
            .update(approvalProcess)
            .returns(Future.successful(Right(validId)))

          MockApprovalRepository
            .getById("oct90001")
            .returns(Future.successful(Left(NotFoundError)))

          whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
            case result @ Left(_) => result shouldBe expected
            case _ => fail()
          }
        }
      }

      "the subsequent get of the process fails with a DatabaseError" should {
        "return an internal error" in new Test {

          val expected: RequestOutcome[String] = Left(InternalServerError)

          MockPublishedRepository
            .getByProcessCode("cup-of-tea")
            .returns(Future.successful(publishedProcessSuccessResponse))

          MockApprovalRepository
            .update(approvalProcess)
            .returns(Future.successful(Right(validId)))

          MockApprovalRepository
            .getById("oct90001")
            .returns(Future.successful(Left(DatabaseError)))

          whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
            case result @ Left(_) => result shouldBe expected
            case _ => fail()
          }
        }
      }
    }

    "the JSON is invalid" should {
      "not call the repository" in new Test {
        MockApprovalRepository
          .update(approvalProcess)
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

        MockApprovalRepository
          .update(approvalProcess)
          .returns(Future.successful(repositoryResponse))

        whenReady(service.save(validOnePageJson.as[JsObject], ReviewType2i, StatusSubmittedFor2iReview)) {
          case result @ Left(_) => result shouldBe expected
          case _ => fail()
        }
      }
    }
  }

  "Calling the approvalSummaryList method" when {
    "there are entries to return and role is not 2iReviewer" should {
      "return a List of approval processes" in new Test {
        implicit val formats: OFormat[ApprovalProcessSummary] = Json.format[ApprovalProcessSummary]

        val expected: RequestOutcome[List[ApprovalProcessSummary]] = Right(List(approvalProcessSummary))

        MockApprovalRepository
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

    "there are entries to return and role is 2iReviewer" should {
      "return a List of approval processes and published" in new Test {
        implicit val formats: OFormat[ApprovalProcessSummary] = Json.format[ApprovalProcessSummary]

        val expectedApproval: RequestOutcome[List[ApprovalProcessSummary]] = Right(List(approvalProcessSummary))
        val expectedPublished: RequestOutcome[List[PublishedProcess]] = Right(List(publishedProcess))

        MockApprovalRepository
          .approvalSummaryList(List("2iReviewer"))
          .returns(Future.successful(expectedApproval))

        MockPublishedRepository
          .list()
          .returns(Future.successful(expectedPublished))

        whenReady(service.approvalSummaryList(List("2iReviewer"))) {
          case Right(jsonList) =>
            val list: List[ApprovalProcessSummary] = jsonList.as[List[ApprovalProcessSummary]]
            list.size shouldBe 2
            val entry1 = list.head
            val entry2 = list(1)
            entry1.id shouldBe approvalProcessSummary.id
            entry1.title shouldBe approvalProcessSummary.title
            entry1.status shouldBe approvalProcessSummary.status
            entry1.reviewType shouldBe approvalProcessSummary.reviewType
            entry2.id shouldBe publishedProcess.id
            entry2.title shouldBe publishedProcess.process.validate[Process].fold(_ => "", _.meta.title)
            entry2.status shouldBe "Published"
            entry2.reviewType shouldBe "2i-review"
          case _ => fail()
        }
      }
    }

    "there are no processes in the database" should {
      "return an empty list" in new Test {

        val expected: RequestOutcome[JsArray] = Right(JsArray())
        val returnedList: RequestOutcome[List[ApprovalProcessSummary]] = Right(List())

        MockApprovalRepository
          .approvalSummaryList(List("FactChecker"))
          .returns(Future.successful(returnedList))

        whenReady(service.approvalSummaryList(List("FactChecker"))) { result =>
          result shouldBe expected
        }
      }
    }

    "the repository reports a database error" should {
      "return a database error" in new Test {

        val repositoryError: RequestOutcome[List[ApprovalProcessSummary]] = Left(DatabaseError)
        val expected: RequestOutcome[List[ApprovalProcessSummary]] = Left(DatabaseError)

        MockApprovalRepository
          .approvalSummaryList(List("FactChecker"))
          .returns(Future.successful(repositoryError))

        whenReady(service.approvalSummaryList(List("FactChecker"))) { result =>
          result shouldBe expected
        }
      }
    }
  }

}
