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

package models.ocelot

import base.UnitSpec
import play.api.libs.json._

class LinkSpec extends UnitSpec {

  val id1 = 0
  val dest1 = "http://www.bbc.co.uk/news"
  val title1 = "BBC News"
  val id2 = 1
  val dest2 = "http://gov.uk"
  val title2 = "GOV"
  val window = false
  val leftbar = false
  val always = false
  val popUp = false

  val linkStr1: String =
    s"""
       |{
       |   "id": ${id1},
       |   "dest": "${dest1}",
       |   "title": "${title1}",
       |   "window": ${window},
       |   "leftbar": ${leftbar},
       |   "always": ${always},
       |   "popup": ${popUp}
       |}
    """.stripMargin

  val linkStr2: String =
    s"""
       |{
       |   "id": ${id2},
       |   "dest": "${dest2}",
       |   "title": "${title2}",
       |   "window": ${window},
       |   "leftbar": ${leftbar},
       |   "always": ${always},
       |   "popup": ${popUp}
       |}
    """.stripMargin

  val link1Json = Json.parse(linkStr1).as[JsObject]
  val link2Json = Json.parse(linkStr2).as[JsObject]

  val linksStr: String = s"""[${linkStr1}, ${linkStr2}]"""

  val link1: Link = Link(id1, dest1, title1, window)
  val link2: Link = Link(id2, dest2, title2, window)

  "Link" must {
    "deserialise from json" in {

      link1Json.as[Link] shouldBe link1

      link2Json.as[Link] shouldBe link2
    }

    missingJsObjectAttrTests[Link](link1Json, List("popup", "always", "leftbar"))
  }

  "Sequence of multiple links" must {

    "deserialise from links section json" in {

      Json.parse(linksStr).as[Vector[Link]] shouldBe Vector(link1, link2)

    }

  }

}
