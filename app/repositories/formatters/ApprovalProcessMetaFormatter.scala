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

import models.ApprovalProcessMeta
import org.joda.time.DateTime
import play.api.libs.json._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

object ApprovalProcessMetaFormatter {

  implicit val dateFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats

  val read: JsValue => JsResult[ApprovalProcessMeta] = json =>
    for {
      id <- (json \ "id").validate[String]
      status <- (json \ "status").validate[String]
      title <- (json \ "title").validate[String]
      dateSubmitted <- (json \ "dateSubmitted").validate[DateTime](dateFormat)
    } yield ApprovalProcessMeta(id, title, status, dateSubmitted)

  val write: ApprovalProcessMeta => JsObject = meta =>
    Json.obj(
      "id" -> meta.id,
      "status" -> meta.status,
      "title" -> meta.title,
      "dateSubmitted" -> meta.dateSubmitted
    )

  val mongoFormat: OFormat[ApprovalProcessMeta] = OFormat(read, write)
}
