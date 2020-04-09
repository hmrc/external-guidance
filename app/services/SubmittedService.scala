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
import models.RequestOutcome
import models.errors.{BadRequestError, Errors, InternalServiceError, NotFoundError}
import play.api.Logger
import play.api.libs.json.JsObject
import repositories.SubmittedRepository
import utils.Validators._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SubmittedService @Inject()(repository: SubmittedRepository) {

  private val logger: Logger = Logger(this.getClass)

  def save(id: String, process: JsObject): Future[RequestOutcome[String]] = {

    def saveProcess: Future[RequestOutcome[String]] = repository.save(id, process) map {
      case Left(_) => Left(Errors(InternalServiceError))
      case result => result
    }

    // TODO DO we need to also validate the JSON process conforms to specific format (like Scratch does)
    validateProcessId(id) match {
      case Right(_) => saveProcess
      case Left(_) =>
        logger.error(s"Invalid process id submitted to method save. The requested id was $id")
        Future.successful(Left(Errors(BadRequestError)))
    }
  }

  def getById(id: String): Future[RequestOutcome[JsObject]] = {

    def getProcess: Future[RequestOutcome[JsObject]] = repository.getById(id) map {
      case error @ Left(Errors(NotFoundError :: Nil)) => error
      case Left(_) => Left(Errors(InternalServiceError))
      case result => result
    }

    validateProcessId(id) match {
      case Right(_) => getProcess
      case Left(_) =>
        logger.error(s"Invalid process id submitted to method getById. The requested id was $id")
        Future.successful(Left(Errors(BadRequestError)))
    }
  }

}
