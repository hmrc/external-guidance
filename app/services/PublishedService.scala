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

package services

import javax.inject.{Inject, Singleton}
import core.models.errors.{BadRequestError, DuplicateKeyError, InternalServerError, NotFoundError}
import core.models.ocelot._
import core.models.RequestOutcome
import models.PublishedProcess
import play.api.Logger
import play.api.libs.json.JsObject
import repositories.{ApprovalRepository, ArchiveRepository, PublishedRepository}
import core.services.validateProcessId
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PublishedService @Inject() (published: PublishedRepository,
                                  archive: ArchiveRepository,
                                  approval: ApprovalRepository) {

  val logger: Logger = Logger(this.getClass)

  def getById(id: String): Future[RequestOutcome[PublishedProcess]] =
    validateProcessId(id) match {
      case Right(id) => published.getById(id) map {
        case error @ Left(NotFoundError) => error
        case Left(_) => Left(InternalServerError)
        case result => result
      }
      case Left(_) =>
        logger.error(s"Invalid process id submitted to method getById. The requested id was $id")
        Future.successful(Left(BadRequestError))
    }

  def getByProcessCode(processCode: String): Future[RequestOutcome[PublishedProcess]] =
    published.getByProcessCode(processCode) map {
      case error @ Left(NotFoundError) => error
      case Left(_) => Left(InternalServerError)
      case result => result
    }

  def save(id: String, user: String, processCode: String, jsonProcess: JsObject): Future[RequestOutcome[String]] =
    jsonProcess
      .validate[Process]
      .fold( _ => {
        logger.error(s"Publish process $id has failed - invalid process passed in")
        Future.successful(Left(BadRequestError))
      }, process =>
        published.save(id, user, processCode, jsonProcess).map{
          case Left(DuplicateKeyError) => Left(DuplicateKeyError)
          case Left(_) =>
            logger.error(s"Request to publish $id has failed")
            Left(InternalServerError)
          case result => result
        }
      )

  def getTimescalesInUse(): Future[RequestOutcome[List[String]]] =
    published.getTimescalesInUse().map{
      case Right(timescalesInUse) => Right(timescalesInUse)
      case err => err
    }

  def archive(id: String, user: String): Future[RequestOutcome[String]] =
    published.getById(id).flatMap {
      case Left(_) =>
        logger.error(s"Invalid process id submitted to method getById. The requested id was $id")
        Future.successful(Left(BadRequestError))
      case Right(process) =>
        archive.archive(id, user, process.processCode, process).flatMap {
          case Left(_) => Future.successful(Left(InternalServerError))
          case _ =>
            logger.warn(s"ARCHIVE: Process $id archived by $user")
            for {
              _       <- approval.changeStatus(id, "Archived", user)
              deleted <- published.delete(id)
            } yield deleted
        }
    }
}
