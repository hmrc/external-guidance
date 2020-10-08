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
import models.ocelot._
import models.ocelot.errors.{GuidanceError, UnknownTestType}

class ChoiceStanzaSpec extends BaseSpec {

  val stanzaType = "ChoiceStanza"
  val valueType = "scalar"
  val pageNameLabel = "PageName"
  val pageName = "Telling HMRC about extra income"
  val pageUrlLabel = "PageUrl"
  val pageUrl = "/rent/less-than-1000/do-you-want-to-use-the-rent-a-room-scheme"
  val next = Seq("40", "41", "50")
  val stack = "false"

  val onePageJsonWithInvalidTestType: JsValue = Json.parse(
    """
      |{
      |  "meta": {
      |    "title": "Customer wants to make a cup of tea",
      |    "id": "oct90001",
      |    "ocelot": 1,
      |    "lastAuthor": "000000",
      |    "lastUpdate": 1500298931016,
      |    "version": 4,
      |    "filename": "oct90001.js",
      |    "titlePhrase": 8
      |  },
      |  "howto": [],
      |  "contacts": [],
      |  "links": [],
      |  "flow": {
      |    "start": {
      |      "type": "PageStanza",
      |      "url": "/feeling-bad",
      |      "next": ["3"],
      |      "stack": true
      |    },
      |    "3": {
      |      "type": "ChoiceStanza",
      |      "tests": [
      |        {
      |          "left": "VAL-1",
      |          "test": "UnknownType",
      |          "right": "VAL-2"
      |        }],
      |      "next": [
      |        "2", "3"
      |      ],
      |      "stack": true
      |    },
      |    "2": {
      |      "type": "InstructionStanza",
      |      "text": 0,
      |      "next": [
      |        "end"
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
  )


  val validChoiceStanzaJson: JsObject = Json
    .parse(
      s"""{
      |  "type": "${stanzaType}",
      |  "tests": [
      |    {
      |      "left": "VAL-1",
      |      "test": "lessThanOrEquals",
      |      "right": "VAL-2"
      |    },
      |    {
      |      "left": "VAL-3",
      |      "test": "lessThanOrEquals",
      |      "right": "VAL-4"
      |    }
      |  ],
      |  "next": [${next.map(x => s""""$x"""").mkString(",")}],
      |  "stack": ${stack}
      |}
    """.stripMargin
    )
    .as[JsObject]

  val invalidChoiceStanzaJson: JsObject = Json
    .parse(
      s"""{
      |  "type": "${stanzaType}",
      |  "tests": [
      |    {
      |      "left": "VAL-1",
      |      "test": "UnknownTest",
      |      "right": "VAL-2"
      |    },
      |    {
      |      "left": "VAL-3",
      |      "test": "lessThanOrEquals",
      |      "right": "VAL-4"
      |    }
      |  ],
      |  "next": [${next.map(x => s""""$x"""").mkString(",") }],
      |  "stack": ${stack}
      |}
    """.stripMargin
    )
    .as[JsObject]

  "ChoiceStanza" must {

    "deserialise from json" in {

      val stanza: ChoiceStanza = validChoiceStanzaJson.as[ChoiceStanza]

      stanza.stack shouldBe false
      stanza.next.length shouldBe 3
      stanza.next shouldBe next
      stanza.tests.length shouldBe 2
      stanza.tests(0) shouldBe ChoiceStanzaTest("VAL-1", LessThanOrEquals, "VAL-2")
      stanza.tests(1) shouldBe ChoiceStanzaTest("VAL-3", LessThanOrEquals, "VAL-4")
    }

    "serialise to json" in {
      val stanza: ChoiceStanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("VAL-1", LessThanOrEquals, "VAL-2"), ChoiceStanzaTest("VAL-3", LessThanOrEquals, "VAL-4")), false)
      val expectedJson: String = s"""{"next":[${next.map(x => s""""$x"""").mkString(",")}],"tests":[{"left":"VAL-1","test":"lessThanOrEquals","right":"VAL-2"},{"left":"VAL-3","test":"lessThanOrEquals","right":"VAL-4"}],"stack":false}"""
      val json: String = Json.toJson(stanza).toString
      json shouldBe expectedJson
    }

    "serialise to json from a Stanza reference" in {
      val stanza: Stanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("VAL-1", LessThanOrEquals, "VAL-2"), ChoiceStanzaTest("VAL-3", LessThanOrEquals, "VAL-4")), false)
      val expectedJson: String = s"""{"next":[${next.map(x => s""""$x"""").mkString(",")}],"stack":false,"tests":[{"left":"VAL-1","test":"lessThanOrEquals","right":"VAL-2"},{"left":"VAL-3","test":"lessThanOrEquals","right":"VAL-4"}],"type":"ChoiceStanza"}"""
      val json: String = Json.toJson(stanza).toString
      json shouldBe expectedJson
    }

    "fail to parse if an unkown value type is found" in {
      invalidChoiceStanzaJson.as[JsObject].validate[ChoiceStanza] match {
        case JsSuccess(_, _) => fail(s"Value objects must be of valid type")
        case JsError(_) => succeed
      }
    }

    "contain at least one Value object" in {
      validChoiceStanzaJson.as[ChoiceStanza].tests.length should be > 0
    }

    missingJsObjectAttrTests[ChoiceStanza](validChoiceStanzaJson, List("type"))

    incorrectPropertyTypeJsObjectAttrTests[ChoiceStanza](validChoiceStanzaJson, List("type"))

  }

  "Choice" must {

    "be creatable from a ChoiceStanza " in {
      val stanza: ChoiceStanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("4", LessThanOrEquals, "3"), ChoiceStanzaTest("3", LessThanOrEquals, "4")), false)
      val choice = Choice(stanza)
      choice.next shouldBe stanza.next
      choice.tests.zipWithIndex.foreach{
        case (LessThanOrEqualsTest(_,_), index) if stanza.tests(index).test == LessThanOrEquals => succeed
        case x => fail

      }
    }

    "Evaluate to correct result when one of the tests succeed" in {
      val next = Seq("40", "41", "50")
      val stanza: ChoiceStanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("4", LessThanOrEquals, "3"),
                                                        ChoiceStanzaTest("3", LessThanOrEquals, "4")), false)
      val choice = Choice(stanza)
      val lc = LabelCache()
      val expectedResult = ("41", lc)
      choice.eval(lc) shouldBe expectedResult
    }

    "Evaluate to correct result when no tests succeed" in {
      val next = Seq("40", "41", "50")
      val stanza: ChoiceStanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("4", LessThanOrEquals, "3"),
                                                        ChoiceStanzaTest("3", MoreThan, "4")), false)
      val choice = Choice(stanza)
      val lc = LabelCache()
      val expectedResult = ("50", lc)
      choice.eval(lc) shouldBe expectedResult
    }

    "Evaluate to correct result when one of the tests succeed referencing labels" in {
      val next = Seq("40", "41", "50", "51")
      val stanza: ChoiceStanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("[label:X]", LessThanOrEquals, "[label:Y]"),
                                                        ChoiceStanzaTest("3", LessThanOrEquals, "4"),
                                                        ChoiceStanzaTest("3", NotEquals, "4")), false)
      val choice = Choice(stanza)
      val labels = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y", Some("44")))
      val lc = LabelCache(labels)
      val expectedResult = ("40", lc)
      choice.eval(lc) shouldBe expectedResult
    }

    "Evaluate to correct result when no tests succeed referencing labels" in {
      val next = Seq("40", "41", "50", "51")
      val stanza: ChoiceStanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("[label:X]", LessThanOrEquals, "[label:Y]"),
                                                        ChoiceStanzaTest("3", Equals, "4"),
                                                        ChoiceStanzaTest("1", MoreThanOrEquals, "4")),
                                                        false)
      val choice = Choice(stanza)
      val labels = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y", Some("4")))
      val lc = LabelCache(labels)
      val expectedResult = ("51", lc)
      choice.eval(lc) shouldBe expectedResult
    }

  }

  "ChoiceTest" must {
    "provide support to EqualsTest" in {
      EqualsTest("5", "5").eval(LabelCache()) shouldBe true

      EqualsTest("4", "5").eval(LabelCache()) shouldBe false

      EqualsTest("hello", "hello").eval(LabelCache()) shouldBe true

      EqualsTest("4", "hello").eval(LabelCache()) shouldBe false
    }

    "provide support to NotEqualsTest" in {
      NotEqualsTest("5", "5").eval(LabelCache()) shouldBe false

      NotEqualsTest("4", "5").eval(LabelCache()) shouldBe true

      NotEqualsTest("hello", "hello").eval(LabelCache()) shouldBe false

      NotEqualsTest("4", "hello").eval(LabelCache()) shouldBe true
    }

    "provide support to MoreThanTest" in {
      MoreThanTest("5", "5").eval(LabelCache()) shouldBe false

      MoreThanTest("4", "5").eval(LabelCache()) shouldBe false

      MoreThanTest("4", "3").eval(LabelCache()) shouldBe true

      MoreThanTest("hello", "hello").eval(LabelCache()) shouldBe false

      MoreThanTest("4", "hello").eval(LabelCache()) shouldBe false
    }

    "provide support to MoreThanOrEqualsTest" in {
      MoreThanOrEqualsTest("5", "5").eval(LabelCache()) shouldBe true

      MoreThanOrEqualsTest("4", "5").eval(LabelCache()) shouldBe false

      MoreThanOrEqualsTest("4", "3").eval(LabelCache()) shouldBe true

      MoreThanOrEqualsTest("hello", "hello").eval(LabelCache()) shouldBe true

      MoreThanOrEqualsTest("4", "hello").eval(LabelCache()) shouldBe false
    }

    "provide support to LessThanOrEqualsTest" in {
      LessThanOrEqualsTest("5", "5").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("4", "5").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("4", "3").eval(LabelCache()) shouldBe false

      LessThanOrEqualsTest("hello", "hello").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("4", "hello").eval(LabelCache()) shouldBe true
    }
  }

  "ChoiceStanzaTest" must {

    val lte = """{"left": "VAL-1","test": "lessThanOrEquals","right": "VAL-2"}"""
    val e = """{"left": "VAL-3","test": "equals","right": "VAL-4"}"""
    val ne = """{"left": "VAL-3","test": "notEquals","right": "VAL-4"}"""
    val m = """{"left": "VAL-3","test": "moreThan","right": "VAL-4"}"""
    val me = """{"left": "VAL-3","test": "moreThanOrEquals","right": "VAL-4"}"""
    def choiceStanzaJson(t1: String, t2: String) = s"""{"type": "ChoiceStanza","tests": [${t1},${t2}],"next": ["1", "2", "3"],"stack": true}"""

    "DeSerialise EqualsTest" in {
      Json.parse(choiceStanzaJson(e,e)).validate[ChoiceStanza].fold(err => fail, cs => {
        val choice = Choice(cs)
        choice.tests(0) shouldBe EqualsTest("VAL-3", "VAL-4")
        choice.tests(1) shouldBe EqualsTest("VAL-3", "VAL-4")
      }
      )
    }

    "DeSerialise NotEqualsTest" in {
      Json.parse(choiceStanzaJson(ne,ne)).validate[ChoiceStanza].fold(err => fail, cs => {
        val choice = Choice(cs)
        choice.tests(0) shouldBe NotEqualsTest("VAL-3", "VAL-4")
        choice.tests(1) shouldBe NotEqualsTest("VAL-3", "VAL-4")
      }
      )
    }
    "DeSerialise LessThanOrEqualsTest" in {
      Json.parse(choiceStanzaJson(lte,lte)).validate[ChoiceStanza].fold(err => fail, cs => {
        val choice = Choice(cs)
        choice.tests(0) shouldBe LessThanOrEqualsTest("VAL-1", "VAL-2")
        choice.tests(1) shouldBe LessThanOrEqualsTest("VAL-1", "VAL-2")
      }
      )
    }
    "DeSerialise MoreThanTest" in {
      Json.parse(choiceStanzaJson(m,m)).validate[ChoiceStanza].fold(err => fail, cs => {
        val choice = Choice(cs)
        choice.tests(0) shouldBe MoreThanTest("VAL-3", "VAL-4")
        choice.tests(1) shouldBe MoreThanTest("VAL-3", "VAL-4")
      }
      )
    }
    "DeSerialise MoreThanOrEqualsTest" in {
      Json.parse(choiceStanzaJson(me,me)).validate[ChoiceStanza].fold(err => fail, cs => {
        val choice = Choice(cs)
        choice.tests(0) shouldBe MoreThanOrEqualsTest("VAL-3", "VAL-4")
        choice.tests(1) shouldBe MoreThanOrEqualsTest("VAL-3", "VAL-4")
      }
      )
    }

    "Serialise EqualsTest" in {
      Json.toJson(ChoiceStanzaTest("3", Equals, "4")).toString shouldBe """{"left":"3","test":"equals","right":"4"}"""
    }
    "Serialise NotEqualsTest" in {
      Json.toJson(ChoiceStanzaTest("3", NotEquals, "4")).toString shouldBe """{"left":"3","test":"notEquals","right":"4"}"""
    }
    "Serialise LessThanOrEqualsTest" in {
      Json.toJson(ChoiceStanzaTest("3", LessThanOrEquals, "4")).toString shouldBe """{"left":"3","test":"lessThanOrEquals","right":"4"}"""
    }
    "Serialise MoreThanTest" in {
      Json.toJson(ChoiceStanzaTest("3", MoreThan, "4")).toString shouldBe """{"left":"3","test":"moreThan","right":"4"}"""
    }
    "Serialise MoreThanOrEqualsTest" in {
      Json.toJson(ChoiceStanzaTest("3", MoreThanOrEquals, "4")).toString shouldBe """{"left":"3","test":"moreThanOrEquals","right":"4"}"""
    }

    "Detect unknown test type strings at json parse level" in {
      val invalidChoiceStanzaJson: JsObject = Json.parse(s"""{"left": "VAL-1","test": "UnknownType","right": "VAL-2"}""").as[JsObject]
      invalidChoiceStanzaJson.validate[ChoiceStanzaTest] match {
        case JsError(errTuple :: _) => errTuple match {
          case (_, err +: _) if err.messages(0) == "TestType" && err.args.contains("UnknownType") => succeed
          case _ => fail
        }
        case JsError(_) => fail
        case JsSuccess(_, _) => fail
      }
    }

    "Detect all unknown test types at json parse level" in {
      val invalidChoiceStanzaJson: JsObject = Json.parse(s"""{"left": "VAL-1","test": 44,"right": "VAL-2"}""").as[JsObject]
      invalidChoiceStanzaJson.validate[ChoiceStanzaTest] match {
        case JsError(errTuple :: _) => errTuple match {
          case (_, err +: _) if err.messages(0) == "TestType" && err.args.contains("44") => succeed
          case _ => fail
        }
        case JsError(_) => fail
        case JsSuccess(_, _) => fail
      }
    }
  }

  "Page buiding" must {
    "be able to detect UnknownTestType error" in {
      onePageJsonWithInvalidTestType.as[JsObject].validate[Process] match {
        case JsSuccess(_, _) => fail
        case JsError(errs) => GuidanceError.fromJsonValidationErrors(errs) match {
          case Nil => fail
          case UnknownTestType("3", "UnknownType") :: _ => succeed
          case errs => fail
        }
      }

    }
  }
}
