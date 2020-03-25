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

import base.UnitSpec
import play.api.libs.json._

class ValueStanzaSpec extends UnitSpec {

  val stanzaType = "ValueStanza"
  val valueType = "scalar"
  val pageNameLabel = "PageName"
  val pageName = "Telling HMRC about extra income"
  val pageUrlLabel = "PageUrl"
  val pageUrl = "/rent/less-than-1000/do-you-want-to-use-the-rent-a-room-scheme"
  val next = "40"
  val stack = "false"

  val validValueStanzaJson: JsObject = Json
    .parse(
      s"""{
      |  "type": "${stanzaType}",
      |  "values": [
      |    {
      |      "type": "${valueType}",
      |      "label": "${pageNameLabel}",
      |      "value": "${pageName}"
      |    },
      |    {
      |      "type": "${valueType}",
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

  val invalidValueStanzaJson: JsObject = Json
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
      |      "type": "${valueType}",
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

  "ValueStanza" must {

    "deserialise from json" in {

      val stanza: ValueStanza = validValueStanzaJson.as[ValueStanza]

      stanza.stack shouldBe false
      stanza.next.length shouldBe 1
      stanza.next(0) shouldBe next
      stanza.values.length shouldBe 2
      stanza.values(0) shouldBe Value(Scalar, pageNameLabel, pageName)
      stanza.values(1) shouldBe Value(Scalar, pageUrlLabel, pageUrl)
    }

    "fail to parse if an unkown value type is found" in {
      invalidValueStanzaJson.as[JsObject].validate[ValueStanza] match {
        case JsSuccess(_, _) => fail(s"Value objects must be of valid type")
        case JsError(_) => succeed
      }
    }

    "contain at least one Value object" in {
      validValueStanzaJson.as[ValueStanza].values.length should be > 0
    }

    missingJsObjectAttrTests[ValueStanza](validValueStanzaJson, List("type"))

  }

}
