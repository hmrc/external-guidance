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
import core.models.errors.{DatabaseError, DuplicateKeyError, NotFoundError}
import core.models.RequestOutcome
import models.PublishedProcess
import play.api.libs.json.{Format, JsObject, JsResultException, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.ImplicitBSONHandlers._
import repositories.formatters.PublishedProcessFormatter
import uk.gov.hmrc.mongo.ReactiveRepository
import reactivemongo.api.WriteConcern
import scala.concurrent.{ExecutionContext, Future}

trait PublishedRepository {

  def save(id: String, user: String, processCode: String, process: JsObject): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[PublishedProcess]]
  def getByProcessCode(processCode: String): Future[RequestOutcome[PublishedProcess]]
  def delete(id: String): Future[RequestOutcome[String]]
}

@Singleton
class PublishedRepositoryImpl @Inject() (mongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends ReactiveRepository[PublishedProcess, String](
      collectionName = "publishedProcesses",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = PublishedProcessFormatter.mongoFormat,
      idFormat = implicitly[Format[String]]
    )
    with PublishedRepository {

  private def processCodeIndexName = "published-secondary-Index-process-code"

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] =
    // If current configuration includes an update to the unique attribute, drop the current index to allow its re-creation
    collection.indexesManager.list().flatMap { indexes =>
      indexes
        .filter(idx =>
          idx.name.contains(processCodeIndexName) && !idx.unique
        )
        .map { _ =>
          logger.warn(s"Dropping $processCodeIndexName ready for re-creation, due to configured unique change")
          collection.indexesManager.drop(processCodeIndexName).map(ret => logger.info(s"Drop of $processCodeIndexName index returned $ret"))
        }

      super.ensureIndexes
    }

  override def indexes: Seq[Index] = Seq(
    Index(
      key = Seq("processCode" -> IndexType.Ascending),
      name = Some(processCodeIndexName),
      unique = true
    )
  )

  def save(id: String, user: String, processCode: String, process: JsObject): Future[RequestOutcome[String]] = {

    logger.info(s"Saving process $id to collection published")

    val selector = Json.obj("_id" -> id)
    val modifier = Json.obj(
      "$inc" -> Json.obj("version" -> 1),
      "$set" -> Json.obj(
        "process" -> process,
        "publishedBy" -> user,
        "processCode" -> processCode,
        "datePublished" -> Json.obj("$date" -> ZonedDateTime.now.toInstant.toEpochMilli)
      )
    )

    this
      .findAndUpdate(selector, modifier, upsert = true)
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

  def getById(id: String): Future[RequestOutcome[PublishedProcess]] = {

    findById(id)
      .map {
        case Some(publishedProcess) => Right(publishedProcess)
        case None => Left(NotFoundError)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def getByProcessCode(processCode: String): Future[RequestOutcome[PublishedProcess]] = {

    val selector = Json.obj("processCode" -> processCode)
    collection
      .find[JsObject, JsObject](selector)
      .one[PublishedProcess]
      .map {
        case Some(publishedProcess) => Right(publishedProcess)
        case None => Left(NotFoundError)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $processCode from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def delete(id: String): Future[RequestOutcome[String]] = {
    val selector = Json.obj("_id" -> id)
    collection.findAndRemove(selector,
                             sort = None,
                             fields = None,
                             writeConcern = WriteConcern.Acknowledged,
                             maxTime = None,
                             collation = None,
                             arrayFilters = Seq.empty)
      .map { _ => Right(id) }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to delete process $id from collection published failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

}
