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

import models.{ApprovalProcess, ApprovalProcessMeta}
import play.api.libs.json._

object ApprovalProcessFormatter {

  implicit val metaFormatter: Format[ApprovalProcessMeta] = ApprovalProcessMetaFormatter.mongoFormat

  implicit val read: JsValue => JsResult[ApprovalProcess] = json =>
    for {
      id <- (json \ "_id").validateOpt[String]
      meta <- (json \ "meta").validate[ApprovalProcessMeta]
      process <- (json \ "process").validate[JsObject]
      version <- (json \ "version").validateOpt[Int]
    } yield ApprovalProcess(id.getOrElse(meta.id), meta, process, version.getOrElse(1))

  implicit val write: ApprovalProcess => JsObject = approvalProcess =>
    Json.obj(
      "_id" -> approvalProcess.id,
      "meta" -> approvalProcess.meta,
      "process" -> Json.toJson(approvalProcess.process),
      "version" -> approvalProcess.version
    )
  implicit val mongoFormat: OFormat[ApprovalProcess] = OFormat(read, write)
}
