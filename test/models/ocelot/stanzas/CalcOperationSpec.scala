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

class CalcOperationSpec extends BaseSpec {

  val sqrt = "sqrt"

  def getCalcOperationAsJsValue(calcOperationType: String): JsValue = Json.parse(
    s"""|{
        | "left": "[label:inputA]",
        | "op": "$calcOperationType",
        | "right": "[label:inputB]",
        | "label": "result"
        |}""".stripMargin
  )

  val validCalcOperationAsJsObject: JsObject = getCalcOperationAsJsValue("add").as[JsObject]

  "Reading a valid JSON representation of calculation operation" should {

    "deserialize valid addition operation" in {
      val additionOperation: JsValue = getCalcOperationAsJsValue("add")

      additionOperation.validate[CalcOperation] match {
        case JsSuccess(calcOperation, _) => calcOperation shouldBe CalcOperation( "[label:inputA]", Addition, "[label:inputB]", "result")
        case e: JsError => fail("Unable to parse valid addition operation")
      }
    }

    "deserialize JSON representation of subtraction operation" in {
      val subtractionOperation: JsValue = getCalcOperationAsJsValue("subtract")

      subtractionOperation.validate[CalcOperation] match {
        case JsSuccess(calcOperation, _) => calcOperation shouldBe CalcOperation( "[label:inputA]", Subtraction, "[label:inputB]", "result")
        case e: JsError => fail("Unable to parse valid subtraction operation")
      }
    }
  }

  "Reading invalid JSON representation of calculation operation" should {

    "Raise an error on deserialization of JsValue with invalid operation type" in {

      val invalidOperation: JsValue = getCalcOperationAsJsValue(sqrt)

        invalidOperation.validate[CalcOperation] match {
          case e: JsError => succeed
          case _ => fail("An instance of CalcOperation should not be created when the operation type is invalid")
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
  }


}
