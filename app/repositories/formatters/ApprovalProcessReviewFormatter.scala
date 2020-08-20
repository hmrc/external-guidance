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

package repositories.formatters

import java.time.{LocalDate, ZonedDateTime}
import java.util.UUID

import models.{ApprovalProcessPageReview, ApprovalProcessReview}
import play.api.libs.json._

object ApprovalProcessReviewFormatter {

  implicit val reviewFormatter: Format[ApprovalProcessPageReview] = ApprovalProcessPageReviewFormatter.mongoFormat

  val read: JsValue => JsResult[ApprovalProcessReview] = json =>
    for {
      id <- (json \ "_id").validateOpt[UUID]
      ocelotId <- (json \ "ocelotId").validate[String]
      version <- (json \ "version").validate[Int]
      reviewType <- (json \ "reviewType").validate[String]
      title <- (json \ "title").validate[String]
      lastUpdated <- (json \ "lastUpdated").validate[LocalDate]
      result <- (json \ "result").validate[String]
      completionDate <- (json \ "completionDate").validateOpt[ZonedDateTime]
      completionUser <- (json \ "completionUser").validateOpt[String]
      pages <- (json \ "pages").validate[List[ApprovalProcessPageReview]]
    } yield ApprovalProcessReview(
      id.getOrElse(UUID.randomUUID()),
      ocelotId,
      version,
      reviewType,
      title,
      pages,
      lastUpdated,
      result,
      completionDate,
      completionUser
    )

  val write: ApprovalProcessReview => JsObject = review =>
    Json.obj(
      "_id" -> review.id,
      "ocelotId" -> review.ocelotId,
      "version" -> review.version,
      "reviewType" -> review.reviewType,
      "title" -> review.title,
      "lastUpdated" -> review.lastUpdated,
      "result" -> review.result,
      "completionDate" -> review.completionDate,
      "completionUser" -> review.completionUser,
      "pages" -> Json.toJson(review.pages)
    )

  implicit val mongoFormat: OFormat[ApprovalProcessReview] = OFormat(read, write)
}
