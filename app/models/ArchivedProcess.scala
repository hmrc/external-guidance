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

import java.time.ZonedDateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ArchivedProcess(id: Long, dateArchived: ZonedDateTime, process: JsObject, archivedBy: String, processCode: String)

trait ArchivedProcessFormats {
  val standardformat: Format[ArchivedProcess] = Json.format[ArchivedProcess]

  import core.models.MongoDateTimeFormats.Implicits._

  val reads: Reads[ArchivedProcess] = (
    (__ \ "_id").read[Long] and
      (__ \ "dateArchived").read[ZonedDateTime] and
      (__ \ "process").read[JsObject] and
      (__ \ "archivedBy").read[String] and
      (__ \ "processCode").read[String]
  )(ArchivedProcess.apply _)

  val writes: OWrites[ArchivedProcess] = (
    (__ \ "_id").write[Long] and
      (__ \ "dateArchived").write[ZonedDateTime] and
      (__ \ "process").write[JsObject] and
      (__ \ "archivedBy").write[String] and
      (__ \ "processCode").write[String]
  )(unlift(ArchivedProcess.unapply))

  val mongoFormat: Format[ArchivedProcess] = Format(reads, writes)

  trait Implicits {
    implicit val ppformats: Format[ArchivedProcess] = standardformat
  }

  trait MongoImplicits {
    implicit val formats: Format[ArchivedProcess] = mongoFormat
  }

  object Implicits extends Implicits
  object MongoImplicits extends MongoImplicits
}

object ArchivedProcess extends ArchivedProcessFormats
