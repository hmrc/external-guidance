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

package core.models.ocelot

import base.BaseSpec
import play.api.libs.json._
import play.api.i18n.Lang

class PhraseSpec extends BaseSpec with ProcessJson {

  val p1 = "Ask the customer if they have a tea bag"
  val p1w = "Welsh: Ask the customer if they have a tea bag"
  val p2 = "Do you have a tea bag?"
  val p2w = "Welsh: Do you have a tea bag?"
  val p3 = "Yes - they do have a tea bag"
  val p3w = "Welsh: Yes - they do have a tea bag"

  val phrase1 = s"""["$p1", "$p1w"]"""
  val phrase2 = s"""["$p2", "$p2w"]"""
  val phrase3 = s"""["$p3", "$p3w"]"""

  "Phrase" must {
    "deserialise " in {
      Json.parse(phrase1).as[Phrase] shouldBe Phrase(Vector(p1, p1w))
      Json.parse(phrase2).as[Phrase] shouldBe Phrase(Vector(p2, p2w))
      Json.parse(phrase3).as[Phrase] shouldBe Phrase(Vector(p3, p3w))
    }

    "allow contruction of a blank phrase" in {
      Phrase() shouldBe Phrase(Vector("", ""))
    }

    "Return language text given a valid Lang setting" in {
      val p: Phrase = Phrase(Vector(p1, p1w))

      p.value(Lang("en")) shouldBe p1
      p.value(Lang("cy")) shouldBe p1w
    }
  }

  "Phrases section" must {

    "deserialise from phrases section json" in {

      Json.parse(s"""[ $phrase1, $phrase2, $phrase3 ]""").as[Vector[Phrase]] shouldBe
        Vector(Phrase(Vector(p1, p1w)), Phrase(Vector(p2, p2w)), Phrase(Vector(p3, p3w)))

    }

    "serialise from Phrase to json" in {
      val phrase = Phrase(Vector("Hello World", "Welsh: Hello World"))
      Json.toJson(phrase).toString shouldBe """["Hello World","Welsh: Hello World"]"""
    }

    "allow access to Phrase language strings" in {

      val protoTypePhrases = Json.parse(prototypePhrasesSection).as[Vector[Phrase]]
      val thirdPhraseIndex = 2

      protoTypePhrases(thirdPhraseIndex).welsh shouldBe "Welsh: Overview"
      protoTypePhrases(thirdPhraseIndex).english shouldBe "Overview"
    }

  }

}
