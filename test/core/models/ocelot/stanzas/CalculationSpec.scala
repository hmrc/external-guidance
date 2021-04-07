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

class CalculationSpec extends BaseSpec {

  def opJson(l: String, r: String, lbl: String, t: String) =
    s"""{"left":"$l","right":"$r","label":"$lbl","type":"$t"}"""
  def opTypeJson(l: String, r: String, lbl: String) =
    s"""{"left":"$l","right":"$r","label":"$lbl"}"""

  def calcJson(next: Seq[String], calcs: Seq[String], typeAttr: String = "") =
    s"""{"next":[${next.map(n => s""""$n"""").mkString(",")}]${typeAttr},"calcs":[${calcs.mkString(",")}]}"""

  val ADD = "add"
  val SUB = "sub"
  val MULT = "mult"
  val DIV = "div"
  val CEIL = "ceil"
  val FLR = "flr"

  "AddOperation" should {
    "serialise to json" in {
      Json.toJson(AddOperation("[label:X]", "[label:Y]", "label")).toString shouldBe opTypeJson("[label:X]", "[label:Y]", "label")
    }

    "serialise to json from Operation ref" in {
      val add: Operation = AddOperation("[label:X]", "[label:Y]", "label")
      Json.toJson(add).toString shouldBe opJson("[label:X]", "[label:Y]", "label", ADD)
    }

    "Construct from valid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", ADD)).as[Operation] shouldBe AddOperation("[label:X]", "[label:Y]", "label")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", "ad")).validate[Operation] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "SubtractOperation" should {
    "serialise to json" in {
      Json.toJson(SubtractOperation("[label:X]", "[label:Y]", "label")).toString shouldBe opTypeJson("[label:X]", "[label:Y]", "label")
    }

    "serialise to json from Operation ref" in {
      val sub: Operation = SubtractOperation("[label:X]", "[label:Y]", "label")
      Json.toJson(sub).toString shouldBe opJson("[label:X]", "[label:Y]", "label", SUB)
    }

    "Construct from valid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", SUB)).as[Operation] shouldBe SubtractOperation("[label:X]", "[label:Y]", "label")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", "ad")).validate[Operation] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "MultiplyOperation" should {
    "serialise to json" in {
      Json.toJson(MultiplyOperation("[label:X]", "[label:Y]", "label")).toString shouldBe opTypeJson("[label:X]", "[label:Y]", "label")
    }

    "serialise to json from Operation ref" in {
      val mult: Operation = MultiplyOperation("[label:X]", "[label:Y]", "label")
      Json.toJson(mult).toString shouldBe opJson("[label:X]", "[label:Y]", "label", MULT)
    }

    "Construct from valid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", MULT)).as[Operation] shouldBe MultiplyOperation("[label:X]", "[label:Y]", "label")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", "mul")).validate[Operation] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "DivideOperation" should {
    "serialise to json" in {
      Json.toJson(DivideOperation("[label:X]", "[label:Y]", "label")).toString shouldBe opTypeJson("[label:X]", "[label:Y]", "label")
    }

    "serialise to json from Operation ref" in {
      val div: Operation = DivideOperation("[label:X]", "[label:Y]", "label")
      Json.toJson(div).toString shouldBe opJson("[label:X]", "[label:Y]", "label", DIV)
    }

    "Construct from valid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", DIV)).as[Operation] shouldBe DivideOperation("[label:X]", "[label:Y]", "label")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", "di")).validate[Operation] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "CeilingOperation" should {
    "serialise to json" in {
      Json.toJson(CeilingOperation("[label:X]", "[label:Y]", "label")).toString shouldBe opTypeJson("[label:X]", "[label:Y]", "label")
    }

    "serialise to json from Operation ref" in {
      val ceil: Operation = CeilingOperation("[label:X]", "[label:Y]", "label")
      Json.toJson(ceil).toString shouldBe opJson("[label:X]", "[label:Y]", "label", CEIL)
    }

    "Construct from valid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", CEIL)).as[Operation] shouldBe CeilingOperation("[label:X]", "[label:Y]", "label")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", "ad")).validate[Operation] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "FloorOperation" should {
    "serialise to json" in {
      Json.toJson(FloorOperation("[label:X]", "[label:Y]", "label")).toString shouldBe opTypeJson("[label:X]", "[label:Y]", "label")
    }

    "serialise to json from Operation ref" in {
      val flr: Operation = FloorOperation("[label:X]", "[label:Y]", "label")
      Json.toJson(flr).toString shouldBe opJson("[label:X]", "[label:Y]", "label", FLR)
    }

    "Construct from valid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", FLR)).as[Operation] shouldBe FloorOperation("[label:X]", "[label:Y]", "label")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(opJson("[label:X]", "[label:Y]", "label", "ad")).validate[Operation] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "Calculation" should {
    val calculation = Calculation(Seq("1"),Seq(AddOperation("[label:X]", "[label:Y]", "label"),
                                           SubtractOperation("[label:X]", "[label:Y]", "label"),
                                           MultiplyOperation("[label:X]", "[label:Y]", "label"),
                                           DivideOperation("[label:X]", "[label:Y]", "label"),
                                           CeilingOperation("[label:X]", "[label:Y]", "label"),
                                           FloorOperation("[label:X]", "[label:Y]", "label")))
    val json = calcJson(Seq("1"),Seq(opJson("[label:X]", "[label:Y]", "label", ADD),
                                     opJson("[label:X]", "[label:Y]", "label", SUB),
                                     opJson("[label:X]", "[label:Y]", "label", MULT),
                                     opJson("[label:X]", "[label:Y]", "label", DIV),
                                     opJson("[label:X]", "[label:Y]", "label", CEIL),
                                     opJson("[label:X]", "[label:Y]", "label", FLR)))
    val stanzaJson = calcJson(Seq("1"),Seq(opJson("[label:X]", "[label:Y]", "label", ADD),
                                     opJson("[label:X]", "[label:Y]", "label", SUB),
                                     opJson("[label:X]", "[label:Y]", "label", MULT),
                                     opJson("[label:X]", "[label:Y]", "label", DIV),
                                     opJson("[label:X]", "[label:Y]", "label", CEIL),
                                     opJson("[label:X]", "[label:Y]", "label", FLR)), ""","type":"Calculation"""")
    val invalidJson = calcJson(Seq("1"),Seq(opJson("[label:X]", "[label:Y]", "label", "ad"),
                                            opJson("[label:X]", "[label:Y]", "label", SUB),
                                            opJson("[label:X]", "[label:Y]", "label", MULT),
                                            opJson("[label:X]", "[label:Y]", "label", DIV),
                                            opJson("[label:X]", "[label:Y]", "label", CEIL),
                                            opJson("[label:X]", "[label:Y]", "label", FLR)))

    "serialise to json" in {
      Json.toJson(calculation).toString shouldBe json
    }

    "serialise to json from a Stanza ref" in {
      val stanza: Stanza = calculation
      Json.toJson(stanza).toString shouldBe stanzaJson
    }

    "Construct from valid Json" in {
      Json.parse(json).as[Calculation] shouldBe calculation
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(invalidJson).validate[Calculation] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }
}
