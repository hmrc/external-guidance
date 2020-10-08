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

package utils

import models.ocelot.stanzas._
import models.ocelot.{Link, Phrase, Process}

trait StanzaHelper {

  // Define simple constants for use when creating stanzas
  val two: Int = 2
  val three: Int = 3
  val four: Int = 4
  val five: Int = 5
  val six: Int = 6
  val seven: Int = 7
  val eight: Int = 8

  val phrases: Vector[Phrase] = Vector(
    Phrase(Vector("Text 0", "Welsh, Text 0")),
    Phrase(Vector("Text 1", "Welsh, Text 1")),
    Phrase(Vector("Text 2", "Welsh, Text 2")),
    Phrase(Vector("Text 3", "Welsh, Text 3")),
    Phrase(Vector("Text 4", "Welsh, Text 4")),
    Phrase(Vector("Text 5", "Welsh, Text 5")),
    Phrase(Vector("Text 6", "Welsh, Text 6"))
  )

  val links: Vector[Link] = Vector(Link(0, "http://my.com/news", "MyCOM Daily News", true))

  // Define stanzas used in simple question page test
  val sqpQpValue = "/page/1"
  val sqpFapValue = "/page/2"
  val sqpSapValue = "/page/3"

  // Question page - BEFORE
  val sqpQpPageStanza = PageStanza(sqpQpValue, Seq("1"), false)
  val sqpQpInstructionStanza = InstructionStanza(0, Seq("2"), None, false)
  val sqpQpCalloutStanza = CalloutStanza(SubTitle, 1, Seq("3"), false)
  val sqpQpQuestionStanza = QuestionStanza(two, Seq(three, four), Seq("4", "6"), None, false)
  // Question page - After
  val sqpQpInstruction = Instruction(phrases(0), Seq("2"), None, false)
  val sqpQpCallout = Callout(SubTitle, phrases(1), Seq("3"), false)
  val sqpQpQuestion = Question(phrases(two), Seq(phrases(three), phrases(four)), Seq("4", "6"), None, false)

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
  val sqpSapCallout = Callout(Lede, phrases(six), Seq("end"), false)

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

}
