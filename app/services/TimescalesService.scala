/*
 * Copyright 2024 HM Revenue & Customs
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
import core.models.ocelot.errors.{GuidanceError, MissingTimescaleDefinition}
import core.models.errors.{InternalServerError, NotFoundError, ValidationError}
import play.api.libs.json.{JsObject, JsValue, Json}
import repositories.LabelledDataRepository
import scala.concurrent.{ExecutionContext, Future}
import config.AppConfig
import play.api.Logging
import java.time.{ZoneId, ZonedDateTime, Instant}
import models.{LabelledDataUpdateStatus, UpdateDetails, Timescales}

@Singleton
class TimescalesService @Inject() (
    repository: LabelledDataRepository,
    appConfig: AppConfig)(implicit ec: ExecutionContext) extends LabelledDataServiceProvider[Int] with Logging {

  def details(): Future[RequestOutcome[LabelledDataUpdateStatus]] =
    repository.get(Timescales) map {
      case Right(update) =>
        update.data.validate[Map[String, Int]].fold(_ => Left(InternalServerError),
          mp => Right(LabelledDataUpdateStatus(mp.size, Some(UpdateDetails(ZonedDateTime.ofInstant(update.when, ZoneId.of("UTC")), update.credId, update.user, update.email))))
        )
      case Left(NotFoundError) =>
        logger.warn(s"No timescales found returning seed timescale details")
        Right(LabelledDataUpdateStatus(appConfig.seedTimescales.size, None))
      case Left(err) =>
        logger.error(s"Unbale to retrieve timescale update details due to error, $err")
        Left(InternalServerError)
    }

  def updateProcessTable(js: JsObject, process: Process): Future[RequestOutcome[(JsObject, Process)]] =
    process.timescales.isEmpty match {
      case true => Future.successful(Right((js, process)))
      case _ => get().map{
        case Right((ts, version)) =>
          val timescalesTable: Map[String, Int] = process.timescales.keys.toList.map(k => (k, ts(k))).toMap
          val updatedProcess: Process = process.copy(meta = process.meta.copy(timescalesVersion = Some(version)), timescales = timescalesTable)
          Json.toJson(updatedProcess).validate[JsObject].fold(_ => Left(ValidationError), jsObj => Right((jsObj, updatedProcess)))
        case Left(err) => Left(err)
      }
    }

  def addProcessDataTable(ids: List[String], process: Process): Process = process.copy(timescales = ids.map((_, 0)).toMap)

  def missingIdError(id: String): GuidanceError = MissingTimescaleDefinition(id)

  def getNativeAsJson(): Future[RequestOutcome[JsValue]] =
    repository.get(Timescales) map {
      case Right(update) => Right(update.data)
      case Left(NotFoundError) =>
        logger.warn(s"No timescales found returning seed timescale details")
        Right(Json.toJson(appConfig.seedTimescales))
      case Left(err) =>
        logger.error(s"Unable to retrieve timescale table due error, $err")
        Left(InternalServerError)
    }

  def get(): Future[RequestOutcome[(Map[String, Int], Long)]] =
    repository.get(Timescales) map {
      case Right(update) => update.data.validate[Map[String, Int]].fold(_ => Left(InternalServerError), mp =>
        Right((mp, update.when.toEpochMilli)))
      case Left(NotFoundError) =>
        logger.warn(s"No timescales found returning seed timescale details")
        Right((appConfig.seedTimescales, 0L))
      case Left(err) =>
        logger.error(s"Unable to retrieve timescale table due error, $err")
        Left(InternalServerError)
    }

  def save(json: JsValue, credId: String, user: String, email: String, inUse: List[String]): Future[RequestOutcome[LabelledDataUpdateStatus]] =
    json.validate[Map[String, Int]].fold(_ => Future.successful(Left(ValidationError)), mp =>
      // Get the current timescale definitions
      get().flatMap{
        case Right(ts) =>
          // Check for deletions from the existing list
          ts._1.keys.toList.diff(mp.keys.toList) match {
            case Nil => saveTimescales(mp, credId, user, email)
            case deletions =>
              logger.warn(s"Timescale update contains the following deletions: ${deletions.mkString(",")}")
              // Check if any of the deletions are currently in use
              deletions.intersect(inUse) match {
                case Nil => saveTimescales(mp, credId, user, email)
                case inUseDeletions =>
                  logger.warn(s"TIMESCALES-WARNING: Timescale deletions still in-use retained: ${inUseDeletions.mkString(",")}")
                  // Save new timescales retaining the in-use deletions
                  saveTimescales(ts._1.view.filterKeys(inUseDeletions.contains(_)).toMap ++ mp, credId, user, email, inUseDeletions)
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
                             retained: List[String] = Nil): Future[RequestOutcome[LabelledDataUpdateStatus]] =
    repository.save(Timescales,Json.toJson(ts), Instant.now, credId, user, email).map{
      case Left(err) =>
        logger.error(s"Unable to save timescale definitions due error, $err")
        Left(InternalServerError)
      case Right(update) =>
        val updateDetails: UpdateDetails = UpdateDetails(ZonedDateTime.ofInstant(update.when, ZoneId.of("UTC")), update.credId, update.user, update.email, retained)
        Right(LabelledDataUpdateStatus(ts.size, Some(updateDetails)))
    }
}
