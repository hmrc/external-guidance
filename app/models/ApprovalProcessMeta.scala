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

import java.time.{LocalDate, ZonedDateTime}
import core.models.MongoDateTimeFormats.Implicits._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.Constants._

case class ApprovalProcessMeta(
  id: String,
  title: String,
  status: String = StatusSubmitted,
  dateSubmitted: LocalDate = LocalDate.now(),
  lastModified: ZonedDateTime = ZonedDateTime.now(),
  ocelotDateSubmitted: Long = 1,
  ocelotVersion: Int = 1,
  reviewType: String = ReviewType2i,
  processCode: String
)

object ApprovalProcessMeta {
  def build(
    id: String,
    title: String,
    status: String,
    dateSubmitted: LocalDate,
    lastModified: Option[ZonedDateTime],
    ocelotDateSubmitted: Option[Long],
    ocelotVersion: Option[Int],
    reviewType: String,
    processCode: String
  ): ApprovalProcessMeta =
  ApprovalProcessMeta(
    id,
    title,
    status,
    dateSubmitted,
    lastModified.getOrElse(ZonedDateTime.now()),
    ocelotDateSubmitted.getOrElse(1L),
    ocelotVersion.getOrElse(1),
    reviewType,
    processCode
  )

  val reads: Reads[ApprovalProcessMeta] = (
    (__ \ "id").read[String] and
      (__ \ "title").read[String] and
      (__ \ "status").read[String] and
      (__ \ "dateSubmitted").read[LocalDate] and
      (__ \ "lastModified").readNullable[ZonedDateTime] and
      (__ \ "ocelotDateSubmitted").readNullable[Long] and
      (__ \ "ocelotVersion").readNullable[Int] and
      (__ \ "reviewType").read[String] and
      (__ \ "processCode").read[String]
  )(ApprovalProcessMeta.build _)

  val writes: OWrites[ApprovalProcessMeta] = (
    (__ \ "id").write[String] and
      (__ \ "title").write[String] and
      (__ \ "status").write[String] and
      (__ \ "dateSubmitted").write[LocalDate] and
      (__ \ "lastModified").write[ZonedDateTime] and
      (__ \ "ocelotDateSubmitted").write[Long] and
      (__ \ "ocelotVersion").write[Int] and
      (__ \ "reviewType").write[String] and
      (__ \ "processCode").write[String]
  )(unlift(ApprovalProcessMeta.unapply))

  implicit val mongoFormat: OFormat[ApprovalProcessMeta] = OFormat(reads, writes)
}
