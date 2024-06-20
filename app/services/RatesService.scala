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

import java.io.InputStream
import java.nio.file.{Paths, Files}
import javax.inject.{Inject, Singleton}
import core.models.RequestOutcome
import core.models.ocelot.Process
import core.models.ocelot.errors.{GuidanceError, MissingRateDefinition}
import core.models.errors.{Error, InternalServerError, NotFoundError, ValidationError}
import play.api.libs.json.{JsObject, JsValue, Json}
import repositories.LabelledDataRepository
import core.services.TodayProvider
import scala.concurrent.{ExecutionContext, Future}
import config.AppConfig
import play.api.Logger
import scala.util.{Try, Success, Failure}
import java.time.{ZoneId, ZonedDateTime, Instant}
import models.{Rates, LabelledDataUpdateStatus, UpdateDetails}
import scala.annotation.tailrec


@Singleton
class RatesService @Inject() (
    repository: LabelledDataRepository,
    coreRatesService: core.services.Rates,
    tp: TodayProvider,
    appConfig: AppConfig)(implicit ec: ExecutionContext) extends LabelledDataServiceProvider[BigDecimal] {

  val YearStringLength: Int = 4
  val logger: Logger = Logger(getClass)

  def details(): Future[RequestOutcome[LabelledDataUpdateStatus]] =
    repository.get(Rates).map{
      case Right(update) =>
        update.data.validate[Map[String, Map[String, Map[String, BigDecimal]]]].fold(
          _ => Left(InternalServerError),
          mp => {
            val updateDetails = UpdateDetails(ZonedDateTime.ofInstant(update.when, ZoneId.of("UTC")), update.credId, update.user, update.email)
            Right(LabelledDataUpdateStatus(twoDimMapFromFour(mp).size, Some(updateDetails)))
        })
      case Left(NotFoundError) =>
        logger.warn(s"No rates found returning seed rates details")
        seedRates()
          .fold[RequestOutcome[LabelledDataUpdateStatus]](Left(InternalServerError))(mp => Right(LabelledDataUpdateStatus(twoDimMapFromFour(mp).size, None)))
      case Left(err) =>
        logger.error(s"Unbale to retrieve rates update details due to error, $err")
        Left(InternalServerError)
    }

  override def expandDataIds(ids: List[String]): List[String] = ids.map(coreRatesService.fullRateId(_, tp))

  def updateProcessTable(js: JsObject, process: Process): Future[RequestOutcome[(JsObject, Process)]] = {
    @tailrec
    def tableUpdate(keys: List[String], rates: Map[String, BigDecimal], acc: List[(String, BigDecimal)] = Nil): RequestOutcome[Map[String, BigDecimal]] = {
      keys match {
        case Nil => Right(acc.toMap)
        case x :: xs =>
          rates.get(coreRatesService.fullRateId(x, tp)) match {
            case None => Left(Error(missingIdError(x)))
            case Some(v) => tableUpdate(xs, rates, (x, v) :: acc)
          }
      }
    }

    process.rates.isEmpty match {
      case true => Future.successful(Right((js, process)))
      case _ => get().map{
        case Right((rates, version)) =>
          tableUpdate(process.rates.keys.toList, rates) match {
            case Left(err) =>
              logger.error(s"RuntimeError: Uunable to update process rate table due to error: ${err.errors}")
              Left(InternalServerError)
            case Right(updatedTable) =>
              val updatedProcess: Process = process.copy(meta = process.meta.copy(ratesVersion = Some(version)), rates = updatedTable)
              Json.toJson(updatedProcess).validate[JsObject].fold(_ => Left(ValidationError), jsObj => Right((jsObj, updatedProcess)))
          }
        case Left(err) => Left(err)
      }
    }
  }

  def addProcessDataTable(ids: List[String], process: Process): Process = process.copy(rates = ids.map((_, BigDecimal(0))).toMap)

  def missingIdError(id: String): GuidanceError = MissingRateDefinition(id)

  def get(): Future[RequestOutcome[(Map[String, BigDecimal], Long)]] =
    repository.get(Rates).map{
      case Right(update) => update.data.validate[Map[String, Map[String, Map[String, BigDecimal]]]].fold(_ => Left(InternalServerError), mp =>
        Right((twoDimMapFromFour(mp), update.when.toEpochMilli)))
      case Left(NotFoundError) =>
        logger.warn(s"No rates found returning seed rates details")
        seedRates()
          .fold[RequestOutcome[(Map[String, BigDecimal], Long)]](Left(InternalServerError))(mp => Right((twoDimMapFromFour(mp), 0L)))
      case Left(err) =>
        logger.error(s"Unable to retrieve rates table due error, $err")
        Left(InternalServerError)
    }

  def getNative(): Future[RequestOutcome[(Map[String, Map[String, Map[String, BigDecimal]]], Long)]] =
    repository.get(Rates).map{
      case Right(update) => update.data.validate[Map[String, Map[String, Map[String, BigDecimal]]]].fold(_ => Left(InternalServerError), mp =>
        Right((mp, update.when.toEpochMilli)))
      case Left(NotFoundError) =>
        logger.warn(s"No rates found returning seed rates details")
        seedRates()
          .fold[RequestOutcome[(Map[String, Map[String, Map[String, BigDecimal]]], Long)]](Left(InternalServerError))(mp => Right((mp, 0L)))
      case Left(err) =>
        logger.error(s"Unable to retrieve rates table due error, $err")
        Left(InternalServerError)
    }

  def save(json: JsValue, credId: String, user: String, email: String, inUse: List[String]): Future[RequestOutcome[LabelledDataUpdateStatus]] =
    json.validate[Map[String, Map[String, Map[String, BigDecimal]]]].fold(_ => Future.successful(Left(ValidationError)), mp => {
      val update2dMap = twoDimMapFromFour(mp) // Convert incoming map to 2d map
      getNative().flatMap{                    // Get the current rates definitions as 2d map
        case Right((fourDimMap, _)) =>
          val current2dMap = twoDimMapFromFour(fourDimMap)
          // Check for deletions from the existing list
          current2dMap.keys.toList.diff(update2dMap.keys.toList) match {
            case Nil => saveRates(update2dMap, credId, user, email)
            case deletions =>
              logger.warn(s"Rates update contains the following deletions: ${deletions.mkString(",")}, in-use: $inUse")
              // Check if any of the deletions are currently in use
              deletions.intersect(inUse) match {
                case Nil => saveRates(update2dMap, credId, user, email)
                case inUseDeletions =>
                  logger.warn(s"RATES: Rates deletions still in-use retained: ${inUseDeletions.mkString(",")}")
                  // Save new rates retaining the in-use deletions
                  saveRates(current2dMap.view.filterKeys(inUseDeletions.contains(_)).toMap ++ update2dMap, credId, user, email, inUseDeletions)
              }
          }
        case Left(NotFoundError) => saveRates(update2dMap, credId, user, email)
        case Left(err) =>
          logger.error(s"Unable to retrieve rate update details due to error, $err")
          Future.successful(Left(InternalServerError))
      }
    })

  private def saveRates(rates: Map[String, BigDecimal],
                        credId: String,
                        user: String,
                        email: String,
                        retained: List[String] = Nil): Future[RequestOutcome[LabelledDataUpdateStatus]] =
    fourDimMapFromTwo(rates).fold[Future[RequestOutcome[LabelledDataUpdateStatus]]](Future.successful(Left(ValidationError))){map4d =>
      repository.save(Rates, Json.toJson(map4d), Instant.now, credId, user, email).map{
        case Left(err) =>
          logger.error(s"Unable to save rate definitions due error, $err")
          Left(InternalServerError)
        case Right(update) =>
          val updateDetails = UpdateDetails(ZonedDateTime.ofInstant(update.when, ZoneId.of("UTC")), update.credId, update.user, update.email, retained)
          Right(LabelledDataUpdateStatus(rates.size, Some(updateDetails)))
      }
    }

  private[services] def fourDimMapFromTwo(rates: Map[String, BigDecimal]): Option[Map[String, Map[String, Map[String, BigDecimal]]]] = {
    @tailrec
    def expand(rates: List[(String, BigDecimal)],
               acc: Map[String, Map[String, Map[String, BigDecimal]]] = Map.empty): Option[Map[String, Map[String, Map[String, BigDecimal]]]] =
      rates match {
        case Nil => Some(acc)
        case (k, v) :: xs =>
          coreRatesService.reverseRateFixedYearId(k) match {
            case None => None
            case Some((s, r, y)) =>
              acc.get(s) match {
                case None => expand(xs, acc ++ Map(s -> Map(r -> Map(y -> v))))
                case Some(s_sector: Map[String, Map[String, BigDecimal]]) =>
                  s_sector.get(r) match {
                    case None =>
                      expand(xs, acc ++ Map(s -> (s_sector ++ Map(r -> Map(y -> v)))))
                    case Some(r_rate: Map[String, BigDecimal]) =>
                      val newRate: Map[String, BigDecimal] = r_rate ++ Map(y -> v)
                      expand(xs, acc ++ Map(s -> (s_sector ++ List((r -> newRate)))))
                  }
              }
          }
      }

    expand(rates.toList)
  }

  private[services] def twoDimMapFromFour(rates: Map[String, Map[String, Map[String, BigDecimal]]]): Map[String, BigDecimal] =
    rates.flatMap{
      case (s: String, sv: Map[String, Map[String, BigDecimal]]) =>
        sv.flatMap{
          case (r: String, rv: Map[String, BigDecimal]) =>
            rv.map{
              case (y: String, v: BigDecimal) => (coreRatesService.rateId(s, r, Some(y)), v)
            }
        }
    }

  private[services] def seedRates(path: String = "conf/seed-rates.json"): Option[Map[String, Map[String, Map[String, BigDecimal]]]] =
    seedRatesAsJson(path).flatMap(_.asOpt[Map[String, Map[String, Map[String, BigDecimal]]]])

  private[services] def seedRatesAsJson(path: String = "conf/seed-rates.json"): Option[JsValue] =
    Try{
      Files.newInputStream(Paths.get(path))
    } match {
      case Success(stream: InputStream) => Json.parse(stream).asOpt[JsValue]
      case Failure(err) =>
        logger.error(s"Unable to load seed rates due error = $err")
        None
    }

}
