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

package core.models.ocelot.stanzas

import base.BaseSpec
import core.models.ocelot.{LabelCache, _}
import core.models.ocelot.errors.{GuidanceError, UnknownCalcOperationType}
import play.api.libs.json._

class CalculationStanzaSpec extends BaseSpec {

  def getMultipleCalcCalculationStanzaAsJsValue: JsValue = Json.parse(
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
        |           ]
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
    val c1CalcCeiling: CalcOperation = CalcOperation(c1Left, Ceiling, c1CeilingRight, c1Label)
    val c1CalcFloor: CalcOperation = CalcOperation(c1Left, Floor, c1FloorRight, c1Label)

    val expectedSingleAdditionCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcAdd), Seq("1"), stack = false)

    val expectedSingleSubtractionCalcCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcSub), Seq("1"), stack = false)

    val expectedSingleCeilingCalcCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcCeiling), Seq("1"), stack = false)

    val expectedSingleFloorCalcCalculationStanza: CalculationStanza =
      CalculationStanza(Seq(c1CalcFloor), Seq("1"), stack = false)

    val expectedMultipleCalcCalculationStanza: CalculationStanza =
      CalculationStanza(
        Seq(CalcOperation("[label:inputA]", Addition, "[label:inputB]", "outputA"),
          CalcOperation("[label:outputA]", Subtraction, "[label:inputC]", "outputB"),
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
          | "next": [ "21" ],
          | "stack": true,
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
          |           ]
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
        case JsError(errs) => GuidanceError.fromJsonValidationErrors(errs) match {
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
          val error = GuidanceError.fromJsonValidationErrors(errs)
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
        CalcOperation(twentyAsString, Subtraction, labelY, result2)
      )

      val calculationStanza: CalculationStanza = CalculationStanza(operations, next, stack = false)

      val expectedOperations: Seq[Operation] = Seq(
        AddOperation(labelX, tenAsString, result1),
        SubtractOperation(twentyAsString, labelY, result2)
      )

      val expectedCalculation: Calculation = Calculation(next, expectedOperations)

      Calculation(calculationStanza) shouldBe expectedCalculation
    }

    "provide a list of the labels defined within a calculation" in new Test {

      val calculation: Calculation = Calculation(exampleCalcStanza)

      // Define expected labels
      val output1: Label = Label("output1")
      val output2: Label = Label("output2")
      val output3: Label = Label("output3")
      val output4: Label = Label("output4")

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

      nextStanza shouldBe "40"

      updatedLabels.value("result") shouldBe Some("35")
    }

    "evaluate a simple subtraction using constants" in {

      val calcOperations: Seq[CalcOperation] = Seq( CalcOperation("10", Subtraction, "25", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val labelCache = LabelCache()

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("result") shouldBe Some("-15")
    }

    "evaluate a simple addition using label values from the label cache" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:input1]", Addition, "[label:input2]", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = Label( "input1", Some("10.00"))
      val input2: Label = Label( "input2", Some("25.00"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("result") shouldBe Some("35.00")
    }

    "evaluate a simple subtraction using label values from the label cache" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:input1]", Subtraction, "[label:input2]", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = Label( "input1", Some("64.00"))
      val input2: Label = Label( "input2", Some("32.00"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val input1: Label = Label( "input1", Some("10.00"))
      val input2: Label = Label( "input2", Some("22.00"))
      val input3: Label = Label( "input3", Some("3.00"))
      val input4: Label = Label( "input4", Some("4.00"))
      val input5: Label = Label( "input5", Some("10.00"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2,
        input3.name -> input3,
        input4.name -> input4,
        input5.name -> input5
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val input1: Label = Label( "input1", Some("10."))
      val input2: Label = Label( "input2", Some("25.0"))

      val input3: Label = Label( "input3", Some("10.0"))
      val input4: Label = Label( "input4", Some("25.00"))

      val input5: Label = Label( "input5", Some("25.0"))
      val input6: Label = Label( "input6", Some("10"))

      val input7: Label = Label( "input7", Some("25.00"))
      val input8: Label = Label( "input8", Some("10"))

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val input1: Label = Label( "input1", Some("Hello"))
      val input2: Label = Label( "input2", Some(" "))
      val input3: Label = Label( "input3", Some("World"))
      val input4: Label = Label( "input4", Some("!"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2,
        input3.name -> input3,
        input4.name -> input4
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

      nextStanza shouldBe "16"

      updatedLabels.value("output3") shouldBe Some( "Hello World!")

    }

    "not support subtraction operations on non-currency input" in {

      val calcOperations: Seq[CalcOperation] = Seq(CalcOperation("[label:input1]", Subtraction, "[label:input2]", "result"))

      val next: Seq[String] = Seq("16")

      val stanza: CalculationStanza = CalculationStanza(calcOperations, next, stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = Label( "input1", Some("Today"))
      val input2: Label = Label( "input2", Some("Yesterday"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1,
        input2.name -> input2
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val date1: Label = Label("date1", Some("6/3/2019"))
      val date2: Label = Label("date2", Some("2/2/2019"))
      val date3: Label = Label("date3", Some("06/03/2020"))
      val date4: Label = Label("date4", Some("02/02/2020"))
      val date5: Label = Label("date5", Some("01/01/2016"))
      val date6: Label = Label("date6", Some("01/01/2015"))
      val date7: Label = Label("date7", Some("10/5/2015"))
      val date8: Label = Label("date8", Some("8/9/2007"))

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val date1: Label = Label("date1", Some("07/10/2019"))
      val date2: Label = Label("date2", Some("20/04/2021"))

      val labelMap: Map[String, Label] = Map(
        date1.name -> date1,
        date2.name -> date2
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val input1: Label = Label("input1", Some("10.5"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

      nextStanza shouldBe "5"

      updatedLabels.value("output1") shouldBe Some("11")
    }

    "support floor operations where the left operand is defined by a label" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("[label:input1]", Floor, "0", "output1" )
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("5"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val input1: Label = Label("input1", Some("1021.25"))

      val labelMap: Map[String, Label] = Map(
        input1.name -> input1
      )

      val labelCache = LabelCache(labelMap)

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

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

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

      nextStanza shouldBe "25"

      updatedLabels shouldBe labelCache
    }

    "not support floor operations on operands of incorrect type" in {

      val calcOperations: Seq[CalcOperation] = Seq(
        CalcOperation("input", Floor, "0", "output")
      )

      val stanza: CalculationStanza = CalculationStanza(calcOperations, Seq("25"), stack = false)

      val calculation: Calculation = Calculation(stanza)

      val label: Label = Label("label", Some("data"))

      val labelMap: Map[String, Label] = Map(label.name -> label)

      val labelCache: Labels = LabelCache(labelMap)

      val (nextStanza, updatedLabels) = calculation.eval(labelCache)

      nextStanza shouldBe "25"

      updatedLabels shouldBe labelCache
    }

  }

}
