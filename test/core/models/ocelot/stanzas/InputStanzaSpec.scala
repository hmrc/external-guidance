/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.libs.json._
import play.api.i18n.Lang
import core.models.ocelot.{Encrypter, Label, LabelCache, Labels, Page, Phrase, Published, SecuredProcess, taxCodePattern}

class InputStanzaSpec extends BaseSpec {

  // Valid tax codes
  val validTaxCodes: List[String] = List(
    "0T", "BR", "SBR", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8",
    "NT", "45L", "145L", "K585", "K630", "K45", "285", "K401", "K27", "K159", "K306",
    "999L", "1000L", "1001L", "SK678X", "CBRW1"
  )

  // Invalid tax codes
  val invalidTaxCodes: List[String] = List("D9", "D15", "X100", "12345", "9L", "KT999", "A1", "ZZZ")

  def getStanzaJson(inputType: String): JsValue = Json.parse(
    s"""|{
        |    "type": "InputStanza",
        |    "ipt_type": "$inputType",
        |    "next": ["1"],
        |    "name": 0,
        |    "help": 1,
        |    "label": "Price",
        |    "placeholder": 2,
        |    "stack": false
        |}""".stripMargin
  )

  def inputStanza(inputType: InputType): InputStanza =
    InputStanza(inputType, Seq("1"), 0, Some(1), "Price", Some(2), stack = false)
  val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
  val expectedCurrencyStanza: InputStanza = inputStanza(Currency)
  val expectedCurrencyPoStanza: InputStanza = inputStanza(CurrencyPoundsOnly)
  val expectedDateStanza: InputStanza = inputStanza(Date)
  val expectedNumberStanza: InputStanza = inputStanza(Number)
  val expectedTextStanza: InputStanza = inputStanza(Txt)
  val expectedPassphraseStanza: InputStanza = inputStanza(PassphraseText)

  val jsonToStanzaMappings: Map[JsValue, InputStanza] = Map(
    getStanzaJson("Currency") -> expectedCurrencyStanza,
    getStanzaJson("CurrencyPoundsOnly") -> expectedCurrencyPoStanza,
    getStanzaJson("Date") -> expectedDateStanza,
    getStanzaJson("Number") -> expectedNumberStanza,
    getStanzaJson("Text") -> expectedTextStanza
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
      val input = Input(expectedCurrencyStanza, Phrase("", ""), None, None)
      val labels = LabelCache()
      val (_, updatedLabels) = input.eval("33", blankPage, labels)
      updatedLabels.updatedLabels(expectedCurrencyStanza.label).english shouldBe List("33")
    }

    "Determine invalid input to be incorrect" in {
      val input = Input(expectedCurrencyStanza, Phrase("", ""), None, None)
      input.validInput("a value") shouldBe Left(Nil)
      input.validInput("100.789") shouldBe Left(Nil)
      input.validInput("100.7a9") shouldBe Left(Nil)
    }

    "Allow for coma separated 1000s" in {
      val input = Input(expectedCurrencyStanza, Phrase("", ""), None, None)
      input.validInput("123,345,768") shouldBe Right("123345768")
    }

    "Allow -ve values" in {
      val input = Input(expectedCurrencyStanza, Phrase("", ""), None, None)
      input.validInput("-567,345") shouldBe Right("-567345")
    }

    "Determine valid input to be correct" in {
      val input = Input(expectedCurrencyStanza, Phrase("", ""), None, None)
      input.validInput("£33") shouldBe Right("33")
      input.validInput("-33") shouldBe Right("-33")
      input.validInput("£33.79") shouldBe Right("33.79")
      input.validInput("-33.99") shouldBe Right("-33.99")
      input.validInput("-£3,453,678.99") shouldBe Right("-3453678.99")
      input.validInput("33") shouldBe Right("33")
      input.validInput("33.9") shouldBe Right("33.9")
      input.validInput("33.") shouldBe Right("33")
      input.validInput("3,334") shouldBe Right("3334")
      input.validInput("1,234,567") shouldBe Right("1234567")
      input.validInput("1  234 567") shouldBe Right("1234567")
      input.validInput("1,234,567.89") shouldBe Right("1234567.89")
      input.validInput("1,234,567.8") shouldBe Right("1234567.8")
    }
  }

  "CurrencyPoundsOnlyInput " should {
    "update the input label" in {
      val input = Input(expectedCurrencyPoStanza, Phrase("", ""), None, None)
      val labels = LabelCache()
      val (_, updatedLabels) = input.eval("33", blankPage, labels)
      updatedLabels.updatedLabels(expectedCurrencyPoStanza.label).english shouldBe List("33")
    }

    "Determine invalid input to be incorrect" in {
      val input = Input(expectedCurrencyPoStanza, Phrase("", ""), None, None)
      input.validInput("a value") shouldBe Left(Nil)
      input.validInput("100.789") shouldBe Left(Nil)
      input.validInput("100.7a9") shouldBe Left(Nil)
    }

    "Allow for coma separated 1000s" in {
      val input = Input(expectedCurrencyPoStanza, Phrase("", ""), None, None)
      input.validInput("123,345,768") shouldBe Right("123345768")
    }

    "Allow -ve values" in {
      val input = Input(expectedCurrencyPoStanza, Phrase("", ""), None, None)
      input.validInput("-567,345") shouldBe Right("-567345")
    }

    "Determine valid input to be correct" in {
      val input = Input(expectedCurrencyPoStanza, Phrase("", ""), None, None)
      input.validInput("£33") shouldBe Right("33")
      input.validInput("-33") shouldBe Right("-33")
      input.validInput("£33.79") shouldBe Left(Nil)
      input.validInput("-33.99") shouldBe Left(Nil)
      input.validInput("-£3,453,678.99") shouldBe Left(Nil)
      input.validInput("33") shouldBe Right("33")
      input.validInput("33.9") shouldBe Left(Nil)
      input.validInput("33.") shouldBe Left(Nil)
      input.validInput("3,334") shouldBe Right("3334")
      input.validInput("3 334") shouldBe Right("3334")
      input.validInput("1,234,567") shouldBe Right("1234567")
      input.validInput("1,234,567.89") shouldBe Left(Nil)
      input.validInput("1,234,567.8") shouldBe Left(Nil)
    }
  }

  "DateInput" should {
    "update the input label" in {
      val input = Input(expectedDateStanza, Phrase("", ""), None, None)
      val labels = LabelCache()
      val (_, updatedLabels) = input.eval("33", blankPage, labels)
      updatedLabels.updatedLabels(expectedDateStanza.label).english shouldBe List("33")
    }

    "Determine invalid input to be incorrect" in {
      val input = Input(expectedDateStanza, Phrase("", ""), None, None)
      input.validInput("a value") shouldBe Left(Nil)
      input.validInput("100.78") shouldBe Left(Nil)
      input.validInput("100.7a") shouldBe Left(Nil)
      input.validInput("1,987") shouldBe Left(Nil)
      input.validInput("-87") shouldBe Left(Nil)
      input.validInput("31/9/2001") shouldBe Left(List(0))
      input.validInput("29/2/2001") shouldBe Left(List(0))
    }

    "Determine valid input to be correct" in {
      val input = Input(expectedDateStanza, Phrase("", ""), None, None)
      input.validInput("5/6/1989") shouldBe Right("5/6/1989")
      input.validInput("28/2/1999") shouldBe Right("28/2/1999")
      input.validInput("28 /2/19 99") shouldBe Right("28/2/1999")
      input.validInput("29/2/2000") shouldBe Right("29/2/2000")
    }
  }

  "TextInput" should {
    "update the input label" in {
      val input = Input(expectedTextStanza, Phrase("", ""), None, None)
      val labels = LabelCache()
      val (_, updatedLabels) = input.eval("hello", blankPage, labels)
      updatedLabels.updatedLabels(expectedTextStanza.label).english shouldBe List("hello")
    }

    "Determine invalid input to be incorrect" in {
      val input = Input(expectedTextStanza, Phrase("Name", "Name"), None, None)
      input.validInput("") shouldBe Left(Nil)
    }

    "Determine valid input to be correct" in {
      val input = Input(expectedTextStanza, Phrase("", ""), None, None)
      input.validInput("a value") shouldBe Right("a value")
      input.validInput("""any valid text!@£%^&*()":;'?><,./""") shouldBe Right("""any valid text!@£%^&*()":;'?><,./""")
    }

    "Not populate tax code labels when the label for the input doesn't match the taxCodeLabelPattern" in {
      val labels = LabelCache()

      val expectedTextStanza: InputStanza = InputStanza(Txt, Seq("1"), 0, Some(1), "assetLabel", Some(2), stack = false)
      val input = Input(expectedTextStanza, Phrase("", ""), None, None)
      val (_, updatedLabels) = input.eval("assetValue", blankPage, labels)

      updatedLabels.updatedLabels("assetLabel").english shouldBe List("assetValue")
      updatedLabels.updatedLabels.get("__TaxCode_primary") shouldBe None
      updatedLabels.updatedLabels.get("__TaxCode_primary_prefix") shouldBe None
      updatedLabels.updatedLabels.get("__TaxCode_primary_main") shouldBe None
      updatedLabels.updatedLabels.get("__TaxCode_primary_suffix") shouldBe None
      updatedLabels.updatedLabels.get("__TaxCode_primary_cumulative") shouldBe None
    }

    "Correctly update labels for a valid tax code" in {
      val labels = LabelCache()

      val expectedTextStanza: InputStanza = InputStanza(Txt, Seq("1"), 0, Some(1), "__TaxCode_primary", Some(2), stack = false)
      val input = Input(expectedTextStanza, Phrase("", ""), None, None)
      val (_, updatedLabels) = input.eval("S1250LX", blankPage, labels)

      updatedLabels.updatedLabels("__TaxCode_primary").english shouldBe List("S1250LX")
      updatedLabels.updatedLabels("__TaxCode_primary_prefix").english shouldBe List("S")
      updatedLabels.updatedLabels("__TaxCode_primary_main").english shouldBe List("1250")
      updatedLabels.updatedLabels("__TaxCode_primary_suffix").english shouldBe List("L")
      updatedLabels.updatedLabels("__TaxCode_primary_cumulative").english shouldBe List("X")
    }

    "Correctly update labels when input doesn't match tax code pattern" in {
      val labels = LabelCache()

      val expectedTextStanza: InputStanza = InputStanza(Txt, Seq("1"), 0, Some(1), "__TaxCode_primary", Some(2), stack = false)
      val input = Input(expectedTextStanza, Phrase("", ""), None, None)
      val (_, updatedLabels) = input.eval("Not A TaxCode value", blankPage, labels)

      updatedLabels.updatedLabels("__TaxCode_primary").english shouldBe List("Not A TaxCode value")
      updatedLabels.updatedLabels("__TaxCode_primary_prefix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_main").english shouldBe List("BLANK")
      updatedLabels.updatedLabels("__TaxCode_primary_suffix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_cumulative").english shouldBe List("")
    }

    "Handle tax codes with spaces" in {
      val labels = LabelCache()

      val expectedTextStanza: InputStanza = InputStanza(Txt, Seq("1"), 0, Some(1), "__TaxCode_primary", Some(2), stack = false)
      val input = Input(expectedTextStanza, Phrase("", ""), None, None)
      val (_, updatedLabels) = input.eval("1250 L", blankPage, labels)

      updatedLabels.updatedLabels("__TaxCode_primary").english shouldBe List("1250 L")
      updatedLabels.updatedLabels("__TaxCode_primary_prefix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_main").english shouldBe List("BLANK")
      updatedLabels.updatedLabels("__TaxCode_primary_suffix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_cumulative").english shouldBe List("")
    }

    "Handle empty tax code string" in {
      val labels = LabelCache()

      val expectedTextStanza: InputStanza = InputStanza(Txt, Seq("1"), 0, Some(1), "__TaxCode_primary", Some(2), stack = false)
      val input = Input(expectedTextStanza, Phrase("", ""), None, None)
      val (_, updatedLabels) = input.eval("", blankPage, labels)

      updatedLabels.updatedLabels("__TaxCode_primary").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_prefix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_main").english shouldBe List("BLANK")
      updatedLabels.updatedLabels("__TaxCode_primary_suffix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_cumulative").english shouldBe List("")
    }
  }

  "PassphraseInput" should {

    "update the passphrase response labels" in {
      val encrypter = new Encrypter { def encrypt(p: String): String = p.reverse }
      val labels: Labels =
        LabelCache(Map[String, Label](), Map[String, Label](), Nil, Map[String, Stanza](), Map[String, Int](), message(Lang("en")), Published, encrypter)
      //val labels = LabelCache()
      val inputText = "Hello"
      val encryptedInput = labels.encrypt(inputText)
      val input = Input(expectedPassphraseStanza, Phrase("", ""), None, None)
      val (_, updatedLabels) = input.eval(inputText, blankPage, labels)
      updatedLabels.updatedLabels(SecuredProcess.PassPhraseResponseLabelName).english shouldBe List(inputText)
      updatedLabels.updatedLabels(SecuredProcess.EncryptedPassphraseResponseLabelName).english shouldBe List(encryptedInput)
    }

    "Determine invalid input to be incorrect" in {
      val input = Input(expectedPassphraseStanza, Phrase("Name", "Name"), None, None)
      input.validInput("") shouldBe Left(Nil)
    }

    "Determine valid input to be correct" in {
      val input = Input(expectedPassphraseStanza, Phrase("", ""), None, None)
      input.validInput("a value") shouldBe Right("a value")
      input.validInput("""any valid text!@£%^&*()":;'?><,./""") shouldBe Right("""any valid text!@£%^&*()":;'?><,./""")
    }
  }

  "NumberInput " should {
    "update the input label" in {
      val input = Input(expectedNumberStanza, Phrase("", ""), None, None)
      val labels = LabelCache()
      val (_, newLabels) = input.eval("33", blankPage, labels)
      newLabels.updatedLabels(expectedNumberStanza.label).english shouldBe List("33")
    }

    "Determine invalid input to be incorrect" in {
      val input = Input(expectedNumberStanza, Phrase("", ""), None, None)
      input.validInput("a value") shouldBe Left(Nil)
      input.validInput("100.78") shouldBe Left(Nil)
      input.validInput("100.7a") shouldBe Left(Nil)
      input.validInput("£33") shouldBe Left(Nil)
      input.validInput("1,000") shouldBe Right("1000")
      input.validInput("") shouldBe Left(Nil)
    }

    "Allow -ve values" in {
      val input = Input(expectedNumberStanza, Phrase("", ""), None, None)
      input.validInput("-567345") shouldBe Right("-567345")
    }

    "Dont allow values outside range of Int.MinValue <= x <= Int.MaxValue" in {
      val input = Input(expectedNumberStanza, Phrase("", ""), None, None)
      val tooNegative: Long = -1L + Int.MinValue
      val tooPositive: Long = 1L + Int.MaxValue

      input.validInput(tooNegative.toString) shouldBe Left(Nil)
      input.validInput(tooPositive.toString) shouldBe Left(Nil)

      input.validInput(Int.MinValue.toString) shouldBe Right(Int.MinValue.toString)
      input.validInput(Int.MaxValue.toString) shouldBe Right(Int.MaxValue.toString)
    }

    "Determine valid input to be correct" in {

      val input = Input(expectedNumberStanza, Phrase("", ""), None, None)
      input.validInput("33") shouldBe Right("33")
      input.validInput("3 3 ") shouldBe Right("33")
    }
  }

  "TaxCodeInput " should {
    "Extract components from a valid tax code for a specific label" in {
      val labels = LabelCache()
      val updatedLabels = TaxCodeUtilities.retrieveTaxCodeComponents(labels, "1250L", "__TaxCode_primary")

      updatedLabels.updatedLabels("__TaxCode_primary_prefix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_main").english shouldBe List("1250")
      updatedLabels.updatedLabels("__TaxCode_primary_suffix").english shouldBe List("L")
      updatedLabels.updatedLabels("__TaxCode_primary_cumulative").english shouldBe List("")
    }

    "Extract components from a valid tax code for any other label" in {
      val labels = LabelCache()
      val updatedLabels = TaxCodeUtilities.retrieveTaxCodeComponents(labels, "1250L", "__TaxCode_test")

      updatedLabels.updatedLabels("__TaxCode_test_prefix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_test_main").english shouldBe List("1250")
      updatedLabels.updatedLabels("__TaxCode_test_suffix").english shouldBe List("L")
      updatedLabels.updatedLabels("__TaxCode_test_cumulative").english shouldBe List("")
    }

    "Set _main to 'BLANK' and others to empty string for an invalid tax code" in {
      val labels = LabelCache()
      val updatedLabels = TaxCodeUtilities.retrieveTaxCodeComponents(labels, "INVALID", "__TaxCode_primary")

      updatedLabels.updatedLabels("__TaxCode_primary_prefix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_main").english shouldBe List("BLANK")
      updatedLabels.updatedLabels("__TaxCode_primary_suffix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_cumulative").english shouldBe List("")
    }

    "Handle the shortest valid tax code" in {
      val labels = LabelCache()
      val updatedLabels = TaxCodeUtilities.retrieveTaxCodeComponents(labels, "1L", "__TaxCode_primary")

      updatedLabels.updatedLabels("__TaxCode_primary_prefix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_main").english shouldBe List("1")
      updatedLabels.updatedLabels("__TaxCode_primary_suffix").english shouldBe List("L")
      updatedLabels.updatedLabels("__TaxCode_primary_cumulative").english shouldBe List("")
    }

    "Handle the longest valid tax code" in {
      val labels = LabelCache()
      val updatedLabels = TaxCodeUtilities.retrieveTaxCodeComponents(labels, "SK9999L X", "__TaxCode_primary")

      updatedLabels.updatedLabels("__TaxCode_primary_prefix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_main").english shouldBe List("BLANK")
      updatedLabels.updatedLabels("__TaxCode_primary_suffix").english shouldBe List("")
      updatedLabels.updatedLabels("__TaxCode_primary_cumulative").english shouldBe List("")
    }

    "Extract components from a tax code with a prefix" in {
      val labels = LabelCache()
      val updatedLabels = TaxCodeUtilities.retrieveTaxCodeComponents(labels, "K1250L", "__TaxCode_primary")

      updatedLabels.updatedLabels("__TaxCode_primary_prefix").english shouldBe List("K")
      updatedLabels.updatedLabels("__TaxCode_primary_main").english shouldBe List("1250")
      updatedLabels.updatedLabels("__TaxCode_primary_suffix").english shouldBe List("L")
      updatedLabels.updatedLabels("__TaxCode_primary_cumulative").english shouldBe List("")
    }
  }

  "TaxCode regex" should {
    "match valid tax codes" in {
      validTaxCodes.foreach { taxCode =>
        withClue(s"Failed for tax code: $taxCode") {
          taxCodePattern.matches(taxCode) shouldBe true
        }
      }
    }

    "not match invalid tax codes" in {
      invalidTaxCodes.foreach { taxCode =>
        withClue(s"Incorrectly matched invalid tax code: $taxCode") {
          taxCodePattern.matches(taxCode) shouldBe false
        }
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

  def inputStanzaJsonFormat(typeName: String): String =
    s"""{"ipt_type":"$typeName","next":["1"],"name":0,"help":1,"label":"Price","placeholder":2,"stack":false}"""
  def stanzaJsonFormat(typeName: String): String =
    s"""{"type":"InputStanza","ipt_type":"$typeName","next":["1"],"name":0,"help":1,"label":"Price","placeholder":2,"stack":false}"""

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
