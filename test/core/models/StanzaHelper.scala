/*
 * Copyright 2021 HM Revenue & Customs
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

package core.models

import core.models.ocelot.stanzas._
import core.models.ocelot.{Link, Phrase, Process}

import base.TestConstants


trait StanzaHelper extends TestConstants {

  val phrases: Vector[Phrase] = Vector(
    Phrase(Vector("Text 0", "Welsh, Text 0")),
    Phrase(Vector("Text 1", "Welsh, Text 1")),
    Phrase(Vector("Text 2", "Welsh, Text 2")),
    Phrase(Vector("Text 3", "Welsh, Text 3")),
    Phrase(Vector("Text 4", "Welsh, Text 4")),
    Phrase(Vector("Text 5", "Welsh, Text 5")),
    Phrase(Vector("Text 6", "Welsh, Text 6")),
    Phrase(Vector("Text 7", "Welsh, Text 7")),
    Phrase(Vector("Text 8", "Welsh, Text 8"))
  )

  val links: Vector[Link] = Vector(Link(0, "http://my.com/news", "MyCOM Daily News", window = true))

  // Define stanzas used in simple question page test
  val sqpQpValue = "/page/1"
  val sqpFapValue = "/page/2"
  val sqpSapValue = "/page/3"

  // Question page - BEFORE
  val sqpQpPageStanza: PageStanza = PageStanza(sqpQpValue, Seq("1"), stack = false)
  val sqpQpInstructionStanza: InstructionStanza = InstructionStanza(0, Seq("2"), None, stack = false)
  val sqpQpCalloutStanza: CalloutStanza = CalloutStanza(SubTitle, 1, Seq("3"), stack = false)
  val sqpQpQuestionStanza: QuestionStanza = QuestionStanza(two, Seq(three, four), Seq("4", "6"), None, stack = false)

  val sqpQpInputStanza = InputStanza(Currency, Seq("4"), two, Some(three), "label", None, false)
  val sqpQpDateInputStanza = InputStanza(Date, Seq("4"), two, Some(three), "label", None, false)
  val sqpQpNumberInputStanza = InputStanza(Number, Seq("4"), two, Some(three), "label", None, false)
  val sqpQpTextInputStanza = InputStanza(Txt, Seq("4"), two, Some(three), "label", None, false)
  val sqpQpSequenceStanza = SequenceStanza(seven, Seq("4", "6"), Seq(eight), None, stack = false )
  val sqpQpSequenceStanzaMissingTitle = SequenceStanza(oneHundred, Seq("4", "6"), Seq(eight), None, stack = false)
  val sqpQpSequenceStanzaMissingOption = SequenceStanza(seven, Seq("4", "6"), Seq(oneHundred), None, stack = false)

  // Question page - After
  val sqpQpInstruction = Instruction(phrases(0), Seq("2"), None, false)
  val sqpQpCallout = SubTitleCallout(phrases(1), Seq("3"), false)
  val sqpQpQuestion = Question(phrases(two), Seq(phrases(three), phrases(four)), Seq("4", "6"), None, false)
  val sqpQpInput = CurrencyInput(Seq("4"), phrases(two), Some(phrases(three)), "label", None, false)
  val sqpQpDateInput = DateInput(Seq("4"), phrases(two), Some(phrases(three)), "label", None, false)
  val sqpQpNumberInput = NumberInput(Seq("4"), phrases(two), Some(phrases(three)), "label", None, false)
  val sqpQpTextInput = TextInput(Seq("4"), phrases(two), Some(phrases(three)), "label", None, false)
  val sqpQpSequence = Sequence(phrases(seven), Seq("4", "6"), Seq(phrases(eight)), None, stack = false)

  // First answer page BEFORE
  val sqpFapPageStanza = PageStanza(sqpFapValue, Seq("5"), false)
  val sqpFapInstructionStanza = InstructionStanza(0, Seq("end"), None, false)
  // First answer page AFTER
  val sqpFapInstruction = Instruction(phrases(0), Seq("end"), None, false)

  // Second answer page BEFORE
  val sqpSapPageStanza = PageStanza(sqpSapValue, Seq("7"), false)
  val sqpSapInstructionStanza = InstructionStanza(five, Seq("8"), Some(0), false)
  val sqpSapCalloutStanza = CalloutStanza(Lede, six, Seq("end"), false)
  // Second answer page AFTER
  val sqpSapInstruction = Instruction(phrases(five), Seq("8"), Some(links(0)), false)
  val sqpSapCallout = LedeCallout(phrases(six), Seq("end"), false)

  def onePage: Map[String, Stanza] = {

    val value1 = "/blah"
    Map(
      Process.StartStanzaId -> PageStanza(value1, Seq("1"), false),
      "1" -> InstructionStanza(0, Seq("2"), None, false),
      "2" -> InstructionStanza(1, Seq("end"), None, false),
      "end" -> EndStanza
    )
  }

  def twoPagesSeperatedByValueStanza: Map[String, Stanza] = {

    val value1 = "/blah"
    val value2 = "/a"
    Map(
      Process.StartStanzaId -> PageStanza(value1, Seq("1"), false),
      "1" -> InstructionStanza(0, Seq("2"), None, false),
      "2" -> InstructionStanza(1, Seq("3"), None, false),
      "3" -> PageStanza(value2, Seq("4"), false),
      "4" -> InstructionStanza(0, Seq("end"), None, false),
      "end" -> EndStanza
    )
  }

  def simpleQuestionPage: Map[String, Stanza] = {

    // Define Map of Stanzas to be processed
    Map(
      Process.StartStanzaId -> sqpQpPageStanza,
      "1" -> sqpQpInstructionStanza,
      "2" -> sqpQpCalloutStanza,
      "3" -> sqpQpQuestionStanza,
      "4" -> sqpFapPageStanza,
      "5" -> sqpFapInstructionStanza,
      "6" -> sqpSapPageStanza,
      "7" -> sqpSapInstructionStanza,
      "8" -> sqpSapCalloutStanza,
      "end" -> EndStanza
    )

  }

  def simpleInputPage: Map[String, Stanza] = {

    // Define Map of Stanzas to be processed
    Map(
      Process.StartStanzaId -> sqpQpPageStanza,
      "1" -> sqpQpInstructionStanza,
      "2" -> sqpQpCalloutStanza,
      "3" -> sqpQpInputStanza,
      "4" -> sqpFapPageStanza,
      "5" -> sqpFapInstructionStanza,
      "6" -> sqpSapPageStanza,
      "7" -> sqpSapInstructionStanza,
      "8" -> sqpSapCalloutStanza,
      "end" -> EndStanza
    )

  }

  def simpleDateInputPage: Map[String, Stanza] = {

    // Define Map of Stanzas to be processed
    Map(
      Process.StartStanzaId -> sqpQpPageStanza,
      "1" -> sqpQpInstructionStanza,
      "2" -> sqpQpDateInputStanza,
      "4" -> sqpFapPageStanza,
      "5" -> sqpFapInstructionStanza,
      "6" -> sqpSapPageStanza,
      "7" -> sqpSapInstructionStanza,
      "8" -> sqpSapCalloutStanza,
      "end" -> EndStanza
    )

  }

  def simpleNumberInputPage: Map[String, Stanza] = {

    // Define Map of Stanzas to be processed
    Map(
      Process.StartStanzaId -> sqpQpPageStanza,
      "1" -> sqpQpInstructionStanza,
      "2" -> sqpQpNumberInputStanza,
      "4" -> sqpFapPageStanza,
      "5" -> sqpFapInstructionStanza,
      "6" -> sqpSapPageStanza,
      "7" -> sqpSapInstructionStanza,
      "8" -> sqpSapCalloutStanza,
      "end" -> EndStanza
    )

  }

  def simpleTextInputPage: Map[String, Stanza] = {

    // Define Map of Stanzas to be processed
    Map(
      Process.StartStanzaId -> sqpQpPageStanza,
      "1" -> sqpQpInstructionStanza,
      "2" -> sqpQpTextInputStanza,
      "4" -> sqpFapPageStanza,
      "5" -> sqpFapInstructionStanza,
      "6" -> sqpSapPageStanza,
      "7" -> sqpSapInstructionStanza,
      "8" -> sqpSapCalloutStanza,
      "end" -> EndStanza
    )

  }

  def simpleSequencePage: Map[String, Stanza] =  Map(
    Process.StartStanzaId -> sqpQpPageStanza,
    "1" -> sqpQpInstructionStanza,
    "2" -> sqpQpSequenceStanza,
    "4" -> sqpFapPageStanza,
    "5" -> sqpFapInstructionStanza,
    "6" -> sqpSapPageStanza,
    "7" -> sqpSapInstructionStanza,
    "8" -> sqpSapCalloutStanza,
    "end" -> EndStanza
  )

  def sequenceWithMissingTitlePage: Map[String, Stanza] =  Map(
    Process.StartStanzaId -> sqpQpPageStanza,
    "1" -> sqpQpInstructionStanza,
    "2" -> sqpQpSequenceStanzaMissingTitle,
    "4" -> sqpFapPageStanza,
    "5" -> sqpFapInstructionStanza,
    "6" -> sqpSapPageStanza,
    "7" -> sqpSapInstructionStanza,
    "8" -> sqpSapCalloutStanza,
    "end" -> EndStanza
  )

  def sequenceWithMissingOptionPage: Map[String, Stanza] =  Map(
    Process.StartStanzaId -> sqpQpPageStanza,
    "1" -> sqpQpInstructionStanza,
    "2" -> sqpQpSequenceStanzaMissingOption,
    "4" -> sqpFapPageStanza,
    "5" -> sqpFapInstructionStanza,
    "6" -> sqpSapPageStanza,
    "7" -> sqpSapInstructionStanza,
    "8" -> sqpSapCalloutStanza,
    "end" -> EndStanza
  )

}
