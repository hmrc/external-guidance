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

import scala.concurrent.Future

import play.api.libs.json.{Format, JsObject}
import play.modules.reactivemongo.ReactiveMongoComponent

import uk.gov.hmrc.mongo.ReactiveRepository

import models.{PublishedProcess, RequestOutcome}
import models.errors.{DatabaseError, Errors, NotFoundError}

import repositories.formatters.PublishedProcessFormatter

import scala.concurrent.ExecutionContext.Implicits.global

trait PublishedRepository {

  def save(id: String, process: JsObject): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[JsObject]]
}

@Singleton
class PublishedRepositoryImpl @Inject() (mongoComponent: ReactiveMongoComponent)
    extends ReactiveRepository[PublishedProcess, String](
      collectionName = "published",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = PublishedProcessFormatter.mongoFormat,
      idFormat = implicitly[Format[String]]
    )
    with PublishedRepository {

  def save(id: String, process: JsObject): Future[RequestOutcome[String]] = {

    logger.info(s"Saving process $id to collection published")

    val document: PublishedProcess = PublishedProcess(id, process)

    insert(document)
      .map { _ =>
        Right(document.id)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to persist process $id to collection published failed with error : ${error.getMessage}")
          Left(Errors(DatabaseError))
      }
  }

  def getById(id: String): Future[RequestOutcome[JsObject]] = {

    findById(id)
      .map {
        case Some(publishedProcess) => Right(publishedProcess.process)
        case None => Left(Errors(NotFoundError))
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection published failed with error : ${error.getMessage}")
          Left(Errors(DatabaseError))
      }
  }

}
