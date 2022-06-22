/*
 * Copyright 2022 HM Revenue & Customs
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

import core.models.ocelot._
import core.models.ocelot.errors.{UnsupportedOperationError, RuntimeError}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.math.BigDecimal.RoundingMode

import TimePeriodArithmetic._

sealed trait Operand[+A] {
  val v: A
  override def toString: String = v.toString
}

sealed trait Scalar[+A] extends Operand[A]
sealed trait Collection[+A] extends Operand[List[A]]

case class StringOperand(v: String) extends Scalar[String]
case class NumericOperand(v: BigDecimal) extends Scalar[BigDecimal]
case class StringCollection(v: List[String]) extends Collection[String]
case class DateOperand(v: LocalDate) extends Scalar[LocalDate] {override def toString: String = stringFromDate(v)}
case class TimePeriodOperand(v: TimePeriod) extends Scalar[TimePeriod]

object Operand {
  def apply(s: String, labels: Labels): Option[Operand[_]] =
    scalar(s, labels).fold[Option[Operand[_]]](collection(s, labels).fold[Option[Operand[_]]](None)(o => Some(o))) { o => Some(o) }

  def scalar(v: String, labels: Labels): Option[Scalar[_]] =
    operandValue(v)(labels).fold[Option[Scalar[_]]](None) { s =>
      asDate(s).fold[Option[Scalar[_]]] {
        asTimePeriod(s).fold[Option[Scalar[_]]] {
          asNumeric(s).fold[Option[Scalar[_]]](Some(StringOperand(s)))(dec => Some(NumericOperand(dec)))
        }(timeperiod => Some(TimePeriodOperand(timeperiod)))
      }(dte => Some(DateOperand(dte)))
    }

  def collection(v: String, labels: Labels): Option[Collection[_]] =
    labelReference(v).fold[Option[Collection[_]]](None) { lo =>
      labels.valueAsList(lo).fold[Option[Collection[_]]](None)(l => Some(StringCollection(l)))
    }
}

sealed trait Operation {
  type Result[T] = Either[RuntimeError, T]

  val logger: Logger = Logger(this.getClass)

  val left: String
  val right: String
  val label: String

  private[stanzas] def evalScalarCollectionOp(l: String, r: List[String]): Result[List[String]] = unsupported(l, r)
  private[stanzas] def evalCollectionScalarOp(l: List[String], r: String): Result[List[String]] = unsupported(l, r)
  private[stanzas] def evalCollectionCollectionOp(l: List[String], r: List[String]): Result[List[String]] = unsupported(l, r)
  private[stanzas] def evalDateOp(l: LocalDate, r: LocalDate): Result[String] = unsupported(l, r)
  private[stanzas] def evalNumericOp(l: BigDecimal, r: BigDecimal): Result[String] = unsupported(l, r)
  private[stanzas] def evalStringOp(l: String, r: String): Result[String] = unsupported(l, r)
  private[stanzas] def evalDateTimePeriod(l: LocalDate, r: TimePeriod): Result[String] = unsupported(l, r)

  def eval(labels: Labels): Result[Labels] = {
    def storeResultLabel[T](v: Result[T], res: (String, T) => Labels): Result[Labels] = v.fold(err => Left(err), result => Right(res(label, result)))

    (Operand(left, labels), Operand(right, labels)) match {
      case (Some(NumericOperand(l)), Some(NumericOperand(r))) => storeResultLabel(evalNumericOp(l, r), labels.update)
      case (Some(DateOperand(l)), Some(DateOperand(r))) => storeResultLabel(evalDateOp(l, r), labels.update)
      case (Some(DateOperand(l)), Some(TimePeriodOperand(r))) => storeResultLabel(evalDateTimePeriod(l,r), labels.update)
      case (Some(DateOperand(l)), Some(NumericOperand(r))) => storeResultLabel(evalDateTimePeriod(l, TimePeriod(r.toInt, Day)), labels.update)
      case (Some(StringOperand(l)), Some(StringOperand(r))) => storeResultLabel(evalStringOp(l, r), labels.update)
      case (Some(StringCollection(l)), Some(StringCollection(r))) => storeResultLabel(evalCollectionCollectionOp(l, r), labels.updateList)
      case (Some(StringCollection(l)), Some(r: Scalar[_])) => storeResultLabel(evalCollectionScalarOp(l, r.toString), labels.updateList)
      case (Some(l: Scalar[_]), Some(StringCollection(r))) => storeResultLabel(evalScalarCollectionOp(l.toString, r), labels.updateList)
      // No typed op, fall back to String, String op
      case (Some(l: Operand[_]), Some(r: Operand[_])) => storeResultLabel(evalStringOp(l.toString, r.toString), labels.update)
      case _ => unsupported(right, left)
    }
  }

  protected def unsupported[A, B, C, D](l: A, r: B): Result[D] = Left(UnsupportedOperationError(getClass.getSimpleName, l.toString, r.toString, left, right))

}

case class AddOperation(left: String, right: String, label: String) extends Operation {
  override def evalDateTimePeriod(date: LocalDate, period1: TimePeriod): Result[String] = Right(stringFromDate(date.add(period1)))
  override def evalScalarCollectionOp(left: String, right: List[String]): Result[List[String]] = Right(left :: right)
  override def evalCollectionScalarOp(left: List[String], right: String): Result[List[String]] = Right((right :: left.reverse).reverse)
  override def evalCollectionCollectionOp(left: List[String], right: List[String]): Result[List[String]] = Right(left ::: right)
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Result[String] = Right((left + right).bigDecimal.toPlainString)
  override def evalStringOp(left: String, right: String): Result[String] = Right(left + right)
}

case class SubtractOperation(left: String, right: String, label: String) extends Operation {
  override def evalDateTimePeriod(date: LocalDate, period1: TimePeriod): Result[String] = Right(stringFromDate(date.minus(period1)))
  override def evalCollectionScalarOp(left: List[String], right: String): Result[List[String]] = Right(left.filterNot(_ == right))
  override def evalCollectionCollectionOp(left: List[String], right: List[String]): Result[List[String]] = Right(left.filterNot(right.contains(_)))
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Result[String] = Right((left - right).bigDecimal.toPlainString)
  override def evalDateOp(left: LocalDate, right: LocalDate): Result[String] = Right(right.until(left, ChronoUnit.DAYS).toString)
}

case class MultiplyOperation(left: String, right: String, label: String) extends Operation {
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Result[String] = Right((left * right).bigDecimal.toPlainString)
}

case class DivideOperation(left: String, right: String, label: String) extends Operation {
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Result[String] =
    if (right.equals(0.0)) Right("Infinity") else Right((left / right).bigDecimal.toPlainString)
}

case class CeilingOperation(left: String, right: String, label: String) extends Operation {
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Result[String] =
    Right(left.setScale(right.toInt, RoundingMode.CEILING).bigDecimal.toPlainString)
}

case class FloorOperation(left: String, right: String, label: String) extends Operation {
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Result[String] =
    Right(left.setScale(right.toInt, RoundingMode.FLOOR).bigDecimal.toPlainString)
}

object Operation {
  implicit val reads: Reads[Operation] = (js: JsValue) => {
    (js \ "type").validate[String] match {
      case err @ JsError(_) => err
      case JsSuccess(typ, _) => typ match {
        case "add" => js.validate[AddOperation]
        case "sub" => js.validate[SubtractOperation]
        case "mult" => js.validate[MultiplyOperation]
        case "div" => js.validate[DivideOperation]
        case "ceil" => js.validate[CeilingOperation]
        case "flr" => js.validate[FloorOperation]
        case typeName => JsError(JsonValidationError(Seq("Operation"), typeName))
      }
    }
  }

  implicit val writes: Writes[Operation] = {
    case o: AddOperation => Json.obj("type" -> "add") ++ Json.toJsObject[AddOperation](o)
    case o: SubtractOperation => Json.obj("type" -> "sub") ++ Json.toJsObject[SubtractOperation](o)
    case o: MultiplyOperation => Json.obj("type" -> "mult") ++ Json.toJsObject[MultiplyOperation](o)
    case o: DivideOperation => Json.obj("type" -> "div") ++ Json.toJsObject[DivideOperation](o)
    case o: CeilingOperation => Json.obj("type" -> "ceil") ++ Json.toJsObject[CeilingOperation](o)
    case o: FloorOperation => Json.obj("type" -> "flr") ++ Json.toJsObject[FloorOperation](o)
  }
}

object AddOperation {
  implicit val reads: Reads[AddOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String]) (AddOperation.apply _)
  implicit val writes: OWrites[AddOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String]) (unlift(AddOperation.unapply))
}

object SubtractOperation {
  implicit val reads: Reads[SubtractOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String]) (SubtractOperation.apply _)
  implicit val writes: OWrites[SubtractOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String]) (unlift(SubtractOperation.unapply))
}

object MultiplyOperation {
  implicit val reads: Reads[MultiplyOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String]) (MultiplyOperation.apply _)
  implicit val writes: OWrites[MultiplyOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String]) (unlift(MultiplyOperation.unapply))
}

object DivideOperation {
  implicit val reads: Reads[DivideOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String]) (DivideOperation.apply _)
  implicit val writes: OWrites[DivideOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String]) (unlift(DivideOperation.unapply))
}

object CeilingOperation {
  implicit val reads: Reads[CeilingOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String]) (CeilingOperation.apply _)
  implicit val writes: OWrites[CeilingOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String]) (unlift(CeilingOperation.unapply))
}

object FloorOperation {
  implicit val reads: Reads[FloorOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String]) (FloorOperation.apply _)
  implicit val writes: OWrites[FloorOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String]) (unlift(FloorOperation.unapply))
}