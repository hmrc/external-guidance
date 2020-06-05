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

package services

import javax.inject.{Inject, Singleton}
import models.errors.{BadRequestError, Errors, InternalServiceError, NotFoundError}
import models.{PublishedProcess, RequestOutcome}
import play.api.Logger
import play.api.libs.json.JsObject
import repositories.PublishedRepository
import utils.ProcessUtils.validateProcess
import utils.Validators._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PublishedService @Inject() (repository: PublishedRepository) {

  val logger: Logger = Logger(this.getClass)

  def getById(id: String): Future[RequestOutcome[PublishedProcess]] = {

    def getProcess(id: String): Future[RequestOutcome[PublishedProcess]] = repository.getById(id) map {
      case error @ Left(Errors(Seq(NotFoundError))) => error
      case Left(_) => Left(Errors(InternalServiceError))
      case result => result
    }

    validateProcessId(id) match {
      case Right(id) => getProcess(id)
      case Left(_) =>
        logger.error(s"Invalid process id submitted to method getById. The requested id was $id")
        Future.successful(Left(Errors(BadRequestError)))
    }
  }

  def save(id: String, jsonProcess: JsObject): Future[RequestOutcome[String]] = {

    def saveProcess: Future[RequestOutcome[String]] = {
      repository.save(id, jsonProcess) map {
        case Left(_) =>
          logger.error(s"Request to publish $id has failed")
          Left(Errors(InternalServiceError))
        case result => result
      }
    }

    validateProcess(jsonProcess) match {
      case Right(_) => saveProcess
      case Left(_) =>
        logger.error(s"Publish process $id has failed - invalid process passed in")
        Future.successful(Left(Errors(BadRequestError)))
    }

  }

}
