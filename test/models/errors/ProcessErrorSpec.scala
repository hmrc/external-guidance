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

package models.errors

import base.BaseSpec
import core.models.ocelot.errors._
import play.api.libs.json.{JsPath, JsonValidationError}

class ProcessErrorSpec extends BaseSpec {

  "Contructing ProcessErrors" should {
    "from UnknownStanza" in {
      val details: ErrorReport = fromGuidanceError(UnknownStanza("33", UnknownStanza.toString))
      details shouldBe ErrorReport(s"UnknownStanza: Unsupported stanza type ${UnknownStanza.toString} found at stanza id 33", "33")
    }

    "from StanzaNotFound" in {
      val details: ErrorReport = fromGuidanceError(StanzaNotFound("id"))
      details shouldBe ErrorReport("StanzaNotFound: Missing stanza at id = id", "id")
    }

    "from PageStanzaMissing" in {
      val details: ErrorReport = fromGuidanceError(PageStanzaMissing("id"))
      details shouldBe ErrorReport("PageStanzaMissing: PageStanza expected but missing at id = id", "id")
    }

    "from PageUrlEmptyOrInvalid" in {
      val details: ErrorReport = fromGuidanceError(PageUrlEmptyOrInvalid("id"))
      details shouldBe ErrorReport("PageUrlEmptyOrInvalid: PageStanza URL empty or invalid at id = id", "id")
    }

    "from PhraseNotFound" in {
      val details: ErrorReport = fromGuidanceError(PhraseNotFound("stanzaId", 3))
      details shouldBe ErrorReport("PhraseNotFound: Referenced phrase at index 3 on stanza id = stanzaId is missing", "stanzaId")
    }

    "from LinkNotFound" in {
      val details: ErrorReport = fromGuidanceError(LinkNotFound("stanzaId", 4))
      details shouldBe ErrorReport("LinkNotFound: Referenced link at index 4 on stanza id = stanzaId is missing", "stanzaId")
    }

    "from DuplicatePageUrl" in {
      val details: ErrorReport = fromGuidanceError(DuplicatePageUrl("id", "/url"))
      details shouldBe ErrorReport("DuplicatePageUrl: Duplicate page url /url found on stanza id = id", "id")
    }

    "from MissingWelshText" in {
      val details: ErrorReport = fromGuidanceError(MissingWelshText("stanzaId", "index", "english"))
      details shouldBe ErrorReport("MissingWelshText: Welsh text at index index on stanza id = stanzaId is empty", "stanzaId")
    }

    "from InconsistentQuestion" in {
      val details: ErrorReport = fromGuidanceError(InconsistentQuestion("stanzaId"))
      details shouldBe ErrorReport("InconsistentQuestion: Inconsistent QuestionStanza at id stanzaId, number of answers and next locations dont match", "stanzaId")
    }

    "from VisualStanzasAfterDataInput" in {
      val details: ErrorReport = fromGuidanceError(VisualStanzasAfterDataInput("stanzaId"))
      details shouldBe ErrorReport("VisualStanzasAfterDataInput: Visual stanza with id = stanzaId found following a data input stanza", "stanzaId")
    }
    "from UnknownCalcOperationType" in {
      val details: ErrorReport = fromGuidanceError(UnknownCalcOperationType("stanzaId", "unknowntype"))
      details shouldBe ErrorReport("UnknownCalcOperationType: Unsupported CalculationStanza operation type unknowntype found at stanza id stanzaId", "stanzaId")
    }
    "from UnknownTestType" in {
      val details: ErrorReport = fromGuidanceError(UnknownTestType("stanzaId", "unknowntype"))
      details shouldBe ErrorReport("UnknownTestType: Unsupported ChoiceStanza test type unknowntype found at stanza id stanzaId", "stanzaId")
    }
    "from ParseError" in {
      val details: ErrorReport = fromGuidanceError(ParseError(JsPath(Nil), Seq(JsonValidationError(Seq("err message")))))
      details shouldBe ErrorReport("ParseError: Unknown parse error err message at location ", "")
    }
    "from IncompleteDateInputPage" in {
      val details: ErrorReport = fromGuidanceError(IncompleteDateInputPage("stanzaId"))
      details shouldBe ErrorReport("IncompleteDateInputPage: Incomplete Error callout group or missing TypeError callout associated with date input page stanzaId", "stanzaId")
    }
    "from IncompleteInputPage" in {
      val details: ErrorReport = fromGuidanceError(IncompleteInputPage("stanzaId"))
      details shouldBe ErrorReport("IncompleteInputPage: Missing Error callout stanza from input page stanzaId", "stanzaId")
    }
    "from PageRedirectNotSupported" in {
      val details: ErrorReport = fromGuidanceError(PageRedirectNotSupported("stanzaId"))
      details shouldBe ErrorReport("PageRedirectNotSupported: Use of ChoiceStanza stanzaId as a page redirect not supported", "stanzaId")
    }
    "from MultipleExclusiveOptions" in {
      val details: ErrorReport = fromGuidanceError(MultipleExclusiveOptions("stanzaId"))
      details shouldBe ErrorReport("MultipleExclusiveOptions: Sequence stanza stanzaId defines multiple exclusive options", "stanzaId")
    }
    "from UseOfReservedUrl" in {
      val details: ErrorReport = fromGuidanceError(UseOfReservedUrl("stanzaId"))
      details shouldBe ErrorReport("UseOfReservedUrl: Use of reserved URL on PageStanza stanzaId", "stanzaId")
    }
    "from IncompleteExclusiveSequencePage" in {
      val details: ErrorReport = fromGuidanceError(IncompleteExclusiveSequencePage("stanzaId"))
      details shouldBe ErrorReport("IncompleteExclusiveSequencePage: Exclusive sequence page stanzaId is missing a TypeError callout definition", "stanzaId")
    }
    "from PageOccursInMultiplSequenceFlows" in {
      val details: ErrorReport = fromGuidanceError(PageOccursInMultiplSequenceFlows("stanzaId"))
      details shouldBe ErrorReport("PageOccursInMultiplSequenceFlows: Page stanzaId occurs in more than one Sequence flow", "stanzaId")
    }
    "from ErrorRedirectToFirstNonPageStanzaOnly" in {
      val details: ErrorReport = fromGuidanceError(ErrorRedirectToFirstNonPageStanzaOnly("stanzaId"))
      details shouldBe ErrorReport("ErrorRedirectToFirstNonPageStanzaOnly: Invalid link to stanza stanzaId. Page redisplay after a ValueError must link to the first stanza after the PageStanza", "stanzaId")
    }
    "from AllFlowsMustContainMultiplePages" in {
      val details: ErrorReport = fromGuidanceError(AllFlowsMustContainMultiplePages("stanzaId"))
      details shouldBe ErrorReport("AllFlowsMustContainMultiplePages: Page stanzaId is reused in a flow, flows containing reused pages must contain more than one page", "stanzaId")
    }
    "from InvalidLabelName" in {
      val details: ErrorReport = fromGuidanceError(InvalidLabelName("stanzaId"))
      details shouldBe ErrorReport("InvalidLabelName: Invalid label name in stanza stanzaId", "stanzaId")
    }
    "from InvalidFieldWidth" in {
      val details: ErrorReport = fromGuidanceError(InvalidFieldWidth("stanzaId"))
      details shouldBe ErrorReport("InvalidFieldWidth: Input stanza (stanzaId) name field includes an unsupported field width", "stanzaId")
    }
    "from MissingTimescaleDefinition" in {
      val details: ErrorReport = fromGuidanceError(MissingTimescaleDefinition("tsId"))
      details shouldBe ErrorReport("MissingTimescaleDefinition: Process references unknown timescale ID \'tsId\'", "")
    }
    "from MissingRateDefinition" in {
      val details: ErrorReport = fromGuidanceError(MissingRateDefinition("tsId"))
      details shouldBe ErrorReport("MissingRateDefinition: Process references unknown rate ID \'tsId\'", "")
    }

  }
}
