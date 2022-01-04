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

import java.time.ZonedDateTime

import javax.inject.{Inject, Singleton}
import core.models.errors.{DatabaseError, DuplicateKeyError, NotFoundError}
import core.models.RequestOutcome
import core.models.ocelot.Process
import models.{PublishedSummary, PublishedProcess}
import play.api.libs.json.{JsObject, JsResultException}
import repositories.formatters.PublishedProcessFormatter
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logger
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

trait PublishedRepository {
  def save(id: String, user: String, processCode: String, process: JsObject): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[PublishedProcess]]
  def getByProcessCode(processCode: String): Future[RequestOutcome[PublishedProcess]]
  def processSummaries(): Future[RequestOutcome[List[PublishedSummary]]]
  def delete(id: String): Future[RequestOutcome[String]]
  def getTimescalesInUse(): Future[RequestOutcome[List[String]]]
}

@Singleton
class PublishedRepositoryImpl @Inject() (component: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[PublishedProcess](
      collectionName = "publishedProcesses",
      mongoComponent = component,
      domainFormat = PublishedProcessFormatter.mongoFormat,
      indexes = Seq(IndexModel(ascending("processCode"),
                               IndexOptions()
                                .name("published-secondary-Index-process-code")
                                .unique(true))),
    )
    with PublishedRepository {

  val logger: Logger = Logger(getClass)

  def save(id: String, user: String, processCode: String, process: JsObject): Future[RequestOutcome[String]] = {

    logger.warn(s"Saving process $id to collection published")

    val selector = equal("_id", id)
    val modifier = combine(
      inc("version",1),
      set("process", process),
      set("publishedBy", user),
      set("processCode", processCode),
      set("datePublished", ZonedDateTime.now)
      )

    collection
      .findOneAndUpdate(selector, modifier, FindOneAndUpdateOptions().upsert(true))
      .toFuture
      .map { _ =>
        Right(id)
      }
      //$COVERAGE-OFF$
      .recover {
        case e: JsResultException if hasDupeKeyViolation(e) =>
          logger.error(s"Failed to publish $id due to duplicate key violation on processCode : $processCode")
          Left(DuplicateKeyError)
        case error =>
          logger.error(s"Attempt to persist process $id to collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def getById(id: String): Future[RequestOutcome[PublishedProcess]] =
    collection
      .find(equal("_id", id))
      .toFuture()
      .map {
        case Nil => Left(NotFoundError)
        case publishedProcess :: _ => Right(publishedProcess)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

  //$COVERAGE-OFF$
  def getTimescalesInUse(): Future[RequestOutcome[List[String]]] =
    collection
      .withReadPreference(ReadPreference.primaryPreferred)
      .find(TimescalesInUseQuery)
      .toFuture()
      .map{ seq =>
        Right(seq.flatMap(pps => pps.process.validate[Process].fold(_ => Nil, p => p.timescales.keys.toList)).distinct.toList)
      }
      .recover{
        case error =>
          logger.error(s"Listing timescales used in the published processes failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$

  def getByProcessCode(processCode: String): Future[RequestOutcome[PublishedProcess]] =
    collection
      .find(equal("processCode", processCode))
      .toFuture
      .map {
        case Nil => Left(NotFoundError)
        case publishedProcess :: _ => Right(publishedProcess)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $processCode from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

  def delete(id: String): Future[RequestOutcome[String]] =
    collection
      .deleteOne(equal("id", id))
    // collection.findAndRemove(Json.obj("_id" -> id),
    //                          sort = None,
    //                          fields = None,
    //                          writeConcern = WriteConcern.Acknowledged,
    //                          maxTime = None,
    //                          collation = None,
    //                          arrayFilters = Seq.empty)
      .toFuture
      .map { _ => Right(id) }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to delete process $id from collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

  //$COVERAGE-OFF$
  def processSummaries(): Future[RequestOutcome[List[PublishedSummary]]] =
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find()
      .toFuture
      .map(res => Right(res.map(doc => PublishedSummary(doc.id, doc.datePublished, doc.processCode, doc.publishedBy)).toList))
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve published process summaries failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  //$COVERAGE-ON$
}
