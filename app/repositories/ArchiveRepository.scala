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
import play.api.libs.json.{Format, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.ImplicitBSONHandlers._
import repositories.formatters.PublishedProcessFormatter
import uk.gov.hmrc.mongo.ReactiveRepository

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait ArchiveRepository {
  def archive(id: String, user: String, processCode: String, process: PublishedProcess): Future[RequestOutcome[String]]
}

@Singleton
class ArchiveRepositoryImpl @Inject() (mongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends ReactiveRepository[PublishedProcess, String](
      collectionName = "archivedProcesses",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = PublishedProcessFormatter.mongoFormat,
      idFormat = implicitly[Format[String]]
    )
    with ArchiveRepository {

  private def processCodeIndexName = "archived-secondary-Index-process-code"

  override def indexes: Seq[Index] = Seq(
    Index(
      key = Seq("processCode" -> IndexType.Ascending),
      name = Some(processCodeIndexName),
      unique = false
    )
  )

  def archive(id: String, user: String, processCode: String, process: PublishedProcess): Future[RequestOutcome[String]] = {

    logger.warn(s"Archiving process $id")

    val date = ZonedDateTime.now.toInstant.toEpochMilli

    val selector = Json.obj("_id" -> date)
    val modifier = Json.obj(
      "$set" -> Json.obj(
        "processId" -> id,
        "process" -> process.process,
        "archivedBy" -> user,
        "processCode" -> processCode,
        "dateArchived" -> Json.obj("$" + "date" -> date)
      )
    )

    findAndUpdate(selector, modifier, upsert = true)
      .map ( _ => Right(id) )
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to archive process $id, failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }
}
