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

import config.AppConfig
import core.models.RequestOutcome
import core.models.errors.{DatabaseError, NotFoundError}
import models.ScratchProcess
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.Logger.logger
import play.api.libs.json.JsObject
import repositories.formatters.ScratchProcessFormatter
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.SECONDS

trait ScratchRepository {
  def save(process: JsObject): Future[RequestOutcome[UUID]]
  def getById(id: UUID): Future[RequestOutcome[JsObject]]
}

@Singleton
class ScratchRepositoryImpl @Inject() (mongoComponent: MongoComponent, appConfig: AppConfig)
    extends PlayMongoRepository[ScratchProcess](
      collectionName = "scratchProcesses",
      mongoComponent = mongoComponent,
      domainFormat = ScratchProcessFormatter.mongoFormat,
      indexes = Seq(IndexModel(ascending("expireAt"), IndexOptions().name("expiryIndex").expireAfter(0, SECONDS))),
      replaceIndexes = false
    )
    with ScratchRepository {

  def save(process: JsObject): Future[RequestOutcome[UUID]] = {
    val expiryTime = ZonedDateTime.now.withHour(appConfig.scratchExpiryHour).withMinute(appConfig.scratchExpiryMinutes).withSecond(0)
    val document = ScratchProcess(UUID.randomUUID(), process, expiryTime)

    collection.insertOne(document)
      .toFuture()
      .map ( _ => Right(document.id) )
      //$COVERAGE-OFF$
      .recover {
        case e =>
          logger.warn(e.getMessage)
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }

  def getById(id: UUID): Future[RequestOutcome[JsObject]] = {
    collection.find(equal("_id", id))
      .first()
      .toFutureOption()
      .map {
        case Some(data) => Right(data.process)
        case None => Left(NotFoundError)
      }
      //$COVERAGE-OFF$
      .recover {
        case e =>
          logger.warn(e.getMessage)
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }

}
