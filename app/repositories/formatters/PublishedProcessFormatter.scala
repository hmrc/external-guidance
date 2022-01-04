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

package repositories.formatters

import java.time.ZonedDateTime
import core.models.MongoDateTimeFormats
import models.PublishedProcess
import play.api.libs.json._

object PublishedProcessFormatter {

  implicit val dateTimeFormat: Format[ZonedDateTime] = MongoDateTimeFormats.zonedDateTimeFormats

  val read: JsValue => JsResult[PublishedProcess] = json =>
    for {
      id <- (json \ "_id").validate[String]
      version <- (json \ "version").validate[Int]
      datePublished <- (json \ "datePublished").validate[ZonedDateTime]
      process <- (json \ "process").validate[JsObject]
      publishedBy <- (json \ "publishedBy").validate[String]
      processCode <- (json \ "processCode").validate[String]
    } yield PublishedProcess(id, version, datePublished, process, publishedBy, processCode)

  val write: PublishedProcess => JsObject = publishedProcess =>
    Json.obj(
      "_id" -> publishedProcess.id,
      "version" -> publishedProcess.version,
      "datePublished" -> publishedProcess.datePublished,
      "process" -> publishedProcess.process,
      "publishedBy" -> publishedProcess.publishedBy,
      "processCode" -> publishedProcess.processCode
    )

  implicit val mongoFormat: OFormat[PublishedProcess] = OFormat(read, write)
}
