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

import java.time.{LocalDate, LocalDateTime}

import play.api.libs.json.{Json, OFormat}
import utils.Constants._

case class ApprovalProcessMeta(
    id: String,
    title: String,
    status: String = StatusSubmitted,
    dateSubmitted: LocalDate = LocalDate.now(),
    lastModified: LocalDateTime = LocalDateTime.now(),
    ocelotDateSubmitted: Long = 1,
    ocelotVersion: Int = 1,
    reviewType: String = ReviewType2i
)

object ApprovalProcessMeta {
  implicit val formats: OFormat[ApprovalProcessMeta] = Json.format[ApprovalProcessMeta]
}
