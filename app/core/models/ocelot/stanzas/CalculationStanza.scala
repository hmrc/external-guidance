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

import scala.annotation.tailrec

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

  def value(arg: String, labels: Labels): Option[String] = labelReference(arg).fold[Option[String]](Some(arg)){ref =>
    labels.value(ref)
  }

  def listValue(arg: String, labels: Labels): Option[List[String]] = {
    labelReference(arg).fold[Option[List[String]]](None){ref => labels.valueAsList(ref)}
  }

  def operands(labels: Labels): (Option[String], Option[List[String]], Option[String], Option[List[String]]) = {

    val xScalar: Option[String] = value(left, labels)
    val yScalar: Option[String] = value(right, labels)

    (xScalar, yScalar) match {
      case (Some(_), Some(_)) => (xScalar, None, yScalar, None) // Optimize for scalar on scalar as these are the most common operations
      case _ =>
        val xList: Option[List[String]] = listValue(left, labels)
        val yList: Option[List[String]] = listValue(right, labels)

        (xScalar, xList, yScalar, yList)
    }
  }

  def unsupportedOperation[A](operationName: String)(arg1: Any, arg2: Any ): Option[A] = {

    logger.error("Unsupported \"" + operationName + "\" calculation stanza operation defined in guidance")

    None
  }

  def op(x : String,
         y: String,
         f: (BigDecimal, BigDecimal) => BigDecimal,
         g: (String, String) => Option[String],
         h: (LocalDate, LocalDate) => Option[String],
         labels: Labels): Labels = {

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

  def listAndValueOp(list: List[String], scalar: String, f:(List[String], String) => Option[List[String]], labels: Labels): Labels =
  f(list, scalar) match {case Some(value) => labels.updateList(label, value) case None => labels}

  def listOp(list1: List[String], list2: List[String], f:(List[String], List[String]) => List[String], labels: Labels): Labels =
    labels.updateList(label, f(list1, list2))

  def rounding(f: (BigDecimal, Int) => BigDecimal, labels: Labels): Labels = {

    value(left, labels).fold(labels)(x => value(right, labels).fold(labels)(y =>

      (asCurrency(x), asAnyInt(y)) match {

        case (Some(value), Some(scale)) =>

          val scaledValue = f(value, scale)
          labels.update(label, scaledValue.bigDecimal.toPlainString)

        case _ =>

          unsupportedOperation("Rounding")(x, y)
          labels

      }))

  }
}

case class AddOperation(left: String, right: String, label: String) extends Operation {

  def eval(labels: Labels): Labels =
    operands(labels) match {
      case (Some(x), None, Some(y), None) => op(x, y, _ + _, (s1: String, s2: String) => Some(s1 + s2), unsupportedOperation("Add"), labels)
      case (None, Some(xList), Some(y), None) => listAndValueOp(xList, y, appendStringToList, labels)
      case (Some(x), None, None, Some(yList)) => listAndValueOp(yList, x, prependStringToList, labels)
      case (None, Some(xList), None, Some(yList)) => listOp(xList, yList, addListToList, labels)
      case _ => unsupportedOperation("Add")(None, None)
        labels
    }

  private def appendStringToList(l: List[String], s: String): Option[List[String]] = Some((s :: l.reverse).reverse)

  private def prependStringToList(l: List[String], s: String): Option[List[String]] = Some(s :: l)

  private def addListToList(list1: List[String], list2: List[String]): List[String] = list1 ::: list2
}

case class SubtractOperation(left: String, right: String, label: String) extends Operation {

  def eval(labels: Labels): Labels =

    operands(labels) match {
      case (Some(x), None, Some(y), None) => op(x, y, _ - _, unsupportedOperation("Subtract"), subtractDate, labels)
      case (None, Some(xList), Some(y), None) => listAndValueOp(xList, y, subtractStringFromList, labels)
      case (Some(x), None, None, Some(yList)) => listAndValueOp(yList, x, unsupportedOperation("Subtract list from string"), labels)
      case (None, Some(xList), None, Some(yList)) => listOp(xList, yList, subtractListFromList, labels)
      case _ => unsupportedOperation("Subtract")(None, None)
        labels
    }

  private def subtractDate(date: LocalDate, other: LocalDate) : Option[String] =
    Some(other.until(date, ChronoUnit.DAYS).toString)

  private def subtractStringFromList(l: List[String], s: String): Option[List[String]] = Some(l.filter(_ != s))

  @tailrec
  private def subtractListFromList(list1: List[String], list2: List[String]): List[String] =
    list2 match {
      case Nil => list1
      case x :: xs => subtractListFromList(list1.filter(_ != x), xs)
    }

}

case class CeilingOperation(left: String, right: String, label: String) extends Operation {

  def eval(labels: Labels): Labels = rounding(_.setScale(_, RoundingMode.CEILING), labels)

}

case class FloorOperation(left: String, right: String, label: String) extends Operation {

  def eval(labels: Labels): Labels = rounding(_.setScale(_, RoundingMode.FLOOR), labels)

}

object Operation {
  implicit val reads: Reads[Operation] = (js: JsValue) => {
    (js \ "type").validate[String] match {
      case err @ JsError(_) => err
      case JsSuccess(typ, _) => typ match {
        case "add" => js.validate[AddOperation]
        case "sub" => js.validate[SubtractOperation]
        case "ceil" => js.validate[CeilingOperation]
        case "flr" => js.validate[FloorOperation]
        case typeName => JsError(JsonValidationError(Seq("Operation"), typeName))
      }
    }
  }

  implicit val writes: Writes[Operation] = {
    case o: AddOperation => Json.obj("type" -> "add") ++ Json.toJsObject[AddOperation](o)
    case o: SubtractOperation => Json.obj("type" -> "sub") ++ Json.toJsObject[SubtractOperation](o)
    case o: CeilingOperation => Json.obj("type" -> "ceil") ++ Json.toJsObject[CeilingOperation](o)
    case o: FloorOperation => Json.obj("type" -> "flr") ++ Json.toJsObject[FloorOperation](o)
  }
}

object AddOperation{
  implicit val reads: Reads[AddOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String])(AddOperation.apply _)
  implicit val writes: OWrites[AddOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String])(unlift(AddOperation.unapply))
}

object SubtractOperation{
  implicit val reads: Reads[SubtractOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String])(SubtractOperation.apply _)
  implicit val writes: OWrites[SubtractOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String])(unlift(SubtractOperation.unapply))

}
object CeilingOperation{
  implicit val reads: Reads[CeilingOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String])(CeilingOperation.apply _)
  implicit val writes: OWrites[CeilingOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String])(unlift(CeilingOperation.unapply))
}
object FloorOperation{
  implicit val reads: Reads[FloorOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String])(FloorOperation.apply _)
  implicit val writes: OWrites[FloorOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String])(unlift(FloorOperation.unapply))
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
          case Ceiling => CeilingOperation(c.left, c.right, c.label)
          case Floor => FloorOperation(c.left, c.right, c.label)
        }
      }
    )
}
