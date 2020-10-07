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
import models.ocelot._
import models.ocelot.errors.{GuidanceError, UnknownCalcOperationType}

import play.api.libs.json._

class CalculationStanzaSpec extends BaseSpec {

  def getMultipleCalcCalculationStanzaAsJsValue(): JsValue = Json.parse(
    s"""|{
        | "next": [ "21" ],
        | "stack": true,
        | "type": "CalculationStanza",
        | "calcs": [
        |            {"left":"[label:inputA]",
        |             "op":"add",
        |             "right":"[label:inputB]",
        |             "label":"outputA"
        |             },
        |             {"left":"[label:outputA]",
        |             "op":"subtract",
        |             "right":"[label:inputC]",
        |             "label":"outputB"
        |             }
        |           ]
        |}""".stripMargin

  )

  val validCalculationStanzaAsJsObject: JsObject = getMultipleCalcCalculationStanzaAsJsValue().as[JsObject]

  trait Test {

    def getSingleCalcCalculationStanzaAsJsValue(
        left: String,
        calcOperationType: String,
        right: String,
        label: String): JsValue = Json.parse(
      s"""|{
          | "next": [ "1" ],
          | "stack": false,
          | "type": "CalculationStanza",
          | "calcs": [
          |            {"left":"$left",
          |             "op":"$calcOperationType",
          |             "right":"$right",
          |             "label":"$label"
          |             }
          |           ]
          |}""".stripMargin
    )

    def getZeroCalcCalculationStanzaAsJsValue(): JsValue = Json.parse(
      s"""|{
          | "type": "CalculationStanza",
          | "calcs": [],
          |  "next": [ "21" ],
          |  "stack": true
          |}""".stripMargin
    )

    // Define expected deserialization results
    val c1Left: String = "[label:input1A]"
    val c1Right: String = "[label:input1B"
    val c1Label: String = "outputA"

    val c1CalcAdd: CalcOperation = CalcOperation( c1Left, Addition, c1Right, c1Label)
    val c1CalcSub: CalcOperation = CalcOperation( c1Left, Subtraction, c1Right, c1Label)

    val expectedSingleAdditionCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcAdd), Seq( "1" ), stack = false)

    val expectedSingleSubtractionCalcCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcSub), Seq( "1" ), stack = false)

    val expectedMultipleCalcCalculationStanza: CalculationStanza =
      CalculationStanza(
        Seq(CalcOperation("[label:inputA]", Addition, "[label:inputB]", "outputA"),
          CalcOperation("[label:outputA]", Subtraction, "[label:inputC]", "outputB" )),
        Seq( "21"),
        stack = true
      )

    val sqrt = "sqrt"

    val onePageJsonWithInvalidCalcOperationType: String =
      s"""
         |{
         |  "meta": {
         |    "title": "Customer wants to make a cup of tea",
         |    "id": "oct90001",
         |    "ocelot": 1,
         |    "lastAuthor": "000000",
         |    "lastUpdate": 1500298931016,
         |    "version": 4,
         |    "filename": "oct90001.js",
         |    "titlePhrase": 8,
         |    "processCode": "CupOfTea"
         |  },
         |  "howto": [],
         |  "contacts": [],
         |  "links": [],
         |  "flow": {
         |    "start": {
         |      "type": "PageStanza",
         |      "url": "/feeling-bad",
         |      "next": ["2"],
         |      "stack": true
         |    },
         |    <calcStanza>,
         |    "2": {
         |      "type": "InstructionStanza",
         |      "text": 0,
         |      "next": [
         |        "3"
         |      ],
         |      "stack": true
         |    },
         |    "end": {
         |      "type": "EndStanza"
         |    }
         |  },
         |  "phrases": [
         |    ["Ask the customer if they have a tea bag", "Welsh, Ask the customer if they have a tea bag"],
         |    ["Do you have a tea bag?", "Welsh, Do you have a tea bag?"],
         |    ["Yes - they do have a tea bag", "Welsh, Yes - they do have a tea bag"],
         |    ["No - they do not have a tea bag", "Welsh, No - they do not have a tea bag"],
         |    ["Ask the customer if they have a cup", "Welsh, Ask the customer if they have a cup"],
         |    ["Do you have a cup?", "Welsh, Do you have a cup?"],
         |    ["yes - they do have a cup ", "Welsh, yes - they do have a cup "],
         |    ["no - they don’t have a cup", "Welsh, no - they don’t have a cup"],
         |    ["Customer wants to make a cup of tea", "Welsh, Customer wants to make a cup of tea"]
         |  ]
         |}
    """.stripMargin

    val calculationStanzaWithUnknownOperationTypeName: String =
      """|"3": {
         |"next": [ "end" ],
         |"stack": false,
         |"type": "CalculationStanza",
         |"calcs": [
         |{  "left":"inputA",
         |   "op":"sqrt",
         |   "right":"inputB",
         | "label":"result"
         |}
         |]
         |}""".stripMargin

    val calculationStanzaWithIncorrectOperationType: String =
      """|"3": {
         |"next": [ "end" ],
         |"stack": false,
         |"type": "CalculationStanza",
         |"calcs": [
         |{  "left":"inputA",
         |   "op":false,
         |   "right":"inputB",
         | "label":"result"
         |}
         |]
         |}""".stripMargin

    def getOnePageJsonWithInvalidCalcOperationType(flowDef: String, calcStanzaDef: String): JsValue = Json.parse(
      flowDef.replaceAll("<calcStanza>", calcStanzaDef)
    )
  }

  "Reading a valid calculation stanza" should {

    "deserialize calculation stanza with single addition operation" in new Test {

      val calcStanzaAsJsValue: JsValue = getSingleCalcCalculationStanzaAsJsValue(c1Left, "add", c1Right, c1Label)

      calcStanzaAsJsValue.validate[CalculationStanza] match {
        case JsSuccess(calcStanza, _) => calcStanza shouldBe expectedSingleAdditionCalculationStanza
        case e: JsError => fail( "Unable to parse single addition calculation stanza")
      }
    }

    "deserialize calculation stanza with single subtraction operation" in new Test {

      val calcStanzaAsJsValue: JsValue = getSingleCalcCalculationStanzaAsJsValue(c1Left, "subtract", c1Right, c1Label)

      calcStanzaAsJsValue.validate[CalculationStanza] match {
        case JsSuccess(calcStanza, _) => calcStanza shouldBe expectedSingleSubtractionCalcCalculationStanza
        case e:JsError => fail( "Unable to parse single subtraction calculation stanza")
      }
    }

    "deserialize calculation stanza with multiple calculation operations" in new Test {

      val calcStanzaAsJsValue: JsValue = getMultipleCalcCalculationStanzaAsJsValue()

      calcStanzaAsJsValue.validate[CalculationStanza] match {
        case JsSuccess(calcStanza, _) => calcStanza shouldBe expectedMultipleCalcCalculationStanza
        case e: JsError => fail( "Unable to parse multiple calculation operations calculation stanza")
      }
    }

  }

  "Reading an invalid JSON representation of a calculation stanza" should {

    "Raise an error for a calculation stanza with an empty array of calculation operations" in new Test {

      val invalidCalcStanzaAsJsValue: JsValue = getZeroCalcCalculationStanzaAsJsValue()

      invalidCalcStanzaAsJsValue.validate[CalculationStanza] match {
        case e: JsError => succeed
        case _ => fail( "An instance of CalculationStanza should not be created when there are no calculation operations")
      }
    }

    "Raise an error for a calculation stanza with an incorrect operation type" in new Test {

      val invalidCalcStanzaAsJsValue: JsValue = getSingleCalcCalculationStanzaAsJsValue(c1Left, sqrt, c1Right, c1Label)

      invalidCalcStanzaAsJsValue.validate[CalculationStanza] match {
        case e: JsError => succeed
        case _ => fail("An instance of CalculationStanza should not be created when an unsupported operation is defined")
      }
    }

    /** Test for missing properties in Json object representing instruction stanzas */
    missingJsObjectAttrTests[CalculationStanza](validCalculationStanzaAsJsObject, List("type"))

    /** Test for properties of the wrong type in json object representing instruction stanzas */
    incorrectPropertyTypeJsObjectAttrTests[CalculationStanza](validCalculationStanzaAsJsObject, List("type"))
  }

  "Writing instances of calculation stanzas to JSON" should {

    "serialize a calculation stanza with a single addition operation" in new Test {

      val expectedResult: String = getSingleCalcCalculationStanzaAsJsValue(c1Left, "add", c1Right, c1Label).toString()

      val stanza: Stanza = expectedSingleAdditionCalculationStanza
      val actualResult: String = Json.toJson(stanza).toString

      actualResult shouldBe expectedResult
    }

    "serialize a calculation stanza with a single subtraction operation" in new Test {

      val expectedResult: String = getSingleCalcCalculationStanzaAsJsValue(c1Left, "subtract", c1Right, c1Label).toString()

      val stanza: Stanza = expectedSingleSubtractionCalcCalculationStanza
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedResult
    }

    "serialize a calculation stanza with multiple operations" in new Test {

      val expectedResult: String = getMultipleCalcCalculationStanzaAsJsValue().toString()

      val stanza: Stanza = expectedMultipleCalcCalculationStanza
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedResult
    }
  }

  "Page building" must {

    "Raise an error for a invalid calculation operation type name" in new Test {

      getOnePageJsonWithInvalidCalcOperationType(
        onePageJsonWithInvalidCalcOperationType,
        calculationStanzaWithUnknownOperationTypeName
      ).as[JsObject].validate[Process] match {
        case JsSuccess(_,_) => fail( "A process should not be created from invalid JSON")
        case JsError(errs) => GuidanceError.fromJsonValidationErrors(errs) match {
          case Nil => fail("Nothing to match from guidance error convergence")
          case UnknownCalcOperationType("3", "sqrt") :: _ => succeed
          case errs => fail( "An error occurred processing Json validation errors")
        }
      }
    }

    "Raise an error when the calculation operation is of the wrong JsValue type" in new Test {

      getOnePageJsonWithInvalidCalcOperationType(
        onePageJsonWithInvalidCalcOperationType,
        calculationStanzaWithIncorrectOperationType
      ).as[JsObject].validate[Process] match {
        case JsSuccess(_,_) => fail("A process should not be created from invalid JSON")
        case JsError(errs) => GuidanceError.fromJsonValidationErrors(errs) match {
          case Nil => fail("Nothing to match from guidance error conversion")
          case UnknownCalcOperationType("3", "false") :: _ => {
            succeed
          }
          case errs => fail("An error occurred processing Json validation errors")
        }
      }
    }
  }

}
