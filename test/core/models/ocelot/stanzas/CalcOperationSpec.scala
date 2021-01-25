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
import play.api.libs.json._

class CalcOperationSpec extends BaseSpec {

  val rightOperand: String = "[label:inputB]"
  val ceiling: String = "ceiling"
  val floor: String = "floor"
  val sqrt: String = "sqrt"

  def getCalcOperationAsJsValue(calcOperationType: String, rightOperand: String): JsValue = Json.parse(
    s"""|{
        | "left": "[label:inputA]",
        | "op": "$calcOperationType",
        | "right": "$rightOperand",
        | "label": "result"
        |}""".stripMargin
  )

  def getCalcOperationWithMissingPropertiesAsJsValue(): JsValue = Json.parse(
    s"""|{
        | "left": "[label:inputA]",
        | "label": "result"
        |}""".stripMargin
  )

  val validCalcOperationAsJsObject: JsObject = getCalcOperationAsJsValue("add", rightOperand).as[JsObject]

  "Reading a valid JSON representation of calculation operation" should {

    "deserialize valid addition operation" in {
      val additionOperation: JsValue = getCalcOperationAsJsValue("add", rightOperand)

      additionOperation.validate[CalcOperation] match {
        case JsSuccess(calcOperation, _) => calcOperation shouldBe CalcOperation( "[label:inputA]", Addition, "[label:inputB]", "result")
        case e: JsError => fail("Unable to parse valid addition operation")
      }
    }

    "deserialize a valid JSON representation of subtraction operation" in {
      val subtractionOperation: JsValue = getCalcOperationAsJsValue("subtract", rightOperand)

      subtractionOperation.validate[CalcOperation] match {
        case JsSuccess(calcOperation, _) => calcOperation shouldBe CalcOperation( "[label:inputA]", Subtraction, "[label:inputB]", "result")
        case e: JsError => fail("Unable to parse valid subtraction operation")
      }
    }

    "deserialize a valid JSON representation of ceiling operation" in {
      val ceilingOperation: JsValue = getCalcOperationAsJsValue("ceiling", "0")

      ceilingOperation.validate[CalcOperation] match {
        case JsSuccess(calcOperation, _) => calcOperation shouldBe CalcOperation( "[label:inputA]", Ceiling, "0", "result")
        case e: JsError => fail("Unable to parse valid subtraction operation")
      }
    }

    "deserialize a valid JSON representation of floor operation" in {
      val floorOperation: JsValue = getCalcOperationAsJsValue("floor", "-1")

      floorOperation.validate[CalcOperation] match {
        case JsSuccess(calcOperation, _) => calcOperation shouldBe CalcOperation( "[label:inputA]", Floor, "-1", "result")
        case e: JsError => fail("Unable to parse valid subtraction operation")
      }
    }

  }

  "Reading invalid JSON representation of calculation operation" should {

    "raise an error on deserialization of JsValue with invalid operation type" in {

      val invalidOperation: JsValue = getCalcOperationAsJsValue(sqrt, rightOperand)

        invalidOperation.validate[CalcOperation] match {
          case e: JsError => succeed
          case _ => fail("An instance of CalcOperation should not be created when the operation type is invalid")
      }

    }

    "raise an error on deserialization of JsValue describing ceiling operation with invalid scale factor" in {

      val invalidOperation: JsValue = getCalcOperationAsJsValue(ceiling, "scale")

      val result: JsResult[CalcOperation] = invalidOperation.validate[CalcOperation]

      result match {
        case e: JsError => succeed
        case _ => fail("An instance of CalcOperation should not be created for a ceiling operation with an invalid scale factor")
      }

    }

    "raise an error on deserialization of JsValue describing floor operation with invalid scale factor" in {

      val invalidOperation: JsValue = getCalcOperationAsJsValue(floor,"")

      val result: JsResult[CalcOperation] = invalidOperation.validate[CalcOperation]

      result match {
        case e: JsError => succeed
        case _ => fail("An instance of CalcOperation should not be created for a floor operation with an invalid scale factor")
      }

    }

    "raise an error on deserialization of JsValue with two missing properties" in {

      val invalidOperation: JsValue = getCalcOperationWithMissingPropertiesAsJsValue()

      val result: JsResult[CalcOperation] = invalidOperation.validate[CalcOperation]

      result match {
        case e: JsError => succeed
        case _ => fail("An instance of CalcOperation should not be created when an operand and the operation are missing")
      }

    }

    /** Test for missing properties in Json object representing instruction stanzas */
    missingJsObjectAttrTests[CalcOperation](validCalcOperationAsJsObject)

    /** Test for properties of the wrong type in json object representing instruction stanzas */
    incorrectPropertyTypeJsObjectAttrTests[CalloutStanza](validCalcOperationAsJsObject)
  }

  "Writing instances of calculation operations to JSON" should {

    "serialize addition operations" in {

      Json.toJson(CalcOperation("[label:inputA]", Addition, "[label:inputB]", "result")).toString shouldBe
      """{"left":"[label:inputA]","op":"add","right":"[label:inputB]","label":"result"}"""
    }

    "serialize subtraction operation" in {

      Json.toJson(CalcOperation("[label:inputA]", Subtraction, "[label:inputB]", "result")).toString shouldBe
      """{"left":"[label:inputA]","op":"subtract","right":"[label:inputB]","label":"result"}"""
    }

    "serialize ceiling operation" in {

      Json.toJson(CalcOperation("[label:inputA]", Ceiling, "[label:inputB]", "result")).toString shouldBe
        """{"left":"[label:inputA]","op":"ceiling","right":"[label:inputB]","label":"result"}"""
    }

    "serialize floor operation" in {

      Json.toJson(CalcOperation("[label:inputA]", Floor, "[label:inputB]", "result")).toString shouldBe
        """{"left":"[label:inputA]","op":"floor","right":"[label:inputB]","label":"result"}"""
    }

  }


}
