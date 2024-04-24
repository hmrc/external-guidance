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

import config.AppConfig

import java.time.{Instant, LocalDate, ZoneId, ZonedDateTime}
import javax.inject.{Inject, Singleton}
import core.models.errors.{DatabaseError, DuplicateKeyError, NotFoundError}
import core.models.RequestOutcome
import core.models.ocelot.Process
import models.{ApprovalProcessSummary, Constants, ProcessSummary, PublishedProcess}
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}
import play.api.Logger
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import org.mongodb.scala.result.DeleteResult
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import core.models.MongoDateTimeFormats.zonedDateTimeFormat
import core.models.MongoDateTimeFormats.Implicits._
import org.mongodb.scala.model.Projections.{excludeId, fields, include}

//$COVERAGE-OFF$
trait PublishedRepository {
  def save(id: String, user: String, processCode: String, process: JsObject): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[PublishedProcess]]
  def getByProcessCode(processCode: String): Future[RequestOutcome[PublishedProcess]]
  def processSummaries(): Future[RequestOutcome[List[ProcessSummary]]]
  def delete(id: String): Future[RequestOutcome[Unit]]
  def getTimescalesInUse(): Future[RequestOutcome[List[String]]]
  def list(): Future[RequestOutcome[List[PublishedProcess]]]
}

@Singleton
class PublishedRepositoryImpl @Inject() (component: MongoComponent)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends PlayMongoRepository[PublishedProcess](
      collectionName = "publishedProcesses",
      mongoComponent = component,
      domainFormat = PublishedProcess.mongoFormat,
      indexes = Seq(IndexModel(ascending("processCode"),
                               IndexOptions()
                                .name("published-secondary-Index-process-code")
                                .unique(true))),
      extraCodecs = Seq(Codecs.playFormatCodec(zonedDateTimeFormat)),
      replaceIndexes = true
    )
    with PublishedRepository {

  val logger: Logger = Logger(getClass)
  override lazy val requiresTtlIndex = false

  def save(id: String, user: String, processCode: String, process: JsObject): Future[RequestOutcome[String]] = {

    logger.warn(s"Saving process $id to collection published")

    val selector = equal("_id", id)
    val modifier = combine(
      inc("version",1),
      set("process", Codecs.toBson(process)),
      set("publishedBy", user),
      set("processCode", processCode),
      set("datePublished", Codecs.toBson(ZonedDateTime.now))
      )

    collection
      .findOneAndUpdate(selector, modifier, FindOneAndUpdateOptions().upsert(true))
      .toFutureOption()
      .map { _ =>
        Right(id)
      }
      .recover {
        case ex: MongoCommandException if ex.getErrorCode == 11000 =>
          logger.error(s"Failed to publish $id due to duplicate key violation on processCode : $processCode")
          Left(DuplicateKeyError)
        case error =>
          logger.error(s"Attempt to persist process $id to collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  }

  def getById(id: String): Future[RequestOutcome[PublishedProcess]] =
    collection
      .find(equal("_id", id))
      .headOption()
      .map {
        case None => Left(NotFoundError)
        case Some(publishedProcess) => Right(publishedProcess)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }

  def getTimescalesInUse(): Future[RequestOutcome[List[String]]] =
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find(TimescalesInUseQuery)
      .collect()
      .toFutureOption()
      .map{
        case None => Right(Nil)
        case Some(ids) => Right(ids.flatMap(pps => pps.process.validate[Process].fold(_ => Nil, p => p.timescales.keys.toList)).distinct.toList)
      }
      .recover{
        case error =>
          logger.error(s"Listing timescales used in the published processes failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }


  def getByProcessCode(processCode: String): Future[RequestOutcome[PublishedProcess]] =
    collection
      .find(equal("processCode", processCode))
      .headOption()
      .map {
        case None => Left(NotFoundError)
        case Some(publishedProcess) => Right(publishedProcess)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $processCode from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }

  def delete(id: String): Future[RequestOutcome[Unit]] =
    collection
      .deleteOne(equal("_id", id))
      .toFutureOption()
      .map {
        case Some(result: DeleteResult) if result.getDeletedCount > 0 => Right(())
        case _ =>
          logger.error(s"Attempt to delete process $id from collection published failed")
          Left(DatabaseError)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to delete process $id from collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
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
              p.id,
              p.processCode,
              process.meta.version,
              process.meta.lastAuthor,
              passphraseStatus(process),
              p.datePublished,
              p.publishedBy,
              "Published"
            )
          }
          Right(summaries.toList)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve published process summaries failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }

  def list(): Future[RequestOutcome[List[PublishedProcess]]] =

    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find()
      .collect()
      .toFutureOption()
      .map {
        case None => Right(Nil)
        case Some(published) => Right(published.toList)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve list of processes from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  //$COVERAGE-ON$
}
