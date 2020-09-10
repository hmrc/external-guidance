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
import play.api.libs.json.Reads._
import play.api.libs.json._

case class InputStanza(
  ipt_type: InputType,
  override val next: Seq[String],
  name: Int,
  help: Int,
  label: String,
  placeholder: Int,
  stack: Boolean
) extends Stanza

object InputStanza {

  implicit val reads: Reads[InputStanza] =
    ((JsPath \ "ipt_type").read[InputType] and
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
      (JsPath \ "name").read[Int] and
      (JsPath \ "help").read[Int] and
      (JsPath \ "label").read[String] and
      (JsPath \ "placeholder").read[Int] and
      (JsPath \ "stack").read[Boolean])(InputStanza.apply _)

  implicit val writes: OWrites[InputStanza] =
    (
      (JsPath \ "ipt_type").write[InputType] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "name").write[Int] and
        (JsPath \ "help").write[Int] and
        (JsPath \ "label").write[String] and
        (JsPath \ "placeholder").write[Int] and
        (JsPath \ "stack").write[Boolean]
    )(unlift(InputStanza.unapply))

}