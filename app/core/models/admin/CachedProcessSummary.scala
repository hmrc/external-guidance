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

package core.models.admin

import play.api.libs.json.{Format, Json}
import java.time.Instant

case class CachedProcessSummary(
  id: String,
  processVersion: Long,
  timescalesVersion: Option[Long],
  ratesVersion: Option[Long],
  title: String,
  expiryTime: Instant
)
object CachedProcessSummary {
  implicit lazy val formats: Format[CachedProcessSummary] = Json.format[CachedProcessSummary]
}
