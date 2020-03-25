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

package repositories.formatters

import java.util.UUID

import base.UnitSpec
import models.ScratchProcess
import play.api.libs.json.{JsSuccess, Json}

class ScratchProcessFormatterSpec extends UnitSpec {

  private val id = "3475e5c5-343d-4214-9efc-58270867214c"
  private val process = ScratchProcess(UUID.fromString(id), Json.obj())

  private val json = Json.parse(
    s"""
       |{
       | "_id": "$id",
       | "process": {}
       |}
       |""".stripMargin
  )

  "Formatting a valid JSON payload to a ScratchProcess" should {
    "result in a successful conversion" in {
      json.validate[ScratchProcess](ScratchProcessFormatter.mongoFormat) match {
        case JsSuccess(result, _) if result == process => succeed
        case JsSuccess(_, _) => fail("JSON parsed with incorrect values")
        case _ => fail("Unable to parse valid JSON")
      }
    }
  }

  "Serialising a ScratchProcess to JSON" should {
    "result in the correct JSON" in {
      val result = Json.toJson(process)(ScratchProcessFormatter.mongoFormat)
      result shouldBe json
    }
  }
}
