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

package core.models.ocelot.stanzas

import play.api.libs.json._

sealed trait InputType

case object Currency extends InputType
case object CurrencyPoundsOnly extends InputType
case object Number extends InputType
case object Txt extends InputType
case object Date extends InputType

object InputType {

  implicit val reads: Reads[InputType] = {
    case JsString("Currency") => JsSuccess(Currency, __)
    case JsString("CurrencyPoundsOnly") => JsSuccess(CurrencyPoundsOnly, __)
    case JsString("Date") => JsSuccess(Date, __)
    case JsString("Number") => JsSuccess(Number, __)
    case JsString("Text") => JsSuccess(Txt, __)
    case typeName: JsString => JsError(JsonValidationError(Seq("InputType"), typeName.value))
    case unknown => JsError(JsonValidationError(Seq("InputType"), unknown.toString))
  }

  implicit val writes: Writes[InputType] = {
    case Currency => Json.toJson("Currency")
    case CurrencyPoundsOnly => Json.toJson("CurrencyPoundsOnly")
    case Date => Json.toJson("Date")
    case Number => Json.toJson("Number")
    case Txt => Json.toJson("Text")
  }

}
