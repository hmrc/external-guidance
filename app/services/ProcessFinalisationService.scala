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
import core.models.errors.Error
import core.models.ocelot.errors._
import core.models._
import core.models.ocelot.{Phrase, SecuredProcess, Process, Page}
import core.models.ocelot.stanzas.{Value, Stanza}
import core.services.LabelledDataReferencing
import play.api.libs.json._
import scala.concurrent.{Future, ExecutionContext}
import config.AppConfig
import play.api.Logging
import core.services.EncrypterService

@Singleton
class ProcessFinalisationService @Inject() (
    appConfig: AppConfig,
    vpb: ValidatingPageBuilder,
    timescalesService: TimescalesService,
    encrypter: EncrypterService) extends Logging {

  def guidancePagesAndProcess(jsObject: JsObject, checkLevel: GuidanceCheckLevel = Strict)
                             (implicit c: AppConfig, ec: ExecutionContext): Future[RequestOutcome[(Process, Seq[Page], JsObject)]] =
    jsObject.validate[Process].fold(errs => Future.successful(Left(Error(GuidanceError.fromJsonValidationErrors(errs)))),
      incomingProcess => {
        // Transform process if fake welsh, secured process or timescales are indicated
        val (p, js) = fakeWelshTextIfRequired _ tupled securedProcessIfRequired(incomingProcess, Some(jsObject))
        vpb.pagesWithValidation(p, p.startPageId, checkLevel).fold(
          errs => Future.successful(Left(Error(errs))),
          pages => buildTimescalesTable(vpb.pageBuilder.labelledData.timescales, pages, p, js)
        )
      }
    )

  private def buildTimescalesTable(dataRef: LabelledDataReferencing, pages: Seq[Page], process: Process, js: Option[JsObject])
                                  (implicit ec: ExecutionContext): Future[RequestOutcome[(Process, Seq[Page], JsObject)]] =
    // If valid process, collect list of timescale ids from process flow and phrases
    (dataRef.referencedNonPhraseIds(process.flow) ++ dataRef.referencedIds(process.phrases)).distinct match {
      case Nil => Future.successful(Right((process, pages, js.fold(Json.toJsObject(process))(json => json))))
      case ids =>
        timescalesService.get().flatMap{
          case Left(err) => Future.successful(Left(err))
          case Right(timescales) if ids.forall(id => timescales._1.contains(id)) =>
            // All timescales used in process are currently available from the timescales service
            val updatedProcess = process.copy(timescales = ids.map(id => (id, 0)).toMap)
            Future.successful(Right((updatedProcess, pages, Json.toJsObject(updatedProcess))))
          case Right(timescales) =>
            Future.successful(Left(Error(ids.filterNot(timescales._1.contains).map(MissingTimescaleDefinition))))
        }
    }

  // PROTOTYPE RATES FUNCTION TO BE INTEGRATED INTO guidancePagesAndProcess() WHEN services.RatesService COMPLETE
  // private def buildRatesTable(dataRef: LabelledDataReferencing, pages: Seq[Page], process: Process, js: Option[JsObject])
  //                            (implicit ec: ExecutionContext): Future[RequestOutcome[(Process, Seq[Page], JsObject)]] =
  //   // If valid process, collect list of rate ids from process flow and phrases
  //   (dataRef.referencedNonPhraseIds(process.flow) ++ dataRef.referencedIds(process.phrases)).distinct match {
  //     case Nil => Future.successful(Right((process, pages, js.fold(Json.toJsObject(process))(json => json))))
  //     case ids =>
  //       ratesService.get().flatMap{
  //         case Left(err) => Future.successful(Left(err))
  //         case Right(rates) if ids.forall(id => rates._1.contains(id)) =>
  //           // All timescales used in process are currently available from the timescales service
  //           val updatedProcess = process.copy(rates = ids.map(id => (id, 0)).toMap)
  //           Future.successful(Right((updatedProcess, pages, Json.toJsObject(updatedProcess))))
  //         case Right(rates) =>
  //           Future.successful(Left(Error(ids.filterNot(rates._1.contains).map(MissingRatesDefinition))))
  //       }
  //   }

  private[services] def fakeWelshTextIfRequired(process: Process, jsObject: Option[JsObject])(implicit c: AppConfig): (Process,  Option[JsObject]) =
    if (process.passPhrase.isDefined || process.encryptedPassPhrase.isDefined || c.fakeWelshInUnauthenticatedGuidance) {
      val fakedWelshProcess = process.copy(phrases = process.phrases.map(p => if (p.welsh.trim.isEmpty) Phrase(p.english, s"Welsh: ${p.english}") else p))
      (fakedWelshProcess, None)
    } else (process, jsObject)

  private[services] def securedProcessIfRequired(p: Process, jsObject: Option[JsObject]): (Process, Option[JsObject]) =
    p.valueStanzaPassPhrase.fold((p, jsObject)){passPhrase =>
      val encryptedPhrase = encrypter.encrypt(passPhrase)                                          // Encrypt password
      val securedProcess = p.copy(meta = p.meta.copy(encryptedPassPhrase = Some(encryptedPhrase)), // Add optional passphrase to process meta section
                                  flow = updateFlowPassPhrase(p, encryptedPhrase))
      (securedProcess, None)
    }

  private[services] def updateFlowPassPhrase(p: Process, encryptedPassPhrase: String): Map[String, Stanza] =
    p.passphraseValueStanza.fold(p.flow){
      case (id, valueStanza) =>
        val updatedValues = valueStanza.values.map{
          case v: Value if v.label == SecuredProcess.PassPhraseLabelName => v.copy(value = encryptedPassPhrase)
          case v => v
        }
        p.flow.filterNot(p => p._1 == id) ++ Map(id -> valueStanza.copy(values = updatedValues))
    }
}


