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

package repositories.formatters

import java.time.ZonedDateTime

import base.BaseSpec
import models.{MongoDateTimeFormats, PublishedProcess}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import repositories.formatters.PublishedProcessFormatter.mongoFormat

class PublishedProcessFormatterSpec extends BaseSpec with MongoDateTimeFormats {

  private val process: JsObject = Json.obj()
  private val id: String = "ext90002"

  private val datePublished: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, localZoneID)
  private val publishedProcess: PublishedProcess = PublishedProcess(id, 1, datePublished, process, "user", processCode = "processCode")

  private val json = Json.parse(
    s"""
       |{
       | "_id": "$id",
       | "version": 1,
       | "datePublished": {"$$date": ${datePublished.toInstant.toEpochMilli}},
       | "process": {},
       | "publishedBy": "user",
       | "processCode" : "processCode"
       |}
       |""".stripMargin
  )

  private val invalidJson = Json.parse("{}")

  "Deserializing a JSON payload into an instance of PublishedProcess" should {

    "Result in a successful conversion for valid JSON" in {

      json.validate[PublishedProcess] match {
        case JsSuccess(result, _) if result == publishedProcess => succeed
        case JsSuccess(_, _) => fail("Deserializing valid JSON did not create correct process")
        case _ => fail("Unable to parse valid Json")
      }
    }

    "Result in a failure when for invalid JSON" in {

      invalidJson.validate[PublishedProcess] match {
        case e: JsError => succeed
        case _ => fail("Invalid JSON payload should not have been successfully deserialized")
      }
    }

  }

  "Serializing a published process into JSON" should {

    "Generate the expected JSON" in {
      val result = Json.toJson(publishedProcess)(PublishedProcessFormatter.mongoFormat)
      result shouldBe json
    }
  }

}
