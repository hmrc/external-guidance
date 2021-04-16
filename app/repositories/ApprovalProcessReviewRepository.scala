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

package repositories

import core.models.RequestOutcome
import core.models.errors.{DatabaseError, NotFoundError}
import models.{ApprovalProcessPageReview, ApprovalProcessReview}
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.Logger.logger
import play.api.libs.json.Json
import repositories.formatters.ApprovalProcessReviewFormatter
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ApprovalProcessReviewRepository {
  def save(review: ApprovalProcessReview): Future[RequestOutcome[UUID]]
  def getByIdVersionAndType(id: String, version: Int, reviewType: String): Future[RequestOutcome[ApprovalProcessReview]]
  def updateReview(id: String, version: Int, reviewType: String, updateUser: String, result: String): Future[RequestOutcome[Unit]]
  def updatePageReview(id: String, version: Int, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]]
}

@Singleton
class ApprovalProcessReviewRepositoryImpl @Inject() (implicit mongoComponent: MongoComponent)
    extends PlayMongoRepository[ApprovalProcessReview](
      collectionName = "approvalProcessReviews",
      mongoComponent = mongoComponent,
      domainFormat = ApprovalProcessReviewFormatter.mongoFormat,
      indexes = Seq(IndexModel(
        ascending("ocelotId", "version", "reviewType"),
        IndexOptions().name("review-secondary-Index").unique(true))
      ),
      replaceIndexes = false
    )
    with ApprovalProcessReviewRepository {

  def save(review: ApprovalProcessReview): Future[RequestOutcome[UUID]] = {

    collection.insertOne(review).toFuture()
      .map ( _ => Right(review.id) )
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to persist process ${review.id} to collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def getByIdVersionAndType(id: String, version: Int, reviewType: String): Future[RequestOutcome[ApprovalProcessReview]] = {

    collection
      .find(and(equal("ocelotId", id), equal("version", version), equal("reviewType", reviewType)))
      .first()
      .toFutureOption()
      .map {
        case Some(review) => Right(review)
        case None => Left(NotFoundError)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve review $id and version $version from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }

  def updatePageReview(id: String, version: Int, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]] = {

    collection.findOneAndUpdate(
      filter = and(
        equal("ocelotId", id), equal("version", version), equal("reviewType", reviewType), equal("pages.pageUrl", pageUrl)
      ),
      update = combine(
          set("pages.$.result", reviewInfo.result),
          set("pages.$.status", reviewInfo.status),
          set("pages.$.comment", reviewInfo.comment),
          set("pages.$.updateUser", reviewInfo.updateUser),
          set("pages.$.updateDate", Json.obj("$date" -> ZonedDateTime.now.toInstant.toEpochMilli))
        )
      ).toFutureOption()
      .map(_ => Right(()))
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to update page review $id and pageUrl $pageUrl from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }

  def updateReview(id: String, version: Int, reviewType: String, updateUser: String, result: String): Future[RequestOutcome[Unit]] = {

    collection.findOneAndUpdate(
      filter = and(
        equal("ocelotId", id), equal("version", version), equal("reviewType", reviewType)
      ),
      update = combine(
        set("result", result),
        set("completionUser", updateUser),
        set("completionDate", Json.obj("$date" -> ZonedDateTime.now.toInstant.toEpochMilli))
      )
    ).toFutureOption()
    .map(_ => Right(()))
    //$COVERAGE-OFF$
    .recover {
      case error =>
        logger.error(s"Attempt to update review $id from collection $collectionName failed with error : ${error.getMessage}")
        Left(DatabaseError)
    }
    //$COVERAGE-ON$
  }
}
