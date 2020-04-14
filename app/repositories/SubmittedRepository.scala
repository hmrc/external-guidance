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
import models.{RequestOutcome, SubmittedProcess}
import play.api.libs.json.{Format, JsObject}
import play.modules.reactivemongo.ReactiveMongoComponent
import repositories.formatters.SubmittedProcessFormatter
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SubmittedRepository {
  def save(id: String, process: JsObject): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[JsObject]]
}

@Singleton
class SubmittedRepositoryImpl @Inject() (mongoComponent: ReactiveMongoComponent)
  extends ReactiveRepository[SubmittedProcess, String](
    collectionName = "submittedProcesses",
    mongo = mongoComponent.mongoConnector.db,
    domainFormat = SubmittedProcessFormatter.mongoFormat,
    idFormat = implicitly[Format[String]]
  ) with SubmittedRepository {

  def save(id: String, process: JsObject): Future[RequestOutcome[String]] = {

    logger.info(s"Saving process $id to collection submitted")
    val document: SubmittedProcess = SubmittedProcess(id, process)

    insert(document)
      .map { _ =>
        Right(document.id)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to persist process $id to collection submitted failed with error : ${error.getMessage}")
          Left(Errors(DatabaseError))
      }
  }

  def getById(id: String): Future[RequestOutcome[JsObject]] = {

    findById(id)
      .map {
        case Some(submittedProcess) => Right(submittedProcess.process)
        case None => Left(Errors(NotFoundError))
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection submitted failed with error : ${error.getMessage}")
          Left(Errors(DatabaseError))
      }
  }

}