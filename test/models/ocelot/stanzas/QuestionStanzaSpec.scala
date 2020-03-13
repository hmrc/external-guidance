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

import play.api.libs.json._

import base.BaseSpec

class QuestionStanzaSpec extends BaseSpec {

  val zero: Int = 0
  val one: Int = 1
  val two: Int = 2
  val three: Int = 3
  val four: Int = 4
  val five: Int = 5
  val seven: Int = 7
  val eight: Int = 8

  val twoStr: String = "2"
  val fourStr: String = "4"
  val fiveStr: String = "5"
  val sevenStr: String = "7"
  val eightStr: String = "8"

  val stanzaType: String = "QuestionStanza"

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

  val validQuestionStanzaJson: JsObject = Json.parse( twoAnswersQuestionStanzaJsonInput ).as[JsObject]

  val expectedTwoQuestionsQuestionStanza = QuestionStanza( one, Seq( zero,  two ), Seq( twoStr, fiveStr ), stack )

  val expectedThreeAnswersQuestionStanza = QuestionStanza( one, Seq( three, four, five ), Seq( fourStr, sevenStr, eightStr ), stack )

  "Question stanza" must {

    "reading a valid QuestionStanza with two answers should create an instance of the class QuestionStanza" in {

      val twoAnswersQuestionStanzaJson: JsValue = Json.parse(twoAnswersQuestionStanzaJsonInput)

      val twoAnswersQuestionStanza: QuestionStanza = twoAnswersQuestionStanzaJson.as[QuestionStanza]

      twoAnswersQuestionStanza mustBe expectedTwoQuestionsQuestionStanza
    }

    "reading a valid QuestionStanza with three answers should create an instance of the class QuestionStanza" in {

      val threeAnswersQuestionStanzaJson: JsValue = Json.parse(threeAnswersQuestionStanzaJsonInput)

      val threeAnswersQuestionStanza: QuestionStanza = threeAnswersQuestionStanzaJson.as[QuestionStanza]

      threeAnswersQuestionStanza mustBe expectedThreeAnswersQuestionStanza
    }


    /** Test for missing properties in Json object */
    missingJsObjectAttrTests[QuestionStanza]( validQuestionStanzaJson, List( "type" ) )

    /** Test for properties of the wrong type in json object representing question stanza */
    incorrectPropertyTypeJsObjectAttrTests[QuestionStanza]( validQuestionStanzaJson, List( "type" ) )

  }

}
