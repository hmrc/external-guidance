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

import java.time._
import base.BaseSpec
import play.api.libs.json._

class LabelledDataSpec extends BaseSpec {

  val dataJson: JsValue = Json.parse("""{"JRSRefCB" : 15}""")
  val when: Instant = ZonedDateTime.of(2024, 6,21,2,38,0,0, ZoneId.of("UTC")).toInstant()
  val data = LabelledData(Timescales, dataJson, when, "12345566", "Someone", "Someone@blah.com")

  val labelledDataJsonString =
  """
  |  {
  |      "_id": "Timescales",
  |      "data":
  |      {
  |          "JRSRefCB": 15
  |      },
  |      "when":
  |      {
  |          "$date":
  |          {
  |              "$numberLong": "1718937480000"
  |          }
  |      },
  |      "credId": "12345566",
  |      "user": "Someone",
  |      "email": "Someone@blah.com"
  |  }
  """.stripMargin
  val labelledDataJson = Json.parse(labelledDataJsonString)
  val invalidlabelledDataJsonString =
  """
  |  {
  |      "_id": "Blah",
  |      "data":
  |      {
  |          "JRSRefCB": 15
  |      },
  |      "when":
  |      {
  |          "$date":
  |          {
  |              "$numberLong": "1718937480000"
  |          }
  |      },
  |      "credId": "12345566",
  |      "user": "Someone",
  |      "email": "Someone@blah.com"
  |  }
  """.stripMargin
  val invalidLabelledDataJson = Json.parse(invalidlabelledDataJsonString)
  private val invalidJson = Json.parse("{}")

  "Deserializing a JSON payload into an instance of LabelledData" should {

    "Result in a successful conversion for valid JSON" in {
      labelledDataJson.validate[LabelledData] match {
        case JsSuccess(result, _) if result.id == Timescales => succeed
        case JsSuccess(_, _) => fail("Deserializing valid JSON did not create correct process")
        case JsError(err) => fail(s"Unable to parse valid Json: $err")
      }
    }

    "Result in a failure when for invalid JSON" in {

      invalidJson.validate[LabelledData] match {
        case e: JsError => succeed
        case _ => fail("Invalid JSON payload should not have been successfully deserialized")
      }
    }

    "Result in a failure when for valid JSON within invalid labelled data id" in {

      invalidLabelledDataJson.validate[LabelledData] match {
        case e: JsError => succeed
        case _ => fail("Invalid JSON payload should not have been successfully deserialized")
      }
    }

  }

  "Serializing an approval process into JSON" should {

    "Generate the expected JSON" in {
      val result: JsObject = Json.toJson(data).as[JsObject]
      result("_id") shouldBe JsString("Timescales")
    }
  }

}
