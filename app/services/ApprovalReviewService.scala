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

import config.AppConfig

import javax.inject.{Inject, Singleton}
import models._
import Constants._
import core.models._
import core.models.errors._
import core.services.fromPageDetails
import play.api.Logger
import play.api.libs.json._
import repositories.{ApprovalsRepository, PublishedRepository}
import core.models.ocelot.Process
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{Json, OFormat}

@Singleton
class ApprovalReviewService @Inject() (
    repository: ApprovalsRepository,
    publishedRepository: PublishedRepository,
    publishedService: PublishedService,
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
            val review = ApprovalReview(fromPageDetails(pages)(ApprovalProcessPageReview(_, _, _)))
            repository.createOrUpdate(Approval(process.meta.id, processMetaSection, review, json))
        }
    }

  // private def saveReview(approvalProcessReview: ApprovalProcessReview): Future[RequestOutcome[String]] =
  //   reviewRepository.save(approvalProcessReview) map {
  //     case Right(_) => Right(approvalProcessReview.ocelotId)
  //     case _ => Left(InternalServerError)
  //   }

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

  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[JsValue]] = {
    implicit val formats: OFormat[ApprovalProcessSummary] = Json.format[ApprovalProcessSummary]

    repository.approvalSummaryList(roles).map {
      case Left(_) => Left(InternalServerError)
      case Right(success) => Right(Json.toJson(success))
    }
  }

  def list: Future[RequestOutcome[JsValue]] = {
    implicit val formats: OFormat[ProcessSummary] = Json.format[ProcessSummary]
    repository.processSummaries() map {
      case Left(_) => Left(InternalServerError)
      case Right(summaries) => Right(Json.toJson(summaries))
    }
  }

  // ReviewService

  def approvalReviewInfo(id: String, reviewType: String): Future[RequestOutcome[ProcessReview]] =
    repository.getById(id) flatMap {
      case Left(NotFoundError) => Future.successful(Left(NotFoundError))
      case Left(_) => Future.successful(Left(InternalServerError))
      case Right(process) =>
        publishedService.getByProcessCode(process.meta.processCode) map {
          case Right(p) if p.id != process.meta.id =>
            logger.error(s"Attempt to review approval process ${process.meta.id} with code ${process.meta.processCode} " +
              s": duplicate key in published collection for process ${p.id}")
            Left(DuplicateKeyError)
          case _ =>
            val pages: List[PageReview] = process.review.pages.map(p => PageReview(p.id, p.pageTitle, p.pageUrl, p.status, p.result))
            Right(ProcessReview(process.id, process.id, process.version, process.meta.reviewType, process.meta.title, process.review.lastUpdated, pages))
        }
    }

  def approvalPageInfo(id: String, pageUrl: String, reviewType: String): Future[RequestOutcome[ApprovalProcessPageReview]] =
    repository.getById(id) map {
      case Left(NotFoundError) => Left(NotFoundError)
      case Left(_) => Left(InternalServerError)
      case Right(approval) =>
        approval.review.pages.find(p => p.pageUrl == pageUrl) match {
          case Some(page) => Right(page)
          case _ => Left(NotFoundError)
        }
    }

  def twoEyeReviewComplete(id: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[AuditInfo]] = {

    def publishIfRequired(approvalProcess: Approval): Future[RequestOutcome[Approval]] = info.status match {
      case StatusPublished =>
        publishedService.save(id, info.userId, approvalProcess.meta.processCode, approvalProcess.process) map {
          case Right(_) =>
            logger.warn(s"PUBLISH: Process $id with processCode ${approvalProcess.meta.processCode} successfully published by ${info.userName}(${info.userId})")
            Right(approvalProcess)
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
            changeStatus(id, info.status, info.userId, ReviewType2i) map {
              case Right(_) => validateProcess(ap, info)
              case Left(error) => Left(error)
            }
          case Left(errors) =>
            logger.error(s"updateReviewOnCompletion: Could not change status of 2i review for process $id, $errors")
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
        repository.updateReview(id, ap.version, ReviewTypeFactCheck, info.userId, info.status) flatMap {
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

  private def validateProcess(ap: Approval, info: ApprovalProcessStatusChange): RequestOutcome[AuditInfo] =
    ap.process
      .validate[Process]
      .fold(
        _ => Left(BadRequestError),
        process => Right(AuditInfo(info.userId, ap.id,  ap.version, ap.meta.title, process))
      )

  def approvalPageComplete(id: String, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]] =
    repository.getById(id) flatMap {
      case Left(NotFoundError) =>
        logger.warn(s"approvalPageComplete - process $id not found.")
        Future.successful(Left(NotFoundError))
      case Left(_) => Future.successful(Left(InternalServerError))
      case Right(process) =>
        repository.updatePageReview(process.id, process.version, pageUrl, reviewType, reviewInfo) flatMap {
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

  def checkProcessInCorrectStateForCompletion(id: String, reviewType: String): Future[RequestOutcome[Approval]] =
    getApprovalProcessToUpdate(id).map{
      case Right(approval) if (approval.review.pages.count(pr => pr.status == InitialPageReviewStatus) > 0) =>
        logger.error(s"$reviewType Complete - request invalid - not all pages reviewed")
        Left(IncompleteDataError)
      case Right(approval) => Right(approval)
      case Left(errors) =>
        logger.error(s"$reviewType Complete - request invalid - $errors")
        Left(errors)
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

  private def getApprovalProcessToUpdate(id: String): Future[RequestOutcome[Approval]] = 
    repository.getById(id) map {
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

  // private[services] def getReviewInfo(id: String, reviewType: String, version: Int): Future[RequestOutcome[ProcessReview]] = {
  //   repository.getByIdVersionAndType(id, version, reviewType) map {
  //     case Left(NotFoundError) => Left(NotFoundError)
  //     case Left(_) => Left(InternalServerError)
  //     case Right(info) =>
  //       val pages: List[PageReview] = info.pages.map(p => PageReview(p.id, p.pageTitle, p.pageUrl, p.status, p.result))
  //       Right(ProcessReview(info.id, info.ocelotId, info.version, info.reviewType, info.title, info.lastUpdated, pages))
  //   }
  // }



}
