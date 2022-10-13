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

import core.models.ocelot.errors._

package object errors {

  def fromGuidanceError(err: GuidanceError): ErrorReport = err match {
    case e: StanzaNotFound => ErrorReport(s"StanzaNotFound: Missing stanza at id = ${e.id}", e.id)
    case e: PageStanzaMissing => ErrorReport(s"PageStanzaMissing: PageStanza expected but missing at id = ${e.id}", e.id)
    case e: PageUrlEmptyOrInvalid => ErrorReport(s"PageUrlEmptyOrInvalid: PageStanza URL empty or invalid at id = ${e.id}", e.id)
    case e: PhraseNotFound => ErrorReport(s"PhraseNotFound: Referenced phrase at index ${e.index} on stanza id = ${e.id} is missing", e.id)
    case e: LinkNotFound => ErrorReport(s"LinkNotFound: Referenced link at index ${e.index} on stanza id = ${e.id} is missing", e.id)
    case e: DuplicatePageUrl => ErrorReport(s"DuplicatePageUrl: Duplicate page url ${e.url} found on stanza id = ${e.id}", e.id)
    case e: InconsistentQuestion =>
      ErrorReport(s"InconsistentQuestion: Inconsistent QuestionStanza at id ${e.id}, number of answers and next locations dont match", e.id)
    case e: MissingWelshText => ErrorReport(s"MissingWelshText: Welsh text at index ${e.index} on stanza id = ${e.id} is empty", e.id)
    case e: VisualStanzasAfterDataInput =>
      ErrorReport(s"VisualStanzasAfterDataInput: Visual stanza with id = ${e.id} found following a data input stanza", e.id)
    case e: UnknownStanza => ErrorReport(s"UnknownStanza: Unsupported stanza type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownCalloutType => ErrorReport(s"UnknownCalloutType: Unsupported CalloutStanza type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownValueType => ErrorReport( s"UnknownValueType: Unsupported ValueStanza Value type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownCalcOperationType =>
      ErrorReport(s"UnknownCalcOperationType: Unsupported CalculationStanza operation type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownTestType => ErrorReport( s"UnknownTestType: Unsupported ChoiceStanza test type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: UnknownInputType => ErrorReport( s"UnknownInputType: Unsupported InputStanza type ${e.typeName} found at stanza id ${e.id}", e.id)
    case e: IncompleteDateInputPage => ErrorReport(s"IncompleteDateInputPage: Incomplete Error callout group or missing TypeError callout associated with date input page ${e.id}", e.id)
    case e: IncompleteInputPage => ErrorReport(s"IncompleteInputPage: Missing Error callout stanza from input page ${e.id}", e.id)
    case e: PageRedirectNotSupported => ErrorReport(s"PageRedirectNotSupported: Use of ChoiceStanza ${e.id} as a page redirect not supported", e.id)
    case e: MultipleExclusiveOptions => ErrorReport(s"MultipleExclusiveOptions: Sequence stanza ${e.id} defines multiple exclusive options", e.id)
    case e: UseOfReservedUrl => ErrorReport(s"UseOfReservedUrl: Use of reserved URL on PageStanza ${e.id}", e.id)
    case e: IncompleteExclusiveSequencePage =>
      ErrorReport(s"IncompleteExclusiveSequencePage: Exclusive sequence page ${e.id} is missing a TypeError callout definition", e.id)
    case e: PageOccursInMultiplSequenceFlows => ErrorReport(s"PageOccursInMultiplSequenceFlows: Page ${e.id} occurs in more than one Sequence flow", e.id)
    case e: ErrorRedirectToFirstNonPageStanzaOnly =>
      ErrorReport(s"ErrorRedirectToFirstNonPageStanzaOnly: Invalid link to stanza ${e.id}. " +
                    "Page redisplay after a ValueError must link to the first stanza after the PageStanza", e.id)
    case e: MissingUniqueFlowTerminator =>
      ErrorReport(s"MissingUniqueFlowTerminator: Flow doesn't have a unique termination page ${e.id}, possible main flow connection into a sequence flow",e.id)
    case e: InvalidLabelName =>
      ErrorReport(s"InvalidLabelName: Invalid label name in stanza ${e.id}", e.id)
    case e: InvalidFieldWidth =>
      ErrorReport(s"InvalidFieldWidth: Input stanza (${e.id}) name field includes an unsupported field width", e.id)
    case e: MissingTimescaleDefinition =>
      ErrorReport(s"MissingTimescaleDefinition: Process references unknown timescale ID \'${e.timescaleId}\'", "")
    case e: MissingTitle =>
      ErrorReport(s"MissingTitle: Non input page \'${e.id}\' does not contain a Callout of type Title", e.id)

    case e: ParseError =>
      ErrorReport(s"ParseError: Unknown parse error ${e.errs.map(_.messages.mkString(",")).mkString(",")} at location ${e.jsPath.toString}", "")
    case e: FlowParseError => ErrorReport(s"FlowParseError: Process Flow section parse error, reason: ${e.msg}, stanzaId: ${e.id}, target: ${e.arg}", e.id)
    case e: MetaParseError => ErrorReport(s"MetaParseError: Process Meta section parse error, reason: ${e.msg}, target: ${e.id}", "")
    case e: PhrasesParseError => ErrorReport(s"PhrasesParseError: Process Phrases section parse error, reason: ${e.msg}, index: ${e.id}", "")
    case e: LinksParseError => ErrorReport(s"LinksParseError: Process Links section parse error, reason: ${e.msg}, index: ${e.id}", "")
    case e: TimescalesParseError => ErrorReport(s"TimescalesParseError: Process timescales section parse error, reason: ${e.msg}, index: ${e.id}", "")
  }

  def fromGuidanceErrors(errs: List[GuidanceError]): List[ErrorReport] = errs.map(fromGuidanceError)

  object DuplicateProcessCodeError extends
    ErrorReport(s"Duplicate ProcessCode: process has the same processCode as an existing approval or published process", "")
}


