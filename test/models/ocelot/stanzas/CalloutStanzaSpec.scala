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
import models.ocelot.stanzas.CalloutStanza._
import play.api.libs.json._

class CalloutStanzaSpec extends BaseSpec {

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

  val title: String = "Title"
  val subTitle: String = "SubTitle"
  val lede: String = "Lede"
  val error: String = "Error"
  val section: String = "Section"
  val subSection: String = "SubSection"
  val important: String = "Important"
  val yourCall: String = "YourCall"
  val invalid: String = "invalid"
  val typeError: String = "TypeError"
  val valueError: String = "ValueError"
  val numberedList: String = "NumberedListItem"
  val numberedCircleList: String = "NumberedCircleListItem"
  val oldNumberedList: String = "NumberedList"
  val oldNumberedCircleList: String = "NumberedCircleList"
  val noteType: String = "Note"  // note is defined in parent class
  val end: String = "end"

  val stackFalse: Boolean = false

  val titleCalloutStanzaInputJson: JsValue = getStanzaJson(title)
  val subTitleCalloutStanzaInputJson: JsValue = getStanzaJson(subTitle)
  val ledeCalloutStanzaInputJson: JsValue = getStanzaJson(lede)
  val errorCalloutStanzaInputJson: JsValue = getStanzaJson(error)
  val typeErrorCalloutStanzaInputJson: JsValue = getStanzaJson(typeError)
  val valueErrorCalloutStanzaInputJson: JsValue = getStanzaJson(valueError)
  val sectionCalloutStanzaInputJson: JsValue = getStanzaJson(section)
  val subSectionCalloutStanzaInputJson: JsValue = getStanzaJson(subSection)
  val importantCalloutStanzaInputJson: JsValue = getStanzaJson(important)
  val yourCallCalloutStanzaInputJson: JsValue = getStanzaJson(yourCall)
  val numberedListCalloutStanzaInputJson: JsValue = getStanzaJson(numberedList)
  val numberedCircleListCalloutStanzaInputJson: JsValue = getStanzaJson(numberedCircleList)
  val oldNumberedListCalloutStanzaInputJson: JsValue = getStanzaJson(oldNumberedList)
  val oldNumberedCircleListCalloutStanzaInputJson: JsValue = getStanzaJson(oldNumberedCircleList)
  val noteCalloutStanzaInputJson: JsValue = getStanzaJson(noteType)

  val invalidCalloutStanzaInputJson: JsValue = getStanzaJson(invalid)


  val validCalloutStanzaAsJsObject: JsObject = titleCalloutStanzaInputJson.as[JsObject]

  val expectedTitleCalloutStanza: CalloutStanza = buildCalloutStanza(Title)
  val expectedSubTitleCalloutStanza: CalloutStanza = buildCalloutStanza(SubTitle)
  val expectedLedeCalloutStanza: CalloutStanza = buildCalloutStanza(Lede)
  val expectedErrorCalloutStanza: CalloutStanza = buildCalloutStanza(Error)
  val expectedTypeErrorCalloutStanza: CalloutStanza = buildCalloutStanza(TypeError)
  val expectedValueErrorCalloutStanza: CalloutStanza = buildCalloutStanza(ValueError)
  val expectedSectionCalloutStanza: CalloutStanza = buildCalloutStanza(Section)
  val expectedSubSectionCalloutStanza: CalloutStanza = buildCalloutStanza(SubSection)
  val expectedImportantCalloutStanza: CalloutStanza = buildCalloutStanza(Important)
  val expectedYourCallCalloutStanza: CalloutStanza = buildCalloutStanza(YourCall)
  val expectedNumberedListItemCalloutStanza: CalloutStanza = buildCalloutStanza(NumListItem)
  val expectedNumberedCircleListItemCalloutStanza: CalloutStanza = buildCalloutStanza(NumCircListItem)
  val expectedErrorCalloutStatus: CalloutStanza = CalloutStanza(Error, ten, Seq(end), stackFalse)
  val expectedTypeErrorCalloutStatus: CalloutStanza = CalloutStanza(TypeError, ten, Seq(end), stackFalse)
  val expectedValueErrorCalloutStatus: CalloutStanza = CalloutStanza(ValueError, ten, Seq(end), stackFalse)
  val expectedNoteCalloutStanza: CalloutStanza = buildCalloutStanza(Note)

  val jsonToStanzaMappings: Map[JsValue, CalloutStanza] = Map(
    titleCalloutStanzaInputJson -> expectedTitleCalloutStanza,
    subTitleCalloutStanzaInputJson -> expectedSubTitleCalloutStanza,
    ledeCalloutStanzaInputJson -> expectedLedeCalloutStanza,
    errorCalloutStanzaInputJson -> expectedErrorCalloutStanza,
    typeErrorCalloutStanzaInputJson -> expectedTypeErrorCalloutStanza,
    valueErrorCalloutStanzaInputJson -> expectedValueErrorCalloutStanza,
    sectionCalloutStanzaInputJson -> expectedSectionCalloutStanza,
    subSectionCalloutStanzaInputJson -> expectedSubSectionCalloutStanza,
    importantCalloutStanzaInputJson -> expectedImportantCalloutStanza,
    yourCallCalloutStanzaInputJson -> expectedYourCallCalloutStanza,
    numberedListCalloutStanzaInputJson -> expectedNumberedListItemCalloutStanza,
    numberedCircleListCalloutStanzaInputJson -> expectedNumberedCircleListItemCalloutStanza,
    oldNumberedListCalloutStanzaInputJson -> expectedNumberedListItemCalloutStanza,
    oldNumberedCircleListCalloutStanzaInputJson -> expectedNumberedCircleListItemCalloutStanza,
    noteCalloutStanzaInputJson -> expectedNoteCalloutStanza
  )

  jsonToStanzaMappings foreach { mapping =>
    val (json, expectedStanza) = mapping
    val jsonNoteType = (json \ "noteType").as[String]

    s"Reading valid JSON for a $jsonNoteType Callout" should {
      s"create a ${expectedStanza.noteType} Callout Stanza" in {
        val calloutStanza: CalloutStanza = json.as[CalloutStanza]
        calloutStanza shouldBe expectedStanza
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

  "Creating a populated callout from a CalloutStanza and Phrase" should {
    "Create TitleCallout from Title noteType" in {
      Callout(expectedTitleCalloutStanza, models.ocelot.Phrase()) match {
        case _: TitleCallout => succeed
        case _ => fail
      }
    }

    "Create SubTitleCallout from SubTitle noteType" in {
      Callout(expectedSubTitleCalloutStanza, models.ocelot.Phrase()) match {
        case _: SubTitleCallout => succeed
        case _ => fail
      }
    }

    "Create SectionCallout from Section noteType" in {
      Callout(expectedSectionCalloutStanza, models.ocelot.Phrase()) match {
        case _: SectionCallout => succeed
        case _ => fail
      }
    }

    "Create SubSectionCallout from SubSection noteType" in {
      Callout(expectedSubSectionCalloutStanza, models.ocelot.Phrase()) match {
        case _: SubSectionCallout => succeed
        case _ => fail
      }
    }

    "Create LedeCallout from Lede noteType" in {
      Callout(expectedLedeCalloutStanza, models.ocelot.Phrase()) match {
        case _: LedeCallout => succeed
        case _ => fail
      }
    }

    "Create ErrorCallout from Error noteType" in {
      Callout(expectedErrorCalloutStanza, models.ocelot.Phrase()) match {
        case _: ErrorCallout => succeed
        case _ => fail
      }
    }

    "Create ValueErrorCallout from ValueError noteType" in {
      Callout(expectedValueErrorCalloutStanza, models.ocelot.Phrase()) match {
        case _: ValueErrorCallout => succeed
        case _ => fail
      }
    }

    "Create TypeErrorCallout from TypeError noteType" in {
      Callout(expectedTypeErrorCalloutStanza, models.ocelot.Phrase()) match {
        case _: TypeErrorCallout => succeed
        case _ => fail
      }
    }

    "Create ImportantCallout from Important noteType" in {
      Callout(expectedImportantCalloutStanza, models.ocelot.Phrase()) match {
        case _: ImportantCallout => succeed
        case _ => fail
      }
    }

    "Create YourCallCallout from YourCall noteType" in {
      Callout(expectedYourCallCalloutStanza, models.ocelot.Phrase()) match {
        case _: YourCallCallout => succeed
        case _ => fail
      }
    }

    "Create NumberedListItemCallout from NumberedList noteType" in {
      Callout(expectedNumberedListItemCalloutStanza, models.ocelot.Phrase()) match {
        case _: NumberedListItemCallout => succeed
        case _ => fail
      }
    }

    "Create NumberedCircleListItemCallout from NumberedCircleList noteType" in {
      Callout(expectedNumberedCircleListItemCalloutStanza, models.ocelot.Phrase()) match {
        case _: NumberedCircleListItemCallout => succeed
        case _ => fail
      }
    }

    "Create NoteCallout from Note noteType" in {
      Callout(expectedNoteCalloutStanza, models.ocelot.Phrase()) match {
        case _: NoteCallout => succeed
        case _ => fail
      }
    }
  }

  "serialise to json with noteType Title" in {
    Json.toJson(expectedTitleCalloutStanza).toString shouldBe """{"noteType":"Title","text":0,"next":["1"],"stack":false}"""
  }

  "serialise to json noteType Title from a Stanza reference" in {
    val stanza: Stanza = expectedTitleCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"Title","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  "serialise to json with noteType SubTitle" in {
    Json.toJson(expectedSubTitleCalloutStanza).toString shouldBe """{"noteType":"SubTitle","text":0,"next":["1"],"stack":false}"""
  }

  "serialise to json noteType SubTitle from a Stanza reference" in {
    val stanza: Stanza = expectedSubTitleCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"SubTitle","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  "serialise to json with noteType Lede" in {
    Json.toJson(expectedLedeCalloutStanza).toString shouldBe """{"noteType":"Lede","text":0,"next":["1"],"stack":false}"""
  }

  "serialise to json noteType Lede from a Stanza reference" in {
    val stanza: Stanza = expectedLedeCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"Lede","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  "serialise to json with noteType Error" in {
    Json.toJson(expectedErrorCalloutStatus).toString shouldBe """{"noteType":"Error","text":10,"next":["end"],"stack":false}"""
  }

  "serialise to json with noteType TypeError" in {
    Json.toJson(expectedTypeErrorCalloutStatus).toString shouldBe """{"noteType":"TypeError","text":10,"next":["end"],"stack":false}"""
  }

  "serialise to json with noteType ValueError" in {
    Json.toJson(expectedValueErrorCalloutStatus).toString shouldBe """{"noteType":"ValueError","text":10,"next":["end"],"stack":false}"""
  }

  "serialise to json noteType Error from a Stanza reference" in {
    val stanza: Stanza = expectedErrorCalloutStatus
    Json.toJson(stanza).toString shouldBe """{"next":["end"],"noteType":"Error","stack":false,"text":10,"type":"CalloutStanza"}"""
  }

  "serialise to json noteType NumberedList from a Stanza reference" in {
    val stanza: Stanza = expectedNumberedListItemCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"NumberedListItem","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  "serialise to json noteType NumberedCircleList from a Stanza reference" in {
    val stanza: Stanza = expectedNumberedCircleListItemCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"NumberedCircleListItem","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  "serialise to json with noteType Section" in {
    Json.toJson(expectedSectionCalloutStanza).toString shouldBe """{"noteType":"Section","text":0,"next":["1"],"stack":false}"""
  }

  "serialise to json noteType Section from a Stanza reference" in {
    val stanza: Stanza = expectedSectionCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"Section","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  "serialise to json with noteType SubSection" in {
    Json.toJson(expectedSubSectionCalloutStanza).toString shouldBe """{"noteType":"SubSection","text":0,"next":["1"],"stack":false}"""
  }

  "serialise to json noteType SubSection from a Stanza reference" in {
    val stanza: Stanza = expectedSubSectionCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"SubSection","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  "serialise to json with noteType Important" in {
    Json.toJson(expectedImportantCalloutStanza).toString shouldBe """{"noteType":"Important","text":0,"next":["1"],"stack":false}"""
  }

  "serialise to json noteType Important from a Stanza reference" in {
    val stanza: Stanza = expectedImportantCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"Important","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  "serialise to json with noteType YourCall" in {
    Json.toJson(expectedYourCallCalloutStanza).toString shouldBe """{"noteType":"YourCall","text":0,"next":["1"],"stack":false}"""
  }

  "serialise to json noteType YourCall from a Stanza reference" in {
    val stanza: Stanza = expectedYourCallCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"YourCall","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  "serialise to json noteType Note from a Stanza reference" in {
    val stanza: Stanza = expectedNoteCalloutStanza
    Json.toJson(stanza).toString shouldBe """{"next":["1"],"noteType":"Note","stack":false,"text":0,"type":"CalloutStanza"}"""
  }

  /** Test for missing properties in Json object representing instruction stanzas */
  missingJsObjectAttrTests[CalloutStanza](validCalloutStanzaAsJsObject, List("type"))

  /** Test for properties of the wrong type in json object representing instruction stanzas */
  incorrectPropertyTypeJsObjectAttrTests[CalloutStanza](validCalloutStanzaAsJsObject, List("type"))

}
