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

package core.models

import java.time.{Instant, LocalDate, ZonedDateTime}
import play.api.libs.json._

trait MongoDateTimeFormats {

  val localZoneID = ZonedDateTime.now.getZone

  implicit val localDateRead: Reads[LocalDate] = (__ \ "$date").read[Long].map(mi => Instant.ofEpochMilli(mi).atZone(localZoneID).toLocalDate)
  implicit val localDateWrite: Writes[LocalDate] = (dte: LocalDate) => Json.obj("$date" -> dte.atStartOfDay(localZoneID).toInstant.toEpochMilli)
  implicit val localDateFormats: Format[LocalDate] = Format(localDateRead, localDateWrite)

  implicit val zonedDateTimeRead: Reads[ZonedDateTime] =(__ \ "$date").read[Long].map(mi => Instant.ofEpochMilli(mi).atZone(localZoneID))
  implicit val zonedDateTimeWrite: Writes[ZonedDateTime] = (zdt: ZonedDateTime) => Json.obj("$date" -> zdt.toInstant.toEpochMilli)
  implicit val zonedDateTimeFormats: Format[ZonedDateTime] = Format(zonedDateTimeRead, zonedDateTimeWrite)

  implicit val instantRead: Reads[Instant] =(__ \ "$date").read[Long].map(mi => Instant.ofEpochMilli(mi))
  implicit val instantWrite: Writes[Instant] = (i: Instant) => Json.obj("$date" -> i.toEpochMilli)
  implicit val instantFormats: Format[Instant] = Format(instantRead, instantWrite)

}

object MongoDateTimeFormats extends MongoDateTimeFormats
