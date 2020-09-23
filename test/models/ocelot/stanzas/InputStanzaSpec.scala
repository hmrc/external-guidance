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

import base.BaseSpec
import play.api.libs.json._

class InputStanzaSpec extends BaseSpec {

  def getStanzaJson(inputType: String): JsValue = Json.parse(
    s"""|{
        |    "type": "InputStanza",
        |    "ipt_type": "$inputType",
        |    "name": 0,
        |    "help": 1,
        |    "next": ["1"],
        |    "label": "Price",
        |    "placeholder": 2,
        |    "stack": false
        |}""".stripMargin
  )

  def inputStanza(inputType: InputType): InputStanza =
    InputStanza(inputType,  Seq("1"), 0, 1,"Price", Some(2), stack = false)

  val expectedCurrencyStanza: InputStanza = inputStanza(Currency)
  val expectedDateStanza: InputStanza = inputStanza(Date)
  val expectedNumberStanza: InputStanza = inputStanza(Number)
  val expectedTextStanza: InputStanza = inputStanza(Txt)

  val jsonToStanzaMappings: Map[JsValue, InputStanza] = Map(
    getStanzaJson("Currency") -> expectedCurrencyStanza,
    getStanzaJson("Date") -> expectedDateStanza,
    getStanzaJson("Number") -> expectedNumberStanza,
    getStanzaJson("Text") -> expectedTextStanza,
  )

  jsonToStanzaMappings foreach { mapping =>
    val (json, expectedStanza) = mapping
    val jsonType = (json \ "ipt_type").as[String]

    s"Reading valid JSON for a $jsonType Input" should {
      s"create a ${expectedStanza.ipt_type} Input Stanza" in {
        val inputStanza: InputStanza = json.as[InputStanza]
        inputStanza shouldBe expectedStanza
      }
    }
  }

  "Reading invalid JSON for a Input" should {
    "generate a JsError" in {
      getStanzaJson("invalid").validate[InputStanza] match {
        case JsError(_) => succeed
        case _ => fail("An instance of InputStanza should not be created when the note type is incorrect")
      }
    }
  }

  def inputStanzaJsonFormat(typeName: String): String = s"""{"ipt_type":"$typeName","next":["1"],"name":0,"help":1,"label":"Price","placeholder":2,"stack":false}"""
  def stanzaJsonFormat(typeName: String): String = s"""{"next":["1"],"help":1,"stack":false,"ipt_type":"$typeName","name":0,"label":"Price","placeholder":2,"type":"InputStanza"}"""

  "serialise to json with ipt_type Currency" in {
    Json.toJson(expectedCurrencyStanza).toString shouldBe inputStanzaJsonFormat("Currency")
  }

  "serialise to json ipt_type Currency from a Stanza reference" in {
    val stanza: Stanza = expectedCurrencyStanza
    Json.toJson(stanza).toString shouldBe stanzaJsonFormat("Currency")
  }

  "serialise to json with ipt_type Date" in {
    Json.toJson(expectedDateStanza).toString shouldBe inputStanzaJsonFormat("Date")
  }

  "serialise to json ipt_type Date from a Stanza reference" in {
    val stanza: Stanza = expectedDateStanza
    Json.toJson(stanza).toString shouldBe stanzaJsonFormat("Date")
  }


  "serialise to json with ipt_type Number" in {
    Json.toJson(expectedNumberStanza).toString shouldBe inputStanzaJsonFormat("Number")
  }

  "serialise to json ipt_type Number from a Stanza reference" in {
    val stanza: Stanza = expectedNumberStanza
    Json.toJson(stanza).toString shouldBe stanzaJsonFormat("Number")
  }

  "serialise to json with ipt_type Text" in {
    Json.toJson(expectedTextStanza).toString shouldBe inputStanzaJsonFormat("Text")
  }

  "serialise to json ipt_type Text from a Stanza reference" in {
    val stanza: Stanza = expectedTextStanza
    Json.toJson(stanza).toString shouldBe stanzaJsonFormat("Text")
  }

  /** Test for missing properties in Json object representing instruction stanzas */
  missingJsObjectAttrTests[InputStanza](getStanzaJson("Currency").as[JsObject], List("type", "placeholder"))

  /** Test for properties of the wrong type in json object representing instruction stanzas */
  incorrectPropertyTypeJsObjectAttrTests[InputStanza](getStanzaJson("Currency").as[JsObject], List("type"))

}
