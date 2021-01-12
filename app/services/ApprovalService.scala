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

import java.util.UUID

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models._
import models.errors.{DuplicateKeyError, InternalServerError, NotFoundError}
import play.api.Logger
import play.api.libs.json._
import repositories.{ApprovalProcessReviewRepository, ApprovalRepository, PublishedRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApprovalService @Inject() (
    repository: ApprovalRepository,
    reviewRepository: ApprovalProcessReviewRepository,
    publishedRepository: PublishedRepository,
    pageBuilder: PageBuilder,
    secureProcessBuilder: SecureProcessBuilder,
    appConfig: AppConfig
) {

  val logger: Logger = Logger(this.getClass)

  def save(incomingJson: JsObject, reviewType: String, initialStatus: String): Future[RequestOutcome[String]] =
    guidancePages(pageBuilder, secureProcessBuilder, incomingJson).fold(
      err => Future.successful(Left(err)),
      t => {
        val (process, pages, json) = t
        val processMetaSection =
          ApprovalProcessMeta(
            process.meta.id,
            process.meta.title,
            initialStatus,
            reviewType = reviewType,
            processCode = process.meta.processCode)

        // Check no published process with this processCode for another processId
        publishedRepository.getByProcessCode(process.meta.processCode) flatMap {
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
                        pageBuilder.fromPageDetails(pages)(ApprovalProcessPageReview(_, _, _))
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
    )

  private def saveReview(approvalProcessReview: ApprovalProcessReview): Future[RequestOutcome[String]] =
    reviewRepository.save(approvalProcessReview) map {
      case Right(_) => Right(approvalProcessReview.ocelotId)
      case _ => Left(InternalServerError)
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

  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[JsArray]] = {
    repository.approvalSummaryList(roles).map {
      case Left(_) => Left(InternalServerError)
      case Right(success) => Right(Json.toJson(success).as[JsArray])
    }
  }

}
