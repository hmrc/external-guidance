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

  def approval2iReviewInfo(id: String): Future[RequestOutcome[ProcessReview]] =
    getReviewInfo(id, ReviewType2i)

  def approvalFactCheckInfo(id: String): Future[RequestOutcome[ProcessReview]] =
    getReviewInfo(id, ReviewTypeFactCheck)

  def getReviewInfo(id: String, reviewType: String): Future[RequestOutcome[ProcessReview]] =
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

  def approval2iReviewPageInfo(id: String, pageUrl: String): Future[RequestOutcome[PageReview]] =
    getPageInfo(id, pageUrl, ReviewType2i)

  def approvalFactCheckPageInfo(id: String, pageUrl: String): Future[RequestOutcome[PageReview]] =
    getPageInfo(id, pageUrl, ReviewTypeFactCheck)

  def getPageInfo(id: String, pageUrl: String, reviewType: String): Future[RequestOutcome[PageReview]] =
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

  def change2iReviewStatus(id: String, currentStatus: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[Unit]] = {

    def getContentToUpdate: Future[Option[ApprovalProcess]] = repository.getById(id) map {
        case Right(process) => if (process.meta.status == currentStatus) Some(process) else None
        case _ => None
      }

    def publishIfRequired(approvalProcess: ApprovalProcess): Future[RequestOutcome[Unit]] = info.status match {
        case StatusApprovedForPublishing =>
          publishedService.save(id, approvalProcess.process) map {
            case Right(_) => Right(())
            case Left(errors) => Left(errors)
          }
        case _ => Future.successful(Right(()))
      }

    getContentToUpdate flatMap {
      case Some(approvalProcess) =>
        val status = if (info.status == StatusApprovedForPublishing) StatusPublished else info.status
        repository.changeStatus(id, status, info.userId) flatMap {
          case Left(Errors(DatabaseError :: Nil)) => Future.successful(Left(Errors(InternalServiceError)))
          case error @ Left(Errors(NotFoundError :: Nil)) => Future.successful(error)
          case _ => publishIfRequired(approvalProcess)
        }
      case None =>
        logger.warn(s"Invalid process id submitted to method changeStatus. The requested id was $id")
        Future.successful(Left(Errors(NotFoundError)))
    }
  }

  def changeFactCheckStatus(id: String, currentStatus: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[Unit]] = {

    def getContentToUpdate: Future[Option[ApprovalProcess]] = repository.getById(id) map {
      case Right(process) => if (process.meta.status == currentStatus) Some(process) else None
      case _ => None
    }

    def publishIfRequired(approvalProcess: ApprovalProcess): Future[RequestOutcome[Unit]] = info.status match {
      case StatusApprovedForPublishing =>
        publishedService.save(id, approvalProcess.process) map {
          case Right(_) => Right(())
          case Left(errors) => Left(errors)
        }
      case _ => Future.successful(Right(()))
    }

    getContentToUpdate flatMap {
      case Some(approvalProcess) =>
        val status = if (info.status == StatusApprovedForPublishing) StatusPublished else info.status
        repository.changeStatus(id, status, info.userId) flatMap {
          case Left(Errors(DatabaseError :: Nil)) => Future.successful(Left(Errors(InternalServiceError)))
          case error @ Left(Errors(NotFoundError :: Nil)) => Future.successful(error)
          case _ => publishIfRequired(approvalProcess)
        }
      case None =>
        logger.warn(s"Invalid process id submitted to method changeStatus. The requested id was $id")
        Future.successful(Left(Errors(NotFoundError)))
    }
  }

  def changeStatus(id: String, currentStatus: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[Unit]] = {

    def getContentToUpdate: Future[RequestOutcome[ApprovalProcess]] = repository.getById(id) map {
      case Right(process) if process.meta.status == currentStatus => Right (process)
      case Right(process) =>
        logger.warn(s"Invalid Process Status Change requested for process $id: " +
          s"Expected Status: '$currentStatus' Status Found: '${process.meta.status}' Desired Status: ${info.status} ")
        Left(Errors(StaleDataError))
      case Left(errors) =>
        logger.warn(s"ChangeStatus - error retrieving process $id - error returned $errors.")
        Left(errors)
    }

    def publishIfRequired(approvalProcess: ApprovalProcess): Future[RequestOutcome[Unit]] = info.status match {
      case StatusApprovedForPublishing =>
        publishedService.save(id, approvalProcess.process) map {
          case Right(_) => Right(())
          case Left(errors) =>
            logger.error(s"Failed to publish $id - $errors")
            Left(errors)
        }
      case _ => Future.successful(Right(()))
    }

    getContentToUpdate flatMap {
      case Right(approvalProcess) =>
        val status = if (info.status == StatusApprovedForPublishing) StatusPublished else info.status
        repository.changeStatus(id, status, info.userId) flatMap {
          case Left(Errors(DatabaseError :: Nil)) => Future.successful(Left(Errors(InternalServiceError)))
          case error @ Left(Errors(NotFoundError :: Nil)) =>
            logger.warn(s"Change Status: process $id was not found")
            Future.successful(error)
          case _ => publishIfRequired(approvalProcess)
        }
      case Left(errors) =>
        Future.successful(Left(errors))
    }
  }

  def approval2iReviewPageComplete(id: String, pageUrl: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]] =
    repository.getById(id) flatMap {
      case Left(Errors(NotFoundError :: Nil)) =>
        logger.warn(s"approval2iReviewPageComplete - process $id not found.")
        Future.successful(Left(Errors(NotFoundError)))
      case Left(_) => Future.successful(Left(Errors(InternalServiceError)))
      case Right(process) =>
        reviewRepository.updatePageReview(process.id, process.version, pageUrl, reviewInfo) map {
          case Left(Errors(NotFoundError :: Nil)) =>
            logger.warn(s"updatePageReview failed for process $id, version ${process.version} and pageUrl $pageUrl not found.")
            Left(Errors(NotFoundError))
          case Left(_) => Left(Errors(InternalServiceError))
          case Right(_) => Right(())
        }
    }

}
