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

import java.util.UUID
import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import core.models.errors.{DatabaseError, NotFoundError}
import core.models.RequestOutcome
import models.ScratchProcess
import play.api.libs.json.JsObject
import config.AppConfig
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.Logger
import java.util.concurrent.TimeUnit
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.play.json.formats.MongoUuidFormats.Implicits.uuidFormat
import org.mongodb.scala.result.InsertOneResult

trait ScratchRepository {
  def save(process: JsObject): Future[RequestOutcome[UUID]]
  def getById(id: UUID): Future[RequestOutcome[JsObject]]
}

@Singleton
class ScratchRepositoryImpl @Inject() (component: MongoComponent, appConfig: AppConfig)
    extends PlayMongoRepository[ScratchProcess](
      collectionName = "scratchProcesses",
      mongoComponent = component,
      domainFormat = ScratchProcess.mongoFormat,
      indexes = Seq(IndexModel(ascending("expireAt"),
                               IndexOptions()
                                .name("expiryIndex")
                                .unique(false)
                                .expireAfter(0, TimeUnit.SECONDS))),
      extraCodecs = Seq(Codecs.playFormatCodec(uuidFormat)),
      replaceIndexes = true
    )
    with ScratchRepository {
  val logger: Logger = Logger(getClass)

  //$COVERAGE-OFF$
  def save(process: JsObject): Future[RequestOutcome[UUID]] = {
    val expiryTime = ZonedDateTime.now.withHour(appConfig.scratchExpiryHour).withMinute(appConfig.scratchExpiryMinutes).withSecond(0)
    val document = ScratchProcess(UUID.randomUUID(), process, expiryTime)

    collection
      .insertOne(document)
      .toFutureOption
      .map {
        case Some(r: InsertOneResult) if r.wasAcknowledged => Right(document.id)
        case _ =>
          logger.error(s"Failed to save scratch process")
          Left(DatabaseError)
      }
      .recover {
        case e =>
          logger.warn(e.getMessage)
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def getById(id: UUID): Future[RequestOutcome[JsObject]] =
    collection
      .find(equal("_id", id))
      .headOption()
      .map {
        case None => Left(NotFoundError)
        case Some(data) => Right(data.process)
      }
      //$COVERAGE-OFF$
      .recover {
        case e =>
          logger.warn(e.getMessage)
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

}
