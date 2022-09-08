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

package services

import javax.inject.{Inject, Singleton}
import core.models.errors.{InternalServerError, NotFoundError}
import core.models.RequestOutcome
import models.{ProcessSummary, ArchivedProcess}
import play.api.Logger
import repositories.ArchiveRepository
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.libs.json.{Json, OFormat, JsValue}

@Singleton
class ArchiveService @Inject() (archive: ArchiveRepository) {

  val logger: Logger = Logger(this.getClass)

  def list: Future[RequestOutcome[JsValue]] = {
    implicit val formats: OFormat[ProcessSummary] = Json.format[ProcessSummary]
    archive.processSummaries() map {
      case Left(_) => Left(InternalServerError)
      case Right(summaries) => Right(Json.toJson(summaries))
    }
  }

  def getById(id: String): Future[RequestOutcome[ArchivedProcess]] =
    archive.getById(id) map {
      case error @ Left(NotFoundError) => error
      case Left(_) => Left(InternalServerError)
      case result => result
    }
}
