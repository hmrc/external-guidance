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

package core.models

//import java.time.{Instant, LocalDate, ZonedDateTime, ZoneId, ZoneOffset}
import java.time.{ZonedDateTime, ZoneOffset, Instant, LocalDate}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import play.api.libs.json._

trait MongoDateTimeFormats extends MongoJavatimeFormats {
  outer =>
  val localZoneID = ZonedDateTime.now.getZone
  // implicit val instantReads: Reads[Instant] = (js: JsValue) =>
  //   (js \ "$date" \ "$numberLong").validate[String] match {
  //     case err @ JsError(_) =>
  //       // Fall back to try and read date in Mongo Json v1 format
  //       (js \ "$date").validate[String] match {
  //         case err2 @ JsError(_) => err2
  //         case res2 @ JsSuccess(dt, pth) => JsSuccess(Instant.ofEpochMilli(dt.toLong), pth)
  //       }
  //     case JsSuccess(dt, pth) => JsSuccess(Instant.ofEpochMilli(dt.toLong), pth)
  //     }

  // final val instantWrites: Writes[Instant] =
  //   Writes.at[String](__ \ "$date" \ "$numberLong")
  //     .contramap(_.toEpochMilli.toString)

  // final val instantFormat: Format[Instant] =
  //   Format(instantReads, instantWrites)

  // implicit val zonedDateTimeReads: Reads[ZonedDateTime] = (js: JsValue) =>
  //   (js \ "$date" \ "$numberLong").validate[String] match {
  //     case err @ JsError(_) =>
  //       // Fall back to try and read date in Mongo Json v1 format
  //       (js \ "$date").validate[String] match {
  //         case err2 @ JsError(_) => err2
  //         case res2 @ JsSuccess(dt, pth) => JsSuccess(Instant.ofEpochMilli(dt.toLong).atZone(ZoneId.systemDefault), pth)
  //       }
  //     case JsSuccess(dt, pth) => JsSuccess(Instant.ofEpochMilli(dt.toLong).atZone(ZoneId.systemDefault), pth)
  //     }

  // final val zonedDateTimeWrites: Writes[ZonedDateTime] =
  //   Writes.at[String](__ \ "$date" \ "$numberLong")
  //     .contramap(_.toInstant.toEpochMilli.toString)

  // implicit val localDateTimeReads: Reads[LocalDate] =
  //   Reads.at[Instant](__).map(_.atZone(ZoneOffset.UTC).toLocalDate)

  // final val localDateTimeWrites: Writes[LocalDate] =
  //   Writes.at[Instant](__).contramap(_.atStartOfDay(ZoneOffset.UTC).toInstant)

  // final val localDateTimeFormat: Format[LocalDate] =
  //   Format(localDateTimeReads, localDateTimeWrites)

  // final val zonedDateTimeReads: Reads[ZonedDateTime] =  (js: JsValue) =>
  //   (js \ "$date" \ "$numberLong").validate[String] match {
  //     case err @ JsError(_) =>
  //       // Fall back to try and read date in Mongo Json v1 format
  //       (js \ "$date").validate[String] match {
  //         case err2 @ JsError(_) =>
  //           println(s"Failing from zonedDateTimeReads")
  //           err2
  //         case res2 @ JsSuccess(dt, pth) => JsSuccess(Instant.ofEpochMilli(dt.toLong).atZone(ZoneId.systemDefault), pth)
  //       }
  //     case JsSuccess(dt, pth) => JsSuccess(Instant.ofEpochMilli(dt.toLong).atZone(ZoneId.systemDefault), pth)
  //     }
  final val zonedDateTimeReads: Reads[ZonedDateTime] =
    Reads.at[String](__ \ "$date" \ "$numberLong")
      .map(dateTime => Instant.ofEpochMilli(dateTime.toLong).atZone(localZoneID))

  final val zonedDateTimeWrites: Writes[ZonedDateTime] =
    Writes.at[String](__ \ "$date" \ "$numberLong")
      .contramap(_.toInstant.toEpochMilli.toString)

  final val zonedDateTimeFormat: Format[ZonedDateTime] =
    Format(zonedDateTimeReads, zonedDateTimeWrites)

  final val tolerantLocalDateReads: Reads[LocalDate] = (js: JsValue) =>
    (js \ "$date" \ "$numberLong").validate[String] match {
      case err @ JsError(_) =>
        // Fall back to try and read date as string
        (js).validate[String] match {
          case err2 @ JsError(_) => err2
          case res2 @ JsSuccess(dt, pth) => JsSuccess(LocalDate.parse(dt), pth)
        }
      case JsSuccess(dt, pth) => JsSuccess(Instant.ofEpochMilli(dt.toLong).atZone(ZoneOffset.UTC).toLocalDate, pth)
      }

  final val tolerantLocalDateFormat: Format[LocalDate] =
    Format(tolerantLocalDateReads, localDateWrites)


  trait MongoImplicits {
    implicit val mdInstantFormat: Format[Instant] = outer.instantFormat
    implicit val mdLocalDateFormat: Format[LocalDate] = outer.tolerantLocalDateFormat
    implicit val mdZonedDateTimeFormat: Format[ZonedDateTime] = outer.zonedDateTimeFormat
  }

  object MongoImplicits extends MongoImplicits
}

object MongoDateTimeFormats extends MongoDateTimeFormats
