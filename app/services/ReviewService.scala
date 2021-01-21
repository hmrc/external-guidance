/*
 * Copyright 2021 HM Revenue & Customs
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
import core.models._
import Constants._
import core.models.errors._
import core.models.ocelot.Process
import play.api.Logger
import repositories.{ApprovalProcessReviewRepository, ApprovalRepository}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReviewService @Inject() (publishedService: PublishedService, repository: ApprovalRepository, reviewRepository: ApprovalProcessReviewRepository) {

  val logger: Logger = Logger(this.getClass)

  def approvalReviewInfo(id: String, reviewType: String): Future[RequestOutcome[ProcessReview]] =
    repository.getById(id) flatMap {
      case Left(NotFoundError) => Future.successful(Left(NotFoundError))
      case Left(_) => Future.successful(Left(InternalServerError))
      case Right(process) =>
        publishedService.getByProcessCode(process.meta.processCode) flatMap {
          case Right(p) if p.id != process.meta.id =>
            logger.error(s"Attempt to review approval process ${process.meta.id} with code ${process.meta.processCode} " +
              s": duplicate key in published collection for process ${p.id}")
            Future.successful(Left(DuplicateKeyError))
          case _ =>
            getReviewInfo(id, reviewType, process.version)
        }
    }

  def approvalPageInfo(id: String, pageUrl: String, reviewType: String): Future[RequestOutcome[ApprovalProcessPageReview]] =
    repository.getById(id) flatMap {
      case Left(NotFoundError) => Future.successful(Left(NotFoundError))
      case Left(_) => Future.successful(Left(InternalServerError))
      case Right(process) =>
        reviewRepository.getByIdVersionAndType(id, process.version, reviewType) map {
          case Left(NotFoundError) => Left(NotFoundError)
          case Left(_) => Left(InternalServerError)
          case Right(info) =>
            info.pages.find(p => p.pageUrl == pageUrl) match {
              case Some(page) => Right(page)
              case _ => Left(NotFoundError)
            }
        }
    }

  def twoEyeReviewComplete(id: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[AuditInfo]] = {

    def publishIfRequired(approvalProcess: ApprovalProcess): Future[RequestOutcome[ApprovalProcess]] = info.status match {
      case StatusPublished =>
        publishedService.save(id, info.userId, approvalProcess.meta.processCode, approvalProcess.process) map {
          case Right(_) => Right(approvalProcess)
          case Left(DuplicateKeyError) => Left(DuplicateKeyError)
          case Left(errors) =>
            logger.error(s"Failed to publish $id - $errors")
            Left(errors)
        }
      case _ => Future.successful(Right(approvalProcess))
    }
    checkProcessInCorrectStateForCompletion(id, ReviewType2i) flatMap {
      case Right(ap) =>
        publishIfRequired(ap).flatMap {
          case Right(ap) =>
            reviewRepository.updateReview(id, ap.version, ReviewType2i, info.userId, info.status) flatMap {
              case Right(()) =>
                changeStatus(id, info.status, info.userId, ReviewType2i) map {
                  case Right(_) => validateProcess(ap, info)
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
          case Right(_) =>
            changeStatus(id, info.status, info.userId, ReviewTypeFactCheck) map {
              case Right(_) => validateProcess(ap, info)
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

  private def validateProcess(ap: ApprovalProcess, info: ApprovalProcessStatusChange): RequestOutcome[AuditInfo] =
    ap.process
      .validate[Process]
      .fold(
        _ => Left(BadRequestError),
        process => Right(AuditInfo(info.userId, ap, process))
      )

  def approvalPageComplete(id: String, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]] =
    repository.getById(id) flatMap {
      case Left(NotFoundError) =>
        logger.warn(s"approvalPageComplete - process $id not found.")
        Future.successful(Left(NotFoundError))
      case Left(_) => Future.successful(Left(InternalServerError))
      case Right(process) =>
        reviewRepository.updatePageReview(process.id, process.version, pageUrl, reviewType, reviewInfo) flatMap {
          case Left(NotFoundError) =>
            logger.warn(s"updatePageReview failed for process $id, version ${process.version}, reviewType $reviewType and pageUrl $pageUrl not found.")
            Future.successful(Left(NotFoundError))
          case Left(err) =>
            logger.warn(s"updatePageReview failed with err $err for process $id, version ${process.version}, reviewType $reviewType " +
              s"and pageUrl $pageUrl not found.")
            Future.successful(Left(InternalServerError))
          case Right(_) =>
            changeStatus(id, "InProgress", reviewInfo.updateUser.getOrElse("System"), reviewType).map{
              case Left(err) =>
                logger.error(s"changeStatus failed with err $err for process $id, version ${process.version}, reviewType $reviewType " +
                  s"and pageUrl $pageUrl not found. Continuing")
                Right(())
              case ok @ Right(_) => ok
            }
        }
    }

  private def changeStatus(id: String, status: String, userId: String, reviewType: String): Future[RequestOutcome[Unit]] =
    repository.changeStatus(id, status, userId) map {
      case Left(DatabaseError) =>
        logger.error(s"$reviewType - database error changing status")
        Left(InternalServerError)
      case error @ Left(NotFoundError) =>
        logger.warn(s"$reviewType: Change Status: process $id was not found")
        error
      case Left(errors) =>
        logger.error(s"$reviewType: changeStatus: for $id - $errors")
        Left(errors)
      case Right(_) => Right(())
    }

  private def getApprovalProcessToUpdate(id: String): Future[RequestOutcome[ApprovalProcess]] = repository.getById(id) map {
    case Right(process) if StatusAllowedForReviewCompletion.contains(process.meta.status) => Right(process)
    case Right(process) =>
      logger.warn(
        s"Invalid Process Status Change requested for process $id: " +
          s"Expected Status One Of: '${StatusAllowedForReviewCompletion.mkString}' Status Found: '${process.meta.status}'"
      )
      Left(StaleDataError)
    case Left(errors) =>
      logger.warn(s"getApprovalProcessToUpdate - error retrieving process $id - error returned $errors.")
      Left(errors)
  }

  private def getReviewInfo(id: String, reviewType: String, version: Int): Future[RequestOutcome[ProcessReview]] = {
    reviewRepository.getByIdVersionAndType(id, version, reviewType) map {
      case Left(NotFoundError) => Left(NotFoundError)
      case Left(_) => Left(InternalServerError)
      case Right(info) =>
        val pages: List[PageReview] = info.pages.map(p => PageReview(p.id, p.pageTitle, p.pageUrl, p.status, p.result))
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
            Left(IncompleteDataError)
          case Right(_) => Right(approvalProcess)
          case Left(errors) =>
            logger.error(s"$reviewType Complete - request invalid - $errors")
            Left(errors)
        }
      case Left(errors) => Future.successful(Left(errors))
    }
  }

}
