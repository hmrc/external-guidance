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
import play.api.libs.json.{__, _}
import play.api.libs.functional.syntax._
import core.models.MongoDateTimeFormats.Implicits._

case class LabelledData(labelledData: JsValue, when: ZonedDateTime, credId: String, user: String, email: String)

object LabelledData {

  implicit val reads: Reads[LabelledData] =
    ((__ \ "timescales").read[JsValue] and
      (__ \ "when").read[ZonedDateTime] and
      (__ \ "credId").read[String] and
      (__ \ "user").read[String] and
      (__ \ "email").read[String])(LabelledData.apply _)

  implicit val writes: OWrites[LabelledData] =
    ((__ \ "timescales").write[JsValue] and
        (__ \ "when").write[ZonedDateTime] and
        (__ \ "credId").write[String] and
        (__ \ "user").write[String] and
        (__ \ "email").write[String]
    )(unlift(LabelledData.unapply))

  val format: OFormat[LabelledData] = OFormat(reads, writes)
}
