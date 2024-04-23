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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class PublishedProcessMeta(
  id: String,
  title: String,
  ocelot: Int = 1,
  lastAuthor: String,
  lastUpdate: Long,
  version: Int = 1,
  filename: String,
  processCode: String
)

object PublishedProcessMeta {
  def build(
    id: String,
    title: String,
    ocelot: Int,
    lastAuthor: String,
    lastUpdate: Long,
    version: Option[Int],
    filename: String,
    processCode: String
  ): PublishedProcessMeta =
  PublishedProcessMeta(
    id,
    title,
    ocelot,
    lastAuthor,
    lastUpdate,
    version.getOrElse(1),
    filename,
    processCode
  )

  val reads: Reads[PublishedProcessMeta] = (
    (__ \ "id").read[String] and
      (__ \ "title").read[String] and
      (__ \ "ocelot").read[Int] and
      (__ \ "lastAuthor").read[String] and
      (__ \ "lastUpdate").read[Long] and
      (__ \ "version").readNullable[Int] and
      (__ \ "filename").read[String] and
      (__ \ "processCode").read[String]
  )(PublishedProcessMeta.build _)

  val writes: OWrites[PublishedProcessMeta] = (
    (__ \ "id").write[String] and
      (__ \ "title").write[String] and
      (__ \ "ocelot").write[Int] and
      (__ \ "lastAuthor").write[String] and
      (__ \ "lastUpdate").write[Long] and
      (__ \ "version").write[Int] and
      (__ \ "filename").write[String] and
      (__ \ "processCode").write[String]
  )(unlift(PublishedProcessMeta.unapply))

  implicit val mongoFormat: OFormat[PublishedProcessMeta] = OFormat(reads, writes)
}