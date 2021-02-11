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

import core.models.ocelot.{ListLabel, ScalarLabel}

class ValueStanzaSpec extends BaseSpec {

  val stanzaType = "ValueStanza"
  val scalarType = "scalar"
  val listType = "list"
  val pageNameLabel = "PageName"
  val pageName = "Telling HMRC about extra income"
  val pageUrlLabel = "PageUrl"
  val pageUrl = "/rent/less-than-1000/do-you-want-to-use-the-rent-a-room-scheme"
  val listLabel = "monthsInSpring"
  val listValue = "March,April,May"
  val next = "40"
  val stack = "false"

  val validValueStanzaJson: JsObject = Json
    .parse(
      s"""{
      |  "type": "${stanzaType}",
      |  "values": [
      |    {
      |      "type": "${scalarType}",
      |      "label": "${pageNameLabel}",
      |      "value": "${pageName}"
      |    },
      |    {
      |      "type": "${scalarType}",
      |      "label": "${pageUrlLabel}",
      |      "value": "${pageUrl}"
      |    }
      |  ],
      |  "next": ["${next}"],
      |  "stack": ${stack}
      |}
    """.stripMargin
    )
    .as[JsObject]

  val invalidValueStanzaWithIncorrectTypeJson: JsObject = Json
    .parse(
      s"""{
      |  "type": "${stanzaType}",
      |  "values": [
      |    {
      |      "type": "unknown",
      |      "label": "${pageNameLabel}",
      |      "value": "${pageName}"
      |    },
      |    {
      |      "type": "${scalarType}",
      |      "label": "${pageUrlLabel}",
      |      "value": "${pageUrl}"
      |    }
      |  ],
      |  "next": ["${next}"],
      |  "stack": ${stack}
      |}
    """.stripMargin
    )
    .as[JsObject]

  val invalidValueStanzaWithInappropriateTypeJson: JsObject = Json
    .parse(
      s"""{
         |  "type": "${stanzaType}",
         |  "values": [
         |    {
         |      "type": false,
         |      "label": "${pageNameLabel}",
         |      "value": "${pageName}"
         |    },
         |    {
         |      "type": "${scalarType}",
         |      "label": "${pageUrlLabel}",
         |      "value": "${pageUrl}"
         |    }
         |  ],
         |  "next": ["${next}"],
         |  "stack": ${stack}
         |}
    """.stripMargin
    )
    .as[JsObject]

  val validValueStanzaWithListJson: JsObject = Json
    .parse(
      s"""{
         |  "type": "${stanzaType}",
         |  "values": [
         |    {
         |      "type": "${listType}",
         |      "label": "${listLabel}",
         |      "value": "${listValue}"
         |    }
         |  ],
         |  "next": ["${next}"],
         |  "stack": ${stack}
         |}
    """.stripMargin
    )
    .as[JsObject]

  val validValueStanzaWithMixedValueTypesJson: JsObject = Json
    .parse(
      s"""{
         |  "type": "${stanzaType}",
         |  "values": [
         |  {
         |      "type": "${scalarType}",
         |      "label": "${pageNameLabel}",
         |      "value": "${pageName}"
         |    },
         |    {
         |      "type": "${listType}",
         |      "label": "${listLabel}",
         |      "value": "${listValue}"
         |    }
         |  ],
         |  "next": ["${next}"],
         |  "stack": ${stack}
         |}
    """.stripMargin
    )
    .as[JsObject]

  "ValueStanza" must {

    "deserialize scalar label from json" in {

      val stanza: ValueStanza = validValueStanzaJson.as[ValueStanza]

      stanza.stack shouldBe false
      stanza.next.length shouldBe 1
      stanza.next.head shouldBe next
      stanza.values.length shouldBe 2
      stanza.values.head shouldBe Value(ScalarType, pageNameLabel, pageName)
      stanza.values(1) shouldBe Value(ScalarType, pageUrlLabel, pageUrl)
    }

    "serialize scalar label to json" in {
      val stanza: ValueStanza = ValueStanza(List(Value(ScalarType, "LabelName", "/")), Seq("4"), true)
      val expectedJson: String = """{"values":[{"type":"scalar","label":"LabelName","value":"/"}],"next":["4"],"stack":true}"""
      val json: String = Json.toJson(stanza).toString
      json shouldBe expectedJson
    }

    "serialize scalar label to json from a Stanza reference" in {
      val stanza: Stanza = ValueStanza(List(Value(ScalarType, "LabelName", "/")), Seq("4"), true)
      val expectedJson: String = """{"next":["4"],"stack":true,"values":[{"type":"scalar","label":"LabelName","value":"/"}],"type":"ValueStanza"}"""
      val json: String = Json.toJson(stanza).toString
      json shouldBe expectedJson
    }

    "fail to parse if an unknown value type is found" in {
      invalidValueStanzaWithIncorrectTypeJson.as[JsObject].validate[ValueStanza] match {
        case JsSuccess(_, _) => fail(s"Value objects must be of valid type")
        case JsError(_) => succeed
      }
    }

    "fail to parse if a value type of incorrect JsValue type is found" in {
      invalidValueStanzaWithInappropriateTypeJson.as[JsObject].validate[ValueStanza] match {
        case JsSuccess(_, _) => fail(s"Value objects must be of valid type")
        case JsError(_) => succeed
      }
    }

    "contain at least one Value object" in {
      validValueStanzaJson.as[ValueStanza].values.length should be > 0
    }

    "deserialize value stanza with list from json" in {

      val valueStanza: ValueStanza = validValueStanzaWithListJson.as[ValueStanza]

      valueStanza.stack shouldBe false
      valueStanza.next.length shouldBe 1
      valueStanza.next.head shouldBe next
      valueStanza.values.length shouldBe 1
      valueStanza.values.head shouldBe Value(ListType, listLabel, listValue)
    }

    "serialize a value stanza with list to json" in {

      val valueStanza: ValueStanza = ValueStanza(
        List(Value(ListType, listLabel, listValue)),
        Seq("5"),
        stack = true
      )

      val expectedJson: String = s"""{"values":[{"type":"$listType","label":"$listLabel","value":"$listValue"}],"next":["5"],"stack":true}"""
      val json: String = Json.toJson(valueStanza).toString

      json shouldBe expectedJson
    }

    "serialize a value stanza with list referenced by stanza to json" in {

      val stanza: Stanza = ValueStanza(
        List(Value(ListType, listLabel, listValue)),
        Seq("2"),
        stack = false
      )

      val expectedJson: String =
        s"""{"next":["2"],"stack":false,"values":[{"type":"$listType","label":"$listLabel","value":"$listValue"}],"type":"ValueStanza"}"""

      val json: String = Json.toJson(stanza).toString

      json shouldBe expectedJson

    }

    "define a list of the labels in a deserialized value stanza" in {

      val stanza: ValueStanza = validValueStanzaWithMixedValueTypesJson.as[ValueStanza]

      stanza.labels shouldBe List(ScalarLabel(pageNameLabel), ListLabel(listLabel))
    }

    missingJsObjectAttrTests[ValueStanza](validValueStanzaJson, List("type"))
  }

}
