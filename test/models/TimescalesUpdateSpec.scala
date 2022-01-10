/*
 * Copyright 2022 HM Revenue & Customs
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

import base.BaseSpec
import play.api.libs.json.{JsSuccess, Json}
import java.time.{ZonedDateTime, Instant}
import core.models.MongoDateTimeFormats.localZoneID

class TimescalesUpdateSpec extends BaseSpec {
  val credId: String = "1234566789"
  val user: String = "User Blah"
  val email: String = "user@blah.com"
  val milliseconds: Long = 1586450476247L
  val when: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), localZoneID)

  private val timescales: TimescalesUpdate = TimescalesUpdate(Json.obj(), when, credId, user, email)
  private val json = Json.parse("""{"timescales":{},"when":{"$date":  {"$numberLong": "1586450476247"}},"credId":"1234566789","user":"User Blah","email":"user@blah.com"}""")

  "Formatting a valid JSON payload to a Timescales" should {
    "result in a successful conversion" in {
      json.validate[TimescalesUpdate](TimescalesUpdate.format) match {
        case JsSuccess(result, _) if result == timescales => succeed
        case JsSuccess(_, _) => fail("JSON parsed with incorrect values")
        case _ => fail("Unable to parse valid JSON")
      }
    }
  }

  "Serialising a TimescalesUpdate to JSON" should {
    "result in the correct JSON" in {
      val result = Json.toJson(timescales)(TimescalesUpdate.format)
      result shouldBe json
    }
  }
}
