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

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import core.models.ocelot.{asAnyInt, asCurrency, asDate, labelReference, labelReferences, Label, ScalarLabel, Labels}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

import scala.math.BigDecimal.RoundingMode

case class CalculationStanza(calcs: Seq[CalcOperation], override val next: Seq[String], stack: Boolean) extends Stanza {
  override val labels: List[Label] = calcs.map(op => ScalarLabel(op.label)).toList
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

sealed trait Operation {

  val logger: Logger = Logger(this.getClass)

  val left: String
  val right: String
  val label: String

  def eval(labels: Labels): Labels

  def value(arg: String, labels: Labels): String = labelReference(arg).fold(arg) { ref =>
    labels.value(ref).getOrElse("")
  }

  def unsupportedOperation(operationName: String)(arg1: Any, arg2: Any): Option[String] = {

    logger.error("Unsupported \"" + operationName + "\" calculation stanza operation defined in guidance")

    None
  }

  def op(f: (BigDecimal, BigDecimal) => BigDecimal,
         g: (String, String) => Option[String],
         h: (LocalDate, LocalDate) => Option[String],
         labels: Labels): Labels = {

    val x: String = value(left, labels)
    val y: String = value(right, labels)

    (asDate(x), asDate(y)) match {
      case (Some(ld1), Some(ld2)) =>
        // Treat operands as instances of local date
        h(ld1, ld2) match {
          case Some(value) => labels.update(label, value)
          case None => labels
        }
      case _ =>
        (asCurrency(x), asCurrency(y)) match {
          case (Some(bg1), Some(bg2)) =>
            // Treat operands as instances of big decimal
            val bg3 = f(bg1, bg2)
            labels.update(label, bg3.bigDecimal.toPlainString)
          case _ =>
            // Treat both operands as strings
            g(x, y) match {
              case Some(value) => labels.update(label, value)
              case None => labels
            }
        }
    }

  }

  def rounding(f: (BigDecimal, Int) => BigDecimal, labels: Labels): Labels = {

    val x: String = value(left, labels)
    val y: String = value(right, labels)

    (asCurrency(x), asAnyInt(y)) match {

      case (Some(value), Some(scale)) =>

        val scaledValue = f(value, scale)
        labels.update(label, scaledValue.bigDecimal.toPlainString)

      case _ =>

        unsupportedOperation("Rounding")(x, y)
        labels

    }

  }

}

case class AddOperation(left: String, right: String, label: String) extends Operation {

  def eval(labels: Labels): Labels = op(_ + _, (s1:String, s2:String) => Some(s1 + s2), unsupportedOperation("Add"), labels)

}

case class SubtractOperation(left: String, right: String, label: String) extends Operation {

  def eval(labels: Labels): Labels = op(_ - _, unsupportedOperation("Subtract"), subtractDate, labels)

  private def subtractDate(date: LocalDate, other: LocalDate) : Option[String] =
    Some(other.until(date, ChronoUnit.DAYS).toString)
}

case class CeilingOperation(left: String, right: String, label: String) extends Operation {

  def eval(labels: Labels): Labels = rounding(_.setScale(_, RoundingMode.CEILING), labels)

}

case class FloorOperation(left: String, right: String, label: String) extends Operation {

  def eval(labels: Labels): Labels = rounding(_.setScale(_, RoundingMode.FLOOR), labels)

}

case class Calculation(override val next: Seq[String], calcs: Seq[Operation]) extends Stanza with Evaluate {

  override val labels: List[Label] = calcs.map(op => ScalarLabel(op.label)).toList
  override val labelRefs: List[String] = calcs.flatMap(op => labelReferences(op.left) ++ labelReferences(op.right)).toList

  def eval(labels: Labels): (String, Labels) = {

    val updatedLabels: Labels = calcs.foldLeft(labels) { case (l, f) => f.eval(l) }

    (next.last, updatedLabels)
  }

}

object Calculation {

  def apply(stanza: CalculationStanza): Calculation =
    Calculation(
      stanza.next,
      stanza.calcs.map { c =>
        c.op match {
          case Addition => AddOperation(c.left, c.right, c.label)
          case Subtraction => SubtractOperation(c.left, c.right, c.label)
          case Ceiling => CeilingOperation(c.left, c.right, c.label)
          case Floor => FloorOperation(c.left, c.right, c.label)
        }
      }
    )
}
