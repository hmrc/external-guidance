/*
 * Copyright 2024 HM Revenue & Customs
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

import java.util.UUID
import config.AppConfig

import javax.inject.{Inject, Singleton}
import models._
import core.models._
import core.models.ocelot.Process
import core.models.errors.{DuplicateKeyError, InternalServerError, NotFoundError}
import core.services.fromPageDetails
import play.api.Logger
import play.api.libs.json._
import repositories.{ApprovalProcessReviewRepository, ApprovalRepository, PublishedRepository}

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{Json, OFormat}

@Singleton
class ApprovalService @Inject() (
    repository: ApprovalRepository,
    reviewRepository: ApprovalProcessReviewRepository,
    publishedRepository: PublishedRepository,
    finalisationService: ProcessFinalisationService
)(implicit ec: ExecutionContext, val appConfig: AppConfig) {

  val logger: Logger = Logger(this.getClass)

  def save(incomingJson: JsObject, reviewType: String, initialStatus: String, checkLevel: GuidanceCheckLevel = Strict): Future[RequestOutcome[String]] =
    finalisationService.guidancePagesAndProcess(incomingJson, checkLevel).flatMap{
      case Left(err) => Future.successful(Left(err))
      case Right((process, pages, json)) =>
        val processMetaSection =
          ApprovalProcessMeta(
            process.meta.id,
            process.meta.title,
            initialStatus,
            reviewType = reviewType,
            processCode = process.meta.processCode)

        // Check no published process with this processCode for another processId
        publishedRepository.getByProcessCode(process.meta.processCode).flatMap{
          case Right(p) if p.id != process.meta.id =>
            logger.error(s"Attempt to persist approval process ${process.meta.id} with code ${process.meta.processCode} " +
              s": duplicate key in published collection for process ${p.id}")
            Future.successful(Left(DuplicateKeyError))
          case _ =>
            repository.update(ApprovalProcess(process.meta.id, processMetaSection, json)) flatMap {
              case Right(savedId) =>
                repository.getById(savedId) flatMap {
                  case Right(approvalProcess) =>
                    saveReview(
                      ApprovalProcessReview(
                        UUID.randomUUID(),
                        process.meta.id,
                        approvalProcess.version,
                        reviewType,
                        process.meta.title,
                        fromPageDetails(pages)(ApprovalProcessPageReview(_, _, _))
                      )
                    )
                  case Left(NotFoundError) => Future.successful(Left(NotFoundError))
                  case Left(_) => Future.successful(Left(InternalServerError))
                }
              case Left(DuplicateKeyError) => Future.successful(Left(DuplicateKeyError))
              case _ => Future.successful(Left(InternalServerError))
            }
        }
    }

  private def saveReview(approvalProcessReview: ApprovalProcessReview): Future[RequestOutcome[String]] =
    reviewRepository.save(approvalProcessReview) map {
      case Right(_) => Right(approvalProcessReview.ocelotId)
      case _ => Left(InternalServerError)
    }

  def getTimescalesInUse(): Future[RequestOutcome[List[String]]] =
    repository.getTimescalesInUse().map{
      case Right(timescalesInUse) => Right(timescalesInUse)
      case err => err
    }

  def getById(id: String): Future[RequestOutcome[JsObject]] =
    repository.getById(id) map {
      case Left(NotFoundError) => Left(NotFoundError)
      case Left(_) => Left(InternalServerError)
      case Right(result) => Right(result.process)
    }

  def getByProcessCode(processCode: String): Future[RequestOutcome[JsObject]] =
    repository.getByProcessCode(processCode) map {
      case Left(NotFoundError) => Left(NotFoundError)
      case Left(_) => Left(InternalServerError)
      case Right(result) => Right(result.process)
    }

  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[JsValue]] =
    repository.approvalSummaryList(roles).flatMap {
      case Left(err) => Future.successful(Left(err))
      case Right(approvals) if roles.contains("2iReviewer") || roles.contains("Designer") =>
        publishedRepository.list().map {
          case Left(err) => Left(err)
          case Right(published) =>
            val approvalIds = approvals.map(_.id)
            val publishedToInclude = published.filter(p => appConfig.includeAllPublishedInReviewList || !approvalIds.contains(p.id)).map { p =>
              ApprovalProcessSummary(p.id, p.process.validate[Process].fold(_ => "", _.meta.title), p.datePublished.toLocalDate, "Published", "2i-review")
            }
            Right(Json.toJson(approvals ++ publishedToInclude))
          }
      case Right(approvals) => Future.successful(Right(Json.toJson(approvals)))
    }

  def list(): Future[RequestOutcome[JsValue]] = {
    implicit val formats: OFormat[ProcessSummary] = Json.format[ProcessSummary]
    repository.processSummaries() map {
      case Left(_) => Left(InternalServerError)
      case Right(summaries) => Right(Json.toJson(summaries))
    }
  }

}
