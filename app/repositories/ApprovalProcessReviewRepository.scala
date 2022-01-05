/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.ZonedDateTime
import java.util.UUID

import javax.inject.{Inject, Singleton}
import core.models.errors.{DatabaseError, NotFoundError}
import core.models.RequestOutcome
import models.{ApprovalProcessPageReview, ApprovalProcessReview}
import play.api.Logger
import core.models.MongoDateTimeFormats.MongoImplicits._
import repositories.formatters.ApprovalProcessReviewFormatter
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ApprovalProcessReviewRepository {
  def save(review: ApprovalProcessReview): Future[RequestOutcome[UUID]]
  def getByIdVersionAndType(id: String, version: Int, reviewType: String): Future[RequestOutcome[ApprovalProcessReview]]
  def updateReview(id: String, version: Int, reviewType: String, updateUser: String, result: String): Future[RequestOutcome[Unit]]
  def updatePageReview(id: String, version: Int, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]]
}

@Singleton
class ApprovalProcessReviewRepositoryImpl @Inject() (implicit mongo: MongoComponent)
    extends PlayMongoRepository[ApprovalProcessReview](
      mongoComponent = mongo,
      collectionName = "approvalProcessReviews",
      domainFormat = ApprovalProcessReviewFormatter.mongoFormat,
      indexes = Seq(IndexModel(ascending("ocelotId", "version", "reviewType"),
                               IndexOptions()
                                .name("review-secondary-Index")
                                .unique(true))),
      extraCodecs = Seq(Codecs.playFormatCodec(mdZonedDateTimeFormat)),
      replaceIndexes = true
    )
    with ApprovalProcessReviewRepository {
  val logger: Logger = Logger(getClass)

  def save(review: ApprovalProcessReview): Future[RequestOutcome[UUID]] =
    collection.insertOne(review)
      .toFuture()
      .map { _ =>
        Right(review.id)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to persist process ${review.id} to collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

  def getByIdVersionAndType(id: String, version: Int, reviewType: String): Future[RequestOutcome[ApprovalProcessReview]] =
    collection
      .find(and(equal("ocelotId",id), equal("version", version), equal("reviewType", reviewType)))
      .toFuture()
      .map {
        case Nil => Left(NotFoundError)
        case review :: _ => Right(review)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve review $id and version $version from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

  def updatePageReview(id: String, version: Int, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]] = {
    val selector = and(equal("ocelotId", id),
                       equal("version", version),
                       equal("reviewType", reviewType),
                       equal("pages.pageUrl", pageUrl))
    val modifier = combine(
      set("pages.$.result", reviewInfo.result.getOrElse("")),
      set("pages.$.status",reviewInfo.status),
      set("pages.$.comment",reviewInfo.comment.getOrElse("")),
      set("pages.$.updateUser", reviewInfo.updateUser.getOrElse("")),
      set("pages.$.updateDate", Codecs.toBson(ZonedDateTime.now))
    )

    collection
      .findOneAndUpdate(selector, modifier)
      .toFutureOption()
      .map{_.fold[RequestOutcome[Unit]](Left(NotFoundError))( _ => Right(()))}
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to update page review $id and pageUrl $pageUrl from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def updateReview(id: String, version: Int, reviewType: String, updateUser: String, result: String): Future[RequestOutcome[Unit]] = {

    val selector = and(equal("ocelotId", id), equal("version", version), equal("reviewType", reviewType))
    val modifier = combine(
      set("result", result),
      set("completionUser", updateUser),
      set("completionDate", Codecs.toBson(ZonedDateTime.now))
    )

    collection
      .findOneAndUpdate(selector, modifier)
      .toFutureOption
      .map{_.fold[RequestOutcome[Unit]](Left(NotFoundError))( _ => Right(()))}
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to update review $id from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }
}
