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
import core.models.ocelot.{Page, Phrase, LabelCache, Labels, Process, Flow, LabelValue, Continuation}

class SequenceStanzaSpec extends BaseSpec {

  val json: JsValue = Json.parse(
    s"""|{
        |    "next": ["1", "2", "3", "4", "end"],
        |    "stack": false,
        |    "options": [1,2,3,4],
        |    "text": 0,
        |    "label": "Items",
        |    "type": "SequenceStanza"
        |}""".stripMargin
  )

  val invalidJson_1: JsValue = Json.parse(
    s"""|{
        |    "type": "SequenceStanza",
        |    "text": 0,
        |    "next": ["2", "3", "4","end"],
        |    "options": [1,2,3,4],
        |    "label": "Items",
        |    "stack": false
        |}""".stripMargin
  )

  val invalidJson_2: JsValue = Json.parse(
    s"""|{
        |    "type": "SequenceStanza",
        |    "text": 0,
        |    "next": ["start", "end"],
        |    "options": [1,2,3,4],
        |    "label": "Items",
        |    "stack": false
        |}""".stripMargin
  )

  val invalidJson_3: JsValue = Json.parse(
    s"""|{
        |    "type": "SequenceStanza",
        |    "text": 0,
        |    "next": ["2", "3", "4","end"],
        |    "options": [1],
        |    "label": "Items",
        |    "stack": false
        |}""".stripMargin
  )

  val expectedStanza: SequenceStanza =
    SequenceStanza(0, Seq("1","2","3","4","end"), Seq(1,2,3,four), Some("Items"), stack = false)

  val expectedNonExclusiveSequence: NonExclusiveSequence =
    NonExclusiveSequence(
      Phrase("Select","Select"),
      expectedStanza.next,
      Seq(Phrase("One","One"),Phrase("Two","Two"),Phrase("Three","Three"),Phrase("Four","Four")),
      expectedStanza.label,
      expectedStanza.stack
    )

  val expectedExclusiveSequence: ExclusiveSequence =
  ExclusiveSequence(
    Phrase("Select","Select"),
    expectedStanza.next,
    Seq(
      Phrase("One","One"),
      Phrase("Two","Two"),
      Phrase("Three","Three"),
      Phrase(
        "Four [exclusive:Selecting this checkbox will deselect the other checkboxes]",
        "Four [exclusive:Welsh: Selecting this checkbox will deselect the other checkboxes]"
      )),
    expectedStanza.label,
    expectedStanza.stack
  )

  "Reading valid JSON" should {
    "create a Sequence Stanza" in {
      json.as[SequenceStanza] shouldBe expectedStanza
    }
  }

  "Reading invalid JSON" should {

    "generate a JsError when the number of entries in next is not one greater than the number of entries in options" in {
      invalidJson_1.validate[SequenceStanza] match {
        case JsError(_) => succeed
        case _ => fail("An instance of SequenceStanza should not be created when the next and options list lengths differ")
      }
    }

    "generate a JsError if the number of entries in next is less than three" in {
      invalidJson_2.validate[SequenceStanza] match {
        case JsError(_) => succeed
        case _ => fail("An instance of sequence stanza should not be created when the number of entries in next is less than 3")
      }
    }

    "generate a JsError if the number of entries in options is less than one" in {
      invalidJson_3.validate[SequenceStanza] match {
        case JsError(_) => succeed
        case _ => fail("An instance of SequenceStanza should not be created when the number of entries in options is less than 1")
      }
    }

  }

  "serialise to json" in {
    Json.toJson(expectedStanza).toString shouldBe """{"text":0,"next":["1","2","3","4","end"],"options":[1,2,3,4],"label":"Items","stack":false}"""
  }

  "serialise to json from a Stanza reference" in {
    val stanza: Stanza = expectedStanza
    Json.toJson(stanza) shouldBe json
  }

  "Non-exclusive sequence" should {

    "Determine invalid input to be incorrect" in {
      expectedNonExclusiveSequence.validInput("a,b,c") shouldBe None
      expectedNonExclusiveSequence.validInput("5,6,7") shouldBe None
    }

    "Determine valid input to be correct" in {
      expectedNonExclusiveSequence.validInput("1,2") shouldBe Some("1,2")
      expectedNonExclusiveSequence.validInput("2,3") shouldBe Some("2,3")
      expectedNonExclusiveSequence.validInput("0") shouldBe Some("0")
    }

    "assign Nil to labels property when no label is used" in {
      expectedNonExclusiveSequence.copy(label = None).labels shouldBe Nil
    }

    "Evaluate valid input and return with next of first flow in sequence" in {
      val labels = LabelCache()
      val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
      val (next, updatedLabels) = expectedNonExclusiveSequence.eval("0", blankPage, labels)
      next shouldBe Some("1")
      updatedLabels.flowStack shouldBe List(Flow("1", Some(LabelValue("Items", "One"))), Continuation(Process.EndStanzaId))
      updatedLabels.value("Items") shouldBe Some("One")
    }

    "Evaluate invalid input to error return" in {
      val labels = LabelCache()
      val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
      val noopReturn: (Option[String], Labels) = (None, labels)
      expectedNonExclusiveSequence.eval("hello", blankPage, labels) shouldBe noopReturn
    }

    "Evaluate indexes which don't exist in the options list to error return" in {
      val labels = LabelCache()
      val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
      val noopReturn: (Option[String], Labels) = (None, labels)
      expectedNonExclusiveSequence.eval("24", blankPage, labels) shouldBe noopReturn
      labels.flowStack shouldBe Nil
    }
  }

  "Exclusive sequence" should {

    "Determine invalid input to be incorrect" in {
      expectedExclusiveSequence.validInput("a,b,c") shouldBe None
      expectedExclusiveSequence.validInput("5,6,7") shouldBe None
      expectedExclusiveSequence.validInput("2,3") shouldBe None
      expectedExclusiveSequence.validInput("0,3") shouldBe None
      expectedExclusiveSequence.validInput("1,3") shouldBe None
    }

    "Determine valid input to be correct" in {
      expectedExclusiveSequence.validInput("1,2") shouldBe Some("1,2")
      expectedExclusiveSequence.validInput("0,1") shouldBe Some("0,1")
      expectedExclusiveSequence.validInput("0") shouldBe Some("0")
      expectedExclusiveSequence.validInput("3") shouldBe Some("3")
    }

    "assign Nil to labels property when no label is used" in {
      expectedExclusiveSequence.copy(label = None).labels shouldBe Nil
    }

    "Evaluate valid input and return with next of first flow in sequence" in {
      val labels = LabelCache()
      val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
      val (next, updatedLabels) = expectedExclusiveSequence.eval("0", blankPage, labels)
      next shouldBe Some("1")
      updatedLabels.flowStack shouldBe List(Flow("1", Some(LabelValue("Items", "One"))), Continuation(Process.EndStanzaId))
      updatedLabels.value("Items") shouldBe Some("One")
    }

    "Evaluate invalid input to error return" in {
      val labels = LabelCache()
      val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
      val noopReturn: (Option[String], Labels) = (None, labels)
      expectedExclusiveSequence.eval("hello", blankPage, labels) shouldBe noopReturn
    }

    "Evaluate indexes which don't exist in the options list to error return" in {
      val labels = LabelCache()
      val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
      val noopReturn: (Option[String], Labels) = (None, labels)
      expectedExclusiveSequence.eval("24", blankPage, labels) shouldBe noopReturn
      labels.flowStack shouldBe Nil
    }

  }

  /** Test for missing properties in Json object representing instruction stanzas */
  missingJsObjectAttrTests[SequenceStanza](json.as[JsObject], List("type", "label"))

  /** Test for properties of the wrong type in json object representing instruction stanzas */
  incorrectPropertyTypeJsObjectAttrTests[SequenceStanza](json.as[JsObject], List("type", "label"))
}
