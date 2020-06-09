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

import javax.inject.{Inject, Singleton}
import models._
import models.errors._
import play.api.Logger
import repositories.{ApprovalProcessReviewRepository, ApprovalRepository}
import utils.Constants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReviewService @Inject() (publishedService: PublishedService, repository: ApprovalRepository, reviewRepository: ApprovalProcessReviewRepository) {

  val logger: Logger = Logger(this.getClass)

  def approvalReviewInfo(id: String, reviewType: String): Future[RequestOutcome[ProcessReview]] =
    repository.getById(id) flatMap {
      case Left(Errors(NotFoundError :: Nil)) => Future.successful(Left(Errors(NotFoundError)))
      case Left(_) => Future.successful(Left(Errors(InternalServiceError)))
      case Right(process) =>
        reviewRepository.getByIdVersionAndType(id, process.version, reviewType) map {
          case Left(Errors(NotFoundError :: Nil)) => Left(Errors(NotFoundError))
          case Left(_) => Left(Errors(InternalServiceError))
          case Right(info) =>
            val pages: List[PageReview] = info.pages.map(p => PageReview(p.id, p.pageUrl, p.status))
            Right(ProcessReview(info.id, info.ocelotId, info.version, info.reviewType, info.title, info.lastUpdated, pages))
        }
    }

  def approvalPageInfo(id: String, pageUrl: String, reviewType: String): Future[RequestOutcome[ApprovalProcessPageReview]] =
    repository.getById(id) flatMap {
      case Left(Errors(NotFoundError :: Nil)) => Future.successful(Left(Errors(NotFoundError)))
      case Left(_) => Future.successful(Left(Errors(InternalServiceError)))
      case Right(process) =>
        reviewRepository.getByIdVersionAndType(id, process.version, reviewType) map {
          case Left(Errors(NotFoundError :: Nil)) => Left(Errors(NotFoundError))
          case Left(_) => Left(Errors(InternalServiceError))
          case Right(info) =>
            info.pages.find(p => p.pageUrl == pageUrl) match {
              case Some(page) => Right(page)
              case _ => Left(Errors(NotFoundError))
            }
        }
    }

  def twoEyeReviewComplete(id: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[Unit]] = {

    def publishIfRequired(approvalProcess: ApprovalProcess): Future[RequestOutcome[Unit]] = info.status match {
      case StatusPublished =>
        publishedService.save(id, info.userId, approvalProcess.process) map {
          case Right(_) => Right(())
          case Left(errors) =>
            logger.error(s"Failed to publish $id - $errors")
            Left(errors)
        }
      case _ => Future.successful(Right(()))
    }

    getContentToUpdate(id, StatusSubmittedFor2iReview) flatMap {
      case Right(approvalProcess) =>
        updateReviewOnCompletion(id, approvalProcess.version, ReviewType2i, info.userId, info.status) flatMap {
          case Right(()) =>
            // TODO should we include required status (or version) in repo call in case changed between check above and now
            repository.changeStatus(id, info.status, info.userId) flatMap {
              case Left(Errors(DatabaseError :: Nil)) => Future.successful(Left(Errors(InternalServiceError)))
              case error@Left(Errors(NotFoundError :: Nil)) =>
                logger.warn(s"Change Status: process $id was not found")
                Future.successful(error)
              case _ => publishIfRequired(approvalProcess)
            }
          case Left(errors) =>
            logger.error(s"Could not change status of fact check review for process $id")
            Future.successful(Left(errors))
        }
      case Left(errors) =>
        Future.successful(Left(errors))
    }
  }

  def factCheckComplete(id: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[Unit]] =
    getContentToUpdate(id, StatusSubmittedForFactCheck) flatMap {
      case Right(approvalProcess) =>
        updateReviewOnCompletion(id, approvalProcess.version, ReviewTypeFactCheck, info.userId, info.status) flatMap {
          case Right(()) =>
            // TODO should we include required status (or version) in repo call in case changed between check above and now
            repository.changeStatus(id, info.status, info.userId) flatMap {
              case Left(Errors(DatabaseError :: Nil)) => Future.successful(Left(Errors(InternalServiceError)))
              case error@Left(Errors(NotFoundError :: Nil)) =>
                logger.warn(s"Change Status: process $id was not found")
                Future.successful(error)
              case _ => Future.successful(Right(()))
            }
          case Left(errors) =>
            logger.error(s"Could not change status of fact check review for process $id")
            Future.successful(Left(errors))
        }
      case Left(errors) =>
        Future.successful(Left(errors))
    }

  private def updateReviewOnCompletion(id: String, version: Int, reviewType: String, user: String, status: String): Future[RequestOutcome[Unit]] = {
    reviewRepository.updateReview(id, version, reviewType, user, status)
  }

  private def getContentToUpdate(id: String, expectedStatus: String): Future[RequestOutcome[ApprovalProcess]] = repository.getById(id) map {
    case Right(process) if process.meta.status == expectedStatus => Right (process)
    case Right(process) =>
      logger.warn(s"Invalid Process Status Change requested for process $id: " +
        s"Expected Status: '$expectedStatus' Status Found: '${process.meta.status}'")
      Left(Errors(StaleDataError))
    case Left(errors) =>
      logger.warn(s"ChangeStatus - error retrieving process $id - error returned $errors.")
      Left(errors)
  }

  def approvalPageComplete(id: String, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]] =
    repository.getById(id) flatMap {
      case Left(Errors(NotFoundError :: Nil)) =>
        logger.warn(s"approvalPageComplete - process $id not found.")
        Future.successful(Left(Errors(NotFoundError)))
      case Left(_) => Future.successful(Left(Errors(InternalServiceError)))
      case Right(process) =>
        reviewRepository.updatePageReview(process.id, process.version, pageUrl, reviewType, reviewInfo) map {
          case Left(Errors(NotFoundError :: Nil)) =>
            logger.warn(s"updatePageReview failed for process $id, version ${process.version}, reviewType $reviewType and pageUrl $pageUrl not found.")
            Left(Errors(NotFoundError))
          case Left(_) => Left(Errors(InternalServiceError))
          case Right(_) => Right(())
        }
    }

}
