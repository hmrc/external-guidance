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

import models.ocelot._
import models.errors.{Error, ProcessError, ValidationError}
import models.ocelot.errors._
import java.util.UUID
import models.RequestOutcome
import models.ocelot.{Label, Page, Process}
import play.api.libs.json._

package object services {
  val processIdformat = "^[a-z]{3}[0-9]{5}$"
  val uuidFormat = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"

  def validateUUID(id: String): Option[UUID] = if (id.matches(uuidFormat)) Some(UUID.fromString(id)) else None
  def validateProcessId(id: String): Either[Error, String] = if (id.matches(processIdformat)) Right(id) else Left(ValidationError)

  def uniqueLabels(pages: Seq[Page]):Seq[Label] = pages.flatMap(p => p.labels).distinct

  def uniqueLabelRefs(pages: Seq[Page]): Seq[String] = pages.flatMap(_.labelRefs)

  implicit def toProcessErr(err: GuidanceError): ProcessError = err match {
    case e: StanzaNotFound => ProcessError(s"Missing stanza at id = ${e.id}", e.id)
    case e: PageStanzaMissing => ProcessError(s"PageSanza expected but missing at id = ${e.id}", e.id)
    case e: PageUrlEmptyOrInvalid => ProcessError(s"PageStanza URL empty or invalid at id = ${e.id}", e.id)
    case e: PhraseNotFound => ProcessError(s"Referenced phrase at index ${e.index} on stanza id = ${e.id} is missing", e.id)
    case e: LinkNotFound => ProcessError(s"Referenced link at index ${e.index} on stanza id = ${e.id} is missing", e.id)
    case e: DuplicatePageUrl => ProcessError(s"Duplicate page url ${e.url} found on stanza id = ${e.id}", e.id)
    case e: MissingWelshText => ProcessError(s"Welsh text at index ${e.index} on stanza id = ${e.id} is empty", e.id)
    case e: VisualStanzasAfterQuestion => ProcessError(s"Visual stanza with id = ${e.id} found following a Question stanza", e.id)
    case e: UnknownStanza => ProcessError(s"Unsupported stanza type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownCalloutType => ProcessError(s"Unsupported CalloutStanza type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownValueType => ProcessError( s"Unsupported ValueStanza Value type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownCalcOperationType => ProcessError(s"Unsupported CalculationStanza operation type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownTestType => ProcessError( s"Unsupported ChoiceStanza test type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownInputType => ProcessError( s"Unsupported InputStanza type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: ParseError => ProcessError(s"Unknown parse error ${e.errs.map(_.messages.mkString(",")).mkString(",")} at location ${e.jsPath.toString}", "")
    case e: FlowParseError => ProcessError(s"Process Flow section parse error, reason: ${e.msg}, stanzaId: ${e.id}, target: ${e.arg}", e.id)
    case e: MetaParseError => ProcessError(s"Process Meta section parse error, reason: ${e.msg}, target: ${e.id}", "")
    case e: PhrasesParseError => ProcessError(s"Process Phrases section parse error, reason: ${e.msg}, index: ${e.id}", "")
    case e: LinksParseError => ProcessError(s"Process Links section parse error, reason: ${e.msg}, index: ${e.id}", "")
  }

  implicit def processErrs(errs: List[GuidanceError]): List[ProcessError] = errs.map(toProcessErr)

  def guidancePages(pageBuilder: PageBuilder, jsValue: JsValue): RequestOutcome[(Process, Seq[Page])] =
    jsValue.validate[Process].fold(
      errs => Left(Error(GuidanceError.fromJsonValidationErrors(errs))),
      process => pageBuilder.pagesWithValidation(process).fold(errs => Left(Error(errs)), p => Right((process, p)))
    )
}
