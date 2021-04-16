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
import core.models.errors.{DatabaseError, DuplicateKeyError, NotFoundError}
import models.PublishedProcess
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions}
import play.api.Logger.logger
import play.api.libs.json.{JsObject, JsResultException, Json}
import repositories.formatters.PublishedProcessFormatter
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait PublishedRepository {

  def save(id: String, user: String, processCode: String, process: JsObject): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[PublishedProcess]]
  def getByProcessCode(processCode: String): Future[RequestOutcome[PublishedProcess]]
}

@Singleton
class PublishedRepositoryImpl @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[PublishedProcess](
      collectionName = "publishedProcesses",
      mongoComponent = mongoComponent,
      domainFormat = PublishedProcessFormatter.mongoFormat,
      indexes = Seq(IndexModel(
        ascending("processCode"),
        IndexOptions().name("published-secondary-Index-process-code").unique(true))
      ),
      replaceIndexes = false
    )
    with PublishedRepository {

  def save(id: String, user: String, processCode: String, process: JsObject): Future[RequestOutcome[String]] = {

    logger.info(s"Saving process $id to collection published")

    collection.findOneAndUpdate(
      equal("_id", id),
      combine(
        set("process", process),
        set("publishedBy", user),
        set("processCode", processCode),
        set("datePublished", Json.obj("$date" -> ZonedDateTime.now.toInstant.toEpochMilli))
      ),
      FindOneAndUpdateOptions().upsert(true)
    ).toFuture()
      .map( _ => Right(id) )
      //$COVERAGE-OFF$
      .recover {
        case e: JsResultException if hasDupeKeyViolation(e) =>
          logger.error(s"Failed to publish $id due to duplicate key violation on processCode : $processCode")
          Left(DuplicateKeyError)
        case error =>
          logger.error(s"Attempt to persist process $id to collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }

  def getById(id: String): Future[RequestOutcome[PublishedProcess]] = {

    collection.find(equal("_id", id))
      .first()
      .toFutureOption()
      .map {
        case Some(publishedProcess) => Right(publishedProcess)
        case None => Left(NotFoundError)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }

  def getByProcessCode(processCode: String): Future[RequestOutcome[PublishedProcess]] = {

    collection
      .find(equal("processCode", processCode))
      .first()
      .toFutureOption()
      .map {
        case Some(publishedProcess) => Right(publishedProcess)
        case None => Left(NotFoundError)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $processCode from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }

}
