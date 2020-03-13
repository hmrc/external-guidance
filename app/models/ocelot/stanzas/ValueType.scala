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

sealed trait ValueType

case object Scalar extends ValueType

object ValueType {

  implicit val reads: Reads[ValueType] = new Reads[ValueType] {

    override def reads(json: JsValue): JsResult[ValueType] = json match {
      case JsString("scalar") => JsSuccess(Scalar, __)
      case _ => JsError("Invalid ValueType type")
    }

  }
}

class WithName(string: String) {
  override val toString: String = string
}

case object PageUrlValueName extends WithName("PageUrl")
