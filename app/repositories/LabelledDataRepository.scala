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

import javax.inject.{Inject, Singleton}
import java.time.Instant
import core.models.errors.{DatabaseError, NotFoundError}
import core.models.RequestOutcome
import play.api.libs.json.JsValue
import play.api.Logger
import config.AppConfig
import scala.concurrent.{ExecutionContext, Future}
import models.{LabelledDataId, LabelledData}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import core.models.MongoDateTimeFormats.Implicits._

//$COVERAGE-OFF$
trait LabelledDataRepository {
  def save(id: LabelledDataId, data: JsValue, when: Instant, credId: String, user: String, email: String): Future[RequestOutcome[LabelledData]]
  def get(id: LabelledDataId): Future[RequestOutcome[LabelledData]]
}

@Singleton
class LabelledDataRepositoryImpl @Inject() (component: MongoComponent, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[LabelledData](
      collectionName = "labelledData",
      mongoComponent = component,
      domainFormat = LabelledData.format,
      extraCodecs = Seq(Codecs.playFormatCodec(mdInstantFormat)),
      indexes = Seq.empty
    )
    with LabelledDataRepository {
  val logger: Logger = Logger(getClass)
  override lazy val requiresTtlIndex: Boolean = false

  def save(id: LabelledDataId, data: JsValue, when: Instant, credId: String, user: String, email: String): Future[RequestOutcome[LabelledData]] =
    collection
      .findOneAndUpdate(
        equal("_id", id.toString()),
        combine(
          set("data", Codecs.toBson(data)),
          set("when", Codecs.toBson(when)),
          set("credId", credId),
          set("user", user),
          set("email", email)
        ),
        FindOneAndUpdateOptions()
          .upsert(true)
          .returnDocument(ReturnDocument.AFTER)
      )
      .toFutureOption()
      .map{
        case None =>
          logger.error(s"Failed to update/insert LabelledData")
          Left(DatabaseError)
        case Some(labelledData) => Right(labelledData)
      }
      .recover {
        case e =>
          logger.warn(s"Failed to update/insert LabelledData due to error, ${e.getMessage}")
          Left(DatabaseError)
      }

  def get(id: LabelledDataId): Future[RequestOutcome[LabelledData]] =
    collection
      .find(equal("_id", id.toString()))
      .headOption()
      .map {
        case None => Left(NotFoundError)
        case Some(labelledData) => Right(labelledData)
      }
      .recover {
        case e =>
          logger.warn(s"Failed to retrieve LabelledData due to error, ${e.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

}
