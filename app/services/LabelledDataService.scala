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
import core.models.ocelot.{Page, Process}
import core.models.errors.{Error, ValidationError}
import core.services.LabelledDataReferencing
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import config.AppConfig
import play.api.Logging
import core.models.ocelot.errors.GuidanceError
import models.{Rates, LabelledDataId, LabelledDataUpdateStatus, Timescales}

trait LabelledDataServiceProvider[A] {
  def get(): Future[RequestOutcome[(Map[String, A], Long)]]
  def expandDataIds(ids: List[String]): List[String] = ids
  def addProcessDataTable(ids: List[String], process: Process): Process
  def updateProcessTable(js: JsObject, process: Process): Future[RequestOutcome[(JsObject, Process)]]
  def missingIdError(id: String): GuidanceError
  def details(): Future[RequestOutcome[LabelledDataUpdateStatus]]
  def getNativeAsJson(): Future[RequestOutcome[JsValue]]
  def save(json: JsValue, credId: String, user: String, email: String, inUse: List[String]): Future[RequestOutcome[LabelledDataUpdateStatus]]
}

@Singleton
class LabelledDataService @Inject() (
    timescaleProvider: TimescalesService,
    timescales: core.services.Timescales,
    ratesProvider: RatesService,
    rates: core.services.Rates,
    appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  def updateProcessLabelledDataTablesAndVersions(js: JsObject): Future[RequestOutcome[JsObject]] =
    js.validate[Process].fold(_ => Future.successful(Left(ValidationError)), process =>
      timescaleProvider.updateProcessTable(js, process).flatMap{
        case Right((jt, pt)) => ratesProvider.updateProcessTable(jt, pt).map{
          case Right((jr, pr)) => Right(jr)
          case Left(err) => Left(err)
        }
      case Left(err) =>
        logger.error(s"Unable to update Process timescales table due to error: $err")
        Future.successful(Left(err))
    })

  def addLabelledDataTables(pages: Seq[Page], process: Process, js: Option[JsObject])
                             (implicit ec: ExecutionContext): Future[RequestOutcome[(Process, Seq[Page], JsObject)]] = {
    def buildTable[A](process: Process,
                      js: Option[JsObject],
                      dataRef: LabelledDataReferencing,
                      service: LabelledDataServiceProvider[A]): Future[RequestOutcome[(Process, Seq[Page], JsObject)]] =
      (dataRef.referencedNonPhraseIds(process.flow) ++ dataRef.referencedIds(process.phrases)).distinct match {
            case Nil => Future.successful(Right((process, pages, js.fold(Json.toJsObject(process))(json => json))))
            case rawIds =>
              val ids = service.expandDataIds(rawIds) // Generate full ids valid as of today for use in the contains test against current set of labelled data
              service.get().flatMap{
              case Left(err) => Future.successful(Left(err))
              case Right(data) if ids.forall(id => data._1.contains(id)) => // If labelled data used in process are available from the service
                val updatedProcess =service.addProcessDataTable(rawIds, process)
                Future.successful(Right((updatedProcess, pages, Json.toJsObject(updatedProcess))))
              case Right(data) =>
                Future.successful(Left(Error(ids.filterNot(data._1.contains).map(service.missingIdError))))
            }
      }

    buildTable(process, js, timescales, timescaleProvider).flatMap {
      case Right((p, _, j)) => buildTable(p, Some(j), rates, ratesProvider)
      case Left(err) => Future.successful(Left(err))
    }
  }

  def save(dataId: LabelledDataId, json: JsValue, credId: String, user: String, email: String, inUse: List[String]): Future[RequestOutcome[LabelledDataUpdateStatus]] =
    dataId match {
      case Rates => ratesProvider.save(json, credId, user, email, inUse)
      case Timescales => timescaleProvider.save(json, credId, user, email, inUse)
    }

  def details(dataId: LabelledDataId): Future[RequestOutcome[LabelledDataUpdateStatus]] =
    dataId match {
      case Rates => ratesProvider.details()
      case Timescales => timescaleProvider.details()
    }

  def get(dataId: LabelledDataId): Future[RequestOutcome[JsValue]] =
    dataId match {
      case Rates => ratesProvider.getNativeAsJson()
      case Timescales => timescaleProvider.getNativeAsJson()
    }
}
