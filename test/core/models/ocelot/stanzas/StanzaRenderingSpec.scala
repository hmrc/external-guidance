/*
 * Copyright 2023 HM Revenue & Customs
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

package core.models.ocelot.stanzas

import base.BaseSpec
import core.models.ocelot._

class StanzaRenderingSpec extends BaseSpec {

  def expand(s:Phrase): Phrase = Phrase(s.english.reverse, s.welsh)

  val unexpandedText = "X values [label:X], [label:X:currency], [label:X:currencyPoundsOnly]"
  val expandedText = "X values 3.56, £3.56, £3"
  val phrase = Phrase(unexpandedText, unexpandedText)
  val expEn = Phrase(unexpandedText.reverse,unexpandedText)
  val expCy = Phrase(unexpandedText,unexpandedText.reverse)

  "Label references" must {
    "expand in English text of Instructions" in  {
      Instruction(phrase, Seq("1"), None, stack = false).rendered(expand) shouldBe Instruction(expEn, Seq("1"), None, stack = false)
    }

    "expand in English text of Callouts" in  {
      TitleCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe TitleCallout(expEn, Seq("1"), stack = false)
      SubTitleCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe SubTitleCallout(expEn, Seq("1"), stack = false)
      SectionCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe SectionCallout(expEn, Seq("1"), stack = false)
      SubSectionCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe SubSectionCallout(expEn, Seq("1"), stack = false)
      LedeCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe LedeCallout(expEn, Seq("1"), stack = false)
      ErrorCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe ErrorCallout(expEn, Seq("1"), stack = false)
      ValueErrorCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe ValueErrorCallout(expEn, Seq("1"), stack = false)
      TypeErrorCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe TypeErrorCallout(expEn, Seq("1"), stack = false)
      ImportantCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe ImportantCallout(expEn, Seq("1"), stack = false)
      YourCallCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe YourCallCallout(expEn, Seq("1"), stack = false)
      NumberedListItemCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe NumberedListItemCallout(expEn, Seq("1"), stack = false)
      NumberedCircleListItemCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe NumberedCircleListItemCallout(expEn, Seq("1"), stack = false)
      NoteCallout(phrase, Seq("1"), stack = false).rendered(expand) shouldBe NoteCallout(expEn, Seq("1"), stack = false)
    }

    "expand in English text of Inputs" in  {
      NumberInput(Seq("1"), phrase, Some(phrase),"label", Some(phrase), stack = false).rendered(expand) shouldBe
        NumberInput(Seq("1"), expEn, Some(expEn),"label", Some(expEn), stack = false)
      TextInput(Seq("1"), phrase, Some(phrase),"label", Some(phrase), stack = false).rendered(expand) shouldBe
        TextInput(Seq("1"), expEn, Some(expEn),"label", Some(expEn), stack = false)
      CurrencyInput(Seq("1"), phrase, Some(phrase),"label", Some(phrase), stack = false).rendered(expand) shouldBe
        CurrencyInput(Seq("1"), expEn, Some(expEn),"label", Some(expEn), stack = false)
      CurrencyPoundsOnlyInput(Seq("1"), phrase, Some(phrase),"label", Some(phrase), stack = false).rendered(expand) shouldBe
        CurrencyPoundsOnlyInput(Seq("1"), expEn, Some(expEn),"label", Some(expEn), stack = false)
      DateInput(Seq("1"), phrase, Some(phrase),"label", Some(phrase), stack = false).rendered(expand) shouldBe
        DateInput(Seq("1"), expEn, Some(expEn),"label", Some(expEn), stack = false)
    }

    "expand in English text of Rows" in  {
      Row(Seq(phrase, phrase), Seq("1")).rendered(expand) shouldBe Row(Seq(expEn, expEn), Seq("1"))
    }

    "expand in English text of Questions" in  {
      Question(phrase, Seq(phrase, phrase), Seq("1"), None, stack = false).rendered(expand) shouldBe Question(expEn, Seq(expEn, expEn), Seq("1"), None, stack = false)
    }

    "expand in English text of Sequences" in  {
      Sequence(phrase, Seq("1"), Seq(phrase, phrase), Some(phrase), None, stack = false).rendered(expand) shouldBe Sequence(expEn, Seq("1"), Seq(expEn, expEn), Some(expEn), None, stack = false)
    }
 }
}