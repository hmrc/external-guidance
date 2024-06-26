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

package testOnly.repositories

import javax.inject.{Inject, Singleton}
import core.models.errors.DatabaseError
import core.models.RequestOutcome
import models.PublishedProcess
import play.api.Logger
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import core.models.MongoDateTimeFormats.zonedDateTimeFormat
import scala.concurrent.{ExecutionContext, Future}
import java.time.ZonedDateTime

@Singleton
class PublishedRepository @Inject() (component: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[PublishedProcess](
      collectionName = "publishedProcesses",
      mongoComponent = component,
      domainFormat = PublishedProcess.mongoFormat,
      extraCodecs = Seq(Codecs.playFormatCodec(zonedDateTimeFormat)),
      indexes = Seq.empty
    ) {

  val logger: Logger = Logger(getClass)

  def delete(id: String): Future[RequestOutcome[String]] = {

    logger.info(s"[test-only] Deleting published process with the ID $id")

    collection
      .deleteOne(equal("_id", id))
      .toFuture()
      .map { _ =>
        Right(id)
      }
      .recover {
        case error =>
          logger.error(s"[test-only] Failed to delete published process with the ID $id", error)
          Left(DatabaseError)
      }
  }

  def setPublishedDate(id: String, dateTime: ZonedDateTime): Future[RequestOutcome[Unit]] =
    collection
      .findOneAndUpdate(equal("_id", id), set("datePublished", dateTime), FindOneAndUpdateOptions().upsert(true))
      .toFutureOption()
      .map { _ =>
        Right(())
      }
      .recover {
        case error =>
          logger.error(s"Attempt to update published process $id failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }

}
