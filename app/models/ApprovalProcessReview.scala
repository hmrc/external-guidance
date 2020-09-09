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

import java.time.{LocalDate, ZonedDateTime}
import java.util.UUID

case class ApprovalProcessReview(
    id: UUID,
    ocelotId: String,
    version: Int,
    reviewType: String,
    title: String,
    pages: List[ApprovalProcessPageReview],
    lastUpdated: LocalDate = LocalDate.now(),
    result: String = "",
    completionDate: Option[ZonedDateTime] = Option.empty[ZonedDateTime],
    completionUser: Option[String] = Option.empty[String]
)
