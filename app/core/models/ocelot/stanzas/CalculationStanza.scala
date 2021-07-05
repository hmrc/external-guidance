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

package core.models.ocelot.stanzas

import core.models.ocelot.{labelReferences, asAnyInt, Labels}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

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

case class CalculationStanza(calcs: Seq[CalcOperation], override val next: Seq[String], stack: Boolean) extends Stanza {
  override val labels: List[String] = calcs.map(op => op.label).toList
  override val labelRefs: List[String] = calcs.flatMap(op => labelReferences(op.left) ++ labelReferences(op.right)).toList
}

object CalculationStanza {

  implicit val calculationReads: Reads[CalculationStanza] =
    (
      (JsPath \ "calcs").read[Seq[CalcOperation]](minLength[Seq[CalcOperation]](1)) and
        (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
        (JsPath \ "stack").read[Boolean]
    )(CalculationStanza.apply _)

  implicit val calculationWrites: OWrites[CalculationStanza] =
    (
      (JsPath \ "calcs").write[Seq[CalcOperation]] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "stack").write[Boolean]
    )(unlift(CalculationStanza.unapply))
}

case class Calculation(override val next: Seq[String], calcs: Seq[Operation]) extends Stanza with Evaluate {
  override val labels: List[String] = calcs.map(op => op.label).toList
  override val labelRefs: List[String] = calcs.flatMap(op => labelReferences(op.left) ++ labelReferences(op.right)).toList

  def eval(labels: Labels): (String, Labels) = {
    val updatedLabels: Labels = calcs.foldLeft(labels) { case (l, f) => f.eval(l) }
    (next.last, updatedLabels)
  }
}

object Calculation {
  def buildCalculation(next: Seq[String], calcs: Seq[Operation]): Calculation = Calculation(next, calcs)
  implicit val reads: Reads[Calculation] =
    ((JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and (JsPath \ "calcs").read[Seq[Operation]])(buildCalculation _)

  implicit val writes: OWrites[Calculation] =
    ((JsPath \ "next").write[Seq[String]] and (JsPath \ "calcs").write[Seq[Operation]])(unlift(Calculation.unapply))


  def apply(stanza: CalculationStanza): Calculation =
    Calculation(
      stanza.next,
      stanza.calcs.map { c =>
        c.op match {
          case Addition => AddOperation(c.left, c.right, c.label)
          case Subtraction => SubtractOperation(c.left, c.right, c.label)
          case Multiply => MultiplyOperation(c.left, c.right, c.label)
          case Divide => DivideOperation(c.left, c.right, c.label)
          case Ceiling => CeilingOperation(c.left, c.right, c.label)
          case Floor => FloorOperation(c.left, c.right, c.label)
        }
      }
    )
}
