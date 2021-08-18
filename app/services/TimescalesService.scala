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
import core.models.ocelot.Process
import core.models.errors.{ValidationError, InternalServerError, NotFoundError}
import play.api.libs.json.{Json, JsValue, JsObject}
import repositories.TimescalesRepository
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import config.AppConfig
import play.api.Logger

@Singleton
class TimescalesService @Inject() (repository: TimescalesRepository, appConfig: AppConfig) {
  val logger: Logger = Logger(getClass)

  def save(json: JsValue): Future[RequestOutcome[Unit]] =
    json.validate[Map[String, Int]].fold(_ => Future.successful(Left(ValidationError)), _ =>
      repository.save(json).map{
        case Left(_) => Left(InternalServerError)
        case _ =>
          logger.warn("TIMESCALES: update to timescales definitions saved")
          Right(())
      }
    )

  def updateTimescaleTable(js: JsObject): Future[RequestOutcome[JsObject]] =
    js.validate[Process].fold(_ => Future.successful(Left(ValidationError)),
      process =>
        if (process.timescales.isEmpty) Future.successful(Right(js))
        else get().map{
          case Right(ts) =>
            val timescalesTable: Map[String, Int] = process.timescales.keys.toList.map(k => (k, ts(k))).toMap
            Json.toJson(process.copy(timescales = timescalesTable)).validate[JsObject].fold(
              _ => Left(ValidationError),
              jsObj => Right(jsObj)
            )
          case Left(err) => Left(err)
        }
    )

  def get(): Future[RequestOutcome[Map[String, Int]]] =
    repository.get(repository.CurrentTimescalesID) map {
      case Right(timescales) => timescales.validate[Map[String, Int]].fold(_ => Left(InternalServerError), mp => Right(mp))
      case Left(NotFoundError) => Right(appConfig.seedTimescales)
      case Left(_) => Left(InternalServerError)
    }
}
