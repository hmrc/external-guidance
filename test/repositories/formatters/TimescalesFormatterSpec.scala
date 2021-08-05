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

import base.BaseSpec
import play.api.libs.json.{JsSuccess, Json}
import java.time.{ZonedDateTime, Instant}
import core.models.MongoDateTimeFormats
import repositories.Timescales

class TimescalesFormatterSpec extends BaseSpec with MongoDateTimeFormats {

  val milliseconds: Long = 1586450476247L
  val when: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), localZoneID)
  private val timescales: Timescales = Timescales("1", Json.obj(), when)

  private val json = Json.parse("""{"_id":"1","timescales":{},"when":{"$date":1586450476247}}""")

  "Formatting a valid JSON payload to a Timescales" should {
    "result in a successful conversion" in {
      json.validate[Timescales](TimescalesFormatter.mongoFormat) match {
        case JsSuccess(result, _) if result == timescales => succeed
        case JsSuccess(_, _) => fail("JSON parsed with incorrect values")
        case _ => fail("Unable to parse valid JSON")
      }
    }
  }

  "Serialising a Timescales to JSON" should {
    "result in the correct JSON" in {
      val result = Json.toJson(timescales)(TimescalesFormatter.mongoFormat)
      result shouldBe json
    }
  }
}
