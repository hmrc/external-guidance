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

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import play.api.libs.json.Json

package object repositories {
  val json: String = Json.obj("properties" -> Json.obj(
                                "process.timescales" -> Json.obj(
                                  "type" -> "object",
                                  "properties" -> Json.obj(),
                                  "additionalProperties" -> false
                                )
                              )).toString
  val TimescalesInUseQuery = nor(jsonSchema(Document(json)))
}
