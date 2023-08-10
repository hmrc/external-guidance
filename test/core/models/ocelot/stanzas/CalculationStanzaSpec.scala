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
import core.models.ocelot.{LabelCache, _}
import core.models.ocelot.errors.{GuidanceError, UnknownCalcOperationType}
import play.api.libs.json._

class CalculationStanzaSpec extends BaseSpec {

  def getMultipleCalcCalculationStanzaAsJsValue: JsValue = Json.parse(
    s"""|{
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
        |             },
        |             {"left":"[label:outputA]",
        |             "op":"multiply",
        |             "right":"[label:inputC]",
        |             "label":"outputE"
        |             },
        |             {"left":"[label:outputA]",
        |             "op":"divide",
        |             "right":"[label:inputC]",
        |             "label":"outputF"
        |             },
        |             {"left":"[label:inputC]",
        |             "op":"ceiling",
        |             "right":"0",
        |             "label":"outputC"
        |             },
        |             {"left":"[label:inputD]",
        |             "op":"floor",
        |             "right":"2",
        |             "label":"outputD"
        |             }
        |           ],
        | "next": [ "21" ],
        | "stack": true
        |}""".stripMargin

  )

  val validCalculationStanzaAsJsObject: JsObject = getMultipleCalcCalculationStanzaAsJsValue.as[JsObject]

  trait Test {

    def getSingleCalcCalculationStanzaAsJsValue(
                                                 left: String,
                                                 calcOperationType: String,
                                                 right: String,
                                                 label: String): JsValue = Json.parse(
      s"""|{
          | "type": "CalculationStanza",
          | "calcs": [
          |            {"left":"$left",
          |             "op":"$calcOperationType",
          |             "right":"$right",
          |             "label":"$label"
          |             }
          |           ],
          | "next": [ "1" ],
          | "stack": false
          |}""".stripMargin
    )

    def getZeroCalcCalculationStanzaAsJsValue: JsValue = Json.parse(
      s"""|{
          | "type": "CalculationStanza",
          | "calcs": [],
          |  "next": [ "21" ],
          |  "stack": true
          |}""".stripMargin
    )

    val stanzaId: String = "5"

    // Define expected deserialization results
    val c1Left: String = "[label:input1A]"
    val c1Right: String = "[label:input1B]"
    val c1CeilingRight: String = "1"
    val c1FloorRight: String = "-2"
    val c1Label: String = "outputA"

    val c1CalcAdd: CalcOperation = CalcOperation(c1Left, Addition, c1Right, c1Label)
    val c1CalcSub: CalcOperation = CalcOperation(c1Left, Subtraction, c1Right, c1Label)
    val c1CalcMult: CalcOperation = CalcOperation(c1Left, Multiply, c1Right, c1Label)
    val c1CalcDiv: CalcOperation = CalcOperation(c1Left, Divide, c1Right, c1Label)
    val c1CalcCeiling: CalcOperation = CalcOperation(c1Left, Ceiling, c1CeilingRight, c1Label)
    val c1CalcFloor: CalcOperation = CalcOperation(c1Left, Floor, c1FloorRight, c1Label)

    val expectedSingleAdditionCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcAdd), Seq("1"), stack = false)

    val expectedSingleSubtractionCalcCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcSub), Seq("1"), stack = false)

    val expectedSingleMultiplyCalcCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcMult), Seq("1"), stack = false)

    val expectedSingleDivideCalcCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcDiv), Seq("1"), stack = false)

    val expectedSingleCeilingCalcCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcCeiling), Seq("1"), stack = false)

    val expectedSingleFloorCalcCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcFloor), Seq("1"), stack = false)

    val expectedMultipleCalcCalculationStanza: CalculationStanza =
      CalculationStanza(
        Seq(CalcOperation("[label:inputA]", Addition, "[label:inputB]", "outputA"),
          CalcOperation("[label:outputA]", Subtraction, "[label:inputC]", "outputB"),
          CalcOperation("[label:outputA]", Multiply, "[label:inputC]", "outputE"),
          CalcOperation("[label:outputA]", Divide, "[label:inputC]", "outputF"),
          CalcOperation("[label:inputC]", Ceiling, "0", "outputC"),
          CalcOperation("[label:inputD]", Floor, "2", "outputD")
        ),
        Seq("21"),
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
         |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
         |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
         |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
         |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
         |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
         |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
         |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
         |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
         |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
         |  ]
         |}
    """.stripMargin

    val calculationStanzaWithUnknownOperationType: String =
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

    val calculationStanzaWithIncorrectType: String =
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

    def getIntegerScaleFactorCalculationStanzaAsJsValue: JsValue = Json.parse(
      s"""|{
          | "type": "CalculationStanza",
          | "calcs": [
          |             {"left":"[label:inputC]",
          |             "op":"ceiling",
          |             "right":"0",
          |             "label":"outputC"
          |             },
          |             {"left":"[label:inputE]",
          |             "op":"floor",
          |             "right":"-1",
          |             "label":"outputD"
          |             }
          |           ],
          | "next": [ "21" ],
          | "stack": true,
          |}""".stripMargin
    )

    def getInvalidScaleFactorCalculationStanzaAsJsValue(op: String, scaleFactor: String): JsValue = Json.parse(
      s"""|{
          | "next": [ "21" ],
          | "stack": true,
          | "type": "CalculationStanza",
          | "calcs": [
          |             {"left":"[label:inputE]",
          |             "op":"$op",
          |             "right":"$scaleFactor",
          |             "label":"outputD"
          |             }
          |           ]
          |}""".stripMargin
    )

    val labelX: String = "[label:x]"
    val tenAsString = "10"
    val labelY: String = "[label:y]"
    val twentyAsString: String = "20"
    val result1: String = "result1"
    val result2: String = "result2"

    val calcOperations: Seq[CalcOperation] = Seq(
      CalcOperation("[label:input1]", Addition, "[label:input2]", "output1"),
      CalcOperation("[label:input3]", Addition, "[label:input4]", "output2"),
      CalcOperation("[label:input5]", Subtraction, "[label:input6]", "output3"),
      CalcOperation("[label:input7]", Subtraction, "[label:input8]", "output4")
    )

    val next: Seq[String] = Seq("16")

    val exampleCalcStanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)
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

    "deserialize calculation stanza with single multiply operation" in new Test {

      val calcStanzaAsJsValue: JsValue = getSingleCalcCalculationStanzaAsJsValue(c1Left, "multiply", c1Right, c1Label)

      calcStanzaAsJsValue.validate[CalculationStanza] match {
        case JsSuccess(calcStanza, _) => calcStanza shouldBe expectedSingleMultiplyCalcCalculationStanza
        case e:JsError => fail( "Unable to parse single multiply calculation stanza")
      }
    }

    "deserialize calculation stanza with single divide operation" in new Test {

      val calcStanzaAsJsValue: JsValue = getSingleCalcCalculationStanzaAsJsValue(c1Left, "divide", c1Right, c1Label)

      calcStanzaAsJsValue.validate[CalculationStanza] match {
        case JsSuccess(calcStanza, _) => calcStanza shouldBe expectedSingleDivideCalcCalculationStanza
        case e:JsError => fail( "Unable to parse single divide calculation stanza")
      }
    }

    "deserialize calculation stanza with single ceiling operation" in new Test {

      val calcStanzaAsJsValue: JsValue = getSingleCalcCalculationStanzaAsJsValue(c1Left, "ceiling", c1CeilingRight, c1Label)

      calcStanzaAsJsValue.validate[CalculationStanza] match {
        case JsSuccess(calcStanza, _) => calcStanza shouldBe expectedSingleCeilingCalcCalculationStanza
        case e:JsError => fail( "Unable to parse single ceiling calculation stanza")
      }
    }

    "deserialize calculation stanza with single floor operation" in new Test {

      val calcStanzaAsJsValue: JsValue = getSingleCalcCalculationStanzaAsJsValue(c1Left, "floor", c1FloorRight, c1Label)

      calcStanzaAsJsValue.validate[CalculationStanza] match {
        case JsSuccess(calcStanza, _) => calcStanza shouldBe expectedSingleFloorCalcCalculationStanza
        case e:JsError => fail( "Unable to parse single floor calculation stanza")
      }
    }

    "deserialize calculation stanza with multiple calculation operations" in new Test {

      val calcStanzaAsJsValue: JsValue = getMultipleCalcCalculationStanzaAsJsValue

      calcStanzaAsJsValue.validate[CalculationStanza] match {
        case JsSuccess(calcStanza, _) => calcStanza shouldBe expectedMultipleCalcCalculationStanza
        case e: JsError => fail( "Unable to parse multiple calculation operations calculation stanza")
      }
    }

  }

  "Reading an invalid JSON representation of a calculation stanza" should {

    "Raise an error for a calculation stanza with an empty array of calculation operations" in new Test {

      val invalidCalcStanzaAsJsValue: JsValue = getZeroCalcCalculationStanzaAsJsValue

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

    /** Test for missing properties in Json object representing calculation stanza */
    missingJsObjectAttrTests[CalculationStanza](validCalculationStanzaAsJsObject, List("type"))

    /** Test for properties of the wrong type in json object representing calculation stanza */
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

    "serialize a calculation stanza with a single multiply operation" in new Test {

      val expectedResult: String = getSingleCalcCalculationStanzaAsJsValue(c1Left, "multiply", c1Right, c1Label).toString()

      val stanza: Stanza = expectedSingleMultiplyCalcCalculationStanza
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedResult
    }

    "serialize a calculation stanza with a single divide operation" in new Test {

      val expectedResult: String = getSingleCalcCalculationStanzaAsJsValue(c1Left, "divide", c1Right, c1Label).toString()

      val stanza: Stanza = expectedSingleDivideCalcCalculationStanza
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedResult
    }

    "serialize a calculation stanza with a single ceiling operation" in new Test {

      val expectedResult: String = getSingleCalcCalculationStanzaAsJsValue(c1Left, "ceiling", c1CeilingRight, c1Label).toString()

      val stanza: Stanza = expectedSingleCeilingCalcCalculationStanza
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedResult
    }

    "serialize a calculation stanza with a single floor operation" in new Test {

      val expectedResult: String = getSingleCalcCalculationStanzaAsJsValue(c1Left, "floor", c1FloorRight, c1Label).toString()

      val stanza: Stanza = expectedSingleFloorCalcCalculationStanza
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedResult
    }

    "serialize a calculation stanza with multiple operations" in new Test {

      val expectedResult: String = getMultipleCalcCalculationStanzaAsJsValue.toString()

      val stanza: Stanza = expectedMultipleCalcCalculationStanza
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedResult
    }
  }

  "Page building" must {

    "Raise an error for a unknown calculation operation type" in new Test {

      getOnePageJsonWithInvalidCalcOperationType(
        onePageJsonWithInvalidCalcOperationType,
        calculationStanzaWithUnknownOperationType
      ).as[JsObject].validate[Process] match {
        case JsSuccess(_, _) => fail("A process should not be created from invalid JSON")
        case JsError(errs) => GuidanceError.fromJsonValidationErrors(mapValidationErrors(errs)) match {
            case Nil => fail("Nothing to match from guidance error conversion")
            case UnknownCalcOperationType("3", sqrt) +: _ => succeed
            case _ => fail("An error occurred processing Json validation errors")
          }
      }
    }

    "Raise an error when the calculation operation is of the wrong JsValue type" in new Test {

      getOnePageJsonWithInvalidCalcOperationType(
        onePageJsonWithInvalidCalcOperationType,
        calculationStanzaWithIncorrectType
      ).as[JsObject].validate[Process] match {
        case JsSuccess(_,_) => fail("A process should not be created from invalid JSON")
        case JsError(errs) =>
          val error = GuidanceError.fromJsonValidationErrors(mapValidationErrors(errs))
          error match {
            case Nil => fail("Nothing to match from guidance error conversion")
            case UnknownCalcOperationType("3", "false") +: _ => succeed
            case errs => fail("An error occurred processing Json validation errors")
          }
      }
    }
  }

  "Calculation" must {

    "provide an apply method to create an instance of Calculation from an instance of CalculationStanza" in new Test {

      val operations: Seq[CalcOperation] = Seq(
        CalcOperation(labelX, Addition, tenAsString, result1),
        CalcOperation(twentyAsString, Subtraction, labelY, result2),
        CalcOperation(twentyAsString, Multiply, labelY, result2),
        CalcOperation(twentyAsString, Divide, labelY, result2)
      )

      val calculationStanza: CalculationStanza = CalculationStanza(operations, next, stack = false)

      val expectedOperations: Seq[Operation] = Seq(
        AddOperation(labelX, tenAsString, result1),
        SubtractOperation(twentyAsString, labelY, result2),
        MultiplyOperation(twentyAsString, labelY, result2),
        DivideOperation(twentyAsString, labelY, result2)
      )

      val expectedCalculation: Calculation = Calculation(next, expectedOperations)

      Calculation(calculationStanza) shouldBe expectedCalculation
    }

    "provide a list of the labels defined within a calculation" in new Test {

      val calculation: Calculation = Calculation(exampleCalcStanza)

      // Define expected labels
      val output1: String = "output1"
      val output2: String = "output2"
      val output3: String = "output3"
      val output4: String = "output4"

      calculation.labels shouldBe List(output1, output2, output3, output4)
    }

    "provide a list of the label references defined in the calculation" in new Test {

      val calculation: Calculation = Calculation(exampleCalcStanza)

      val expectedReferences: List[String] = List(
        "input1",
        "input2",
        "input3",
        "input4",
        "input5",
        "input6",
        "input7",
        "input8"
      )

      calculation.labelRefs shouldBe expectedReferences
    }

    "evaluate a simple addition using constants" in {

      val calcOperations: Seq[CalcOperation] = Seq( CalcOperation("10", Addition, "25", "result"))

      val next: Seq[String] = Seq("40")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "40"

      updatedLabels.value("result") shouldBe Some("35")
    }

    "evaluate a simple subtraction using constants" in {

      val calcOperations: Seq[CalcOperation] = Seq( CalcOperation("10", Subtraction, "25", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("result") shouldBe Some("-15")
    }

    "evaluate a simple multiply using constants" in {

      val calcOperations: Seq[CalcOperation] = Seq( CalcOperation("10", Multiply, "25", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("result") shouldBe Some("250")
    }

    "evaluate a simple divide using constants" in {

      val calcOperations: Seq[CalcOperation] = Seq( CalcOperation("10", Divide, "25", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("result") shouldBe Some("0.4")
    }

    "evaluate a simple addition using label values from the label cache" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:input1]", Addition, "[label:input2]", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = ScalarLabel( "input1", List("10.00"))
      val input2: Label = ScalarLabel( "input2", List("25.00"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("result") shouldBe Some("35.00")
    }

    "evaluate a simple subtraction using label values from the label cache" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:input1]", Subtraction, "[label:input2]", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = ScalarLabel( "input1", List("64.00"))
      val input2: Label = ScalarLabel( "input2", List("32.00"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("result") shouldBe Some("32.00")
    }

    "evaluate a complex sequence of calculations using label values from the label cache" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:input1]", Addition, "[label:input2]", "output1"),
        CalcOperation("[label:output1]", Subtraction, "[label:input3]", "output2"),
        CalcOperation("[label:output2]", Addition, "[label:input4]", "output3"),
        CalcOperation("[label:output3]", Subtraction, "[label:input5]", "output4")
      )

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = ScalarLabel( "input1", List("10.00"))
      val input2: Label = ScalarLabel( "input2", List("22.00"))
      val input3: Label = ScalarLabel( "input3", List("3.00"))
      val input4: Label = ScalarLabel( "input4", List("4.00"))
      val input5: Label = ScalarLabel( "input5", List("10.00"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2,
        input3.name -> input3,
        input4.name -> input4,
        input5.name -> input5
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("output1") shouldBe Some("32.00")
      updatedLabels.value("output2") shouldBe Some("29.00")
      updatedLabels.value("output3") shouldBe Some("33.00")
      updatedLabels.value("output4") shouldBe Some("23.00")
    }

    "evaluate a simple addition using label values that have varying precision" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:input1]", Addition, "[label:input2]", "output1"),
        CalcOperation("[label:input3]", Addition, "[label:input4]", "output2"),
        CalcOperation("[label:input5]", Subtraction, "[label:input6]", "output3"),
        CalcOperation("[label:input7]", Subtraction, "[label:input8]", "output4")
      )

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = ScalarLabel( "input1", List("10."))
      val input2: Label = ScalarLabel( "input2", List("25.0"))

      val input3: Label = ScalarLabel( "input3", List("10.0"))
      val input4: Label = ScalarLabel( "input4", List("25.00"))

      val input5: Label = ScalarLabel( "input5", List("25.0"))
      val input6: Label = ScalarLabel( "input6", List("10"))

      val input7: Label = ScalarLabel( "input7", List("25.00"))
      val input8: Label = ScalarLabel( "input8", List("10"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2,
        input3.name -> input3,
        input4.name -> input4,
        input5.name -> input5,
        input6.name -> input6,
        input7.name -> input7,
        input8.name -> input8
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("output1") shouldBe Some("35.0")
      updatedLabels.value( "output2") shouldBe Some("35.00")
      updatedLabels.value( "output3") shouldBe Some("15.0")
      updatedLabels.value( "output4") shouldBe Some("15.00")
    }

    "support string addition in the form of concatenation" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:input1]", Addition, "[label:input2]", "output1"),
        CalcOperation("[label:output1]", Addition, "[label:input3]", "output2"),
        CalcOperation("[label:output2]", Addition, "[label:input4]", "output3")
      )

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = ScalarLabel( "input1", List("Hello"))
      val input2: Label = ScalarLabel( "input2", List(" "))
      val input3: Label = ScalarLabel( "input3", List("World"))
      val input4: Label = ScalarLabel( "input4", List("!"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2,
        input3.name -> input3,
        input4.name -> input4
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("output3") shouldBe Some( "Hello World!")

    }

    "not support subtraction operations on non-currency input" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:input1]", Subtraction, "[label:input2]", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = ScalarLabel( "input1", List("Today"))
      val input2: Label = ScalarLabel( "input2", List("Yesterday"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels shouldBe labelCache
    }

    "evaluate a set of date subtraction operations using constants" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("10/01/2020", Subtraction, "08/01/2020", "result1"),
        CalcOperation("8/1/2020", Subtraction, "10/1/2020", "result2"),
        CalcOperation("8/1/2020", Subtraction, "08/01/2020", "result3")
      )

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("result1") shouldBe Some("2")
      updatedLabels.value("result2") shouldBe Some("-2")
      updatedLabels.value("result3") shouldBe Some("0")

    }

    "evaluate a set of date subtraction operations using values from the label cache" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:date1]", Subtraction, "[label:date2]", "result1"),
        CalcOperation("[label:date3]", Subtraction, "[label:date4]", "result2"),
        CalcOperation("[label:date5]", Subtraction, "[label:date6]", "result3"),
        CalcOperation("[label:date7]", Subtraction, "[label:date8]", "result4")
      )

      val next: Seq[String] = Seq("44")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val date1: Label = ScalarLabel("date1", List("6/3/2019"))
      val date2: Label = ScalarLabel("date2", List("2/2/2019"))
      val date3: Label = ScalarLabel("date3", List("06/03/2020"))
      val date4: Label = ScalarLabel("date4", List("02/02/2020"))
      val date5: Label = ScalarLabel("date5", List("01/01/2016"))
      val date6: Label = ScalarLabel("date6", List("01/01/2015"))
      val date7: Label = ScalarLabel("date7", List("10/5/2015"))
      val date8: Label = ScalarLabel("date8", List("8/9/2007"))

      val labelMap: Map[String, Label] = Map(
        date1.name -> date1,
        date2.name -> date2,
        date3.name -> date3,
        date4.name -> date4,
        date5.name -> date5,
        date6.name -> date6,
        date7.name -> date7,
        date8.name -> date8
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "44"

      updatedLabels.value("result1") shouldBe Some("32")
      updatedLabels.value("result2") shouldBe Some("33")
      updatedLabels.value("result3") shouldBe Some("365")
      updatedLabels.value("result4") shouldBe Some("2801")
    }

    "not support addition of date values" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:date1]", Addition, "[label:date2]", "result")
      )

      val next: Seq[String] = Seq("202")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val date1: Label = ScalarLabel("date1", List("07/10/2019"))
      val date2: Label = ScalarLabel("date2", List("20/04/2021"))

      val labelMap: Map[String, Label] = Map(
        date1.name -> date1,
        date2.name -> date2
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "202"

      updatedLabels shouldBe labelCache
    }

    "support ceiling operations where operands are defined by constants and the scale is zero" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("10.3", Ceiling, "0", "output1"),
        CalcOperation("10.7", Ceiling, "0", "output2"),
        CalcOperation("10.36", Ceiling, "0", "output3"),
        CalcOperation("10.82", Ceiling, "0", "output4"),
        CalcOperation("10.45", Ceiling, "0", "output5"),
        CalcOperation("-10.3", Ceiling, "0", "output6"),
        CalcOperation("-10.45", Ceiling, "0", "output7")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("11")
      updatedLabels.value("output2") shouldBe Some("11")
      updatedLabels.value("output3") shouldBe Some("11")
      updatedLabels.value("output4") shouldBe Some("11")
      updatedLabels.value("output5") shouldBe Some("11")
      updatedLabels.value("output6") shouldBe Some("-10")
      updatedLabels.value("output7") shouldBe Some("-10")
    }

    "support ceiling operations where operands are defined by constants and the scale is one" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("10.36", Ceiling, "1", "output1"),
        CalcOperation("10.82", Ceiling, "1", "output2"),
        CalcOperation("-10.82", Ceiling, "1", "output3")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("10.4")
      updatedLabels.value("output2") shouldBe Some("10.9")
      updatedLabels.value("output3") shouldBe Some("-10.8")
    }

    "not modify values with two decimal places when applying ceiling with a scale factor of two" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("122.11", Ceiling, "2", "output1"),
        CalcOperation("-122.11", Ceiling, "2", "output2")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("122.11")
      updatedLabels.value("output2") shouldBe Some("-122.11")
    }

    "support ceiling operations where operands are defined by constants and the scale is minus one" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("122.11", Ceiling, "-1", "output1"),
        CalcOperation("-122.11", Ceiling, "-1", "output2")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("130")
      updatedLabels.value("output2") shouldBe Some("-120")
    }

    "support ceiling operations where operands are defined by constants and the scale is minus two" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("122.11", Ceiling, "-2", "output1"),
        CalcOperation("-122.11", Ceiling, "-2", "output2")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("200")
      updatedLabels.value("output2") shouldBe Some("-100")
    }

    "support floor operations where operands are defined by constants and the scale is zero" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("10.3", Floor, "0", "output1"),
        CalcOperation("10.7", Floor, "0", "output2"),
        CalcOperation("10.36", Floor, "0", "output3"),
        CalcOperation("10.82", Floor, "0", "output4"),
        CalcOperation("10.45", Floor, "0", "output5"),
        CalcOperation("-10.3", Floor, "0", "output6"),
        CalcOperation("-10.45", Floor, "0", "output7")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("10")
      updatedLabels.value("output2") shouldBe Some("10")
      updatedLabels.value("output3") shouldBe Some("10")
      updatedLabels.value("output4") shouldBe Some("10")
      updatedLabels.value("output5") shouldBe Some("10")
      updatedLabels.value("output6") shouldBe Some("-11")
      updatedLabels.value("output7") shouldBe Some("-11")
    }

    "support floor operations where operands are defined by constants and the scale is one" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("10.36", Floor, "1", "output1"),
        CalcOperation("10.82", Floor, "1", "output2"),
        CalcOperation("-10.82", Floor, "1", "output3")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("10.3")
      updatedLabels.value("output2") shouldBe Some("10.8")
      updatedLabels.value("output3") shouldBe Some("-10.9")
    }

    "not modify values with two decimal places when applying floor with a scale factor of two" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("122.11", Floor, "2", "output1"),
        CalcOperation("-122.11", Floor, "2", "output2")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("122.11")
      updatedLabels.value("output2") shouldBe Some("-122.11")
    }

    "support floor operations where operands are defined by constants and the scale is minus one" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("122.11", Floor, "-1", "output1"),
        CalcOperation("-122.11", Floor, "-1", "output2")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("120")
      updatedLabels.value("output2") shouldBe Some("-130")
    }

    "support floor operations where operands are defined by constants and the scale is minus two" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("122.11", Floor, "-2", "output1"),
        CalcOperation("-122.11", Floor, "-2", "output2")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("100")
      updatedLabels.value("output2") shouldBe Some("-200")
    }

    "support ceiling operations where the left operand is defined by a label" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:input1]", Ceiling, "0", "output1" )
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = ScalarLabel("input1", List("10.5"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("11")
    }

    "support floor operations where the left operand is defined by a label" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:input1]", Floor, "0", "output1" )
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = ScalarLabel("input1", List("1021.25"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("1021")
    }

    "not support ceiling operations on operands of incorrect type" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("", Ceiling, "1", "output")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("25"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "25"

      updatedLabels shouldBe labelCache
    }

    "not support floor operations on operands of incorrect type" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("input", Floor, "0", "output")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("25"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val label: Label = ScalarLabel("label", List("data"))

      val labelMap: Map[String, Label] = Map(label.name -> label)

      val labelCache: Labels = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "25"

      updatedLabels shouldBe labelCache
    }

    "ignore floor operations where the value to be rounded is not defined" in {

      val calcOperation: Seq[CalcOperation] = Seq(CalcOperation("[label:missing]", Floor, "0", "output"))

      val stanza: CalculationStanza = CalculationStanza(calcOperation, Seq("28"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache: Labels = LabelCache()

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "28"

      updatedLabels shouldBe labelCache
    }

    "ignore ceiling operations where the scale value is not defined" in {

      val calcOperation: Seq[CalcOperation] = Seq(CalcOperation("[label:value]", Ceiling, "[label:missing]", "output"))

      val stanza: CalculationStanza = CalculationStanza(calcOperation, Seq("28"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val value: ScalarLabel = ScalarLabel("value", List("10.4"))

      val labelMap: Map[String, Label] = Map(
        value.name -> value
      )

      val labelCache: Labels = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = calculation.eval(labelCache)

      nextStanza shouldBe "28"

      updatedLabels shouldBe labelCache
    }

    "evaluate addition to a list using a constant" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:list]", Addition, "four", "result"))

      val next: Seq[String] = Seq("20")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = true)

      val calculation: Calculation = Calculation(stanza)

      val list: ListLabel = ListLabel("list", List("one", "two", "three"))

      val labelMap: Map[String, Label] = Map(list.name -> list)

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result") shouldBe Some(List("one", "two", "three", "four"))
    }

    "evaluate addition to a list using a label value" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:list]", Addition, "[label:scalar]", "result"))

      val next: Seq[String] = Seq("1")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = true)

      val calculation: Calculation = Calculation(stanza)

      val list: ListLabel = ListLabel("list", List("five", "six"))
      val scalar: ScalarLabel = ScalarLabel("scalar", List("seven"))

      val labelMap: Map[String, Label] = Map(
        list.name -> list,
        scalar.name -> scalar
      )

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result") shouldBe Some(List("five", "six", "seven"))
    }

    "evaluate addition to an empty list" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:emptyList]", Addition, "one", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = true)

      val calculation: Calculation = Calculation(stanza)

      val emptyList: ListLabel = ListLabel("emptyList")

      val labelMap: Map[String, Label] = Map(emptyList.name -> emptyList)

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result") shouldBe Some(List("one"))
    }

    "evaluate addition of multiple values to a list" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:initialList]", Addition, "two", "result1"),
        CalcOperation("[label:result1]", Addition, "[label:three]", "result2"),
        CalcOperation("four", Addition, "[label:result2]", "result3")
      )

      val next: Seq[String] = Seq("45")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = true)

      val calculation: Calculation = Calculation(stanza)

      val initialList: ListLabel = ListLabel("initialList", List("one"))
      val three: ScalarLabel = ScalarLabel("three", List("three"))

      val labelMap: Map[String, Label] = Map(
        initialList.name -> initialList,
        three.name -> three
      )

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result3") shouldBe Some(List("four", "one", "two", "three"))
    }

    "not apply addition if one of the operands does not exist" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:missingList]", Addition, "10", "result"))

      val calculation: Calculation = createCalculation(calcOperations, Seq("22"))

      val labelCache: Labels = LabelCache()

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels shouldBe labelCache
    }

    "evaluate subtraction of a constant from a list" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:initialList]", Subtraction, "one", "result"))

      val calculation: Calculation = createCalculation(calcOperations, Seq("4"))

      val initialList: ListLabel = ListLabel("initialList", List("one", "two", "three", "four", "five"))

      val labelMap: Map[String, Label] = Map(initialList.name -> initialList)

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result") shouldBe Some(List("two", "three", "four", "five"))
    }

    "evaluate subtraction of value that appears multiple times in a list" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:initialList]", Subtraction, "c", "result"))

      val calculation: Calculation = createCalculation(calcOperations, Seq("52"))

      val initialList: ListLabel = ListLabel("initialList", List("a", "b", "c", "c", "d"))

      val labelMap: Map[String, Label] = Map(initialList.name -> initialList)

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result") shouldBe Some(List("a", "b", "d"))
    }

    "evaluate subtraction of a label value from a list" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:initialList]", Subtraction, "[label:minus]", "result"))

      val calculation: Calculation = createCalculation(calcOperations, Seq("7"))

      val initialList: ListLabel = ListLabel("initialList", List("January", "February", "March", "April", "May"))

      val minus: ScalarLabel = ScalarLabel("minus", List("April"))

      val labelMap: Map[String, Label] = Map(
        initialList.name -> initialList,
        minus.name -> minus
      )

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result") shouldBe Some(List("January", "February", "March", "May"))
    }

    "not evaluate subtraction of a list from a scalar label as this operation is not supported" in {

      val calcOperation: Seq[CalcOperation] = Seq(CalcOperation("[label:a]", Subtraction, "[label:list]", "result1"))

      val calculation: Calculation = createCalculation(calcOperation, Seq("8"))

      val a: ScalarLabel = ScalarLabel("a", List("A"))

      val list: ListLabel = ListLabel("list", List("A", "B", "C"))

      val labelMap: Map[String, Label] = Map(
        a.name -> a,
        list.name -> list
      )

      val labels: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labels)

      updatedLabels shouldBe labels
    }

    "successfully handle subtraction from an empty list" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:emptyList]", Subtraction, "[label:minus]", "result"))

      val calculation: Calculation = createCalculation(calcOperations, Seq("2"))

      val emptyList: ListLabel = ListLabel("emptyList")

      val minus: ScalarLabel = ScalarLabel("minus", List("Something"))

      val labelMap: Map[String, Label] = Map(
        emptyList.name -> emptyList,
        minus.name -> minus
      )

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result") shouldBe Some(Nil)
    }

    "successfully remove multiple entries from a list" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:initialList]", Subtraction, "June", "result1"),
        CalcOperation("[label:result1]", Subtraction, "[label:may]", "result2"),
        CalcOperation("[label:result2]", Subtraction, "[label:september]", "result3"),
        CalcOperation("[label:result3]", Subtraction, "April", "result4")
      )

      val calculation: Calculation = createCalculation(calcOperations, Seq("88"))

      val initialList: ListLabel = ListLabel(
        "initialList",
        List("January", "February", "March", "April", "May", "June", "July", "August", "September", "October")
      )

      val may: ScalarLabel = ScalarLabel("may", List("May"))
      val september: ScalarLabel = ScalarLabel("september", List("September"))

      val labelMap: Map[String, Label] = Map(
        initialList.name -> initialList,
        may.name -> may,
        september.name -> september
      )

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result4") shouldBe Some(List("January", "February", "March", "July", "August", "October"))
    }

    "not apply subtraction when one of the operands does not exist" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:list]", Subtraction, "[label:missing]", "result"))

      val calculation: Calculation = createCalculation(calcOperations, Seq("9"))

      val list: ListLabel = ListLabel("list", List("202"))

      val labelMap: Map[String, Label] = Map(list.name -> list)

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels shouldBe labelCache
    }

    "evaluate both adding values to and removing values from a list" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:initialList]", Addition, "English", "result1"),
        CalcOperation("[label:result1]", Subtraction, "Geography", "result2")
      )

      val calculation: Calculation = createCalculation(calcOperations, Seq("83"))

      val initialList: ListLabel = ListLabel("initialList", List("French", "Geography", "History"))

      val labelMap: Map[String, Label] = Map(initialList.name -> initialList)

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result2") shouldBe Some(List("French", "History", "English"))
    }

    "evaluate adding two lists together" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:list1]", Addition, "[label:list2]", "result1"),
        CalcOperation("[label:list1]", Addition, "[label:emptyList1]", "result2"),
        CalcOperation("[label:emptyList1]", Addition, "[label:list2]", "result3"),
        CalcOperation("[label:emptyList1]", Addition, "[label:emptyList2]", "result4")
      )

      val calculation: Calculation = createCalculation(calcOperations, Seq("2"))

      val list1: ListLabel = ListLabel("list1", List("a", "b", "c"))
      val list2: ListLabel = ListLabel("list2", List("x", "y", "z"))
      val emptyList1: ListLabel = ListLabel("emptyList1")
      val emptyList2: ListLabel = ListLabel("emptyList2")

      val labelMap: Map[String, Label] = Map(
        list1.name -> list1,
        list2.name -> list2,
        emptyList1.name -> emptyList1,
        emptyList2.name -> emptyList2
      )

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result1") shouldBe Some(List("a", "b", "c", "x", "y", "z"))
      updatedLabels.valueAsList("result2") shouldBe Some(list1.english)
      updatedLabels.valueAsList("result3") shouldBe Some(list2.english)
      updatedLabels.valueAsList("result4") shouldBe Some(Nil)
    }

    "evaluate subtracting one list from another" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:list1]", Subtraction, "[label:list2]", "result1"),
        CalcOperation("[label:list3]", Subtraction, "[label:list4]", "result2"),
        CalcOperation("[label:list5]", Subtraction, "[label:list6]", "result3"),
        CalcOperation("[label:list7]", Subtraction, "[label:emptyList1]", "result4"),
        CalcOperation("[label:emptyList1]", Subtraction, "[label:list8]", "result5"),
        CalcOperation("[label:emptyList1]", Subtraction, "[label:emptyList2]", "result6")
      )

      val calculation: Calculation = createCalculation(calcOperations, Seq("14"))

      val list1: ListLabel = ListLabel("list1", List("a", "b", "c", "d"))
      val list2: ListLabel = ListLabel("list2", List("c", "d", "e", "f"))
      val list3: ListLabel = ListLabel("list3", List("a", "b", "c", "d", "e"))
      val list4: ListLabel = ListLabel("list4", List("b", "c", "d"))
      val list5: ListLabel = ListLabel("list5", List("a", "b", "b", "b", "c", "d"))
      val list6: ListLabel = ListLabel("list6", List("a", "b", "b"))
      val list7: ListLabel = ListLabel("list7", List("a", "b"))
      val list8: ListLabel = ListLabel("list8", List("c", "d"))
      val emptyList1: ListLabel = ListLabel("emptyList1")
      val emptyList2: ListLabel = ListLabel("emptyList2")

      val labelMap: Map[String, Label] = Map(
        list1.name -> list1,
        list2.name -> list2,
        list3.name -> list3,
        list4.name -> list4,
        list5.name -> list5,
        list6.name -> list6,
        list7.name -> list7,
        list8.name -> list8,
        emptyList1.name -> emptyList1,
        emptyList2.name -> emptyList2
      )

      val labelCache: Labels = LabelCache(labelMap)

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.valueAsList("result1") shouldBe Some(List("a", "b"))
      updatedLabels.valueAsList("result2") shouldBe Some(List("a", "e"))
      updatedLabels.valueAsList("result3") shouldBe Some(List("c", "d"))
      updatedLabels.valueAsList("result4") shouldBe Some(List("a", "b"))
      updatedLabels.valueAsList("result5") shouldBe Some(Nil)
      updatedLabels.valueAsList("result6") shouldBe Some(Nil)
    }

    "evaluate addition of the lengths of two lists" in {

      val list1: ListLabel = ListLabel("list1", List("one", "two", "three", "four", "five"))
      val list2: ListLabel = ListLabel("list2", List("six", "seven", "eight"))
      val labelMap: Map[String, Label] = Map(list1.name -> list1, list2.name -> list2)
      val labelCache: Labels = LabelCache(labelMap)

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[list:list1:length]", Addition, "[list:list2:length]", "result"))

      val calculation: Calculation = createCalculation(calcOperations, Seq("4"))

      val (_, updatedLabels, err) = calculation.eval(labelCache)

      updatedLabels.value("result") shouldBe Some("8")
    }

  }

  trait ListTest extends Test {
    val listA: ListLabel = ListLabel("listA", List("1"))
    val listB: ListLabel = ListLabel("listB", List("33"))
    val three: ScalarLabel = ScalarLabel("three", List("3"))

    val labelMap: Map[String, Label] = Map(
      listA.name -> listA,
      listB.name -> listB,
      three.name -> three
    )

  }
  "Multiply operation" must {
    "Not produce a result when applied to 2 lists" in new ListTest {
      val ops: Seq[CalcOperation] = Seq( CalcOperation("[label:listA]", Multiply, "[label:listB]", "result"))
      val calculation: Calculation = Calculation(CalculationStanza(ops, Seq("16"), stack = false))

      val (nextStanza, updatedLabels, _) = calculation.eval(LabelCache())

      nextStanza shouldBe "16"
      updatedLabels.value("result") shouldBe None
    }

    "Not produce a result when applied to a scalar and list" in new ListTest {
      val ops: Seq[CalcOperation] = Seq( CalcOperation("34", Multiply, "[label:listB]", "result"))
      val calculation: Calculation = Calculation(CalculationStanza(ops, Seq("16"), stack = false))

      val (nextStanza, updatedLabels, _) = calculation.eval(LabelCache())

      nextStanza shouldBe "16"
      updatedLabels.value("result") shouldBe None
    }

    "Not produce a result when applied to a list and scalar" in new ListTest {
      val ops: Seq[CalcOperation] = Seq( CalcOperation("[label:listA]", Multiply, "34", "result"))
      val calculation: Calculation = Calculation(CalculationStanza(ops, Seq("16"), stack = false))

      val (nextStanza, updatedLabels, _) = calculation.eval(LabelCache())

      nextStanza shouldBe "16"
      updatedLabels.value("result") shouldBe None
    }

  }

  "Divide operation" must {
    "Not produce a result when applied to 2 lists" in new ListTest {
      val ops: Seq[CalcOperation] = Seq( CalcOperation("[label:listA]", Divide, "[label:listB]", "result"))
      val calculation: Calculation = Calculation(CalculationStanza(ops, Seq("16"), stack = false))

      val (nextStanza, updatedLabels, _) = calculation.eval(LabelCache())

      nextStanza shouldBe "16"
      updatedLabels.value("result") shouldBe None
    }

    "Not produce a result when applied to a scalar and list" in new ListTest {
      val ops: Seq[CalcOperation] = Seq( CalcOperation("34", Divide, "[label:listB]", "result"))
      val calculation: Calculation = Calculation(CalculationStanza(ops, Seq("16"), stack = false))

      val (nextStanza, updatedLabels, _) = calculation.eval(LabelCache())

      nextStanza shouldBe "16"
      updatedLabels.value("result") shouldBe None
    }

    "Not produce a result when applied to a list and scalar" in new ListTest {
      val ops: Seq[CalcOperation] = Seq( CalcOperation("[label:listA]", Divide, "34", "result"))
      val calculation: Calculation = Calculation(CalculationStanza(ops, Seq("16"), stack = false))

      val (nextStanza, updatedLabels, _) = calculation.eval(LabelCache())

      nextStanza shouldBe "16"
      updatedLabels.value("result") shouldBe None
    }

    "Return a result of Infinity for division by zero" in new ListTest {
      val ops: Seq[CalcOperation] = Seq( CalcOperation("34", Divide, "0", "result"))
      val calculation: Calculation = Calculation(CalculationStanza(ops, Seq("16"), stack = false))

      val (nextStanza, updatedLabels, _) = calculation.eval(LabelCache())

      nextStanza shouldBe "16"
      updatedLabels.value("result") shouldBe Some("Infinity")
    }

  }

  private def createCalculation(calcs: Seq[CalcOperation], next: Seq[String]): Calculation = {

    val stanza: CalculationStanza = CalculationStanza(calcs, next, stack = false)

    Calculation(stanza)
  }

}
