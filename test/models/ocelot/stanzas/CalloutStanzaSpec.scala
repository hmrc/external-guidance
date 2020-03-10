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

import scala.util.{Failure, Success, Try}

import base.BaseSpec

class CalloutStanzaSpec extends BaseSpec {

  val stanzaType: String = "CalloutStanza"

  val title: String = Title.getClass.getSimpleName.dropRight(1)
  val subTitle: String = SubTitle.getClass.getSimpleName.dropRight(1)
  val lede: String = Lede.getClass.getSimpleName.dropRight(1)
  val error: String = Error.getClass.getSimpleName.dropRight(1)

  val zero: Int = 0
  val one: Int = 1
  val two: Int = 2
  val ten: Int = 10

  val oneStr: String = "1"
  val twoStr: String = "2"
  val threeStr: String = "3"
  val end: String = "end"
  val invalid: String = "invalid"

  val stackFalse: Boolean = false
  val stackTrue: Boolean = true

  val titleCalloutStanzaInputJson: String =
      s"""|{
          |    "type": "$stanzaType",
          |    "noteType": "$title",
          |    "text": $zero,
          |    "next": ["$oneStr"],
          |    "stack": $stackFalse
          |}""".stripMargin

  val subTitleCalloutStanzaInputJson: String =
    s"""|{
       |    "type": "$stanzaType",
       |    "noteType": "$subTitle",
       |    "text": $one,
       |    "next": ["$twoStr"],
       |    "stack": $stackTrue
       |}""".stripMargin

  val ledeCalloutStanzaInputJson: String =
    s"""|{
        |    "type": "$stanzaType",
        |    "noteType": "$lede",
        |    "text": $two,
        |    "next": ["$threeStr"],
        |    "stack": $stackFalse
        |}""".stripMargin

  val errorCalloutStanzaInputJson =
    s"""|{
        |    "type": "$stanzaType",
        |    "noteType": "$error",
        |    "text": $ten,
        |    "next": ["$end"],
        |    "stack": $stackFalse
        |}""".stripMargin

  val invalidCalloutStanzaInputJson =
    s"""|{
        |    "type": "$stanzaType",
        |    "noteType": "$invalid",
        |    "text": $ten,
        |    "next": ["$end"],
        |    "stack": $stackFalse
        |}""".stripMargin

  val validCalloutStanzaAsJsObject: JsObject = Json.parse( titleCalloutStanzaInputJson ).as[JsObject]

  val expectedTitleCalloutStanza: CalloutStanza = CalloutStanza( Title, zero, Seq( oneStr ), stackFalse )

  val expectedSubTitleCalloutStanza: CalloutStanza = CalloutStanza( SubTitle, one, Seq( twoStr ), stackTrue )

  val expectedLedeCalloutStanza: CalloutStanza = CalloutStanza( Lede, two, Seq( threeStr ), stackFalse )

  val expectedErrorCalloutStatus: CalloutStanza = CalloutStanza( Error, ten, Seq( end ), stackFalse )

  "CalloutStanza" must {

    "read valid Callout stanza of type Title should create a Callout stanza with a note type of Title" in {

      val titleCalloutStanzaJson: JsValue = Json.parse(titleCalloutStanzaInputJson)

      val titleCalloutStanza: CalloutStanza = titleCalloutStanzaJson.as[CalloutStanza]

      titleCalloutStanza mustBe expectedTitleCalloutStanza
    }


    "read valid Callout stanza of type SubTitle should create a Callout stanza with a note type of SubTitle" in {

      val subTitleCalloutStanzaJson: JsValue = Json.parse(subTitleCalloutStanzaInputJson)

      val subTitleCalloutStanza: CalloutStanza = subTitleCalloutStanzaJson.as[CalloutStanza]

      subTitleCalloutStanza mustBe expectedSubTitleCalloutStanza
    }


    "read valid Callout stanza of type Lede should create a Callout stanza with a note type of Lede" in {

      val ledeCalloutStanzaJson: JsValue = Json.parse(ledeCalloutStanzaInputJson)

      val ledeCalloutStanza: CalloutStanza = ledeCalloutStanzaJson.as[CalloutStanza]

      ledeCalloutStanza mustBe expectedLedeCalloutStanza
    }


    "read valid Callout stanza of type Error should create a Callout stanza with a note type of Error" in {

      val errorCalloutStanzaJson: JsValue = Json.parse(errorCalloutStanzaInputJson)

      val errorCalloutStanza: CalloutStanza = errorCalloutStanzaJson.as[CalloutStanza]

      errorCalloutStanza mustBe expectedErrorCalloutStatus
    }

    "read Callout stanza with invalid note type should cause an exception to be raised" in {

      val invalidCalloutStanzaJson = Json.parse(invalidCalloutStanzaInputJson)

      Try {
        invalidCalloutStanzaJson.as[CalloutStanza]
      }
      match {
        case Success(_) => fail("An instance of CalloutStanza should not be created when the note type is incorrect")
        case Failure(failure) => succeed
      }

    }

    /** Test for missing properties in Json object representing instruction stanzas */
    missingJsObjectAttrTests[CalloutStanza](validCalloutStanzaAsJsObject, List("type"))

    /** Test for properties of the wrong type in json object representing instruction stanzas */
    incorrectPropertyTypeJsObjectAttrTests[CalloutStanza](validCalloutStanzaAsJsObject, List("type"))

  }

}
