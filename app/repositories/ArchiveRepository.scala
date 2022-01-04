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

import core.models.RequestOutcome
import core.models.errors.DatabaseError
import models.PublishedProcess
import play.api.Logger
import repositories.formatters.PublishedProcessFormatter

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import core.models.errors.NotFoundError
import scala.concurrent.{ExecutionContext, Future}

trait ArchiveRepository {
  def archive(id: String, user: String, processCode: String, process: PublishedProcess): Future[RequestOutcome[String]]
}

@Singleton
class ArchiveRepositoryImpl @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[PublishedProcess](
      collectionName = "archivedProcesses",
      mongoComponent = mongo,
      domainFormat = PublishedProcessFormatter.mongoFormat,
      indexes = Seq(IndexModel(ascending("processCode"),
                               IndexOptions()
                                .name("archived-secondary-Index-process-code")
                                .unique(false))),
      replaceIndexes = true
    )
    with ArchiveRepository {
  val logger: Logger = Logger(getClass)

  def archive(id: String, user: String, processCode: String, process: PublishedProcess): Future[RequestOutcome[String]] = {

    logger.warn(s"Archiving process $id")

    val date = ZonedDateTime.now

    val selector = equal("_id", date.toInstant.toEpochMilli)
    val modifier = combine(
                    set("processId", id),
                    set("process", process.process),
                    set("archivedBy", user),
                    set("processCode", processCode),
                    set("dateArchived", date)
                   )

    collection
      .findOneAndUpdate(selector, modifier, FindOneAndUpdateOptions().upsert(true))
      .toFutureOption
      .map ( _.fold[RequestOutcome[String]](Left(NotFoundError))( _ => Right(id)) )
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to archive process $id, failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }
}
