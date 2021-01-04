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

import java.time.ZonedDateTime

import base.BaseSpec
import play.api.libs.json.Json

class MongoDateTimeSpec extends  BaseSpec with MongoDateTimeFormats {

  "a ZonedDateTime" must {

    val dateTime = ZonedDateTime.of(2018,2,1,13,45,0,0, localZoneID)

    val dateMillis = dateTime.toInstant.toEpochMilli

    val json = Json.obj(
      "$date" -> dateMillis
    )

    "must serialise to json" in {
      val result = Json.toJson(dateTime)
      result shouldBe json
    }

    "must deserialise from json" in {
      val result = json.as[ZonedDateTime]
      result shouldBe dateTime
    }

  }
}
