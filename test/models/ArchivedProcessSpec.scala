/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import java.time.ZonedDateTime

import base.BaseSpec
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import models.ArchivedProcess.MongoImplicits.formats
import core.models.MongoDateTimeFormats.localZoneID

class ArchivedProcessSpec extends BaseSpec {

  private val process: JsObject = Json.obj()
  private val id: Long = 1786379160058L

  private val dateArchived: ZonedDateTime = ZonedDateTime.of(2026, 8, 10, 16, 26, 0, 0, localZoneID)
  private val archivedProcess: ArchivedProcess = ArchivedProcess(id, dateArchived, process, "user", processCode = "processCode")

  private val json = Json.parse(
    s"""
       |{
       | "_id": $id,
       | "dateArchived": {"$$date": {"$$numberLong": "${dateArchived.toInstant.toEpochMilli}"}},
       | "process": {},
       | "archivedBy": "user",
       | "processCode" : "processCode"
       |}
       |""".stripMargin
  )

  private val isoStringDateJson = Json.parse(
    s"""
       |{
       | "_id": $id,
       | "dateArchived": "${dateArchived.toInstant.toString}",
       | "process": {},
       | "archivedBy": "user",
       | "processCode" : "processCode"
       |}
       |""".stripMargin
  )

  "Deserializing a JSON payload into an instance of ArchivedProcess" should {

    "Result in a successful conversion for a Mongo dateArchived ($date/$numberLong)" in {

      json.validate[ArchivedProcess] match {
        case JsSuccess(result, _) if result == archivedProcess => succeed
        case JsSuccess(_, _) => fail("Deserializing valid JSON did not create correct process")
        case e: JsError => fail(s"Unable to parse valid Json: ${JsError.toJson(e).toString}")
      }
    }

    "Result in a failure when dateArchived is an ISO-8601 string rather than a Mongo date" in {

      isoStringDateJson.validate[ArchivedProcess] match {
        case _: JsError => succeed
        case _ => fail("A non-Mongo dateArchived should not have been successfully deserialized")
      }
    }

  }

  "Serializing an archived process into JSON" should {

    "Generate the expected Mongo JSON" in {
      val result = Json.toJson(archivedProcess)(ArchivedProcess.mongoFormat)
      result shouldBe json
    }
  }

}
