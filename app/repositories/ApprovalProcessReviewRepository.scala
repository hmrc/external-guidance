/*
 * Copyright 2024 HM Revenue & Customs
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
import core.models.MongoDateTimeFormats.Implicits._
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import org.mongodb.scala.bson.conversions.Bson

trait ApprovalProcessReviewRepository {
  def save(review: ApprovalProcessReview): Future[RequestOutcome[UUID]]
  def getByIdVersionAndType(id: String, version: Int, reviewType: String): Future[RequestOutcome[ApprovalProcessReview]]
  def updateReview(id: String, version: Int, reviewType: String, updateUser: String, result: String): Future[RequestOutcome[Unit]]
  def updatePageReview(id: String, version: Int, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]]
  def deleteForApproval(id: String, version: Int, reviewType: String): Future[RequestOutcome[Unit]]
}

@Singleton
class ApprovalProcessReviewRepositoryImpl @Inject() (implicit mongo: MongoComponent, ec: ExecutionContext)
    extends PlayMongoRepository[ApprovalProcessReview](
      mongoComponent = mongo,
      collectionName = "approvalProcessReviews",
      domainFormat = ApprovalProcessReview.mongoFormat,
      indexes = Seq(IndexModel(ascending("ocelotId", "version", "reviewType"),
                               IndexOptions()
                                .name("review-secondary-Index")
                                .unique(true))),
      extraCodecs = Seq(Codecs.playFormatCodec(mdZonedDateTimeFormat),
                        Codecs.playFormatCodec(ApprovalProcessPageReview.mongoFormat)),
      replaceIndexes = true
    )
    with ApprovalProcessReviewRepository {
  val logger: Logger = Logger(getClass)
  override lazy val requiresTtlIndex = false

  def save(review: ApprovalProcessReview): Future[RequestOutcome[UUID]] =
    collection
      .insertOne(review)
      .toFutureOption()
      .map {
        case Some(r: InsertOneResult) if r.wasAcknowledged => Right(review.id)
        case _ =>
          logger.error(s"Failed to insert ApprovalProcessReview: $review")
          Left(DatabaseError)
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
      .headOption()
      .map {
        case None => Left(NotFoundError)
        case Some(review) => Right(review)
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
      Vector(set("pages.$.status",reviewInfo.status), set("pages.$.updateDate", Codecs.toBson(ZonedDateTime.now))) ++
      reviewInfo.result.fold[Vector[Bson]](Vector())(r => Vector(set("pages.$.result", r))) ++
      reviewInfo.updateUser.fold[Vector[Bson]](Vector())(u => Vector(set("pages.$.updateUser", u))): _*
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
      .toFutureOption()
      .map{_.fold[RequestOutcome[Unit]](Left(NotFoundError))( _ => Right(()))}
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to update review $id from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def deleteForApproval(id: String, version: Int, reviewType: String): Future[RequestOutcome[Unit]] = {
    collection
      .deleteOne(and(equal("ocelotId", id), equal("version", version), equal("reviewType", reviewType)))
      .toFutureOption()
      .map {
        case Some(result: DeleteResult) if result.getDeletedCount > 0 => Right(())
        case _ =>
          logger.error(s"Attempt to delete review with ($id, $version, $reviewType) from collection approvalProcessReviews failed")
          Left(DatabaseError)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to delete review with ($id, $version, $reviewType) from collection approvalProcessReviews failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  }
}
