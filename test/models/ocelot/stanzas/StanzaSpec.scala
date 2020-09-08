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

class StanzaSpec extends UnitSpec {

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

  "Stanza" must {

    "deserialise from EndStanza json" in {

      val stanza: Stanza = Json.parse("""{ "type": "EndStanza" }""").as[Stanza]

      stanza shouldBe EndStanza
    }

    "serialise EndStanza to json from a Stanza reference" in {
      val stanza: Stanza = EndStanza
      val expectedJson: String = """{"type":"EndStanza"}"""
      Json.toJson(stanza).toString shouldBe expectedJson
    }

    "generate an error when an unknown stanza type is encountered" in {

      val jsObject: JsObject = Json.parse("""{ "type": "UnknownStanzaType" }""").as[JsObject]

      jsObject.validate[Stanza] match {
        case JsSuccess(_, _) => fail(s"Stanza incorrectly created when unknown stanza type encountered")
        case JsError(_) => succeed
      }
    }

    "deserialise ValueStanza json in " in {

      val stanza: Stanza = validValueStanzaJson.as[Stanza]

      stanza shouldBe ValueStanza(List(Value(Scalar, pageNameLabel, pageName), Value(Scalar, pageUrlLabel, pageUrl)), Seq(next), false)
    }

  }
}
