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

import javax.inject.{Inject, Singleton}
import java.time.ZonedDateTime
import core.models.errors.{DatabaseError, NotFoundError}
import core.models.RequestOutcome
import play.api.libs.json.{Format, Json, JsValue}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.mongo.ReactiveRepository
import config.AppConfig
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.TimescalesUpdate

trait TimescalesRepository {
  val CurrentTimescalesID: String = "1"
  def save(timescales: JsValue, when: ZonedDateTime, credId: String, user: String, email: String): Future[RequestOutcome[TimescalesUpdate]]
  def get(id: String): Future[RequestOutcome[TimescalesUpdate]]
}

@Singleton
class TimescalesRepositoryImpl @Inject() (mongoComponent: ReactiveMongoComponent, appConfig: AppConfig)
    extends ReactiveRepository[TimescalesUpdate, String](
      collectionName = "timescales",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = TimescalesUpdate.format,
      idFormat = implicitly[Format[String]]
    )
    with TimescalesRepository {

  def save(timescales: JsValue, when: ZonedDateTime, credId: String, user: String, email: String): Future[RequestOutcome[TimescalesUpdate]] =
    //$COVERAGE-OFF$
    findAndUpdate(
      Json.obj("_id" -> CurrentTimescalesID),
      Json.obj(
      "$set" -> Json.obj(
        "timescales" -> timescales,
        "when" -> Json.obj("$date" -> when.toInstant.toEpochMilli),
        "credId" -> credId,
        "user" -> user,
        "email" -> email
      )), upsert = true).map{ update =>
        update.result[TimescalesUpdate].fold[RequestOutcome[TimescalesUpdate]]{
          logger.error(s"Failed to find and update/insert TimescalesUpdate")
          Left(DatabaseError)
        }{ tsUpdate =>
          Right(tsUpdate)
        }
      }
      .recover {
        case e =>
          logger.warn(e.getMessage)
          Left(DatabaseError)
      }
      //$COVERAGE-ON$

  def get(id: String): Future[RequestOutcome[TimescalesUpdate]] =
    //$COVERAGE-OFF$
    findById(id)
      .map {
        case Some(tsUpdate) => Right(tsUpdate)
        case None => Left(NotFoundError)
      }
      .recover {
        case e =>
          logger.warn(e.getMessage)
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

}
