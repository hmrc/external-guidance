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

package models.ocelot.stanzas

import org.scalatest.{Matchers, WordSpec}
import models.ocelot._

class InstructionSpec extends WordSpec with Matchers {

  trait Test {
    val text: Int = 10
    val next: Seq[String] = Seq("3")
    val linkId: Int = 0
    val stack: Boolean = false
    val linkDest = "4"
    val link = Link(linkId, linkDest, "", false)
    val pageLinkedStanzaId = "6"
    val phrase0 = Phrase(Vector(s"hello [link:Blah:${pageLinkedStanzaId}] ;lasdk ", s"Welsh, hello [link:Blah:${pageLinkedStanzaId}] ;lasdk "))

    val phrase1 = Phrase(
      Vector(
        s"hello [link:Blah:${linkDest}] ;lasdk [link:Blah:${pageLinkedStanzaId}]",
        s"Welsh, hello [link:Blah:${linkDest}] ;lasdk [link:Blah:${pageLinkedStanzaId}]"
      )
    )
    val simpleInstruction = InstructionStanza(text, next, None, false)
    val linkInstruction = InstructionStanza(text, next, Some(linkId), false)
  }

  "when constructed via apply() Instruction" must {

    "contain a list of linked stanza ids drawn from placeholders within the text" in new Test {
      val instruction = Instruction(simpleInstruction, phrase0, None, Nil)
      instruction.links.length shouldBe 0
    }

    "contain a list of linked stanza ids drawn from placeholders within the text and link" in new Test {
      val instruction = Instruction(linkInstruction, phrase0, Some(link), Nil)
      instruction.links.length shouldBe 1

      instruction.links.contains(linkDest) shouldBe true
    }

  }

}
