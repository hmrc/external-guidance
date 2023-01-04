/*
 * Copyright 2023 HM Revenue & Customs
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
import java.util.UUID
import play.api.libs.json._
import play.api.libs.functional.syntax._
import core.models.MongoDateTimeFormats.Implicits._

case class ApprovalProcessReview(
  id: UUID,
  ocelotId: String,
  version: Int,
  reviewType: String,
  title: String,
  pages: List[ApprovalProcessPageReview],
  lastUpdated: LocalDate = LocalDate.now(),
  result: Option[String] = None,
  completionDate: Option[ZonedDateTime] = Option.empty[ZonedDateTime],
  completionUser: Option[String] = Option.empty[String]
)

object ApprovalProcessReview {

  implicit val pageReviewFormat = ApprovalProcessPageReview.mongoFormat

  implicit val reads: Reads[ApprovalProcessReview] = (
    (__ \ "_id").read[UUID] and
      (__ \ "ocelotId").read[String] and
      (__ \ "version").read[Int] and
      (__ \ "reviewType").read[String] and
      (__ \ "title").read[String] and
      (__ \ "pages").read[List[ApprovalProcessPageReview]] and
      (__ \ "lastUpdated").read[LocalDate] and
      (__ \ "result").readNullable[String] and
      (__ \ "completionDate").readNullable[ZonedDateTime] and
      (__ \ "completionUser").readNullable[String]
  )(ApprovalProcessReview.apply _)

  implicit val writes: OWrites[ApprovalProcessReview] = (
    (__ \ "_id").write[UUID] and
      (__ \ "ocelotId").write[String] and
      (__ \ "version").write[Int] and
      (__ \ "reviewType").write[String] and
      (__ \ "title").write[String] and
      (__ \ "pages").write[List[ApprovalProcessPageReview]] and
      (__ \ "lastUpdated").write[LocalDate] and
      (__ \ "result").writeNullable[String] and
      (__ \ "completionDate").writeNullable[ZonedDateTime] and
      (__ \ "completionUser").writeNullable[String]
  )(unlift(ApprovalProcessReview.unapply))

  implicit val mongoFormat: OFormat[ApprovalProcessReview] = OFormat(reads, writes)
}
