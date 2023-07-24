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

package testOnly.repositories

import javax.inject.{Inject, Singleton}
import core.models.errors.DatabaseError
import core.models.RequestOutcome
import models.ApprovalProcessReview
import play.api.Logger
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApprovalProcessReviewRepository @Inject() (implicit component: MongoComponent, ec: ExecutionContext)
    extends PlayMongoRepository[ApprovalProcessReview](
      collectionName = "approvalProcessReviews",
      mongoComponent = component,
      domainFormat = ApprovalProcessReview.mongoFormat,
      indexes = Seq.empty
    ) {

  val logger: Logger = Logger(getClass())

  def delete(id: String): Future[RequestOutcome[String]] = {

    logger.info(s"[test-only] Deleting approval reviews with the Ocelot ID $id")

    collection
      .deleteOne(equal("ocelotId",id))
      .toFuture
      .map { _ =>
        Right(id)
      }
      .recover {
        case error =>
          logger.error(s"[test-only] Failed to delete approval reviews with the Ocelot ID $id", error)
          Left(DatabaseError)
      }
  }

}
