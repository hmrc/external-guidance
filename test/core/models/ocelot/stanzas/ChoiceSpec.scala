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
import play.api.libs.json._

class ChoiceSpec extends BaseSpec {

  def testJson(l: String, r: String, t: String) =
    s"""{"type":"$t","left":"$l","right":"$r"}"""
  def testTypeJson(l: String, r: String) =
    s"""{"left":"$l","right":"$r"}"""
  def choiceJson(next: Seq[String], tests: Seq[String], typeAttr: Option[String] = None) =
    s"""{${typeAttr.getOrElse("")}"next":[${next.map(n => s""""$n"""").mkString(",")}],"tests":[${tests.mkString(",")}]}"""

  val EQ = "eq"
  val NEQ = "neq"
  val MT = "mt"
  val MTE = "mte"
  val LT = "lt"
  val LTE = "lte"
  val CNTNS = "cntns"

  "EqualsTest" should {
    "serialise to json" in {
      Json.toJson(EqualsTest("[label:X]", "[label:Y]")).toString shouldBe testTypeJson("[label:X]", "[label:Y]")
    }

    "serialise to json from ChoiceTest ref" in {
      val eq: ChoiceTest = EqualsTest("[label:X]", "[label:Y]")
      Json.toJson(eq).toString shouldBe testJson("[label:X]", "[label:Y]", EQ)
    }

    "Construct from valid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", EQ)).as[ChoiceTest] shouldBe EqualsTest("[label:X]", "[label:Y]")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", "e")).validate[ChoiceTest] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "NotEqualsTest" should {
    "serialise to json" in {
      Json.toJson(NotEqualsTest("[label:X]", "[label:Y]")).toString shouldBe testTypeJson("[label:X]", "[label:Y]")
    }

    "serialise to json from ChoiceTest ref" in {
      val neq: ChoiceTest = NotEqualsTest("[label:X]", "[label:Y]")
      Json.toJson(neq).toString shouldBe testJson("[label:X]", "[label:Y]", NEQ)
    }

    "Construct from valid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", NEQ)).as[ChoiceTest] shouldBe NotEqualsTest("[label:X]", "[label:Y]")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", "e")).validate[ChoiceTest] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "MoreThanTest" should {
    "serialise to json" in {
      Json.toJson(MoreThanTest("[label:X]", "[label:Y]")).toString shouldBe testTypeJson("[label:X]", "[label:Y]")
    }

    "serialise to json from ChoiceTest ref" in {
      val mt: ChoiceTest = MoreThanTest("[label:X]", "[label:Y]")
      Json.toJson(mt).toString shouldBe testJson("[label:X]", "[label:Y]", MT)
    }

    "Construct from valid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", MT)).as[ChoiceTest] shouldBe MoreThanTest("[label:X]", "[label:Y]")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", "e")).validate[ChoiceTest] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "MoreThanOrEqualsTest" should {
    "serialise to json" in {
      Json.toJson(MoreThanOrEqualsTest("[label:X]", "[label:Y]")).toString shouldBe testTypeJson("[label:X]", "[label:Y]")
    }

    "serialise to json from ChoiceTest ref" in {
      val mte: ChoiceTest = MoreThanOrEqualsTest("[label:X]", "[label:Y]")
      Json.toJson(mte).toString shouldBe testJson("[label:X]", "[label:Y]", MTE)
    }

    "Construct from valid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", MTE)).as[ChoiceTest] shouldBe MoreThanOrEqualsTest("[label:X]", "[label:Y]")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", "e")).validate[ChoiceTest] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "LessThanTest" should {
    "serialise to json" in {
      Json.toJson(LessThanTest("[label:X]", "[label:Y]")).toString shouldBe testTypeJson("[label:X]", "[label:Y]")
    }

    "serialise to json from ChoiceTest ref" in {
      val lt: ChoiceTest = LessThanTest("[label:X]", "[label:Y]")
      Json.toJson(lt).toString shouldBe testJson("[label:X]", "[label:Y]", LT)
    }

    "Construct from valid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", LT)).as[ChoiceTest] shouldBe LessThanTest("[label:X]", "[label:Y]")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", "e")).validate[ChoiceTest] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "LessThanOrEqualsTest" should {
    "serialise to json" in {
      Json.toJson(LessThanOrEqualsTest("[label:X]", "[label:Y]")).toString shouldBe testTypeJson("[label:X]", "[label:Y]")
    }

    "serialise to json from ChoiceTest ref" in {
      val lte: ChoiceTest = LessThanOrEqualsTest("[label:X]", "[label:Y]")
      Json.toJson(lte).toString shouldBe testJson("[label:X]", "[label:Y]", LTE)
    }

    "Construct from valid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", LTE)).as[ChoiceTest] shouldBe LessThanOrEqualsTest("[label:X]", "[label:Y]")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", "e")).validate[ChoiceTest] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "ContainsTest" should {
    "serialise to json" in {
      Json.toJson(ContainsTest("[label:X]", "[label:Y]")).toString shouldBe testTypeJson("[label:X]", "[label:Y]")
    }

    "serialise to json from ChoiceTest ref" in {
      val cntns: ChoiceTest = ContainsTest("[label:X]", "[label:Y]")
      Json.toJson(cntns).toString shouldBe testJson("[label:X]", "[label:Y]", CNTNS)
    }

    "Construct from valid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", CNTNS)).as[ChoiceTest] shouldBe ContainsTest("[label:X]", "[label:Y]")
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(testJson("[label:X]", "[label:Y]", "e")).validate[ChoiceTest] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

  "Choice" should {
    val choice = Choice(Seq("1"),Seq(EqualsTest("[label:X]", "[label:Y]"),
                                     NotEqualsTest("[label:X]", "[label:Y]"),
                                     MoreThanTest("[label:X]", "[label:Y]"),
                                     MoreThanOrEqualsTest("[label:X]", "[label:Y]"),
                                     LessThanTest("[label:X]", "[label:Y]"),
                                     LessThanOrEqualsTest("[label:X]", "[label:Y]"),
                                     ContainsTest("[label:X]", "[label:Y]")))
    val json = choiceJson(Seq("1"),Seq(testJson("[label:X]", "[label:Y]", EQ),
                                       testJson("[label:X]", "[label:Y]", NEQ),
                                       testJson("[label:X]", "[label:Y]", MT),
                                       testJson("[label:X]", "[label:Y]", MTE),
                                       testJson("[label:X]", "[label:Y]", LT),
                                       testJson("[label:X]", "[label:Y]", LTE),
                                       testJson("[label:X]", "[label:Y]", CNTNS)))
    val stanzaJson = choiceJson(Seq("1"),Seq(testJson("[label:X]", "[label:Y]", EQ),
                                       testJson("[label:X]", "[label:Y]", NEQ),
                                       testJson("[label:X]", "[label:Y]", MT),
                                       testJson("[label:X]", "[label:Y]", MTE),
                                       testJson("[label:X]", "[label:Y]", LT),
                                       testJson("[label:X]", "[label:Y]", LTE),
                                       testJson("[label:X]", "[label:Y]", CNTNS)), Some(""""type":"Choice","""))
    val invalidJson = choiceJson(Seq("1"),Seq(testJson("[label:X]", "[label:Y]", "ad"),
                                              testJson("[label:X]", "[label:Y]", MT),
                                              testJson("[label:X]", "[label:Y]", MTE),
                                              testJson("[label:X]", "[label:Y]", MTE),
                                              testJson("[label:X]", "[label:Y]", LTE),
                                              testJson("[label:X]", "[label:Y]", CNTNS)))

    "serialise to json" in {
      Json.toJson(choice).toString shouldBe json
    }

    "serialise to json from a Stanza ref" in {
      val stanza: Stanza = choice
      Json.toJson(stanza).toString shouldBe stanzaJson
    }

    "Construct from valid Json" in {
      Json.parse(json).as[Choice] shouldBe choice
    }

    "Fail to contruct from invalid Json" in {
      Json.parse(invalidJson).validate[Choice] match {
        case _: JsError => succeed
        case _ => fail
      }
    }
  }

}
