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

sealed trait CalcOperationType

case object Addition extends CalcOperationType
case object Subtraction extends CalcOperationType

object CalcOperationType {

  implicit val reads: Reads[CalcOperationType] = (json: JsValue) =>
  json match {
    case JsString("add") => JsSuccess(Addition, __)
    case JsString("subtract") => JsSuccess(Subtraction, __)
    case typeName: JsString => JsError(JsonValidationError(Seq("CalcOperationType"), typeName.value))
    case unexpectedJsType => JsError(JsonValidationError(Seq("CalcOperationType"), unexpectedJsType.toString()))
  }

  implicit val writes: Writes[CalcOperationType] = (calcOperationType: CalcOperationType) =>
  calcOperationType match {
    case Addition => Json.toJson("add")
    case Subtraction => Json.toJson("subtract")
  }
}

