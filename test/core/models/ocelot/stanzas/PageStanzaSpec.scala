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

class PageStanzaSpec extends BaseSpec {

  val stanzaType = "PageStanza"
  val pageUrl = "/rent/less-than-1000/do-you-want-to-use-the-rent-a-room-scheme"
  val next = "40"
  val stack = "false"

  val validPageStanzaJson: JsObject = Json
    .parse(
      s"""{
      |  "type": "${stanzaType}",
      |  "url": "${pageUrl}",
      |  "next": ["${next}"],
      |  "stack": ${stack}
      |}
    """.stripMargin
    )
    .as[JsObject]

  val invalidPageStanzaJson: JsObject = Json
    .parse(
      s"""{
      |  "type": "${stanzaType}",
      |  "next": ["${next}"],
      |  "stack": ${stack}
      |}
    """.stripMargin
    )
    .as[JsObject]

  "PageStanza" must {

    "deserialise from json" in {

      val stanza: PageStanza = validPageStanzaJson.as[PageStanza]

      stanza.stack shouldBe false
      stanza.next.length shouldBe 1
      stanza.next(0) shouldBe next
    }

    "serialise to json" in {
      val stanza: PageStanza = PageStanza("/", Seq("4"), true)
      val expectedJson: String = """{"url":"/","next":["4"],"stack":true}"""
      val json: String = Json.toJson(stanza).toString
      json shouldBe expectedJson
    }

    "serialise to json from a Stanza reference" in {
      val stanza: Stanza = PageStanza("/", Seq("4"), true)
      val expectedJson: String = """{"type":"PageStanza","url":"/","next":["4"],"stack":true}"""
      val json: String = Json.toJson(stanza).toString
      json shouldBe expectedJson
    }

    "fail to parse if an unkown value type is found" in {
      invalidPageStanzaJson.as[JsObject].validate[PageStanza] match {
        case JsSuccess(_, _) => fail(s"Value objects must be of valid type")
        case JsError(_) => succeed
      }
    }

    missingJsObjectAttrTests[PageStanza](validPageStanzaJson, List("type"))

  }

}
