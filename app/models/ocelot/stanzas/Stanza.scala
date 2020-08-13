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

trait Stanza {
  val next: Seq[String] = Nil
}

trait PopulatedStanza extends Stanza

case object EndStanza extends Stanza

object Stanza {

  implicit val reads: Reads[Stanza] = (js: JsValue) => {
    (js \ "type").as[String] match {
      case "QuestionStanza" => js.validate[QuestionStanza]
      case "InstructionStanza" => js.validate[InstructionStanza]
      case "CalloutStanza" => js.validate[CalloutStanza]
      case "PageStanza" => js.validate[PageStanza]
      case "ValueStanza" => js.validate[ValueStanza]
      case "EndStanza" => JsSuccess(EndStanza)
      case _ => JsError("Stanza")
    }
  }

  implicit val writes: Writes[Stanza] = (stanza: Stanza) =>
    stanza match {
      case q: QuestionStanza => Json.obj("type" -> "QuestionStanza") ++ Json.toJsObject[QuestionStanza](q)
      case i: InstructionStanza => Json.obj("type" -> "InstructionStanza") ++ Json.toJsObject[InstructionStanza](i)
      case c: CalloutStanza => Json.obj("type" -> "CalloutStanza") ++ Json.toJsObject[CalloutStanza](c)
      case p: PageStanza => Json.obj("type" -> "PageStanza") ++ Json.toJsObject[PageStanza](p)
      case v: ValueStanza => Json.obj("type" -> "ValueStanza") ++ Json.toJsObject[ValueStanza](v)
      case EndStanza => Json.obj("type" -> "EndStanza")
      case s => Json.toJson("")
    }
}
