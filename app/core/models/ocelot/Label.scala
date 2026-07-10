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

package core.models.ocelot

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

sealed trait Label {
  val name: String
  val english: List[String]
  val welsh: List[String]
}

case class ScalarLabel(name: String, english: List[String] = Nil, welsh: List[String] = Nil) extends Label
case class ListLabel(name: String, english: List[String] = Nil, welsh: List[String] = Nil) extends Label

object Label {

  given reads: Reads[Label] = (js: JsValue) =>
    (js \ "type").validate[String] match {
      case error @ JsError(_) => error
      case JsSuccess(typ, _) => typ match {
        case "scalar" => js.validate[ScalarLabel]
        case "list" => js.validate[ListLabel]
        case unknownType => JsError(JsonValidationError(Seq("Label"), unknownType))
      }
    }

  given writes: Writes[Label] = {
    case s: ScalarLabel => Json.obj("type" -> "scalar") ++ Json.toJsObject[ScalarLabel](s)
    case l: ListLabel => Json.obj("type" -> "list") ++ Json.toJsObject[ListLabel](l)
  }
}

object ScalarLabel {

  given reads: Reads[ScalarLabel] = (
    (__ \ "name").read[String] and
      (__ \ "english").read[List[String]] and
      (__ \ "welsh").read[List[String]]
  )(ScalarLabel.apply _)

  given owrites: OWrites[ScalarLabel] = (
    (__ \ "name").write[String] and
      (__ \ "english").write[List[String]] and
      (__ \ "welsh").write[List[String]]
  )(Tuple.fromProductTyped(_))
}

object ListLabel {

  given reads: Reads[ListLabel] = (
    (__ \ "name").read[String] and
      (__ \ "english").read[List[String]] and
      (__ \ "welsh").read[List[String]]
  )(ListLabel.apply _)

  given owrites: OWrites[ListLabel] = (
    (__ \ "name").write[String] and
      (__ \ "english").write[List[String]] and
      (__ \ "welsh").write[List[String]]
  )(Tuple.fromProductTyped(_))

}
