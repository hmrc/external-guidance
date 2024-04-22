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

package migrate.models

import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ApprovalProcess(id: String, meta: ApprovalProcessMeta, process: JsObject, version: Int = 1)

object ApprovalProcess {

  implicit val metaFormat: Format[ApprovalProcessMeta] = ApprovalProcessMeta.mongoFormat

  def build(id: Option[String], meta: ApprovalProcessMeta, process: JsObject, version: Option[Int]): ApprovalProcess =
    ApprovalProcess(id.getOrElse(meta.id), meta, process, version.getOrElse(1))

  val reads: Reads[ApprovalProcess] = (
    (__ \ "_id").readNullable[String] and
      (__ \ "meta").read[ApprovalProcessMeta] and
      (__ \ "process").read[JsObject] and
      (__ \ "version").readNullable[Int]
  )(ApprovalProcess.build _)

  val writes: OWrites[ApprovalProcess] = (
    (__ \ "_id").write[String] and
      (__ \ "meta").write[ApprovalProcessMeta] and
      (__ \ "process").write[JsObject] and
      (__ \ "version").write[Int]
  )(unlift(ApprovalProcess.unapply))

  implicit val mongoFormat: OFormat[ApprovalProcess] = OFormat(reads, writes)
}
