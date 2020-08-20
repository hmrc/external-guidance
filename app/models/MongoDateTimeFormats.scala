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

import java.time.{Instant, LocalDate, ZonedDateTime}

import play.api.libs.json._

trait MongoDateTimeFormats {

  val localZoneID = ZonedDateTime.now.getZone

  implicit val localDateRead: Reads[LocalDate] =
    (__ \ "$date").read[Long].map { millis => Instant.ofEpochMilli(millis).atZone(localZoneID).toLocalDate}

  implicit val localDateWrite: Writes[LocalDate] = (localDate: LocalDate) =>
    Json.obj("$date" -> localDate.atStartOfDay(localZoneID).toInstant.toEpochMilli)

  implicit val localDateFormats: Format[LocalDate] = Format(localDateRead, localDateWrite)

  implicit val zonedDateTimeRead: Reads[ZonedDateTime] =
    (__ \ "$date").read[Long].map { millis => Instant.ofEpochMilli(millis).atZone(localZoneID)}

  implicit val zonedDateTimeWrite: Writes[ZonedDateTime] = (zonedDateTime: ZonedDateTime) =>
    Json.obj("$date" -> zonedDateTime.toInstant.toEpochMilli)

  implicit val zonedDateTimeFormats: Format[ZonedDateTime] = Format(zonedDateTimeRead, zonedDateTimeWrite)

}

object MongoDateTimeFormats extends MongoDateTimeFormats
