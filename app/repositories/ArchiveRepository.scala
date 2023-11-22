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

import core.models.RequestOutcome
import core.models.ocelot.Process
import core.models.errors.{NotFoundError, DatabaseError}
import models.{ArchivedProcess, PublishedProcess, ProcessSummary}
import play.api.Logger
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import core.models.MongoDateTimeFormats.zonedDateTimeFormat
import core.models.MongoDateTimeFormats.Implicits._

//$COVERAGE-OFF$
trait ArchiveRepository {
  def archive(id: String, user: String, processCode: String, process: PublishedProcess): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[ArchivedProcess]]
  def processSummaries(): Future[RequestOutcome[List[ProcessSummary]]]
}

@Singleton
class ArchiveRepositoryImpl @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[ArchivedProcess](
      collectionName = "archivedProcesses",
      mongoComponent = mongo,
      domainFormat = ArchivedProcess.mongoFormat,
      indexes = Seq(IndexModel(ascending("processCode"),
                               IndexOptions()
                                .name("archived-secondary-Index-process-code")
                                .unique(false))),
      extraCodecs = Seq(Codecs.playFormatCodec(zonedDateTimeFormat)),
      replaceIndexes = true
    )
    with ArchiveRepository {
  val logger: Logger = Logger(getClass)
  override lazy val requiresTtlIndex = false

  def archive(id: String, user: String, processCode: String, process: PublishedProcess): Future[RequestOutcome[String]] = {

    logger.warn(s"Archiving process $id")

    val date = ZonedDateTime.now

    val selector = equal("_id", date.toInstant.toEpochMilli)
    val modifier = combine(
                    set("processId", id),
                    set("process", Codecs.toBson(process.process)),
                    set("archivedBy", user),
                    set("processCode", processCode),
                    set("dateArchived", Codecs.toBson(date))
                   )

    collection
      .findOneAndUpdate(selector, modifier, FindOneAndUpdateOptions().upsert(true))
      .toFutureOption()
      .map (_ => Right(id))
      .recover {
        case error =>
          logger.error(s"Attempt to archive process $id, failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  }

  def getById(id: String): Future[RequestOutcome[ArchivedProcess]] = {
    collection
      .find(equal("_id", id.toLong))
      .headOption()
      .map {
        case None =>
          Left(NotFoundError)
        case Some(process) => Right(process)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  }

  def processSummaries(): Future[RequestOutcome[List[ProcessSummary]]] =
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find()
      .collect()
      .toFutureOption()
      .map{
        case None => Right(Nil)
        case Some(res) =>
          val summaries = res.map{p =>
            val process: Process = p.process.as[Process]
            ProcessSummary(
              p.id.toString,
              p.processCode,
              process.meta.version,
              process.meta.lastAuthor,
              passphraseStatus(process),
              p.dateArchived,
              p.archivedBy,
              "Archived"
            )
          }
          Right(summaries.toList)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve published process summaries failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  //$COVERAGE-ON$

}
