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
import core.models.RequestOutcome
import core.models.errors.{ValidationError, InternalServerError, NotFoundError}
import play.api.libs.json.{Json, JsValue}
import repositories.TimescalesRepository
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import config.AppConfig

@Singleton
class TimescalesService @Inject() (repository: TimescalesRepository, appConfig: AppConfig) {
  def save(json: JsValue): Future[RequestOutcome[Unit]] =
    json.validate[Map[String, Int]].fold(_ => Future.successful(Left(ValidationError)), _ =>
      repository.save(json).map{
        case Left(_) => Left(InternalServerError)
        case result => Right(())
      }
    )

  def get(): Future[RequestOutcome[JsValue]] =
    repository.get(repository.CurrentTimescalesID) map {
      case timescales @ Right(_) => timescales
      case Left(NotFoundError) => Right(Json.toJson(appConfig.seedTimescales))
      case Left(_) => Left(InternalServerError)
    }
}
