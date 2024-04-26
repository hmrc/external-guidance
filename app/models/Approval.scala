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

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Approval(id: String, meta: ApprovalProcessMeta, review: ApprovalReview, process: JsObject, version: Int = 1)

object Approval {

  implicit val metaFormat: Format[ApprovalProcessMeta] = ApprovalProcessMeta.mongoFormat
  implicit val reviewFormat: Format[ApprovalReview] = ApprovalReview.format

  def build(id: Option[String], meta: ApprovalProcessMeta, review: ApprovalReview, process: JsObject, version: Option[Int]): Approval =
    Approval(id.getOrElse(meta.id), meta, review, process, version.getOrElse(1))

  val reads: Reads[Approval] = (
    (__ \ "_id").readNullable[String] and
      (__ \ "meta").read[ApprovalProcessMeta] and
      (__ \ "review").read[ApprovalReview] and
      (__ \ "process").read[JsObject] and
      (__ \ "version").readNullable[Int]
  )(Approval.build _)

  val writes: OWrites[Approval] = (
    (__ \ "_id").write[String] and
      (__ \ "meta").write[ApprovalProcessMeta] and
      (__ \ "review").write[ApprovalReview] and
      (__ \ "process").write[JsObject] and
      (__ \ "version").write[Int]
  )(unlift(Approval.unapply))

  implicit val format: OFormat[Approval] = OFormat(reads, writes)
}
