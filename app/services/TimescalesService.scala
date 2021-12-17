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
import java.time.ZonedDateTime
import models.{UpdateDetails, TimescalesResponse}

@Singleton
class TimescalesService @Inject() (
    repository: TimescalesRepository,
    appConfig: AppConfig) {

  val logger: Logger = Logger(getClass)

  def details(): Future[RequestOutcome[TimescalesResponse]] =
    repository.get(repository.CurrentTimescalesID) map {
      case Right(update) =>
        update.timescales.validate[Map[String, Int]].fold(_ => Left(InternalServerError),
          mp => Right(TimescalesResponse(mp.size, Some(UpdateDetails(update.when, update.credId, update.user, update.email))))
        )
      case Left(NotFoundError) =>
        logger.warn(s"No timescales found returning seed timescale details")
        Right(TimescalesResponse(appConfig.seedTimescales.size, None))
      case Left(err) =>
        logger.error(s"Unbale to retrieve timescale update details due to error, $err")
        Left(InternalServerError)
    }

  def updateProcessTimescaleTable(js: JsObject): Future[RequestOutcome[JsObject]] =
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
      case Right(tsUpdate) => tsUpdate.timescales.validate[Map[String, Int]].fold(_ => Left(InternalServerError), mp => Right(mp))
      case Left(NotFoundError) =>
        logger.warn(s"No timescales found returning seed timescale details")
        Right(appConfig.seedTimescales)
      case Left(err) =>
        logger.error(s"Unable to retrieve timescale table due error, $err")
        Left(InternalServerError)
    }

  def save(json: JsValue, credId: String, user: String, email: String, inUse: List[String]): Future[RequestOutcome[TimescalesResponse]] =
    json.validate[Map[String, Int]].fold(_ => Future.successful(Left(ValidationError)), mp =>
      // Get the current timescale definitions
      get().flatMap{
        case Right(ts) =>
          // Check for deletions from the existing list
          ts.keys.toList.diff(mp.keys.toList) match {
            case Nil => saveTimescales(mp, credId, user, email)
            case deletions =>
              logger.warn(s"Timescale update contains the following deletions: ${deletions.mkString(",")}")
              // Check if any of the deletions are currently in use
              deletions.intersect(inUse) match {
                case Nil => saveTimescales(mp, credId, user, email)
                case inUseDeletions =>
                  logger.warn(s"TIMESCALES: Timescale deletions still in-use retained: ${inUseDeletions.mkString(",")}")
                  // Save new timescales retaining the in-use deletions
                  saveTimescales(ts.filterKeys(inUseDeletions.contains(_)) ++ mp, credId, user, email, inUseDeletions)
              }
          }
        case Left(NotFoundError) => saveTimescales(mp, credId, user, email)
        case Left(err) =>
          logger.error(s"Unable to retrieve timescale update details due to error, $err")
          Future.successful(Left(InternalServerError))
      }
    )

  private def saveTimescales(ts: Map[String, Int],
                             credId: String,
                             user: String,
                             email: String,
                             retained: List[String] = Nil): Future[RequestOutcome[TimescalesResponse]] =
    repository.save(Json.toJson(ts), ZonedDateTime.now, credId, user, email).map{
      case Left(err) =>
        logger.error(s"Unable to save timescale definitions due error, $err")
        Left(InternalServerError)
      case Right(update) =>
        Right(TimescalesResponse(ts.size, Some(UpdateDetails(update.when, update.credId, update.user, update.email, retained))))
    }
}
