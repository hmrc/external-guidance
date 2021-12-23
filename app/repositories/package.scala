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

import play.api.libs.json.{JsResultException, Json}
import org.mongodb.scala._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._

package object repositories {
  // val TimescalesInUseQuery =Json.obj(
  //                             "$nor" -> Json.arr(
  //                               Json.obj(
  //                                 "$jsonSchema" -> Json.obj(
  //                                   "properties" -> Json.obj(
  //                                     "process.timescales" -> Json.obj(
  //                                       "type" -> "object",
  //                                       "properties" -> Json.obj(),
  //                                       "additionalProperties" -> false
  //                                     )
  //                                   )
  //                                 )
  //                               )
  //                             )
  //                           )

  val TimescalesInUseQuery = nor(
    jsonSchema(Document(""""{properties": {"process.timescales": {"type": "object", "properties": {"additionalProperties": false}}}}"""))
  )

  // { "$nor"}
  def hasDupeKeyViolation(ex: JsResultException): Boolean = (for {
    validationErrors <- ex.errors.flatMap(_._2)
    message <- validationErrors.messages
    dupeKey = message.matches(".*code=11000[^\\w\\d].*")
  } yield dupeKey).contains(true)
}
