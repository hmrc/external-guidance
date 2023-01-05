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

import java.time.ZonedDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OFormat, OWrites, Reads, __}

case class ApprovalProcessPageReview(
    id: String,
    pageUrl: String,
    pageTitle: String,
    result: Option[String] = None,
    status: String = Constants.InitialPageReviewStatus,
    comment: Option[String] = None,
    updateDate: ZonedDateTime = ZonedDateTime.now(),
    updateUser: Option[String] = None
)

object ApprovalProcessPageReview {
  implicit val httpFormat: OFormat[ApprovalProcessPageReview] = Json.format[ApprovalProcessPageReview]

  import core.models.MongoDateTimeFormats.Implicits._

  val reads: Reads[ApprovalProcessPageReview] = (
    (__ \ "id").read[String] and
      (__ \ "pageUrl").read[String] and
      (__ \ "pageTitle").read[String] and
      (__ \ "result").readNullable[String] and
      (__ \ "status").read[String] and
      (__ \ "comment").readNullable[String] and
      (__ \ "updateDate").read[ZonedDateTime] and
      (__ \ "updateUser").readNullable[String]
  )(ApprovalProcessPageReview.apply _)

  val writes: OWrites[ApprovalProcessPageReview] = (
    (__ \ "id").write[String] and
      (__ \ "pageUrl").write[String] and
      (__ \ "pageTitle").write[String] and
      (__ \ "result").writeNullable[String] and
      (__ \ "status").write[String] and
      (__ \ "comment").writeNullable[String] and
      (__ \ "updateDate").write[ZonedDateTime] and
      (__ \ "updateUser").writeNullable[String]
  )(unlift(ApprovalProcessPageReview.unapply))

  val mongoFormat: OFormat[ApprovalProcessPageReview] = OFormat(reads, writes)
}
