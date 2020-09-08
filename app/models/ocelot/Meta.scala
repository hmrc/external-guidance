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

package models.ocelot

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Meta(id: String,
                title: String,
                ocelot: Int,
                lastAuthor: String,
                lastUpdate: Long,
                version: Int,
                fileName: String,
                titlePhrase: Option[Int] = None,
                processCode: String)

object Meta {

  implicit val metaReads: Reads[Meta] = (
    (__ \ "id").read[String] and
      (__ \ "title").read[String] and
      (__ \ "ocelot").read[Int] and
      (__ \ "lastAuthor").read[String] and
      (__ \ "lastUpdate").read[Long] and
      (__ \ "version").read[Int] and
      (__ \ "filename").read[String] and
      (__ \ "titlePhrase").readNullable[Int] and
      (__ \ "processCode").read[String]
  )(Meta.apply _)
}
