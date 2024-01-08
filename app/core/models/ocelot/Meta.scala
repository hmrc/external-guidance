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

package core.models.ocelot

import java.time.ZonedDateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Meta(id: String,
                title: String,
                passPhrase: Option[String],
                encryptedPassPhrase: Option[String],
                ocelot: Int,
                lastAuthor: String,
                lastUpdate: Long,
                version: Int,
                fileName: Option[String],
                titlePhrase: Option[Int] = None,
                processCode: String,
                timescalesVersion: Option[Long] = None,
                ratesVersion: Option[Long] = None)

object Meta {

  def buildMetaSection(id: String,
                title: String,
                passPhrase: Option[String],
                encryptedPassPhrase: Option[String],
                ocelot: Int,
                lastAuthor: Option[String],
                lastUpdate: Option[Long],
                optionalVersion: Option[Int],
                fileName: Option[String],
                titlePhrase: Option[Int] = None,
                processCode: String,
                timescalesVersion: Option[Long],
                ratesVersion: Option[Long]): Meta =
    Meta(id,
         title,
         passPhrase,
         encryptedPassPhrase,
         ocelot,
         lastAuthor.getOrElse(""),
         lastUpdate.getOrElse(ZonedDateTime.now.toInstant.toEpochMilli),
         optionalVersion.getOrElse(1),
         fileName,
         titlePhrase,
         processCode,
         timescalesVersion,
         ratesVersion
       )

  implicit val metaReads: Reads[Meta] = (
    (__ \ "id").read[String] and
      (__ \ "title").read[String] and
      (__ \ "passPhrase").readNullable[String] and
      (__ \ "encryptedPassPhrase").readNullable[String] and
      (__ \ "ocelot").read[Int] and
      (__ \ "lastAuthor").readNullable[String] and
      (__ \ "lastUpdate").readNullable[Long] and
      (__ \ "version").readNullable[Int] and
      (__ \ "filename").readNullable[String] and
      (__ \ "titlePhrase").readNullable[Int] and
      (__ \ "processCode").read[String] and
      (__ \ "timescalesVersion").readNullable[Long] and
      (__ \ "ratesVersion").readNullable[Long]
  )(buildMetaSection _)

  implicit val writes: Writes[Meta] = (
    (__ \ "id").write[String] and
      (__ \ "title").write[String] and
      (__ \ "passPhrase").writeNullable[String] and
      (__ \ "encryptedPassPhrase").writeNullable[String] and
      (__ \ "ocelot").write[Int] and
      (__ \ "lastAuthor").write[String] and
      (__ \ "lastUpdate").write[Long] and
      (__ \ "version").write[Int] and
      (__ \ "filename").writeNullable[String] and
      (__ \ "titlePhrase").writeNullable[Int] and
      (__ \ "processCode").write[String] and
      (__ \ "timescalesVersion").writeNullable[Long] and
      (__ \ "ratesVersion").writeNullable[Long]
  )(unlift(Meta.unapply))
}
