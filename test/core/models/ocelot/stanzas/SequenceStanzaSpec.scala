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

  val invalidJson: JsValue = Json.parse(
    s"""|{
        |    "type": "SequenceStanza",
        |    "text": 0,
        |    "next": ["2", "3", "4","end"],
        |    "options": [1,2,3,4],
        |    "label": "Items",
        |    "stack": false
        |}""".stripMargin
  )

  val expectedStanza: SequenceStanza =
    SequenceStanza(0, Seq("1","2","3","4","end"), Seq(1,2,3,4), Some("Items"), stack = false)
  val expectedSequence: Sequence =
    Sequence(
      expectedStanza,
      Phrase("Select","Select"),
      Seq(Phrase("One","One"),Phrase("Two","Two"),Phrase("Three","Three"),Phrase("Four","Four")),
      exclusive = false
    )


  "Reading valid JSON" should {
    "create a Sequence Stanza" in {
      json.as[SequenceStanza] shouldBe expectedStanza
    }
  }

  "Reading invalid JSON" should {
    "generate a JsError" in {
      invalidJson.validate[SequenceStanza] match {
        case JsError(_) => succeed
        case _ => fail("An instance of SequenceStanza should not be created when the next and options list lengths differ")
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

  "Sequence" should {

    "Determine invalid input to be incorrect" in {
      expectedSequence.validInput("a,b,c") shouldBe None
      expectedSequence.validInput("5,6,7") shouldBe None
    }

    "Determine valid input to be correct" in {
      expectedSequence.validInput("1,2") shouldBe Some("1,2")
      expectedSequence.validInput("2,3") shouldBe Some("2,3")
      expectedSequence.validInput("0") shouldBe Some("0")
    }

    "assign Nil to labels property when no label is used" in {
      expectedSequence.copy(label = None).labels shouldBe Nil
    }

    "Evaluate valid input and return with next of first flow in sequence" in {
      val labels = LabelCache()
      val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
      val (next, updatedLabels) = expectedSequence.eval("0", blankPage, labels)
      next shouldBe Some("1")
      updatedLabels.flowStack shouldBe List(Flow("1", Some(LabelValue("Items", "One"))), Continuation(Process.EndStanzaId))
      updatedLabels.value("Items") shouldBe Some("One")
    }

    "Evaluate invalid input to error return" in {
      val labels = LabelCache()
      val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
      val noopReturn: (Option[String], Labels) = (None, labels)
      expectedSequence.eval("hello", blankPage, labels) shouldBe noopReturn
    }

    "Evaluate indexes which dont exist in the options list to error return" in {
      val labels = LabelCache()
      val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
      val noopReturn: (Option[String], Labels) = (None, labels)
      expectedSequence.eval("24", blankPage, labels) shouldBe noopReturn
      labels.flowStack shouldBe Nil
    }
  }

  /** Test for missing properties in Json object representing instruction stanzas */
  missingJsObjectAttrTests[SequenceStanza](json.as[JsObject], List("type", "label"))

  /** Test for properties of the wrong type in json object representing instruction stanzas */
  incorrectPropertyTypeJsObjectAttrTests[SequenceStanza](json.as[JsObject], List("type", "label"))
}
