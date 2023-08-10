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
import core.models.ocelot._
import core.models.ocelot.errors.{GuidanceError, UnknownTestType}

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
      |    "titlePhrase": 8,
      |    "processCode": "cup-of-tea"
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
  )

  val validChoiceStanzaJson: JsObject = Json
    .parse(
      s"""{
      |  "type": "$stanzaType",
      |  "next": [${next.map(x => s""""$x"""").mkString(",")}],
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
      |  "stack": $stack
      |}
    """.stripMargin
    )
    .as[JsObject]

  val invalidChoiceStanzaJson: JsObject = Json
    .parse(
      s"""{
      |  "type": "$stanzaType",
      |  "next": [${next.map(x => s""""$x"""").mkString(",")}],
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
      |  "stack": $stack
      |}
    """.stripMargin
    )
    .as[JsObject]

  "ChoiceStanza" must {

    "deserialize from json" in {

      val stanza: ChoiceStanza = validChoiceStanzaJson.as[ChoiceStanza]

      stanza.stack shouldBe false
      stanza.next.length shouldBe 3
      stanza.next shouldBe next
      stanza.tests.length shouldBe 2
      stanza.tests(0) shouldBe ChoiceStanzaTest("VAL-1", LessThanOrEquals, "VAL-2")
      stanza.tests(1) shouldBe ChoiceStanzaTest("VAL-3", LessThanOrEquals, "VAL-4")
    }

    "serialize to json" in {
      val stanza: ChoiceStanza =
        ChoiceStanza(next, Seq(ChoiceStanzaTest("VAL-1", LessThanOrEquals, "VAL-2"), ChoiceStanzaTest("VAL-3", LessThanOrEquals, "VAL-4")), false)
      val expectedJson: String = s"""{"next":[${next
        .map(x => s""""$x"""")
        .mkString(",")}],"tests":[{"left":"VAL-1","test":"lessThanOrEquals","right":"VAL-2"},{"left":"VAL-3","test":"lessThanOrEquals","right":"VAL-4"}],"stack":false}"""
      val json: String = Json.toJson(stanza).toString
      json shouldBe expectedJson
    }

    "serialize to json from a Stanza reference" in {
      val stanza: Stanza =
        ChoiceStanza(next, Seq(ChoiceStanzaTest("VAL-1", LessThanOrEquals, "VAL-2"), ChoiceStanzaTest("VAL-3", LessThanOrEquals, "VAL-4")), false)
      val expectedJson: String = s"""{"type":"ChoiceStanza","next":[${next
        .map(x => s""""$x"""")
        .mkString(",")}],"tests":[{"left":"VAL-1","test":"lessThanOrEquals","right":"VAL-2"},{"left":"VAL-3","test":"lessThanOrEquals","right":"VAL-4"}],"stack":false}"""
      val json: String = Json.toJson(stanza).toString
      json shouldBe expectedJson
    }

    "fail to parse if an unknown value type is found" in {
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
      choice.tests.zipWithIndex.foreach {
        case (LessThanOrEqualsTest(_, _), index) if stanza.tests(index).test == LessThanOrEquals => succeed
        case x => fail

      }
    }

    "Evaluate to correct result when one of the tests succeed" in {
      val next = Seq("40", "41", "50")
      val stanza: ChoiceStanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("4", LessThanOrEquals, "3"), ChoiceStanzaTest("3", LessThanOrEquals, "4")), false)
      val choice = Choice(stanza)
      val lc = LabelCache()
      val expectedResult = ("41", lc, Nil)
      choice.eval(lc) shouldBe expectedResult
    }

    "Evaluate to correct result when no tests succeed" in {
      val next = Seq("40", "41", "50")
      val stanza: ChoiceStanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("4", LessThanOrEquals, "3"), ChoiceStanzaTest("3", MoreThan, "4")), false)
      val choice = Choice(stanza)
      val lc = LabelCache()
      val expectedResult = ("50", lc, Nil)
      choice.eval(lc) shouldBe expectedResult
    }

    "Evaluate to correct result when one of the tests succeed referencing labels" in {
      val next = Seq("40", "41", "50", "51")
      val stanza: ChoiceStanza = ChoiceStanza(
        next,
        Seq(ChoiceStanzaTest("[label:X]", LessThanOrEquals, "[label:Y]"), ChoiceStanzaTest("3", LessThanOrEquals, "4"), ChoiceStanzaTest("3", NotEquals, "4")),
        false
      )
      val choice = Choice(stanza)
      val labels = Map("X" -> ScalarLabel("X", List("33.5")), "Y" -> ScalarLabel("Y", List("44")))
      val lc = LabelCache(labels)
      val expectedResult = ("40", lc, Nil)
      choice.eval(lc) shouldBe expectedResult
    }

    "Evaluate to correct result when no tests succeed referencing labels" in {
      val next = Seq("40", "41", "50", "51")
      val stanza: ChoiceStanza = ChoiceStanza(
        next,
        Seq(ChoiceStanzaTest("[label:X]", LessThanOrEquals, "[label:Y]"), ChoiceStanzaTest("3", Equals, "4"), ChoiceStanzaTest("1", MoreThanOrEquals, "4")),
        false
      )
      val choice = Choice(stanza)
      val labels = Map("X" -> ScalarLabel("X", List("33.5")), "Y" -> ScalarLabel("Y", List("4")))
      val lc = LabelCache(labels)
      val expectedResult = ("51", lc, Nil)
      choice.eval(lc) shouldBe expectedResult
    }

    "Evaluate to correct result when date test succeeds referencing labels" in {

      val next: Seq[String] = Seq("1", "0")

      val stanza: ChoiceStanza = ChoiceStanza(next, Seq(ChoiceStanzaTest("[label:date1]", LessThanOrEquals, "[label:date2]")), stack = false)

      val choice: Choice = Choice(stanza)

      val labels: Map[String, Label] = Map(
        "date1" -> ScalarLabel("date1", List("19/01/2021")),
        "date2" -> ScalarLabel("date2", List("20/01/2021"))
      )

      val lc = LabelCache(labels)

      val expectedResult = ("1", lc, Nil)

      choice.eval(lc) shouldBe expectedResult
    }

    "Evaluate to correct result when date test fails referencing labels" in {

      val next: Seq[String] = Seq("1,", "0")

      val stanza: ChoiceStanza = ChoiceStanza(
        next,
        Seq(ChoiceStanzaTest("[label:date1]", MoreThanOrEquals, "[label:date2]")),
        stack = false
      )

      val choice: Choice = Choice(stanza)

      val labels: Map[String, Label] = Map(
        "date1" -> ScalarLabel("date1", List("19/01/2021")),
        "date2" -> ScalarLabel("date2", List("20/01/2021"))
      )

      val lc = LabelCache(labels)

      val expectedResult = ("0", lc, Nil)

      choice.eval(lc) shouldBe expectedResult
    }

    "Evaluate to correct result when date test succeeds referencing a label and a timescale wrapped literal" in {
      val next: Seq[String] = Seq("1,", "0")
      val choice: Choice = Choice(ChoiceStanza(next, Seq(ChoiceStanzaTest("[label:date1]", LessThanOrEquals, "[timescale:20/01/2021]")), false))

      val labels = LabelCache(Map(
        "date1" -> ScalarLabel("date1", List("19/01/2021")),
        "date2" -> ScalarLabel("date2", List("20/01/2021"))
      ))

      val (nextStanza, updatedLabels, err) = choice.eval(labels)

      nextStanza shouldBe next(0)
      updatedLabels shouldBe labels
    }

    "Evaluate to correct result when date test fails referencing a label and a timescale wrapped literal" in {
      val next: Seq[String] = Seq("1,", "0")
      val choice: Choice = Choice(ChoiceStanza(next, Seq(ChoiceStanzaTest("[label:date1]", MoreThanOrEquals, "[timescale:20/01/2021]")), false))

      val labels = LabelCache(Map(
        "date1" -> ScalarLabel("date1", List("19/01/2021")),
        "date2" -> ScalarLabel("date2", List("20/01/2021"))
      ))

      val (nextStanza, updatedLabels, err) = choice.eval(labels)

      nextStanza shouldBe next(1)
      updatedLabels shouldBe labels
    }
  }

  "ChoiceTest" must {
    "provide support to EqualsTest" in {
      EqualsTest("5", "5").eval(LabelCache()) shouldBe true

      EqualsTest("5,345,777.5", "5345777.5").eval(LabelCache()) shouldBe true

      EqualsTest("5,345,777.5", "5345777.50").eval(LabelCache()) shouldBe true

      EqualsTest("5,345,777.0", "5345777").eval(LabelCache()) shouldBe true

      EqualsTest("5,345,777.5", "5345777.a").eval(LabelCache()) shouldBe false

      EqualsTest("4", "5").eval(LabelCache()) shouldBe false

      EqualsTest("hello", "hello").eval(LabelCache()) shouldBe true

      EqualsTest("4", "hello").eval(LabelCache()) shouldBe false

      EqualsTest("20/01/2021", "20/01/2021").eval(LabelCache()) shouldBe true

      EqualsTest("20/01/2021", "21/01/2021").eval(LabelCache()) shouldBe false
    }

    "provide support to NotEqualsTest" in {
      NotEqualsTest("5.0", "5").eval(LabelCache()) shouldBe false

      NotEqualsTest("5.1", "5.10").eval(LabelCache()) shouldBe false

      NotEqualsTest("5,234.1", "5234.10").eval(LabelCache()) shouldBe false

      NotEqualsTest("4", "5").eval(LabelCache()) shouldBe true

      NotEqualsTest("hello", "hello").eval(LabelCache()) shouldBe false

      NotEqualsTest("4", "hello").eval(LabelCache()) shouldBe true

      NotEqualsTest("20/01/2021", "20/01/2021").eval(LabelCache()) shouldBe false

      NotEqualsTest("20/01/2021", "21/01/2021").eval(LabelCache()) shouldBe true
    }

    "provide support to MoreThanTest" in {
      MoreThanTest("5", "5").eval(LabelCache()) shouldBe false

      MoreThanTest("4", "5").eval(LabelCache()) shouldBe false

      MoreThanTest("4.0", "4").eval(LabelCache()) shouldBe false

      MoreThanTest("4.01", "4").eval(LabelCache()) shouldBe true

      MoreThanTest("4", "3").eval(LabelCache()) shouldBe true

      MoreThanTest("hello", "hello").eval(LabelCache()) shouldBe false

      MoreThanTest("4", "hello").eval(LabelCache()) shouldBe false

      MoreThanTest("20/01/2021", "21/01/2021").eval(LabelCache()) shouldBe false

      MoreThanTest("21/01/2021", "21/01/2021").eval(LabelCache()) shouldBe false

      MoreThanTest("21/01/2021", "20/01/2021").eval(LabelCache()) shouldBe true
    }

    "provide support to MoreThanOrEqualsTest" in {
      MoreThanOrEqualsTest("5", "5").eval(LabelCache()) shouldBe true

      MoreThanOrEqualsTest("5,234.1", "5234.10").eval(LabelCache()) shouldBe true

      MoreThanOrEqualsTest("4", "5").eval(LabelCache()) shouldBe false

      MoreThanOrEqualsTest("4", "3").eval(LabelCache()) shouldBe true

      MoreThanOrEqualsTest("hello", "hello").eval(LabelCache()) shouldBe true

      MoreThanOrEqualsTest("4", "hello").eval(LabelCache()) shouldBe false

      MoreThanOrEqualsTest("20/01/2021", "21/01/2021").eval(LabelCache()) shouldBe false

      MoreThanOrEqualsTest("21/01/2021", "21/01/2021").eval(LabelCache()) shouldBe true

      MoreThanOrEqualsTest("21/01/2021", "20/01/2021").eval(LabelCache()) shouldBe true
    }

    "provide support to LessThanTest" in {
      LessThanTest("5", "5").eval(LabelCache()) shouldBe false

      LessThanTest("4", "5").eval(LabelCache()) shouldBe true

      LessThanTest("4", "3").eval(LabelCache()) shouldBe false

      LessThanTest("4,345", "4345.0").eval(LabelCache()) shouldBe false

      LessThanTest("hello", "hello").eval(LabelCache()) shouldBe false

      LessThanTest("4", "hello").eval(LabelCache()) shouldBe true

      LessThanTest("20/01/2021", "21/01/2021").eval(LabelCache()) shouldBe true

      LessThanTest("20/01/2021", "20/01/2021").eval(LabelCache()) shouldBe false

      LessThanTest("21/01/2021", "20/01/2021").eval(LabelCache()) shouldBe false
    }

    "provide support to LessThanOrEqualsTest" in {
      LessThanOrEqualsTest("5", "5").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("4", "5").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("4", "3").eval(LabelCache()) shouldBe false

      LessThanOrEqualsTest("4,345", "4345.0").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("hello", "hello").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("4", "hello").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("20/01/2021", "21/01/2021").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("20/01/2021", "20/01/2021").eval(LabelCache()) shouldBe true

      LessThanOrEqualsTest("21/01/2021", "20/01/2021").eval(LabelCache()) shouldBe false
    }

    "provide support to ContainsTest" in {
      ContainsTest("2354", "5").eval(LabelCache()) shouldBe true

      ContainsTest("23 54", "5").eval(LabelCache()) shouldBe true

      ContainsTest("Hello World", "wor").eval(LabelCache()) shouldBe true

      ContainsTest("4", "3").eval(LabelCache()) shouldBe false

      ContainsTest("hello", "hello").eval(LabelCache()) shouldBe true

      ContainsTest("4", "hello").eval(LabelCache()) shouldBe false

      ContainsTest("20/01/2021", "21/01/2021").eval(LabelCache()) shouldBe false

      ContainsTest("20/01/2021", "20/01/2021").eval(LabelCache()) shouldBe true

      ContainsTest("21/01/2021", "20/01/2021").eval(LabelCache()) shouldBe false

      val list: ListLabel = ListLabel("Months", List("January", "February", "March", "April", "May"))

      ContainsTest("[label:Months]", "December").eval(LabelCache(Map(list.name -> list))) shouldBe false
      ContainsTest("[label:Months]", "January").eval(LabelCache(Map(list.name -> list))) shouldBe true
      ContainsTest("[label:Months]", "April").eval(LabelCache(Map(list.name -> list))) shouldBe true
      ContainsTest("[label:Months]", "arch").eval(LabelCache(Map(list.name -> list))) shouldBe true
    }
  }

  "ChoiceStanzaTest" must {

    val lte = """{"left": "VAL-1","test": "lessThanOrEquals","right": "VAL-2"}"""
    val lt = """{"left": "VAL-1","test": "lessThan","right": "VAL-2"}"""
    val e = """{"left": "VAL-3","test": "equals","right": "VAL-4"}"""
    val ne = """{"left": "VAL-3","test": "notEquals","right": "VAL-4"}"""
    val m = """{"left": "VAL-3","test": "moreThan","right": "VAL-4"}"""
    val me = """{"left": "VAL-3","test": "moreThanOrEquals","right": "VAL-4"}"""
    val con = """{"left": "VAL-3","test": "contains","right": "VAL-4"}"""
    def choiceStanzaJson(t1: String, t2: String) = s"""{"type": "ChoiceStanza","tests": [$t1,$t2],"next": ["1", "2", "3"],"stack": true}"""

    "DeSerialize EqualsTest" in {
      Json
        .parse(choiceStanzaJson(e, e))
        .validate[ChoiceStanza]
        .fold(err => fail, cs => {
          val choice = Choice(cs)
          choice.tests(0) shouldBe EqualsTest("VAL-3", "VAL-4")
          choice.tests(1) shouldBe EqualsTest("VAL-3", "VAL-4")
        })
    }

    "DeSerialize NotEqualsTest" in {
      Json
        .parse(choiceStanzaJson(ne, ne))
        .validate[ChoiceStanza]
        .fold(err => fail, cs => {
          val choice = Choice(cs)
          choice.tests(0) shouldBe NotEqualsTest("VAL-3", "VAL-4")
          choice.tests(1) shouldBe NotEqualsTest("VAL-3", "VAL-4")
        })
    }
    "DeSerialize LessThanTest" in {
      Json
        .parse(choiceStanzaJson(lt, lt))
        .validate[ChoiceStanza]
        .fold(err => fail, cs => {
          val choice = Choice(cs)
          choice.tests(0) shouldBe LessThanTest("VAL-1", "VAL-2")
          choice.tests(1) shouldBe LessThanTest("VAL-1", "VAL-2")
        })
    }
    "DeSerialize LessThanOrEqualsTest" in {
      Json
        .parse(choiceStanzaJson(lte, lte))
        .validate[ChoiceStanza]
        .fold(
          err => fail,
          cs => {
            val choice = Choice(cs)
            choice.tests(0) shouldBe LessThanOrEqualsTest("VAL-1", "VAL-2")
            choice.tests(1) shouldBe LessThanOrEqualsTest("VAL-1", "VAL-2")
          }
        )
    }
    "DeSerialize MoreThanTest" in {
      Json
        .parse(choiceStanzaJson(m, m))
        .validate[ChoiceStanza]
        .fold(err => fail, cs => {
          val choice = Choice(cs)
          choice.tests(0) shouldBe MoreThanTest("VAL-3", "VAL-4")
          choice.tests(1) shouldBe MoreThanTest("VAL-3", "VAL-4")
        })
    }
    "DeSerialize MoreThanOrEqualsTest" in {
      Json
        .parse(choiceStanzaJson(me, me))
        .validate[ChoiceStanza]
        .fold(
          err => fail,
          cs => {
            val choice = Choice(cs)
            choice.tests(0) shouldBe MoreThanOrEqualsTest("VAL-3", "VAL-4")
            choice.tests(1) shouldBe MoreThanOrEqualsTest("VAL-3", "VAL-4")
          }
        )
    }
    "DeSerialize ContainsTest" in {
      Json
        .parse(choiceStanzaJson(con, con))
        .validate[ChoiceStanza]
        .fold(
          err => fail,
          cs => {
            val choice = Choice(cs)
            choice.tests(0) shouldBe ContainsTest("VAL-3", "VAL-4")
            choice.tests(1) shouldBe ContainsTest("VAL-3", "VAL-4")
          }
        )
    }

    "Serialize EqualsTest" in {
      Json.toJson(ChoiceStanzaTest("3", Equals, "4")).toString shouldBe """{"left":"3","test":"equals","right":"4"}"""
    }
    "Serialize NotEqualsTest" in {
      Json.toJson(ChoiceStanzaTest("3", NotEquals, "4")).toString shouldBe """{"left":"3","test":"notEquals","right":"4"}"""
    }
    "Serialize LessThanTest" in {
      Json.toJson(ChoiceStanzaTest("3", LessThan, "4")).toString shouldBe """{"left":"3","test":"lessThan","right":"4"}"""
    }
    "Serialize LessThanOrEqualsTest" in {
      Json.toJson(ChoiceStanzaTest("3", LessThanOrEquals, "4")).toString shouldBe """{"left":"3","test":"lessThanOrEquals","right":"4"}"""
    }
    "Serialize MoreThanTest" in {
      Json.toJson(ChoiceStanzaTest("3", MoreThan, "4")).toString shouldBe """{"left":"3","test":"moreThan","right":"4"}"""
    }
    "Serialize MoreThanOrEqualsTest" in {
      Json.toJson(ChoiceStanzaTest("3", MoreThanOrEquals, "4")).toString shouldBe """{"left":"3","test":"moreThanOrEquals","right":"4"}"""
    }
    "Serialize ContainsTest" in {
      Json.toJson(ChoiceStanzaTest("3", Contains, "4")).toString shouldBe """{"left":"3","test":"contains","right":"4"}"""
    }

    "Detect unknown test type strings at json parse level" in {
      val invalidChoiceStanzaJson: JsObject = Json.parse(s"""{"left": "VAL-1","test": "UnknownType","right": "VAL-2"}""").as[JsObject]
      invalidChoiceStanzaJson.validate[ChoiceStanzaTest] match {
        case JsError(errTuple :: _) =>
          errTuple match {
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
        case JsError(errTuple :: _) =>
          errTuple match {
            case (_, err +: _) if err.messages(0) == "TestType" && err.args.contains("44") => succeed
            case _ => fail
          }
        case JsError(_) => fail
        case JsSuccess(_, _) => fail
      }
    }
  }

  "Page building" must {
    "be able to detect UnknownTestType error" in {
      onePageJsonWithInvalidTestType.as[JsObject].validate[Process] match {
        case JsSuccess(_, _) => fail
        case JsError(errs) =>
          GuidanceError.fromJsonValidationErrors(mapValidationErrors(errs)) match {
            case Nil => fail
            case UnknownTestType("3", "UnknownType") :: _ => succeed
            case _ => fail
          }
      }

    }
  }
}
