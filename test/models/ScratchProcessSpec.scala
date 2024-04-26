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

import java.util.UUID

import base.BaseSpec
import play.api.libs.json.{JsSuccess, Json}
import java.time.{ZonedDateTime, Instant}
import core.models.MongoDateTimeFormats.localZoneID
import play.api.libs.json.JsError

class ScratchProcessSpec extends BaseSpec {

  private val id = "3475e5c5-343d-4214-9efc-58270867214c"
  val milliseconds: Long = 1586450476247L
  val when: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), localZoneID)
  private val process = ScratchProcess(UUID.fromString(id), Json.obj(), when)

  private val json = Json.parse("""{"_id":{"$binary":{"base64":"NHXlxTQ9QhSe/FgnCGchTA==","subType":"04"}},"process":{},"expireAt":{"$date":{"$numberLong":"1586450476247"}}}""")

  "Formatting a valid JSON payload to a ScratchProcess" should {
    "result in a successful conversion" in {
      json.validate[ScratchProcess](ScratchProcess.mongoFormat) match {
        case JsSuccess(result, _) if result == process => succeed
        case JsSuccess(result, _) =>
          fail("JSON parsed with incorrect values")
        case JsError(errs) =>
          fail(s"Unable to parse valid JSON, $errs")
      }
    }
  }

  "Serialising a ScratchProcess to JSON" should {
    "result in the correct JSON" in {
      val result = Json.toJson(process)(ScratchProcess.mongoFormat)
      result shouldBe json
    }
  }
}
