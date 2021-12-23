/*
 * Copyright 2021 HM Revenue & Customs
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

import java.util.UUID
import java.time.ZonedDateTime
//import core.models.MongoDateTimeFormats
import models.ScratchProcess
import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat, Format}

object ScratchProcessFormatter {

  //implicit val dateFormat: Format[ZonedDateTime] = MongoDateTimeFormats.zonedDateTimeFormats

  val read: JsValue => JsResult[ScratchProcess] = json =>
    for {
      id <- (json \ "_id").validate[UUID]
      process <- (json \ "process").validate[JsObject]
      expireAt <- (json \ "expireAt").validate[ZonedDateTime]
    } yield ScratchProcess(id, process, expireAt)

  val write: ScratchProcess => JsObject = scratchProcess =>
    Json.obj(
      "_id" -> scratchProcess.id,
      "process" -> scratchProcess.process,
      "expireAt" -> scratchProcess.expireAt
    )

  val mongoFormat: OFormat[ScratchProcess] = OFormat(read, write)
}
