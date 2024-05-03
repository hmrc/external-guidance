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

package migrate.repositories

import javax.inject.{Inject, Singleton}
import core.models.errors.{DatabaseError, NotFoundError}
import core.models.RequestOutcome
import models.ApprovalProcessPageReview
import migrate.models.ApprovalProcessReview
import play.api.Logger
import core.models.MongoDateTimeFormats.Implicits._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import scala.concurrent.{ExecutionContext, Future}

trait ApprovalProcessReviewRepository {
  def getByIdVersionAndType(id: String, version: Int, reviewType: String): Future[RequestOutcome[ApprovalProcessReview]]
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
}
