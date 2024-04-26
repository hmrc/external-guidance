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

package core.models.ocelot.stanzas

import play.api.libs.json._

sealed trait TestType

case object Equals extends TestType
case object NotEquals extends TestType
case object MoreThan extends TestType
case object MoreThanOrEquals extends TestType
case object LessThan extends TestType
case object LessThanOrEquals extends TestType
case object Contains extends TestType

object TestType {

  implicit val reads: Reads[TestType] = {
    case JsString("equals") => JsSuccess(Equals, __)
    case JsString("notEquals") => JsSuccess(NotEquals, __)
    case JsString("moreThan") => JsSuccess(MoreThan, __)
    case JsString("moreThanOrEquals") => JsSuccess(MoreThanOrEquals, __)
    case JsString("lessThan") => JsSuccess(LessThan, __)
    case JsString("lessThanOrEquals") => JsSuccess(LessThanOrEquals, __)
    case JsString("contains") => JsSuccess(Contains, __)
    case typeName: JsString => JsError(JsonValidationError(Seq("TestType"), typeName.value))
    case unknown => JsError(JsonValidationError(Seq("TestType"), unknown.toString))
  }

  implicit val writes: Writes[TestType] = {
    case Equals => Json.toJson("equals")
    case NotEquals => Json.toJson("notEquals")
    case MoreThan => Json.toJson("moreThan")
    case MoreThanOrEquals => Json.toJson("moreThanOrEquals")
    case LessThan => Json.toJson("lessThan")
    case LessThanOrEquals => Json.toJson("lessThanOrEquals")
    case Contains => Json.toJson("contains")
  }

}
