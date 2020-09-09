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

import base.BaseSpec
import play.api.libs.json._

class MetaSpec extends BaseSpec {

  val title = "Title"
  val id = "abc90001"
  val ocelotVersion = 1
  val author = "1234567"
  val lastUpdate = 1500298931016L
  val version = 2
  val filename = s"$id.js"
  val titlePhrase: Int = 0
  val processCode = "processIdentifierCode"

  val validJsonAsString = s"""
                             |{
                             |   "id": "$id",
                             |   "title": "$title",
                             |   "ocelot": $ocelotVersion,
                             |   "lastAuthor": "$author",
                             |   "lastUpdate": $lastUpdate,
                             |   "version": $version,
                             |   "filename": "$filename"
                             |}
                             |""".stripMargin

  val validJson: JsObject = Json.parse(validJsonAsString).as[JsObject]

  val validJsonWithOptionalPropertiesAsString = s"""
                                                   |{
                                                   |   "id": "$id",
                                                   |   "title": "$title",
                                                   |   "ocelot": $ocelotVersion,
                                                   |   "lastAuthor": "$author",
                                                   |   "lastUpdate": $lastUpdate,
                                                   |   "version": $version,
                                                   |   "filename": "$filename",
                                                   |   "titlePhrase": $titlePhrase,
                                                   |   "processCode": "$processCode"
                                                   |}
                                                   |""".stripMargin

  val validJsonWithOptionalProperties: JsObject = Json.parse(validJsonWithOptionalPropertiesAsString).as[JsObject]

  val validModel: Meta = Meta(id, title, ocelotVersion, author, lastUpdate, version, filename)

  val validModelWithOptionalProperties: Meta = Meta(
    id, title, ocelotVersion, author, lastUpdate, version, filename, Some(titlePhrase), Some(processCode) )

  "Meta section" must {

    "deserialise correctly" in {
      val result: Meta = validJson.as[Meta]
      result shouldBe validModel
    }

    "deserialise JSON representation with optional fields correctly" in {
      val result: Meta = validJsonWithOptionalProperties.as[Meta]
      result shouldBe validModelWithOptionalProperties
    }

    missingJsObjectAttrTests[Meta](validJsonWithOptionalProperties, List("titlePhrase", "processCode"))

    "serialise JSON representation of instance of class Meta" in {
      Json.toJson(validModel).toString() shouldBe removeSpacesAndNewLines(validJsonAsString)
    }

    "serialize JSON representation of instance of class Meta with optional fields" in {
      Json.toJson(validModelWithOptionalProperties).toString() shouldBe removeSpacesAndNewLines(validJsonWithOptionalPropertiesAsString)
    }
  }

}
