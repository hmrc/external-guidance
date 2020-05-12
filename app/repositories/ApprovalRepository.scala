/*
 * Copyright 2020 HM Revenue & Customs
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
import models.errors.{DatabaseError, Errors, NotFoundError}
import models.{ApprovalProcess, ApprovalProcessSummary, RequestOutcome}
import play.api.libs.json.{Format, JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.ReadPreference
import reactivemongo.play.json.ImplicitBSONHandlers._
import repositories.formatters.ApprovalProcessFormatter
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ApprovalRepository {
  def update(id: String, process: ApprovalProcess): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[JsObject]]
  def approvalSummaryList(): Future[RequestOutcome[List[ApprovalProcessSummary]]]
}

@Singleton
class ApprovalRepositoryImpl @Inject() (implicit mongoComponent: ReactiveMongoComponent)
    extends ReactiveRepository[ApprovalProcess, String](
      collectionName = "approvalProcesses",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = ApprovalProcessFormatter.mongoFormat,
      idFormat = implicitly[Format[String]]
    )
    with ApprovalRepository {

  def update(id: String, process: ApprovalProcess): Future[RequestOutcome[String]] = {

    logger.info(s"Saving process $id to collection $collectionName")
    val selector = Json.obj("_id" -> id)
    val jsonProcess = Json.toJsObject(process)(ApprovalProcessFormatter.mongoFormat)

    this
      .findAndUpdate(selector, jsonProcess, upsert = true)
      .map { _ =>
        Right(id)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to persist process $id to collection $collectionName failed with error : ${error.getMessage}")
          Left(Errors(DatabaseError))
      }
    //$COVERAGE-ON$
  }

  def getById(id: String): Future[RequestOutcome[JsObject]] = {

    findById(id)
      .map {
        case Some(approvalProcess) => Right(approvalProcess.process)
        case None => Left(Errors(NotFoundError))
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection $collectionName failed with error : ${error.getMessage}")
          Left(Errors(DatabaseError))
      }
    //$COVERAGE-ON$
  }

  def approvalSummaryList(): Future[RequestOutcome[List[ApprovalProcessSummary]]] = {
    val selector = Json.obj("meta" -> Json.obj("$exists" -> true))
    val projection = Some(Json.obj("meta" -> 1, "process.meta.id" -> 1))

    collection
      .find(
        selector,
        projection
      )
      .cursor[ApprovalProcess](ReadPreference.primaryPreferred)
      .collect(maxDocs = -1, FailOnError[List[ApprovalProcess]]())
      .map {
        _.map { doc =>
          ApprovalProcessSummary(doc.meta.id, doc.meta.title, doc.meta.dateSubmitted, doc.meta.status)
        }
      }
      .map(list => Right(list))
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve list of processes from collection $collectionName failed with error : ${error.getMessage}")
          Left(Errors(DatabaseError))
      }
    //$COVERAGE-ON$
  }

}
