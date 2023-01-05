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

import core.models.ocelot.LabelCache
import base.BaseSpec
import play.api.libs.json._

import core.models.ocelot.{Label, Labels, LabelCache, ListLabel, ScalarLabel}

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
  val emptyListLabel = "empty"
  val emptyListValue = ""
  val listLengthLabel = "ListLength"
  val singleEntryListLabel = "single"
  val singleEntryListValue = "July"
  val copiedScalarLabel = "copiedScalarLabel"
  val scalarLabelReference = "[label:reference]"
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

  val validValueStanzaJsonWithLabelNameSpaces: JsObject = Json
    .parse(
      s"""{
      |  "type": "${stanzaType}",
      |  "values": [
      |    {
      |      "type": "${scalarType}",
      |      "label": "${pageNameLabel}     ",
      |      "value": "${pageName}"
      |    },
      |    {
      |      "type": "${scalarType}",
      |      "label": "${pageUrlLabel} ",
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

  val validValueStanzaWithMultipleListsListJson: JsObject = Json
    .parse(
      s"""{
         |  "type": "${stanzaType}",
         |  "values": [
         |    {
         |      "type": "${listType}",
         |      "label": "${emptyListLabel}",
         |      "value": "${emptyListValue}"
         |    },
         |    {
         |      "type": "${listType}",
         |      "label": "${singleEntryListLabel}",
         |      "value": "${singleEntryListValue}"
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

  val validValueStanzaWithListLengthValueJson: JsObject = Json
    .parse(
      s"""{
         |  "type": "${stanzaType}",
         |  "values": [
         |    {
         |      "type": "${listType}",
         |      "label": "${singleEntryListLabel}",
         |      "value": "${singleEntryListValue}"
         |    },
         |    {
         |      "type": "${listType}",
         |      "label": "${listLabel}",
         |      "value": "${listValue}"
         |    },
         |    {
         |      "type": "${scalarType}",
         |      "label": "${listLengthLabel}",
         |      "value": "[list:${listLabel}:length]"
         |    }
         |  ],
         |  "next": ["${next}"],
         |  "stack": ${stack}
         |}
    """.stripMargin
    )
    .as[JsObject]

  val validValueStanzaWithScalarLabelRefJson: JsObject = Json
    .parse(
      s"""{
         |  "type": "${stanzaType}",
         |  "values": [
         |    {
         |      "type": "${scalarType}",
         |      "label": "${copiedScalarLabel}",
         |      "value": "${scalarLabelReference}"
         |    }
         |  ],
         |  "next": ["${next}"],
         |  "stack": ${stack}
         |}
    """.stripMargin
    )
    .as[JsObject]

  val validValueStanzaWithListLabelRefJson: JsObject = Json
    .parse(
      s"""{
         |  "type": "${stanzaType}",
         |  "values": [
         |    {
         |      "type": "${listType}",
         |      "label": "${copiedScalarLabel}",
         |      "value": "${scalarLabelReference}"
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
      val stanza: ValueStanza = ValueStanza(List(Value(ScalarType, "LabelName", "/")), Seq("4"), stack = true)
      val expectedJson: String = """{"values":[{"type":"scalar","label":"LabelName","value":"/"}],"next":["4"],"stack":true}"""
      val json: String = Json.toJson(stanza).toString
      json shouldBe expectedJson
    }

    "serialize scalar label to json from a Stanza reference" in {
      val stanza: Stanza = ValueStanza(List(Value(ScalarType, "LabelName", "/")), Seq("4"), stack = true)
      val expectedJson: String = """{"type":"ValueStanza","values":[{"type":"scalar","label":"LabelName","value":"/"}],"next":["4"],"stack":true}"""
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
        s"""{"type":"ValueStanza","values":[{"type":"$listType","label":"$listLabel","value":"$listValue"}],"next":["2"],"stack":false}"""

      val json: String = Json.toJson(stanza).toString

      json shouldBe expectedJson

    }

    "define a list of the labels in a deserialized value stanza" in {

      val stanza: ValueStanza = validValueStanzaWithMixedValueTypesJson.as[ValueStanza]

      stanza.labels shouldBe List(pageNameLabel, listLabel)
    }

    "correctly evaluate scalar labels" in {

      val stanza: ValueStanza = validValueStanzaJson.as[ValueStanza]

      val labels = LabelCache()

      val (nextStanza, updatedLabels, err) = stanza.eval(labels)

      nextStanza shouldBe next

      updatedLabels.value(pageNameLabel) shouldBe Some(pageName)
      updatedLabels.value(pageUrlLabel) shouldBe Some(pageUrl)
    }

    "correctly evaluate a reference to a scalar label" in {

      val stanza: ValueStanza = validValueStanzaWithScalarLabelRefJson.as[ValueStanza]

      val labelMap: Map[String, Label] = Map(
        "reference" -> ScalarLabel("reference", List("Some Data"))
      )

      val labels: Labels = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = stanza.eval(labels)

      nextStanza shouldBe next

      updatedLabels.value(copiedScalarLabel) shouldBe Some("Some Data")
    }

    "correctly evaluate a reference to a non-existent scalar label" in {

      val stanza: ValueStanza = validValueStanzaWithScalarLabelRefJson.as[ValueStanza]

      val labelMap: Map[String, Label] = Map(
        "anotherReference" -> ScalarLabel("anotherReference", List("Some Data"))
      )

      val labels: Labels = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = stanza.eval(labels)

      nextStanza shouldBe next

      updatedLabels.value(copiedScalarLabel) shouldBe Some("")
    }

    "correctly evaluate list labels" in {

      val stanza: ValueStanza = validValueStanzaWithMultipleListsListJson.as[ValueStanza]

      val labels = LabelCache()

      val (nextStanza, updatedLabels, err) = stanza.eval(labels)

      nextStanza shouldBe next

      updatedLabels.valueAsList(emptyListLabel) shouldBe Some(Nil)
      updatedLabels.valueAsList(singleEntryListLabel) shouldBe Some(List(singleEntryListValue))
      updatedLabels.valueAsList(listLabel) shouldBe Some(List("March", "April", "May"))
    }

    "correctly evaluate a reference to a list label" in {

      val stanza: ValueStanza = validValueStanzaWithListLabelRefJson.as[ValueStanza]

      val labelMap: Map[String, Label] = Map(
        "reference" -> ListLabel("reference", List("Some Data", "Some more data"))
      )

      val labels: Labels = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = stanza.eval(labels)

      nextStanza shouldBe next

      updatedLabels.valueAsList(copiedScalarLabel) shouldBe Some(List("Some Data", "Some more data"))
    }

    "correctly evaluate a reference to a non-existent list label" in {

      val stanza: ValueStanza = validValueStanzaWithListLabelRefJson.as[ValueStanza]

      val labelMap: Map[String, Label] = Map(
        "anotherReference" -> ListLabel("anotherReference", List("Some Data", "Some more data"))
      )

      val labels: Labels = LabelCache(labelMap)

      val (nextStanza, updatedLabels, err) = stanza.eval(labels)

      nextStanza shouldBe next

      updatedLabels.valueAsList(copiedScalarLabel) shouldBe Some(Nil)
    }

    "correctly evaluate list length placeholder" in {

      val stanza: ValueStanza = validValueStanzaWithListLengthValueJson.as[ValueStanza]

      val labels = LabelCache()

      val (nextStanza, updatedLabels, err) = stanza.eval(labels)

      nextStanza shouldBe next

      updatedLabels.valueAsList(singleEntryListLabel) shouldBe Some(List(singleEntryListValue))
      updatedLabels.valueAsList(listLabel) shouldBe Some(List("March", "April", "May"))
      updatedLabels.value(listLengthLabel) shouldBe Some("3")
    }


    missingJsObjectAttrTests[ValueStanza](validValueStanzaJson, List("type"))

  }

}
