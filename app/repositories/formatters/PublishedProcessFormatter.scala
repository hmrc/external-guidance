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

package repositories.formatters

import java.time.LocalDateTime

import models.PublishedProcess
import play.api.libs.json._

object PublishedProcessFormatter {

  val read: JsValue => JsResult[PublishedProcess] = json =>
    for {
      id <- (json \ "_id").validate[String]
      version <- (json \ "version").validate[Int]
      datePublished <- (json \ "datePublished").validate[LocalDateTime]
      process <- (json \ "process").validate[JsObject]
    } yield PublishedProcess(id, version, datePublished, process)

  val write: PublishedProcess => JsObject = publishedProcess =>
    Json.obj(
      "_id" -> publishedProcess.id,
      "version" -> publishedProcess.version,
      "datePublished" -> publishedProcess.datePublished,
      "process" -> publishedProcess.process
    )

  implicit val mongoFormat: OFormat[PublishedProcess] = OFormat(read, write)
}
