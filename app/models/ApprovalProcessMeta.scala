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

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

case class ApprovalProcessMeta(id: String, title: String, status: String, dateSubmitted: DateTime)

object ApprovalProcessMeta {
  implicit val dateFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats

  implicit val reads: Reads[ApprovalProcessMeta] = (
    (__ \ "id").read[String] and
      (__ \ "title").read[String] and
      (__ \ "status").read[String] and
      (__ \ "dateSubmitted").read(ReactiveMongoFormats.dateTimeRead)
    )(ApprovalProcessMeta.apply _)

  implicit val writes: Writes[ApprovalProcessMeta] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "title").write[String] and
      (JsPath \ "status").write[String] and
      (JsPath \ "dateSubmitted").write(ReactiveMongoFormats.dateTimeWrite)
    )(unlift(ApprovalProcessMeta.unapply))
}
