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

import play.api.libs.json._
import core.models.ocelot._

import base.{BaseSpec, TestConstants}


class QuestionStanzaSpec extends BaseSpec with TestConstants {

  val twoStr: String = "2"
  val fourStr: String = "4"
  val fiveStr: String = "5"
  val sevenStr: String = "7"
  val eightStr: String = "8"

  val stanzaType: String = "QuestionStanza"
  val blankPage: Page = Page("any", "/url", Seq.empty, Nil)
  val stack: Boolean = false

  val twoAnswersQuestionStanzaJsonInput: String =
    s"""|{
       | "type" : "$stanzaType",
       | "text": $one,
       | "answers": [ $zero, $two ],
       | "next": [ "$twoStr", "$fiveStr" ],
       | "stack": $stack
       |}""".stripMargin

  val threeAnswersQuestionStanzaJsonInput: String =
    s"""|{
       | "type": "$stanzaType",
       | "text": $one,
       | "answers": [ $three, $four, $five ],
       | "next": [ "$fourStr", "$sevenStr", "$eightStr" ],
       | "stack": $stack
       |}""".stripMargin

  val validQuestionStanzaJson: JsObject = Json.parse(twoAnswersQuestionStanzaJsonInput).as[JsObject]

  val expectedTwoQuestionsQuestionStanza: QuestionStanza = QuestionStanza(one, Seq(zero, two), Seq(twoStr, fiveStr), None, stack)

  val expectedThreeAnswersQuestionStanza: QuestionStanza = QuestionStanza(one, Seq(three, four, five), Seq(fourStr, sevenStr, eightStr), None, stack)

  "Question stanza" must {

    "Evaluate valid input and return a next stanza and updated Labels" in {
      val labels = LabelCache()
      val stanza: Question = Question(Phrase("Question",""), Seq(Phrase("Yes",""), Phrase("No", "")), Seq("4", "5"), Some("Answer"), true)
      val (nxt, updatedLabels) = stanza.eval("0", blankPage, labels)

      nxt shouldBe Some("4")
      updatedLabels.value("Answer") shouldBe Some("Yes")
    }

    "Evaluate invalid input and return None and original Labels" in {
      val labels = LabelCache()
      val stanza: Question = Question(Phrase("Question",""), Seq(Phrase("Yes",""), Phrase("No", "")), Seq("4", "5"), Some("Answer"), true)
      val (nxt, updatedLabels) = stanza.eval("7", blankPage, labels)

      nxt shouldBe None
      updatedLabels.value("Answer") shouldBe None
    }

    "reading a valid QuestionStanza with two answers should create an instance of the class QuestionStanza" in {

      val twoAnswersQuestionStanzaJson: JsValue = Json.parse(twoAnswersQuestionStanzaJsonInput)
      val twoAnswersQuestionStanza: QuestionStanza = twoAnswersQuestionStanzaJson.as[QuestionStanza]

      twoAnswersQuestionStanza shouldBe expectedTwoQuestionsQuestionStanza
    }

    "reading a valid QuestionStanza with three answers should create an instance of the class QuestionStanza" in {

      val threeAnswersQuestionStanzaJson: JsValue = Json.parse(threeAnswersQuestionStanzaJsonInput)

      val threeAnswersQuestionStanza: QuestionStanza = threeAnswersQuestionStanzaJson.as[QuestionStanza]

      threeAnswersQuestionStanza shouldBe expectedThreeAnswersQuestionStanza
    }

    "Validate input as correct when valid" in {
      val answers =
        Seq(Phrase(Vector("Some Text 1", "Welsh: Some Text 1")),
            Phrase(Vector("Some Text 2", "Welsh: Some Text 2")),
            Phrase(Vector("Some Text 3", "Welsh: Some Text 3")))
      val answerDestinations = Seq("4", "5", "6")
      val questionPhrase: Phrase = Phrase(Vector("Some Text", "Welsh: Some Text"))

      val question: core.models.ocelot.stanzas.Question = Question(questionPhrase, answers, answerDestinations, None, false)

      question.validInput("1") shouldBe Some("1")
    }

    "Validate input as incorrect when invalid" in {
      val answers =
        Seq(Phrase(Vector("Some Text 1", "Welsh: Some Text 1")),
            Phrase(Vector("Some Text 2", "Welsh: Some Text 2")),
            Phrase(Vector("Some Text 3", "Welsh: Some Text 3")))
      val answerDestinations = Seq("4", "5", "6")
      val questionPhrase: Phrase = Phrase(Vector("Some Text", "Welsh: Some Text"))

      val question: core.models.ocelot.stanzas.Question = Question(questionPhrase, answers, answerDestinations, None, false)

      question.validInput("4") shouldBe None
      question.validInput("-1") shouldBe None
      question.validInput("blah") shouldBe None
    }

    "serialise to json" in {
      val stanza: QuestionStanza = QuestionStanza(0, Seq(1, 2), Seq("4", "5"), None, true)
      val expectedJson: String = """{"text":0,"answers":[1,2],"next":["4","5"],"stack":true}"""
      Json.toJson(stanza).toString shouldBe expectedJson
    }

    "serialise to json from a Stanza reference" in {
      val stanza: Stanza = QuestionStanza(0, Seq(1, 2), Seq("4", "5"), None, true)
      val expectedJson: String = """{"next":["4","5"],"stack":true,"answers":[1,2],"text":0,"type":"QuestionStanza"}"""
      Json.toJson(stanza).toString shouldBe expectedJson
    }

    /** Test for missing properties in Json object */
    missingJsObjectAttrTests[QuestionStanza](validQuestionStanzaJson, List("type"))

    /** Test for properties of the wrong type in json object representing question stanza */
    incorrectPropertyTypeJsObjectAttrTests[QuestionStanza](validQuestionStanzaJson, List("type"))

  }

}
