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

package models

import java.time.ZonedDateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class PublishedProcess(id: String, version: Int, datePublished: ZonedDateTime, process: JsObject, publishedBy: String, processCode: String)
case class PublishedSummary(id: String, datePublished: ZonedDateTime, processCode: String, publishedBy: String)

trait PublishedProcessFormats {
  val standardformat: Format[PublishedProcess] = Json.format[PublishedProcess]

  import core.models.MongoDateTimeFormats.Implicits._

  val reads: Reads[PublishedProcess] = (
    (__ \ "_id").read[String] and
      (__ \ "version").read[Int] and
      (__ \ "datePublished").read[ZonedDateTime] and
      (__ \ "process").read[JsObject] and
      (__ \ "publishedBy").read[String] and
      (__ \ "processCode").read[String]
  )(PublishedProcess.apply _)

  val writes: OWrites[PublishedProcess] = (
    (__ \ "_id").write[String] and
      (__ \ "version").write[Int] and
      (__ \ "datePublished").write[ZonedDateTime] and
      (__ \ "process").write[JsObject] and
      (__ \ "publishedBy").write[String] and
      (__ \ "processCode").write[String]
  )(unlift(PublishedProcess.unapply))

  val mongoFormat: Format[PublishedProcess] = Format(reads, writes)

  trait Implicits {
    implicit val ppformats: Format[PublishedProcess] = standardformat
  }

  trait MongoImplicits {
    implicit val formats: Format[PublishedProcess] = mongoFormat
  }

  object Implicits extends Implicits
  object MongoImplicits extends MongoImplicits
}

object PublishedProcess extends PublishedProcessFormats

object PublishedSummary {
  implicit val formats: OFormat[PublishedSummary] = Json.format[PublishedSummary]
}
