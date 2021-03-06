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

import java.util.UUID
import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import core.models.errors.{DatabaseError, NotFoundError}
import core.models.RequestOutcome
import models.ScratchProcess
import play.api.libs.json.{Format, JsObject}
import play.modules.reactivemongo.ReactiveMongoComponent
import repositories.formatters.ScratchProcessFormatter
import uk.gov.hmrc.mongo.ReactiveRepository
import reactivemongo.bson.BSONDocument
import config.AppConfig
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.indexes.Index

trait ScratchRepository {
  def save(process: JsObject): Future[RequestOutcome[UUID]]
  def getById(id: UUID): Future[RequestOutcome[JsObject]]
}

@Singleton
class ScratchRepositoryImpl @Inject() (mongoComponent: ReactiveMongoComponent, appConfig: AppConfig)
    extends ReactiveRepository[ScratchProcess, UUID](
      collectionName = "scratchProcesses",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = ScratchProcessFormatter.mongoFormat,
      idFormat = implicitly[Format[UUID]]
    )
    with ScratchRepository {

  override def indexes: Seq[Index] = Seq(
    Index(Seq("expireAt" -> IndexType.Ascending), name = Some("expiryIndex"), options = BSONDocument("expireAfterSeconds" -> 0))
  )

  def save(process: JsObject): Future[RequestOutcome[UUID]] = {
    val expiryTime = ZonedDateTime.now.withHour(appConfig.scratchExpiryHour).withMinute(appConfig.scratchExpiryMinutes).withSecond(0)
    val document = ScratchProcess(UUID.randomUUID(), process, expiryTime)

    insert(document)
      .map { _ =>
        Right(document.id)
      }
      //$COVERAGE-OFF$
      .recover {
        case e =>
          logger.warn(e.getMessage)
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def getById(id: UUID): Future[RequestOutcome[JsObject]] = {
    findById(id)
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
