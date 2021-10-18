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

package models

import base.BaseSpec
import play.api.libs.json.{JsSuccess, Json}
import java.time.{ZonedDateTime, Instant}

class TimescalesResponseSpec extends BaseSpec {
  val credId: String = "1234566789"
  val user: String = "User Blah"
  val email: String = "user@blah.com"
  val milliseconds: Long = 1586450476247L
  val when: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZonedDateTime.now.getZone)

  private val update: UpdateDetails = UpdateDetails(when, credId, user, email)
  private val updateJson = Json.parse("""{"when":"2020-04-09T17:41:16.247+01:00[Europe/London]","credId":"1234566789","user":"User Blah","email":"user@blah.com"}""")
  private val responseJson = Json.parse("""{"count":0,"lastUpdate":{"when":"2020-04-09T17:41:16.247+01:00[Europe/London]","credId":"1234566789","user":"User Blah","email":"user@blah.com"},"retainedDeletions":[]}""")
  private val responseWithRetentionsJson = Json.parse("""{"count":0,"lastUpdate":{"when":"2020-04-09T17:41:16.247+01:00[Europe/London]","credId":"1234566789","user":"User Blah","email":"user@blah.com"},"retainedDeletions":["First"]}""")

  val response: TimescalesResponse = TimescalesResponse(0, Some(update), Nil)
  val responseWithRetentions: TimescalesResponse = TimescalesResponse(0, Some(update), List("First"))

  "Formatting a valid UpdateDetails" should {
    "result in a successful conversion" in {
      updateJson.validate[UpdateDetails](UpdateDetails.format) match {
        case JsSuccess(result, _) if result == update => succeed
        case JsSuccess(_, _) => fail("JSON parsed with incorrect values")
        case err => fail(s"Unable to parse valid JSON: $err")
      }
    }
  }

  "Serialising an UpdateDetails to JSON" should {
    "result in the correct JSON" in {
      val result = Json.toJson(update)(UpdateDetails.format)
      result shouldBe updateJson
    }
  }

  "Formatting a valid TimescalesResponse" should {
    "result in a successful conversion" in {
      responseJson.validate[TimescalesResponse](TimescalesResponse.format) match {
        case JsSuccess(result, _) if result == response => succeed
        case JsSuccess(_, _) => fail("JSON parsed with incorrect values")
        case err => fail(s"Unable to parse valid JSON: $err")
      }
    }

    "result in a successful conversion when retentions included" in {
      responseWithRetentionsJson.validate[TimescalesResponse](TimescalesResponse.format) match {
        case JsSuccess(result, _) if result == responseWithRetentions => succeed
        case JsSuccess(_, _) => fail("JSON parsed with incorrect values")
        case err => fail(s"Unable to parse valid JSON: $err")
      }
    }

  }

  "Serialising an TimescalesResponse to JSON" should {
    "result in the correct JSON" in {
      val result = Json.toJson(response)(TimescalesResponse.format)
      result shouldBe responseJson
    }

    "result in the correct JSON whenn retentions included" in {
      val result = Json.toJson(responseWithRetentions)(TimescalesResponse.format)
      result shouldBe responseWithRetentionsJson
    }

  }

}
