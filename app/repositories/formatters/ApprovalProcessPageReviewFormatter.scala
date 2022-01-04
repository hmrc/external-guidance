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

package repositories.formatters

import java.time.ZonedDateTime
//import core.models.MongoDateTimeFormats
import models.ApprovalProcessPageReview
import play.api.libs.json._

object ApprovalProcessPageReviewFormatter {

  //implicit val dateTimeFormat: Format[ZonedDateTime] = MongoDateTimeFormats.zonedDateTimeFormats

  val read: JsValue => JsResult[ApprovalProcessPageReview] = json =>
    for {
      id <- (json \ "id").validate[String]
      pageUrl <- (json \ "pageUrl").validate[String]
      pageTitle <- (json \ "pageTitle").validate[String]
      result <- (json \ "result").validateOpt[String]
      status <- (json \ "status").validate[String]
      comment <- (json \ "comment").validateOpt[String]
      updateDate <- (json \ "updateDate").validate[ZonedDateTime]
      updateUser <- (json \ "updateUser").validateOpt[String]
    } yield ApprovalProcessPageReview(id, pageUrl, pageTitle, result, status, comment, updateDate, updateUser)

  val write: ApprovalProcessPageReview => JsObject = review =>
    Json.obj(
      "id" -> review.id,
      "pageUrl" -> review.pageUrl,
      "pageTitle" -> review.pageTitle,
      "result" -> review.result,
      "status" -> review.status,
      "comment" -> review.comment,
      "updateDate" -> review.updateDate,
      "updateUser" -> review.updateUser
    )

  implicit val mongoFormat: OFormat[ApprovalProcessPageReview] = OFormat(read, write)
}
