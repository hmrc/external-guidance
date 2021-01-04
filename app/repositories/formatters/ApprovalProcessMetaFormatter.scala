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

import java.time.{LocalDate, ZonedDateTime}

import models.{ApprovalProcessMeta, MongoDateTimeFormats}
import play.api.libs.json._

object ApprovalProcessMetaFormatter {

  implicit val dateFormat: Format[LocalDate] = MongoDateTimeFormats.localDateFormats
  implicit val dateTimeFormat: Format[ZonedDateTime] = MongoDateTimeFormats.zonedDateTimeFormats

  val read: JsValue => JsResult[ApprovalProcessMeta] = json =>
    for {
      id <- (json \ "id").validate[String]
      status <- (json \ "status").validate[String]
      title <- (json \ "title").validate[String]
      dateSubmitted <- (json \ "dateSubmitted").validate[LocalDate]
      lastModified <- (json \ "lastModified").validateOpt[ZonedDateTime]
      ocelotDateSubmitted <- (json \ "ocelotDateSubmitted").validateOpt[Long]
      ocelotVersion <- (json \ "ocelotVersion").validateOpt[Int]
      reviewType <- (json \ "reviewType").validate[String]
      processCode <- (json \ "processCode").validate[String]
    } yield ApprovalProcessMeta(
      id,
      title,
      status,
      dateSubmitted,
      lastModified.getOrElse(ZonedDateTime.now()),
      ocelotDateSubmitted.getOrElse(1),
      ocelotVersion.getOrElse(1),
      reviewType,
      processCode
    )

  val write: ApprovalProcessMeta => JsObject = meta =>
    Json.obj(
      "id" -> meta.id,
      "status" -> meta.status,
      "title" -> meta.title,
      "dateSubmitted" -> meta.dateSubmitted,
      "lastModified" -> meta.lastModified,
      "ocelotDateSubmitted" -> meta.ocelotDateSubmitted,
      "ocelotVersion" -> meta.ocelotVersion,
      "reviewType" -> meta.reviewType,
      "processCode" -> meta.processCode
    )

  implicit val mongoFormat: OFormat[ApprovalProcessMeta] = OFormat(read, write)
}
