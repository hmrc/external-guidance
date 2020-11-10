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

package models.ocelot.stanzas

import play.api.libs.json._

sealed trait CalloutType

trait Heading

// Headings
case object Title extends CalloutType with Heading
case object SubTitle extends CalloutType with Heading
case object Section extends CalloutType with Heading
case object SubSection extends CalloutType with Heading

// Errors
case object Error extends CalloutType
case object ValueError extends CalloutType
case object TypeError extends CalloutType

case object Lede extends CalloutType
case object Important extends CalloutType
case object YourCall extends CalloutType


object CalloutType {

  implicit val reads: Reads[CalloutType] = {
    case JsString("Title") => JsSuccess(Title, __)
    case JsString("SubTitle") => JsSuccess(SubTitle, __)
    case JsString("Section") => JsSuccess(Section, __)
    case JsString("SubSection") => JsSuccess(SubSection, __)
    case JsString("Lede") => JsSuccess(Lede, __)
    case JsString("Error") => JsSuccess(Error, __)
    case JsString("ValueError") => JsSuccess(ValueError, __)
    case JsString("TypeError") => JsSuccess(TypeError, __)
    case JsString("Important") => JsSuccess(Important, __)
    case JsString("YourCall") => JsSuccess(YourCall, __)
    case typeName: JsString => JsError(JsonValidationError(Seq("CalloutType"), typeName.value))
    case unknown => JsError(JsonValidationError(Seq("CalloutType"), unknown.toString))
  }

  implicit val writes: Writes[CalloutType] = {
    case Title => Json.toJson("Title")
    case SubTitle => Json.toJson("SubTitle")
    case Section => Json.toJson("Section")
    case SubSection => Json.toJson("SubSection")
    case Error => Json.toJson("Error")
    case ValueError => Json.toJson("ValueError")
    case TypeError => Json.toJson("TypeError")
    case Lede => Json.toJson("Lede")
    case Important => Json.toJson("Important")
    case YourCall => Json.toJson("YourCall")
  }

}
