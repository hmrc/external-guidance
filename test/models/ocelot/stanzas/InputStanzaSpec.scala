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

package models.ocelot.stanzas

import base.BaseSpec
import play.api.libs.json._
import models.ocelot.{LabelCache, Phrase}

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
    InputStanza(inputType,  Seq("1"), 0, Some(1),"Price", Some(2), stack = false)

  val expectedCurrencyStanza: InputStanza = inputStanza(Currency)
  val expectedCurrencyPoStanza: InputStanza = inputStanza(CurrencyPoundsOnly)
  val expectedDateStanza: InputStanza = inputStanza(Date)
  val expectedNumberStanza: InputStanza = inputStanza(Number)
  val expectedTextStanza: InputStanza = inputStanza(Txt)

  val jsonToStanzaMappings: Map[JsValue, InputStanza] = Map(
    getStanzaJson("Currency") -> expectedCurrencyStanza,
    getStanzaJson("CurrencyPoundsOnly") -> expectedCurrencyPoStanza,
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

  "CurrencyInput " should {
    "update the input label" in {

      Input(expectedCurrencyStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyInput =>
          val labels = LabelCache()

          val (nxt, updatedLabels) = currencyInput.eval("33", labels)
          updatedLabels.updatedLabels(expectedCurrencyStanza.label).english shouldBe Some("33")
        case _ => fail
      }

    }

    "Determine invalid input to be incorrect" in {

      Input(expectedCurrencyStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyInput =>

          currencyInput.validInput("a value") shouldBe None
          currencyInput.validInput("100.789") shouldBe None
          currencyInput.validInput("100.7a9") shouldBe None
        case _ => fail
      }

    }

    "Allow for coma separated 1000s" in {

      Input(expectedCurrencyStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyInput =>

          currencyInput.validInput("123,345,768") shouldBe Some("123345768")
        case _ => fail
      }

    }

    "Allow -ve values" in {

      Input(expectedCurrencyStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyInput =>

          currencyInput.validInput("-567,345") shouldBe Some("-567345")
        case _ => fail
      }

    }

    "Determine valid input to be correct" in {

      Input(expectedCurrencyStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyInput =>

          currencyInput.validInput("£33") shouldBe Some("33")
          currencyInput.validInput("-33") shouldBe Some("-33")
          currencyInput.validInput("£33.79") shouldBe Some("33.79")
          currencyInput.validInput("-33.99") shouldBe Some("-33.99")
          currencyInput.validInput("-£3,453,678.99") shouldBe Some("-3453678.99")
          currencyInput.validInput("33") shouldBe Some("33")
          currencyInput.validInput("33.9") shouldBe Some("33.9")
          currencyInput.validInput("33.") shouldBe Some("33")
          currencyInput.validInput("3,334") shouldBe Some("3334")
          currencyInput.validInput("1,234,567") shouldBe Some("1234567")
          currencyInput.validInput("1,234,567.89") shouldBe Some("1234567.89")
          currencyInput.validInput("1,234,567.8") shouldBe Some("1234567.8")
        case _ => fail
      }

    }

  }

  "CurrencyPoundsOnlyInput " should {
    "update the input label" in {

      Input(expectedCurrencyPoStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyPoundsOnlyInput =>
          val labels = LabelCache()

          val (nxt, updatedLabels) = currencyInput.eval("33", labels)
          updatedLabels.updatedLabels(expectedCurrencyPoStanza.label).english shouldBe Some("33")
        case _ => fail
      }

    }

    "Determine invalid input to be incorrect" in {

      Input(expectedCurrencyPoStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyPoundsOnlyInput =>

          currencyInput.validInput("a value") shouldBe None
          currencyInput.validInput("100.78") shouldBe None
          currencyInput.validInput("100.7a") shouldBe None
          currencyInput.validInput("") shouldBe None
        case _ => fail
      }
    }

    "Allow for coma separated 1000s" in {

      Input(expectedCurrencyPoStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyPoundsOnlyInput =>

          currencyInput.validInput("123,345,768") shouldBe Some("123345768")
        case _ => fail
      }
    }

    "Allow -ve values" in {

      Input(expectedCurrencyPoStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyPoundsOnlyInput =>

          currencyInput.validInput("-567,345") shouldBe Some("-567345")
        case _ => fail
      }
    }

    "Determine valid input to be correct" in {

      Input(expectedCurrencyPoStanza, Phrase("",""), None, None).get match {
        case currencyInput: CurrencyPoundsOnlyInput =>

          currencyInput.validInput("£33") shouldBe Some("33")
          currencyInput.validInput("-33") shouldBe Some("-33")
          currencyInput.validInput("£33.79") shouldBe None
          currencyInput.validInput("-33.99") shouldBe None
          currencyInput.validInput("-£3,453,678") shouldBe Some("-3453678")
          currencyInput.validInput("33") shouldBe Some("33")
          currencyInput.validInput("33.9") shouldBe None
          currencyInput.validInput("33.") shouldBe None
          currencyInput.validInput("3,334") shouldBe Some("3334")
          currencyInput.validInput("1,234,567") shouldBe Some("1234567")
        case _ => fail
      }
    }
  }

  "DateInput" should {
    "update the input label" in {
      Input(expectedDateStanza, Phrase("",""), None, None).get match {
        case input: DateInput =>
          val labels = LabelCache()

          val (nxt, updatedLabels) = input.eval("33", labels)
          updatedLabels.updatedLabels(expectedCurrencyPoStanza.label).english shouldBe Some("33")
        case _ => fail
      }
    }

    "Determine invalid input to be incorrect" in {

      Input(expectedDateStanza, Phrase("",""), None, None).get match {
        case input: DateInput =>
          input.validInput("a value") shouldBe None
          input.validInput("100.78") shouldBe None
          input.validInput("100.7a") shouldBe None
          input.validInput("1,987") shouldBe None
          input.validInput("-87") shouldBe None
          input.validInput("31/9/2001") shouldBe None
          input.validInput("29/2/2001") shouldBe None
        case _ => fail
      }
    }

    "Determine valid input to be correct" in {

      Input(expectedDateStanza, Phrase("",""), None, None).get match {
        case input: DateInput =>

          input.validInput("5/6/1989") shouldBe Some("5/6/1989")
          input.validInput("28/2/1999") shouldBe Some("28/2/1999")
          input.validInput("29/2/2000") shouldBe Some("29/2/2000")
        case _ => fail
      }
    }
  }

  "Creating input with type not currently supported" should {
    "return None for Number Stanza" in {
      Input(expectedNumberStanza, Phrase("", ""), None, None) shouldBe None
    }
    "return None for Text Stanza" in {
      Input(expectedTextStanza, Phrase("", ""), None, None) shouldBe None
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

  "serialise to json with ipt_type CurrencyPoundsOnly" in {
    Json.toJson(expectedCurrencyPoStanza).toString shouldBe inputStanzaJsonFormat("CurrencyPoundsOnly")
  }

  "serialise to json ipt_type CurrencyPoundsOnly from a Stanza reference" in {
    val stanza: Stanza = expectedCurrencyPoStanza
    Json.toJson(stanza).toString shouldBe stanzaJsonFormat("CurrencyPoundsOnly")
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
  missingJsObjectAttrTests[InputStanza](getStanzaJson("Currency").as[JsObject], List("type", "help", "placeholder"))

  /** Test for properties of the wrong type in json object representing instruction stanzas */
  incorrectPropertyTypeJsObjectAttrTests[InputStanza](getStanzaJson("Currency").as[JsObject], List("type"))

}
