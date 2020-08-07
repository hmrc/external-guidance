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

package models.errors

import base.UnitSpec
import models.ocelot.errors._
import models.ocelot.stanzas.Stanza

class ErrorDetailSpec extends UnitSpec {

  case object UnknownStanza extends Stanza

  "Contructing ErrorDetails" should {
    "from UnknownStanzaType" in {
      val details: ErrorDetail = UnknownStanzaType(UnknownStanza)
      details shouldBe ErrorDetail(s"Unsupported stanza ${UnknownStanza} found at id = ??", "")
    }

    "from StanzaNotFound" in {
      val details: ErrorDetail = StanzaNotFound("id")
      details shouldBe ErrorDetail("Missing stanza at id = id", "id")
    }

    "from PageStanzaMissing" in {
      val details: ErrorDetail = PageStanzaMissing("id")
      details shouldBe ErrorDetail("PageSanza expected but missing at id = id", "id")
    }

    "from PageUrlEmptyOrInvalid" in {
      val details: ErrorDetail = PageUrlEmptyOrInvalid("id")
      details shouldBe ErrorDetail("PageStanza URL empty or invalid at id = id", "id")
    }

    "from PhraseNotFound" in {
      val details: ErrorDetail = PhraseNotFound(3)
      details shouldBe ErrorDetail("Referenced phrase at index 3 on stanza id = ?? is missing", "")
    }

    "from LinkNotFound" in {
      val details: ErrorDetail = LinkNotFound(4)
      details shouldBe ErrorDetail("Referenced link at index 4 on stanza id = ?? is missing", "")
    }

    "from DuplicatePageUrl" in {
      val details: ErrorDetail = DuplicatePageUrl("id", "/url")
      details shouldBe ErrorDetail("Duplicate page url /url found on stanza id = id", "id")
    }

    "from MissingWelshText" in {
      val details: ErrorDetail = MissingWelshText("index", "english")
      details shouldBe ErrorDetail("Welsh text at index index on stanza id = ?? is empty", "")
    }

  }
}
