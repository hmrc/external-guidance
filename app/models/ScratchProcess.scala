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

package models

import java.util.UUID
import java.time.ZonedDateTime
import play.api.libs.json.JsObject
import java.util.UUID
import java.time.ZonedDateTime
import play.api.libs.json.{Reads, OWrites, JsObject, Format, __}
import play.api.libs.functional.syntax._
import core.models.MongoDateTimeFormats.Implicits._
import uk.gov.hmrc.mongo.play.json.formats.MongoUuidFormats.Implicits.uuidFormat

case class ScratchProcess(id: UUID, process: JsObject, expireAt: ZonedDateTime)

object ScratchProcess {

  val reads: Reads[ScratchProcess] = (
    (__ \ "_id").read[UUID] and
      (__ \ "process").read[JsObject] and
      (__ \ "expireAt").read[ZonedDateTime]
  )(ScratchProcess.apply _)

  val writes: OWrites[ScratchProcess] = (
    (__ \ "_id").write[UUID] and
      (__ \ "process").write[JsObject] and
      (__ \ "expireAt").write[ZonedDateTime]
  )(unlift(ScratchProcess.unapply))

  val mongoFormat: Format[ScratchProcess] = Format(reads, writes)
}

