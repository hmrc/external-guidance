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
import core.models.ocelot.{stringFromDate, asDecimal, asDate, labelReference, labelScalarValue, Labels}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

import scala.math.BigDecimal.RoundingMode

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

object Operand {
  def apply(s: String, labels: Labels): Option[Operand[_]] =
    scalar(s, labels).fold[Option[Operand[_]]](collection(s, labels).fold[Option[Operand[_]]](None)(o => Some(o))){o => Some(o)}

  def scalar(v: String, labels: Labels): Option[Scalar[_]] =
    labelScalarValue(v)(labels).fold[Option[Scalar[_]]](None){s =>
      asDate(s).fold[Option[Scalar[_]]]{
        asDecimal(s).fold[Option[Scalar[_]]](Some(StringOperand(s)))(dec => Some(NumericOperand(dec)))
      }(dte => Some(DateOperand(dte)))
    }

  def collection(v: String, labels: Labels): Option[Collection[_]] =
    labelReference(v).fold[Option[Collection[_]]](None){lo =>
      labels.valueAsList(lo).fold[Option[Collection[_]]](None)(l => Some(StringCollection(l)))
    }
}

sealed trait Operation {
  val logger: Logger = Logger(this.getClass)

  val left: String
  val right: String
  val label: String

  private[stanzas] def evalScalarCollectionOp(left: String, right: List[String]): Option[List[String]] = unsupported[List[String]]()
  private[stanzas] def evalCollectionScalarOp(left: List[String], right: String): Option[List[String]] = unsupported[List[String]]()
  private[stanzas] def evalCollectionCollectionOp(left: List[String], right: List[String]): Option[List[String]] = unsupported[List[String]]()
  private[stanzas] def evalDateOp(left: LocalDate, right: LocalDate): Option[String] = unsupported[String]()
  private[stanzas] def evalNumericOp(left: BigDecimal, right: BigDecimal): Option[String] = unsupported[String]()
  private[stanzas] def evalStringOp(left: String, right: String): Option[String] = unsupported[String]()

  def eval(labels: Labels): Labels =
    (Operand(left, labels), Operand(right, labels)) match {
      case (Some(NumericOperand(l)), Some(NumericOperand(r))) =>
        evalNumericOp(l, r).fold(labels)(result => labels.update(label, result))
      case (Some(DateOperand(l)), Some(DateOperand(r))) =>
        evalDateOp(l, r).fold(labels)(result => labels.update(label, result))
      case (Some(StringOperand(l)), Some(StringOperand(r))) =>
        evalStringOp(l, r).fold(labels)(result => labels.update(label, result))
      case (Some(StringCollection(l)), Some(StringCollection(r))) =>
        evalCollectionCollectionOp(l, r).fold(labels)(result => labels.updateList(label, result))
      case (Some(StringCollection(l)), Some(r: Scalar[_])) =>
        evalCollectionScalarOp(l, r.toString).fold(labels)(result => labels.updateList(label, result))
      case (Some(l: Scalar[_]), Some(StringCollection(r))) =>
        evalScalarCollectionOp(l.toString, r).fold(labels)(result => labels.updateList(label, result))
      case (Some(l: Operand[_]), Some(r: Operand[_])) => // No typed op, fall back to String, String op
        evalStringOp(l.toString, r.toString).fold(labels)(result => labels.update(label, result))
      case _ => unsupported(); labels
    }

  protected def unsupported[A](): Option[A] = {
    logger.error("Unsupported \"" + getClass.getSimpleName + "\" calculation stanza operation defined in guidance")
    None
  }
}

case class AddOperation(left: String, right: String, label: String) extends Operation {
  override def evalScalarCollectionOp(left: String, right: List[String]): Option[List[String]] = Some(left :: right)
  override def evalCollectionScalarOp(left: List[String], right: String): Option[List[String]] = Some((right :: left.reverse).reverse)
  override def evalCollectionCollectionOp(left: List[String], right: List[String]): Option[List[String]] = Some(left ::: right)
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Option[String] = Some((left + right).bigDecimal.toPlainString)
  override def evalStringOp(left: String, right: String): Option[String] = Some(left + right)
}

case class SubtractOperation(left: String, right: String, label: String) extends Operation {
  override def evalCollectionScalarOp(left: List[String], right: String): Option[List[String]] = Some(left.filterNot(_ == right))
  override def evalCollectionCollectionOp(left: List[String], right: List[String]): Option[List[String]] = Some(left.filterNot(right.contains(_)))
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Option[String] = Some((left - right).bigDecimal.toPlainString)
  override def evalDateOp(left: LocalDate, right: LocalDate): Option[String] =  Some(right.until(left, ChronoUnit.DAYS).toString)
}

case class MultiplyOperation(left: String, right: String, label: String) extends Operation {
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Option[String] = Some((left * right).bigDecimal.toPlainString)
}

case class DivideOperation(left: String, right: String, label: String) extends Operation {
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Option[String] =
    if (right.equals(0.0)) Some("Infinity") else Some((left / right).bigDecimal.toPlainString)
}

case class CeilingOperation(left: String, right: String, label: String) extends Operation {
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Option[String] =
    Some(left.setScale(right.toInt, RoundingMode.CEILING).bigDecimal.toPlainString)
}

case class FloorOperation(left: String, right: String, label: String) extends Operation {
  override def evalNumericOp(left: BigDecimal, right: BigDecimal): Option[String] =
    Some(left.setScale(right.toInt, RoundingMode.FLOOR).bigDecimal.toPlainString)
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
object MultiplyOperation{
  implicit val reads: Reads[MultiplyOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String])(MultiplyOperation.apply _)
  implicit val writes: OWrites[MultiplyOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String])(unlift(MultiplyOperation.unapply))
}
object DivideOperation{
  implicit val reads: Reads[DivideOperation] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String] and (JsPath \ "label").read[String])(DivideOperation.apply _)
  implicit val writes: OWrites[DivideOperation] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String] and (JsPath \ "label").write[String])(unlift(DivideOperation.unapply))
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