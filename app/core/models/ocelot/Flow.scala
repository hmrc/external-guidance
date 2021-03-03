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

// FlowStack: Flow, Flow, Flow, Continuation, Flow, Flow, Flow, Continuation

// Continuation: continuation next and post sequence, non-visual stanzas from page

// The PageRenderer will add the current Continuation stanzas into this stanzaMap when Continuation followed


package core.models.ocelot

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class LabelValue(name: String, value: Option[String])

sealed trait FlowStage { val next: String }
final case class Flow(next: String, labelValue: Option[LabelValue]) extends FlowStage
final case class Continuation(next: String) extends FlowStage

object LabelValue {
  implicit val reads: Reads[LabelValue] = (
    (__ \ "name").read[String] and
      (__ \ "value").readNullable[String]
  )(LabelValue.apply _)

  implicit val writes: Writes[LabelValue] = (
    (__ \ "name").write[String] and
      (__ \ "value").writeNullable[String]
  )(unlift(LabelValue.unapply))
}

object Flow {
  implicit val reads: Reads[Flow] = (
    (__ \ "next").read[String] and
      (__ \ "labelValue").readNullable[LabelValue]
  )(Flow.apply _)

  implicit val writes: OWrites[Flow] = (
    (__ \ "next").write[String] and
      (__ \ "labelValue").writeNullable[LabelValue]
  )(unlift(Flow.unapply))
}

object Continuation {
  implicit val reads: Reads[Continuation] = (__ \ "next").read[String].map(Continuation.apply)
  implicit val writes: OWrites[Continuation] = (__ \ "next").write[String].contramap(_.next)
}

object FlowStage {
  implicit val reads: Reads[FlowStage] = (js: JsValue) => {
    (js \ "type").validate[String] match {
      case err @ JsError(_) => err
      case JsSuccess(typ, _) => typ match {
        case "flow" => js.validate[Flow]
        case "cont" => js.validate[Continuation]
        case typeName => JsError(JsonValidationError(Seq("FlowStage"), typeName))
      }
    }
  }

  implicit val writes: Writes[FlowStage] = {
    case f: Flow => Json.obj("type" -> "flow") ++ Json.toJsObject[Flow](f)
    case c: Continuation => Json.obj("type" -> "cont") ++ Json.toJsObject[Continuation](c)
  }
}
