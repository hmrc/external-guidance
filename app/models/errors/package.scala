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

package models

import core.models.errors.ProcessError
import core.models.ocelot.errors._

package object errors {

def toProcessErr(err: GuidanceError): ProcessError = err match {
  case e: StanzaNotFound => ProcessError(s"StanzaNotFound: Missing stanza at id = ${e.id}", e.id)
  case e: PageStanzaMissing => ProcessError(s"PageStanzaMissing: PageStanza expected but missing at id = ${e.id}", e.id)
  case e: PageUrlEmptyOrInvalid => ProcessError(s"PageUrlEmptyOrInvalid: PageStanza URL empty or invalid at id = ${e.id}", e.id)
  case e: PhraseNotFound => ProcessError(s"PhraseNotFound: Referenced phrase at index ${e.index} on stanza id = ${e.id} is missing", e.id)
  case e: LinkNotFound => ProcessError(s"LinkNotFound: Referenced link at index ${e.index} on stanza id = ${e.id} is missing", e.id)
  case e: DuplicatePageUrl => ProcessError(s"DuplicatePageUrl: Duplicate page url ${e.url} found on stanza id = ${e.id}", e.id)
  case e: InconsistentQuestion =>
    ProcessError(s"InconsistentQuestion: Inconsistent QuestionStanza at id ${e.id}, number of answers and next locations dont match", e.id)
  case e: MissingWelshText => ProcessError(s"MissingWelshText: Welsh text at index ${e.index} on stanza id = ${e.id} is empty", e.id)
  case e: VisualStanzasAfterDataInput =>
    ProcessError(s"VisualStanzasAfterDataInput: Visual stanza with id = ${e.id} found following a data input stanza", e.id)
  case e: UnknownStanza => ProcessError(s"UnknownStanza: Unsupported stanza type ${e.typeName} found at stanza id ${e.id}", e.id)
  case e: UnknownCalloutType => ProcessError(s"UnknownCalloutType: Unsupported CalloutStanza type ${e.typeName} found at stanza id ${e.id}", e.id)
  case e: UnknownValueType => ProcessError( s"UnknownValueType: Unsupported ValueStanza Value type ${e.typeName} found at stanza id ${e.id}", e.id)
  case e: UnknownCalcOperationType =>
    ProcessError(s"UnknownCalcOperationType: Unsupported CalculationStanza operation type ${e.typeName} found at stanza id ${e.id}", e.id)
  case e: UnknownTestType => ProcessError( s"UnknownTestType: Unsupported ChoiceStanza test type ${e.typeName} found at stanza id ${e.id}", e.id)
  case e: UnknownInputType => ProcessError( s"UnknownInputType: Unsupported InputStanza type ${e.typeName} found at stanza id ${e.id}", e.id)
  case e: IncompleteDateInputPage => ProcessError(s"IncompleteDateInputPage: Incomplete Error callout group associated with date input page ${e.id}", e.id)
  case e: PageRedirectNotSupported => ProcessError(s"PageRedirectNotSupported: Use of ChoiceStanza ${e.id} as a page redirect not supported", e.id)
  case e: MultipleExclusiveOptions => ProcessError(s"MultipleExclusiveOptions: Sequence stanza ${e.id} defines multiple exclusive options", e.id)
  case e: UseOfReservedUrl => ProcessError(s"UseOfReservedUrl: Use of reserved URL on PageStanza ${e.id}", e.id)
  case e: IncompleteExclusiveSequencePage =>
    ProcessError(s"IncompleteExclusiveSequencePage: Exclusive sequence page ${e.id} is missing a TypeError callout definition", e.id)
  case e: PageOccursInMultiplSequenceFlows => ProcessError(s"PageOccursInMultiplSequenceFlows: Page ${e.id} occurs in more than one Sequence flow", e.id)
  case e: ErrorRedirectToFirstNonPageStanzaOnly =>
    ProcessError(s"ErrorRedirectToFirstNonPageStanzaOnly: Invalid link to stanza ${e.id}. " +
                  "Page redisplay after a ValueError must link to the first stanza after the PageStanza", e.id)
  case e: MissingUniqueFlowTerminator =>
    ProcessError(s"MissingUniqueFlowTerminator: Flow doesn't have a unique termination page ${e.id}, possible main flow connection into a sequence flow", e.id)
  case e: InvalidLabelName =>
    ProcessError(s"InvalidLabelName: Invalid label name in stanza ${e.id}", e.id)
  case e: InvalidFieldWidth =>
    ProcessError(s"InvalidFieldWidth: Input stanza (${e.id}) name field includes an unsupported field width", e.id)
  case e: MissingTimescaleDefinition =>
    ProcessError(s"MissingTimescaleDefinition: Process references unknown timescale ID \'${e.timescaleId}\'", "")

  case e: ParseError =>
    ProcessError(s"ParseError: Unknown parse error ${e.errs.map(_.messages.mkString(",")).mkString(",")} at location ${e.jsPath.toString}", "")
  case e: FlowParseError => ProcessError(s"FlowParseError: Process Flow section parse error, reason: ${e.msg}, stanzaId: ${e.id}, target: ${e.arg}", e.id)
  case e: MetaParseError => ProcessError(s"MetaParseError: Process Meta section parse error, reason: ${e.msg}, target: ${e.id}", "")
  case e: PhrasesParseError => ProcessError(s"PhrasesParseError: Process Phrases section parse error, reason: ${e.msg}, index: ${e.id}", "")
  case e: LinksParseError => ProcessError(s"LinksParseError: Process Links section parse error, reason: ${e.msg}, index: ${e.id}", "")
  case e: TimescalesParseError => ProcessError(s"TimescalesParseError: Process timescales section parse error, reason: ${e.msg}, index: ${e.id}", "")
}

def processErrs(errs: List[GuidanceError]): List[ProcessError] = errs.map(toProcessErr)

object DuplicateProcessCodeError extends
  ProcessError(s"Duplicate ProcessCode: process has the same processCode as an existing approval or published process", "")
}


