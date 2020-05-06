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

package models

import java.time.{Instant, LocalDate, ZoneOffset}

import play.api.libs.json._

trait MongoDateTimeFormats {

  implicit val localDateRead: Reads[LocalDate] =
    (__ \ "$date").read[Long].map { millis =>
      Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate
    }

  implicit val localDateWrite: Writes[LocalDate] = (localDate: LocalDate) =>
    Json.obj(
      "$date" -> localDate.atStartOfDay(ZoneOffset.UTC).toInstant.toEpochMilli
    )

  implicit val localDateFormats: Format[LocalDate] = Format(localDateRead, localDateWrite)

}
object MongoDateTimeFormats extends MongoDateTimeFormats
