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

case object Title extends CalloutType

case object SubTitle extends CalloutType

case object Error extends CalloutType

case object Lede extends CalloutType

case object Section extends CalloutType

case object SubSection extends CalloutType

case object Important extends CalloutType


object CalloutType {

  implicit val reads: Reads[CalloutType] = {
    case JsString("Title") => JsSuccess(Title, __)
    case JsString("SubTitle") => JsSuccess(SubTitle, __)
    case JsString("Lede") => JsSuccess(Lede, __)
    case JsString("Error") => JsSuccess(Error, __)
    case JsString("Section") => JsSuccess(Section, __)
    case JsString("SubSection") => JsSuccess(SubSection, __)
    case JsString("Important") => JsSuccess(SubSection, __)
    case typeName: JsString => JsError(JsonValidationError(Seq("CalloutType"), typeName.value))
    case unknown => JsError(JsonValidationError(Seq("CalloutType"), unknown.toString))
  }

  implicit val writes: Writes[CalloutType] = {
    case Title => Json.toJson("Title")
    case SubTitle => Json.toJson("SubTitle")
    case Error => Json.toJson("Error")
    case Lede => Json.toJson("Lede")
    case Section => Json.toJson("Section")
    case SubSection => Json.toJson("SubSection")
    case Important => Json.toJson("Important")
  }

}
