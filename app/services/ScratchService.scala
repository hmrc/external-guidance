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

import java.util.UUID

import javax.inject.{Inject, Singleton}
import models.RequestOutcome
import models.errors.{BadRequestError, Errors, InternalServiceError, NotFoundError}
import models.ocelot.Process
import play.api.Logger
import play.api.libs.json.{JsError, JsObject, JsSuccess}
import repositories.ScratchRepository
import utils.Validators._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ScratchService @Inject() (repository: ScratchRepository) {

  private val logger: Logger = Logger(this.getClass)

  def save(process: JsObject): Future[RequestOutcome[UUID]] = {

    def saveProcess(process: JsObject): Future[RequestOutcome[UUID]] = repository.save(process) map {
      case Left(_) => Left(Errors(InternalServiceError))
      case result => result
    }

    process.validate[Process] match {
      case JsSuccess(_, _) => saveProcess(process)
      case JsError(errors) =>
        logger.warn(s"Parsing process failed with the following error(s): $errors")
        Future { Left(Errors(BadRequestError)) }
    }
  }

  def getById(id: String): Future[RequestOutcome[JsObject]] = {

    def getProcess(id: UUID): Future[RequestOutcome[JsObject]] = repository.getById(id) map {
      case error @ Left(Errors(NotFoundError :: Nil)) => error
      case Left(_) => Left(Errors(InternalServiceError))
      case result => result
    }

    validateUUID(id) match {
      case Some(id) => getProcess(id)
      case None => Future { Left(Errors(BadRequestError)) }
    }
  }

}
