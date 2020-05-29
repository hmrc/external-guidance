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
import models.errors.{Errors, InternalServiceError, NotFoundError}
import models.{ApprovalProcessStatusChange, PageReview, ProcessReview, RequestOutcome}
import play.api.Logger
import repositories.{ApprovalProcessReviewRepository, ApprovalRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReviewService @Inject() (repository: ApprovalRepository, reviewRepository: ApprovalProcessReviewRepository) {

  val logger: Logger = Logger(this.getClass)

  def approval2iReviewInfo(id: String): Future[RequestOutcome[ProcessReview]] = {
    repository.getById(id) flatMap {
      case Left(Errors(NotFoundError :: Nil)) => Future.successful(Left(Errors(NotFoundError)))
      case Left(_) => Future.successful(Left(Errors(InternalServiceError)))
      case Right(process) =>
        reviewRepository.getByIdVersionAndType(id, process.version, "2i-review") map {
          case Left(Errors(NotFoundError :: Nil)) => Left(Errors(NotFoundError))
          case Left(_) => Left(Errors(InternalServiceError))
          case Right(info) =>
            val pages: List[PageReview] = info.pages.map(p => PageReview(p.id, p.pageUrl, p.status))
            Right(ProcessReview(info.id, info.ocelotId, info.version, info.title, info.lastUpdated, pages))
        }
    }
  }

  def changeStatus(id: String, info: ApprovalProcessStatusChange): Future[RequestOutcome[Unit]] = {
    repository.changeStatus(id, info) map {
      case error @ Left(Errors(NotFoundError :: Nil)) => error
      case Left(_) => Left(Errors(InternalServiceError))
      case result => result
    }
  }

}
