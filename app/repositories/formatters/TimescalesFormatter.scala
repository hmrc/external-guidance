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

import java.time.ZonedDateTime
import core.models.MongoDateTimeFormats
import repositories.Timescales
import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat, Format}

object TimescalesFormatter {

  implicit val dateFormat: Format[ZonedDateTime] = MongoDateTimeFormats.zonedDateTimeFormats

  val read: JsValue => JsResult[Timescales] = json =>
    for {
      id <- (json \ "_id").validate[String]
      timescales <- (json \ "timescales").validate[JsValue]
      when <- (json \ "when").validate[ZonedDateTime]
    } yield Timescales(id, timescales, when)

  val write: Timescales => JsObject = timescales =>
    Json.obj(
      "_id" -> timescales.id,
      "timescales" -> timescales.timescales,
      "when" -> timescales.when
    )

  val mongoFormat: OFormat[Timescales] = OFormat(read, write)
}
