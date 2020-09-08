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

sealed trait TestType

case object LessThanOrEquals extends TestType

object TestType {

  implicit val reads: Reads[TestType] = (json: JsValue) =>
    json match {
      case JsString("lessThanOrEquals") => JsSuccess(LessThanOrEquals, __)
      case typeName: JsString => JsError(JsonValidationError(Seq("TestType"), typeName.value))
      case unknown => JsError(JsonValidationError(Seq("TestType"), unknown.toString))
    }

  implicit val writes: Writes[TestType] = (inputType: TestType) =>
    inputType match {
      case LessThanOrEquals => Json.toJson("lessThanOrEquals")
    }

}
