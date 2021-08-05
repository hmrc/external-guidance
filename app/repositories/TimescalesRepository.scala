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

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import core.models.errors.{DatabaseError, NotFoundError}
import core.models.RequestOutcome
import play.api.libs.json.{Format, Json, JsValue}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.mongo.ReactiveRepository
import config.AppConfig
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import repositories.formatters.TimescalesFormatter

trait TimescalesRepository {
  val CurrentTimescalesID: String = "1"
  def save(timescales: JsValue): Future[RequestOutcome[Unit]]
  def get(id: String): Future[RequestOutcome[JsValue]]
}

case class Timescales(id: String, timescales: JsValue, when: ZonedDateTime)

@Singleton
class TimescalesRepositoryImpl @Inject() (mongoComponent: ReactiveMongoComponent, appConfig: AppConfig)
    extends ReactiveRepository[Timescales, String](
      collectionName = "timescales",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = TimescalesFormatter.mongoFormat,
      idFormat = implicitly[Format[String]]
    )
    with TimescalesRepository {

  def save(timescales: JsValue): Future[RequestOutcome[Unit]] =
    //$COVERAGE-OFF$
    findAndUpdate(
      Json.obj("_id" -> CurrentTimescalesID),
      Json.obj(
      "$set" -> Json.obj(
        "timescales" -> timescales,
        "when" -> Json.obj("$date" -> ZonedDateTime.now.toInstant.toEpochMilli)
      )
    ), upsert = true).map(_ => Right(()))
      .recover {
        case e =>
          logger.warn(e.getMessage)
          Left(DatabaseError)
      }
      //$COVERAGE-ON$

  def get(id: String): Future[RequestOutcome[JsValue]] =
    //$COVERAGE-OFF$
    findById(id)
      .map {
        case Some(data) => Right(data.timescales)
        case None => Left(NotFoundError)
      }
      .recover {
        case e =>
          logger.warn(e.getMessage)
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

}
