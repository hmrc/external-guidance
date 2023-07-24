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

package repositories

import javax.inject.{Inject, Singleton}
import java.time.ZonedDateTime
import core.models.errors.{DatabaseError, NotFoundError}
import core.models.RequestOutcome
import play.api.libs.json.JsValue
import play.api.Logger
import config.AppConfig

import scala.concurrent.{ExecutionContext, Future}
import models.TimescalesUpdate
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import core.models.MongoDateTimeFormats.zonedDateTimeFormat
import core.models.MongoDateTimeFormats.Implicits._

//$COVERAGE-OFF$
trait TimescalesRepository {
  val CurrentTimescalesID: String = "1"
  def save(timescales: JsValue, when: ZonedDateTime, credId: String, user: String, email: String): Future[RequestOutcome[TimescalesUpdate]]
  def get(id: String): Future[RequestOutcome[TimescalesUpdate]]
}

@Singleton
class TimescalesRepositoryImpl @Inject() (component: MongoComponent, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[TimescalesUpdate](
      collectionName = "timescales",
      mongoComponent = component,
      domainFormat = TimescalesUpdate.format,
      extraCodecs = Seq(Codecs.playFormatCodec(zonedDateTimeFormat)),
      indexes = Seq.empty
    )
    with TimescalesRepository {
  val logger: Logger = Logger(getClass)

  def save(timescales: JsValue, when: ZonedDateTime, credId: String, user: String, email: String): Future[RequestOutcome[TimescalesUpdate]] =
    collection
      .findOneAndUpdate(
        equal("_id", CurrentTimescalesID),
        combine(
          set("timescales", Codecs.toBson(timescales)),
          set("when", Codecs.toBson(when.toInstant)),
          set("credId", credId),
          set("user", user),
          set("email", email)
        ),
        FindOneAndUpdateOptions()
          .upsert(true)
          .returnDocument(ReturnDocument.AFTER)
      )
      .toFutureOption
      .map{
        case None =>
          logger.error(s"Failed to find and update/insert TimescalesUpdate")
          Left(DatabaseError)
        case Some(tsUpdate) => Right(tsUpdate)
      }
      .recover {
        case e =>
          logger.warn(s"Failed to save TimescalesUpdate due to error, ${e.getMessage}")
          Left(DatabaseError)
      }

  def get(id: String): Future[RequestOutcome[TimescalesUpdate]] =
    collection
      .find(equal("_id", id))
      .headOption()
      .map {
        case None => Left(NotFoundError)
        case Some(tsUpdate) => Right(tsUpdate)
      }
      .recover {
        case e =>
          logger.warn(s"Failed to retrieve TimescalesUpdate due to error, ${e.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

}
