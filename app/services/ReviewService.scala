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
import models.errors.{Errors, InternalServiceError, NotFoundError}
import play.api.Logger
import repositories.{ApprovalProcessReviewRepository, ApprovalRepository}
import utils.Constants
import utils.Constants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReviewService @Inject() (publishedService: PublishedService,
                               repository: ApprovalRepository,
                               reviewRepository: ApprovalProcessReviewRepository) {

  val logger: Logger = Logger(this.getClass)

  def approval2iReviewInfo(id: String): Future[RequestOutcome[ProcessReview]] = {
    repository.getById(id) flatMap {
      case Left(Errors(NotFoundError :: Nil)) => Future.successful(Left(Errors(NotFoundError)))
      case Left(_) => Future.successful(Left(Errors(InternalServiceError)))
      case Right(process) =>
        reviewRepository.getByIdVersionAndType(id, process.version, ReviewType2i) map {
          case Left(Errors(NotFoundError :: Nil)) => Left(Errors(NotFoundError))
          case Left(_) => Left(Errors(InternalServiceError))
          case Right(info) =>
            val pages: List[PageReview] = info.pages.map(p => PageReview(p.id, p.pageUrl, p.status))
            Right(ProcessReview(info.id, info.ocelotId, info.version, info.reviewType, info.title, info.lastUpdated, pages))
        }
    }
  }

  def changeStatus(id: String, currentStatus: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[Unit]] = {

    def getContentToUpdate: Future[Option[ApprovalProcess]] = {
      repository.getById(id) map  {
        case Right(process) => if (process.meta.status == currentStatus) Some(process) else None
        case _ => None
      }
    }

    getContentToUpdate flatMap  {
      case Some(approvalProcess) =>
        repository.changeStatus(id, info) flatMap  {
          case error @ Left(Errors(NotFoundError :: Nil)) => Future.successful(error)
          case Left(_) => Future.successful(Left(Errors(InternalServiceError)))
          case result =>
            info.status match {
              case Constants.StatusPublished =>
                publishedService.save(id, approvalProcess.process) map {
                  case Right(_) => result
                  case Left(errors) => Left(errors)
                }
              case _ => Future.successful(result)
            }
        }
      case None => Future.successful(Left(Errors(NotFoundError)))
    }
  }

}
