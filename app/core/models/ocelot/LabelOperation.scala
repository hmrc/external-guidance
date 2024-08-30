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

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, OFormat, Reads, Writes}

sealed trait LabelOperation {
  val name: String
}

final case class Delete(name: String) extends LabelOperation
object Delete {
  implicit lazy val formats: OFormat[Delete] = Json.format[Delete]
}

final case class Update(l: Label) extends LabelOperation {
  val name: String = l.name
}
object Update {
  implicit lazy val formats: OFormat[Update] = Json.format[Update]
}

object LabelOperation {
  implicit val reads: Reads[LabelOperation] = (js: JsValue) => {
    (js \ "t").validate[String] match {
      case err @ JsError(_) => err
      case JsSuccess(typ, _) => typ match {
        case "D" => js.validate[Delete]
        case "U" => js.validate[Update]
      }
    }
  }

  implicit val writes: Writes[LabelOperation] = {
    case d: Delete => Json.obj("t" -> "D") ++ Json.toJsObject[Delete](d)
    case u: Update => Json.obj("t" -> "U") ++ Json.toJsObject[Update](u)  }
}
