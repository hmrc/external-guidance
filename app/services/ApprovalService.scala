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

import java.util.UUID

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models._
import models.errors.{Errors, InternalServiceError, NotFoundError}
import play.api.Logger
import play.api.libs.json._
import repositories.{ApprovalProcessReviewRepository, ApprovalRepository}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApprovalService @Inject() (repository: ApprovalRepository,
                                 reviewRepository: ApprovalProcessReviewRepository,
                                 pageBuilder: PageBuilder,
                                 appConfig: AppConfig) {

  val logger: Logger = Logger(this.getClass)

  def save(jsonProcess: JsObject, reviewType: String, initialStatus: String): Future[RequestOutcome[String]] = {

    def saveReview(approvalProcessReview: ApprovalProcessReview): Future[RequestOutcome[String]] = {
      reviewRepository.save(approvalProcessReview) map {
        case Right(_) => Right(approvalProcessReview.ocelotId)
        case _ => Left(Errors(InternalServiceError))
      }
    }

    toGuidancePages(pageBuilder, jsonProcess).fold(
      err => Future.successful(Left(err)),
      t => {
        val (process, pages) = t
        val processMetaSection = ApprovalProcessMeta(process.meta.id, process.meta.title, initialStatus, reviewType = reviewType)       
          repository.update(ApprovalProcess(process.meta.id, processMetaSection, jsonProcess)) flatMap {
            case Right(savedId) =>
              repository.getById(savedId) flatMap {
                case Right(approvalProcess) =>
                  saveReview(ApprovalProcessReview(
                              UUID.randomUUID(),
                              process.meta.id,
                              approvalProcess.version,
                              reviewType,
                              process.meta.title,
                              pageBuilder.fromPageDetails(pages)(ApprovalProcessPageReview(_,_,_))
                            ))
                case Left(Errors(NotFoundError :: Nil)) => Future.successful(Left(Errors(NotFoundError)))
                case Left(_) => Future.successful(Left(Errors(InternalServiceError)))
              }
            case _ => Future.successful(Left(Errors(InternalServiceError)))
          }
       }
    )

  }

  def getById(id: String): Future[RequestOutcome[JsObject]] =
    repository.getById(id) map {
      case Left(Errors(NotFoundError :: Nil)) => Left(Errors(NotFoundError))
      case Left(_) => Left(Errors(InternalServiceError))
      case Right(result) => Right(result.process)
    }


  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[JsArray]] = {
    repository.approvalSummaryList(roles).map {
      case Left(_) => Left(Errors(InternalServiceError))
      case Right(success) => Right(Json.toJson(success).as[JsArray])
    }
  }

}
