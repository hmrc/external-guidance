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
import models.ocelot.Process
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReviewService @Inject() (publishedService: PublishedService, repository: ApprovalRepository, reviewRepository: ApprovalProcessReviewRepository) {

  val logger: Logger = Logger(this.getClass)

  def approvalReviewInfo(id: String, reviewType: String): Future[RequestOutcome[ProcessReview]] =
    repository.getById(id) flatMap {
      case Left(Errors(NotFoundError :: Nil)) => Future.successful(Left(Errors(NotFoundError)))
      case Left(_) => Future.successful(Left(Errors(InternalServiceError)))
      case Right(process) => getReviewInfo(id, reviewType, process.version)
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

  def twoEyeReviewComplete(id: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[AuditInfo]] = {

    def publishIfRequired(approvalProcess: ApprovalProcess): Future[RequestOutcome[ApprovalProcess]] = info.status match {
      case StatusPublished =>
        publishedService.save(id, info.userId, approvalProcess.process) map {
          case Right(_) => Right(approvalProcess)
          case Left(errors) =>
            logger.error(s"Failed to publish $id - $errors")
            Left(errors)
        }
      case _ => Future.successful(Right(approvalProcess))
    }

    checkProcessInCorrectStateForCompletion(id, ReviewType2i) flatMap {
      case Right(ap) =>
        reviewRepository.updateReview(id, ap.version, ReviewType2i, info.userId, info.status) flatMap {
          case Right(()) =>
            changeStatus(id, info.status, info.userId, ReviewType2i) flatMap {
              case Right(_) => publishIfRequired(ap).map{
                case Right(_) => ap.process.validate[Process].fold(
                  _ => Left(Errors(BadRequestError)): RequestOutcome[AuditInfo],
                  process => Right(AuditInfo(info.userId, ap, process))
                )
                case Left(err) => Left(err)
              }
              case Left(errors) => Future.successful(Left(errors))
            }
          case Left(errors) =>
            logger.error(s"updateReviewOnCompletion: Could not change status of 2i review for process $id")
            Future.successful(Left(errors))
        }
      case Left(errors) =>
        logger.error(s"2i Complete - errors returned $errors")
        Future.successful(Left(errors))
    }
  }


  def factCheckComplete(id: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[AuditInfo]] =
    checkProcessInCorrectStateForCompletion(id, ReviewTypeFactCheck) flatMap {
      case Right(ap) =>
        reviewRepository.updateReview(id, ap.version, ReviewTypeFactCheck, info.userId, info.status) flatMap {
          case Right(_) => changeStatus(id, info.status, info.userId, ReviewTypeFactCheck) map {
            case Right(_) => ap.process.validate[Process].fold(
                  _ => Left(Errors(BadRequestError)): RequestOutcome[AuditInfo],
                  process => Right(AuditInfo(info.userId, ap, process))
                )
            case Left(error) => Left(error)
          }
          case Left(errors) =>
            logger.error(s"updateReviewOnCompletion: Could not update fact check review on completion for process $id")
            Future.successful(Left(errors))
        }
      case Left(errors) =>
        logger.error(s"FactCheck Complete - returning $errors")
        Future.successful(Left(errors))
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
          case Right(_) =>
            changeStatus(id, "InProgress", reviewInfo.updateUser.getOrElse("System"), reviewType)
            Right(())
        }
    }

  private def changeStatus(id: String, status: String, userId: String, reviewType: String): Future[RequestOutcome[Unit]] =
    repository.changeStatus(id, status, userId) map {
      case Left(Errors(DatabaseError :: Nil)) =>
        logger.error(s"$reviewType - database error changing status")
        Left(Errors(InternalServiceError))
      case error@Left(Errors(NotFoundError :: Nil)) =>
        logger.warn(s"$reviewType: Change Status: process $id was not found")
        error
      case Left(errors) =>
        logger.error(s"$reviewType: changeStatus: for $id - $errors")
        Left(errors)
      case Right(_) => Right(())
    }

  private def getApprovalProcessToUpdate(id: String): Future[RequestOutcome[ApprovalProcess]] = repository.getById(id) map {
    case Right(process) if StatusAllowedForReviewCompletion.contains(process.meta.status) => Right (process)
    case Right(process) =>
      logger.warn(s"Invalid Process Status Change requested for process $id: " +
        s"Expected Status One Of: '${StatusAllowedForReviewCompletion.mkString}' Status Found: '${process.meta.status}'")
      Left(Errors(StaleDataError))
    case Left(errors) =>
      logger.warn(s"getApprovalProcessToUpdate - error retrieving process $id - error returned $errors.")
      Left(errors)
  }

  private def getReviewInfo(id: String, reviewType: String, version: Int): Future[RequestOutcome[ProcessReview]] = {
    reviewRepository.getByIdVersionAndType(id, version, reviewType) map {
      case Left(Errors(NotFoundError :: Nil)) => Left(Errors(NotFoundError))
      case Left(_) => Left(Errors(InternalServiceError))
      case Right(info) =>
        val pages: List[PageReview] = info.pages.map(p => PageReview(p.id, p.pageTitle, p.pageUrl, p.status))
        Right(ProcessReview(info.id, info.ocelotId, info.version, info.reviewType, info.title, info.lastUpdated, pages))
    }
  }

  def checkProcessInCorrectStateForCompletion(id: String, reviewType: String): Future[RequestOutcome[ApprovalProcess]] = {
    getApprovalProcessToUpdate(id) flatMap {
      case Right(approvalProcess) =>
        // Check that all pages have been reviewed
        getReviewInfo(id, reviewType, approvalProcess.version) map {
          case Right(info) if info.pages.count(p => p.status == InitialPageReviewStatus) > 0 =>
            logger.error(s"$reviewType Complete - request invalid - not all pages reviewed")
            Left(Errors(IncompleteDataError))
          case Right(_) => Right(approvalProcess)
          case Left(errors) =>
            logger.error(s"$reviewType Complete - request invalid - $errors")
            Left(errors)
        }
      case Left(errors) => Future.successful(Left(errors))
    }
  }

}
