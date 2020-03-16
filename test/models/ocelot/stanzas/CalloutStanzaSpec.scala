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

class CalloutStanzaSpec extends BaseSpec {

  def getCalloutType[T](stanzaType: T): String = stanzaType.getClass.getSimpleName.dropRight(1)

  def getStanzaJson(calloutType: String): JsValue = Json.parse(
    s"""|{
        |    "type": "CalloutStanza",
        |    "noteType": "$calloutType",
        |    "text": 0,
        |    "next": ["1"],
        |    "stack": false
        |}""".stripMargin
  )

  def buildCalloutStanza(calloutType: CalloutType): CalloutStanza =
    CalloutStanza(calloutType, 0, Seq("1"), stack = false)

  val title: String = getCalloutType(Title)
  val subTitle: String = getCalloutType(SubTitle)
  val lede: String = getCalloutType(Lede)
  val error: String = getCalloutType(Error)
  val section: String = getCalloutType(Section)
  val invalid: String = "invalid"

  val titleCalloutStanzaInputJson: JsValue = getStanzaJson(title)
  val subTitleCalloutStanzaInputJson: JsValue = getStanzaJson(subTitle)
  val ledeCalloutStanzaInputJson: JsValue = getStanzaJson(lede)
  val errorCalloutStanzaInputJson: JsValue = getStanzaJson(error)
  val sectionCalloutStanzaInputJson: JsValue = getStanzaJson(section)
  val invalidCalloutStanzaInputJson: JsValue = getStanzaJson(invalid)

  val validCalloutStanzaAsJsObject: JsObject = titleCalloutStanzaInputJson.as[JsObject]

  val expectedTitleCalloutStanza: CalloutStanza = buildCalloutStanza(Title)
  val expectedSubTitleCalloutStanza: CalloutStanza = buildCalloutStanza(SubTitle)
  val expectedLedeCalloutStanza: CalloutStanza = buildCalloutStanza(Lede)
  val expectedErrorCalloutStanza: CalloutStanza = buildCalloutStanza(Error)
  val expectedSectionCalloutStanza: CalloutStanza = buildCalloutStanza(Section)

  val jsonToStanzaMappings: Map[JsValue, CalloutStanza] = Map(
    titleCalloutStanzaInputJson -> expectedTitleCalloutStanza,
    subTitleCalloutStanzaInputJson -> expectedSubTitleCalloutStanza,
    ledeCalloutStanzaInputJson -> expectedLedeCalloutStanza,
    errorCalloutStanzaInputJson -> expectedErrorCalloutStanza,
    sectionCalloutStanzaInputJson -> expectedSectionCalloutStanza
  )

  jsonToStanzaMappings foreach { mapping =>
    val (json, expectedStanza) = mapping
    val jsonNoteType = (json \ "noteType").as[String]

    s"Reading valid JSON for a $jsonNoteType Callout" should {
      s"create a ${expectedStanza.noteType} Callout Stanza" in {
        val calloutStanza: CalloutStanza = json.as[CalloutStanza]
        calloutStanza mustBe expectedStanza
      }
    }
  }

  "Reading invalid JSON for a Callout" should {
    "cause an exception to be raised" in {
      invalidCalloutStanzaInputJson.validate[CalloutStanza] match {
        case JsError(_) => succeed
        case _ => fail("An instance of CalloutStanza should not be created when the note type is incorrect")
      }
    }
  }

  /** Test for missing properties in Json object representing instruction stanzas */
  missingJsObjectAttrTests[CalloutStanza](validCalloutStanzaAsJsObject, List("type"))

  /** Test for properties of the wrong type in json object representing instruction stanzas */
  incorrectPropertyTypeJsObjectAttrTests[CalloutStanza](validCalloutStanzaAsJsObject, List("type"))

}
