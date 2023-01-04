/*
 * Copyright 2023 HM Revenue & Customs
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
import core.models.ocelot.{Page, Labels, Phrase, Validation}
import core.models.ocelot.errors.RuntimeError

trait Stanza {
  val visual: Boolean = false
  val next: Seq[String] = Nil
  val links: List[String] = Nil
  val buttonLinks: List[String] = Nil
  val labels: List[String] = Nil
  val labelRefs: List[String] = Nil
}

trait PopulatedStanza extends Stanza

trait VisualStanza extends PopulatedStanza {
  val stack: Boolean
  override val visual: Boolean = true
  def rendered(expand: Phrase => Phrase): VisualStanza = this
}

trait Evaluate {
  def eval(labels: Labels): (String, Labels, List[RuntimeError])
}

trait DataInput {
  def eval(value: String, page: Page, labels: Labels): (Option[String], Labels)
  def validInput(value: String): Validation[String]
}

trait DataInputStanza extends VisualStanza with DataInput {
  override def rendered(expand: Phrase => Phrase): DataInputStanza = this
}

case object EndStanza extends PopulatedStanza

object Stanza {

  implicit val reads: Reads[Stanza] = (js: JsValue) => {
    (js \ "type").validate[String] match {
      case err @ JsError(_) => err
      case JsSuccess(typ, _) => typ match {
        case "QuestionStanza" => js.validate[QuestionStanza]
        case "InstructionStanza" => js.validate[InstructionStanza]
        case "CalloutStanza" => js.validate[CalloutStanza]
        case "PageStanza" => js.validate[PageStanza]
        case "ValueStanza" => js.validate[ValueStanza]
        case "CalculationStanza" => js.validate[CalculationStanza]
        case "SequenceStanza" => js.validate[SequenceStanza]
        case "ChoiceStanza" => js.validate[ChoiceStanza]
        case "InputStanza" => js.validate[InputStanza]
        case "RowStanza" => js.validate[RowStanza]
        case "Choice" => js.validate[Choice]
        case "Calculation" => js.validate[Calculation]
        case "EndStanza" => JsSuccess(EndStanza)
        case typeName => JsError(JsonValidationError(Seq("Stanza"), typeName))
      }
    }
  }

  implicit val writes: Writes[Stanza] = {
    case q: QuestionStanza => Json.obj("type" -> "QuestionStanza") ++ Json.toJsObject[QuestionStanza](q)
    case i: InstructionStanza => Json.obj("type" -> "InstructionStanza") ++ Json.toJsObject[InstructionStanza](i)
    case c: CalloutStanza => Json.obj("type" -> "CalloutStanza") ++ Json.toJsObject[CalloutStanza](c)
    case p: PageStanza => Json.obj("type" -> "PageStanza") ++ Json.toJsObject[PageStanza](p)
    case v: ValueStanza => Json.obj("type" -> "ValueStanza") ++ Json.toJsObject[ValueStanza](v)
    case c: CalculationStanza => Json.obj( "type" -> "CalculationStanza") ++ Json.toJsObject[CalculationStanza](c)
    case s: SequenceStanza => Json.obj( "type" -> "SequenceStanza") ++ Json.toJsObject[SequenceStanza](s)
    case c: ChoiceStanza => Json.obj("type" -> "ChoiceStanza") ++ Json.toJsObject[ChoiceStanza](c)
    case i: InputStanza => Json.obj("type" -> "InputStanza") ++ Json.toJsObject[InputStanza](i)
    case r: RowStanza => Json.obj( "type" -> "RowStanza") ++ Json.toJsObject[RowStanza](r)
    case c: Choice => Json.obj("type" -> "Choice") ++ Json.toJsObject[Choice](c)
    case c: Calculation => Json.obj( "type" -> "Calculation") ++ Json.toJsObject[Calculation](c)
    case EndStanza => Json.obj("type" -> "EndStanza")
    case _ => Json.toJson("")
  }
}
