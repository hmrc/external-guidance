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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

import models.ocelot.asAnyInt

case class CalcOperation(left:String, op: CalcOperationType, right: String, label: String)

object CalcOperation {

  implicit val reads: Reads[CalcOperation] = (js: JsValue) =>
    ((js \ "left").validate[String] and
      (js \ "op").validate[CalcOperationType] and
      (js \ "right").validate[String] and
      (js \ "label").validate[String]).tupled match {
      case err: JsError => err
      case JsSuccess((left, op, right, label), _) =>
        op match {
          case Floor | Ceiling if asAnyInt(right).isDefined => JsSuccess(CalcOperation(left, op, right, label))
          case Floor | Ceiling => JsError(Seq(JsPath \ "right" -> Seq(JsonValidationError(Seq("error", "error.noninteger.scalefactor")))))
          case _ => JsSuccess(CalcOperation(left, op, right, label))
        }
    }

  implicit val writes: OWrites[CalcOperation] =
    (
      (JsPath \ "left").write[String] and
        (JsPath \ "op").write[CalcOperationType] and
        (JsPath \ "right").write[String] and
        (JsPath \ "label").write[String]
    )(unlift(CalcOperation.unapply))

}
