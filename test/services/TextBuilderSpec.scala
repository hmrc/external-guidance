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

package services

import base.UnitSpec
import models.ocelot._
import models.ui.{Link, Text, Words}

class TextBuilderSpec extends UnitSpec {

  trait Test extends ProcessJson {

    val lEnWords1 = Words("This is a ", true)
    val lCyWords1 = Words("Welsh, This is a ", true)
    val lEnWords2 = Words(" followed by ")
    val lCyWords2 = Words(" Welsh, followed by ")
    val lEnWords3 = Words(" and nothing")
    val lCyWords3 = Words(" Welsh, and nothing")
    val link1EnWords = "A link"
    val link1CyWords = "Welsh, A link"
    val link2EnWords = "Another Link"
    val link2CyWords = "Welsh, Another Link"

    val linkEn1 = Link("https://www.bbc.co.uk", link1EnWords)
    val linkCy1 = Link("https://www.bbc.co.uk", link1CyWords)
    val linkEn2 = Link("https://www.gov.uk", link2EnWords)
    val linkCy2 = Link("https://www.gov.uk", link2CyWords)
    val urlMap1: Map[String, String] = Map("3" -> "dummy-path", "5" -> "dummy-path/blah", "34" -> "dummy-path/next")

    val txtWithLinks = Phrase(
      Vector(
        s"[bold:This is a ][link:$link1EnWords:https://www.bbc.co.uk] followed by [link:$link2EnWords:https://www.gov.uk] and nothing",
        s"[bold:Welsh, This is a ][link:$link1CyWords:https://www.bbc.co.uk] Welsh, followed by [link:$link2CyWords:https://www.gov.uk] Welsh, and nothing"
      )
    )

    val brokenLinkPhrase = Phrase(Vector("Hello [link:Blah Blah:htts://www.bbc.co.uk]", "Welsh, Hello [link:Blah Blah:htts://www.bbc.co.uk]"))
    implicit val stanzaIdToUrlMap: Map[String, String] = Map()

    val answerWithNoHint = Phrase("Yes", "Welsh, Yes")
    val answerWithHint = Phrase("Yes[hint:You agree with the assertion]", "Welsh, Yes[hint:Welsh, You agree with the assertion]")

    val answer = Text("Yes", "Welsh, Yes")
    val hint = Text("You agree with the assertion", "Welsh, You agree with the assertion")
  }

  "TextBuilder placeholder parsing" must {

    "Convert a Text with link placeholders in lang strings to Seq[TextItem]" in new Test {

      val txt = TextBuilder.fromPhrase(txtWithLinks)

      txt.english(0) shouldBe lEnWords1
      txt.welsh(0) shouldBe lCyWords1

      txt.english(1).toWords shouldBe Link("https://www.bbc.co.uk", link1EnWords).toWords
      txt.welsh(1).toWords shouldBe Link("https://www.bbc.co.uk", link1CyWords).toWords

      txt.english(2) shouldBe lEnWords2
      txt.welsh(2) shouldBe lCyWords2

      txt.english(3).toWords shouldBe Link("https://www.gov.uk", link2EnWords).toWords
      txt.welsh(3).toWords shouldBe Link("https://www.gov.uk", link2CyWords).toWords

      txt.english(4) shouldBe lEnWords3
      txt.welsh(4) shouldBe lCyWords3
    }

    "leave syntactically incorrect link placeholders as text within a phrase" in new Test {
      val txt: Text = TextBuilder.fromPhrase(brokenLinkPhrase)(urlMap1)

      txt.english.length shouldBe 1
      txt.welsh.length shouldBe 1

      txt.english(0) shouldBe Words("Hello [link:Blah Blah:htts://www.bbc.co.uk]")
      txt.welsh(0) shouldBe Words("Welsh, Hello [link:Blah Blah:htts://www.bbc.co.uk]")
    }

    "convert syntactically correct link placeholders into PageLink or HyperLink" in new Test {
      val linkPhrase =
        Phrase("Hello [link:Blah Blah:https://www.bbc.co.uk] [link:Blah Blah:5]", "Welsh, Hello [link:Blah Blah:https://www.bbc.co.uk] [link:Blah Blah:5]")

      val txt: Text = TextBuilder.fromPhrase(linkPhrase)(urlMap1)

      txt.english.length shouldBe 4
      txt.welsh.length shouldBe 4

      txt.english(0) shouldBe Words("Hello ")
      txt.welsh(0) shouldBe Words("Welsh, Hello ")

      txt.english(1) shouldBe Link("https://www.bbc.co.uk", "Blah Blah")
      txt.welsh(1) shouldBe Link("https://www.bbc.co.uk", "Blah Blah")

      txt.english(2) shouldBe Words(" ")
      txt.welsh(2) shouldBe Words(" ")

      txt.english(3) shouldBe Link("dummy-path/blah", "Blah Blah")
      txt.welsh(3) shouldBe Link("dummy-path/blah", "Blah Blah")

    }

  }

  "TextBuilder answer processing" must {
    "return display answer text only when there is no hint" in new Test {
      val (displayText, hintText) = TextBuilder.singleTextWithOptionalHint(answerWithNoHint)
      displayText shouldBe Text(answerWithNoHint.langs)
      hintText shouldBe None
    }

    "return display answer text with hint" in new Test {
      val (displayText, hintText) = TextBuilder.singleTextWithOptionalHint(answerWithHint)
      displayText shouldBe answer
      hintText shouldBe Some(hint)
    }
  }
}
