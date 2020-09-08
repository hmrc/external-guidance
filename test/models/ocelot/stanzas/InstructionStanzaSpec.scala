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

import play.api.libs.json._

import base.UnitSpec

class InstructionStanzaSpec extends UnitSpec {

  val stanzaType: String = "InstructionStanza"
  val text: Int = 10
  val next: String = "end"
  val link: Int = 0
  val stack: Boolean = false

  val validInstructionStanzaWithLinkJsonInput =
    s"""|{
       | "type": "$stanzaType",
       | "text": $text,
       | "next": [ "$next" ],
       | "link": $link,
       | "stack": $stack
       |}""".stripMargin

  val validInstructionStanzaWithoutLinkJsonInput =
    s"""|{
        | "type": "$stanzaType",
        | "text": $text,
        | "next": [ "$next" ],
        | "stack": $stack
        |}""".stripMargin

  val validInstructionStanzaWithLinkJsObject: JsObject = Json.parse(validInstructionStanzaWithLinkJsonInput).as[JsObject]

  val expectedValidInstructionStanzaWithLink: InstructionStanza = InstructionStanza(text, Seq(next), Some(link), stack)

  val expectedValidInstructionStanzaWithoutLink: InstructionStanza = InstructionStanza(text, Seq(next), None, stack)

  "InstructionStanza" must {

    "Deserialising a valid Instruction Stanza with a link should create an instance of the class InstructionStanza" in {

      val validInstructionStanzaJson: JsValue = Json.parse(validInstructionStanzaWithLinkJsonInput)

      val validInstructionStanza: InstructionStanza = validInstructionStanzaJson.as[InstructionStanza]

      validInstructionStanza shouldBe expectedValidInstructionStanzaWithLink
    }

    "Deserialising a valid Instruction Stanza without a link should create an instance of the class InstructionStanza" in {

      val validInstructionStanzaJson: JsValue = Json.parse(validInstructionStanzaWithoutLinkJsonInput)

      val validInstructionStanza: InstructionStanza = validInstructionStanzaJson.as[InstructionStanza]

      validInstructionStanza shouldBe expectedValidInstructionStanzaWithoutLink
    }

    "serialise to json" in {
      val stanza: InstructionStanza = InstructionStanza(0, Seq("4"), None, true)
      val expectedJson: String = """{"text":0,"next":["4"],"stack":true}"""
      Json.toJson(stanza).toString shouldBe expectedJson

      val stanzaWithLink: InstructionStanza = InstructionStanza(0, Seq("4"), Some(0), true)
      val expectedJsonWithLink: String = """{"text":0,"next":["4"],"link":0,"stack":true}"""
      Json.toJson(stanzaWithLink).toString shouldBe expectedJsonWithLink
    }

    "serialise to json from a Stanza reference" in {
      val stanza: Stanza = InstructionStanza(0, Seq("4"), None, true)
      val expectedJson: String = """{"next":["4"],"stack":true,"text":0,"type":"InstructionStanza"}"""
      Json.toJson(stanza).toString shouldBe expectedJson

      val stanzaWithLink: Stanza = InstructionStanza(0, Seq("4"), Some(0), true)
      val expectedJsonWithLink: String = """{"next":["4"],"stack":true,"link":0,"text":0,"type":"InstructionStanza"}"""
      Json.toJson(stanzaWithLink).toString shouldBe expectedJsonWithLink
    }

    /** Test for missing properties in Json object representing instruction stanzas */
    missingJsObjectAttrTests[InstructionStanza](validInstructionStanzaWithLinkJsObject, List("type", "link"))

    /** Test for properties of the wrong type in json object representing instruction stanzas */
    incorrectPropertyTypeJsObjectAttrTests[InstructionStanza](validInstructionStanzaWithLinkJsObject, List("type"))
  }

}
