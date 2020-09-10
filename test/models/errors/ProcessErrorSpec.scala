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

import base.BaseSpec
import models.ocelot.errors._
import services._

class ProcessErrorSpec extends BaseSpec {

  "Contructing ProcessErrors" should {
    "from UnknownStanza" in {
      val details: ProcessError = UnknownStanza("33", UnknownStanza.toString)
      details shouldBe ProcessError(s"Unsupported stanza type ${UnknownStanza.toString} found at stanza id 33", "33")
    }

    "from StanzaNotFound" in {
      val details: ProcessError = StanzaNotFound("id")
      details shouldBe ProcessError("Missing stanza at id = id", "id")
    }

    "from PageStanzaMissing" in {
      val details: ProcessError = PageStanzaMissing("id")
      details shouldBe ProcessError("PageSanza expected but missing at id = id", "id")
    }

    "from PageUrlEmptyOrInvalid" in {
      val details: ProcessError = PageUrlEmptyOrInvalid("id")
      details shouldBe ProcessError("PageStanza URL empty or invalid at id = id", "id")
    }

    "from PhraseNotFound" in {
      val details: ProcessError = PhraseNotFound("stanzaId", 3)
      details shouldBe ProcessError("Referenced phrase at index 3 on stanza id = stanzaId is missing", "stanzaId")
    }

    "from LinkNotFound" in {
      val details: ProcessError = LinkNotFound("stanzaId", 4)
      details shouldBe ProcessError("Referenced link at index 4 on stanza id = stanzaId is missing", "stanzaId")
    }

    "from DuplicatePageUrl" in {
      val details: ProcessError = DuplicatePageUrl("id", "/url")
      details shouldBe ProcessError("Duplicate page url /url found on stanza id = id", "id")
    }

    "from MissingWelshText" in {
      val details: ProcessError = MissingWelshText("stanzaId", "index", "english")
      details shouldBe ProcessError("Welsh text at index index on stanza id = stanzaId is empty", "stanzaId")
    }

  }
}
