/*
 * Copyright 2024 HM Revenue & Customs
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
import core.models.ocelot.errors.RuntimeError
import core.models.ocelot.{asNumeric, asDate, Labels}
import core.models.ocelot.{operandValue, labelReferences, labelReference}
import play.api.libs.functional.syntax.*
import play.api.libs.json.Reads.*
import play.api.libs.json.*

case class ChoiceStanzaTest(left: String, test: TestType, right: String)

object ChoiceStanzaTest {

  given reads: Reads[ChoiceStanzaTest] =
    (
      (JsPath \ "left").read[String] and
        (JsPath \ "test").read[TestType] and
        (JsPath \ "right").read[String]
    )(ChoiceStanzaTest.apply _)

  given writes: OWrites[ChoiceStanzaTest] =
    (
      (JsPath \ "left").write[String] and
        (JsPath \ "test").write[TestType] and
        (JsPath \ "right").write[String]
    )(Tuple.fromProductTyped(_))
}

case class ChoiceStanza(override val next: Seq[String], tests: Seq[ChoiceStanzaTest], stack: Boolean) extends Stanza {
  override val labelRefs: List[String] = tests.flatMap(op => labelReferences(op.left) ++ labelReferences(op.right)).toList
}

object ChoiceStanza {

  given reads: Reads[ChoiceStanza] =
    (
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](2)) and
        (JsPath \ "tests").read[Seq[ChoiceStanzaTest]](minLength[Seq[ChoiceStanzaTest]](1)) and
        (JsPath \ "stack").read[Boolean]
    )(ChoiceStanza.apply _)

  given writes: OWrites[ChoiceStanza] =
    (
      (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "tests").write[Seq[ChoiceStanzaTest]] and
        (JsPath \ "stack").write[Boolean]
    )(Tuple.fromProductTyped(_))

}

sealed trait ChoiceTest {
  val left: String
  val right: String
  def eval(labels: Labels): Boolean
  def value(arg: String, labels: Labels): String = operandValue(arg)(using labels).getOrElse("")

  def op(f: (BigDecimal, BigDecimal) => Boolean, g: (String, String) => Boolean, h: (LocalDate, LocalDate) => Boolean, labels: Labels): Boolean = {
    val x = value(left, labels)
    val y = value(right, labels)
    (asDate(x), asDate(y)) match {
      case (Some(ld1), Some(ld2)) => h(ld1, ld2)
      case _ =>
        (asNumeric(x), asNumeric(y)) match {
          case (Some(bd1), Some(bd2)) => f(bd1, bd2)
          case _ => g(x, y)
        }
    }
  }
}

case class EqualsTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ == _, _ == _, _.isEqual(_), labels)
}

case class NotEqualsTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ != _, _ != _, !_.isEqual(_), labels)
}

case class MoreThanTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ > _, _ > _, _.isAfter(_), labels)
}

case class MoreThanOrEqualsTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ >= _, _ >= _, _.compareTo(_) >= 0, labels)
}

case class LessThanTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ < _, _ < _, _.isBefore(_), labels)
}

case class LessThanOrEqualsTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ <= _, _ <= _, _.compareTo(_) <= 0, labels)
}

case class ContainsTest(left: String, right: String) extends ChoiceTest {
  def contains(l: BigDecimal, r: BigDecimal): Boolean = l.toString.toLowerCase().contains(r.toString.toLowerCase())
  def contains(l: String, r: String): Boolean = l.toLowerCase().contains(r.toLowerCase())
  def contains(l: LocalDate, r: LocalDate): Boolean = l.toString.toLowerCase().contains(r.toString.toLowerCase())
  def eval(labels: Labels): Boolean =
    labelReference(left).fold(op(contains(_, _), contains(_, _), contains(_, _), labels)){lbl =>
      labels.valueAsList(lbl).fold(op(contains(_, _), contains(_, _), contains(_, _), labels)){l =>
        val y = value(right, labels)
        l.exists{el =>
          (asDate(el), asDate(y)) match {
            case (Some(ld1), Some(ld2)) => contains(ld1, ld2)
            case _ =>
              (asNumeric(el), asNumeric(y)) match {
                case (Some(bd1), Some(bd2)) => contains(bd1, bd2)
                case _ => contains(el.toString, y.toString)
              }
          }
        }
      }
    }
}

object ChoiceTest {
  given reads: Reads[ChoiceTest] = (js: JsValue) => {
    (js \ "type").validate[String] match {
      case err @ JsError(_) => err
      case JsSuccess(typ, _) => typ match {
        case "eq" => js.validate[EqualsTest]
        case "neq" => js.validate[NotEqualsTest]
        case "mt" => js.validate[MoreThanTest]
        case "mte" => js.validate[MoreThanOrEqualsTest]
        case "lt" => js.validate[LessThanTest]
        case "lte" => js.validate[LessThanOrEqualsTest]
        case "cntns" => js.validate[ContainsTest]
        case typeName => JsError(JsonValidationError(Seq("ChoiceTest"), typeName))
      }
    }
  }

  given writes: Writes[ChoiceTest] = {
    case t: EqualsTest => Json.obj("type" -> "eq") ++ Json.toJsObject[EqualsTest](t)
    case t: NotEqualsTest => Json.obj("type" -> "neq") ++ Json.toJsObject[NotEqualsTest](t)
    case t: MoreThanTest => Json.obj("type" -> "mt") ++ Json.toJsObject[MoreThanTest](t)
    case t: MoreThanOrEqualsTest => Json.obj("type" -> "mte") ++ Json.toJsObject[MoreThanOrEqualsTest](t)
    case t: LessThanTest => Json.obj("type" -> "lt") ++ Json.toJsObject[LessThanTest](t)
    case t: LessThanOrEqualsTest => Json.obj( "type" -> "lte") ++ Json.toJsObject[LessThanOrEqualsTest](t)
    case t: ContainsTest => Json.obj( "type" -> "cntns") ++ Json.toJsObject[ContainsTest](t)
  }
}

object EqualsTest {
  given reads: Reads[EqualsTest] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String])(EqualsTest.apply _)
  given writes: OWrites[EqualsTest] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String])(Tuple.fromProductTyped(_))
}

object NotEqualsTest {
  given reads: Reads[NotEqualsTest] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String])(NotEqualsTest.apply _)
  given writes: OWrites[NotEqualsTest] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String])(Tuple.fromProductTyped(_))
}

object MoreThanTest {
  given reads: Reads[MoreThanTest] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String])(MoreThanTest.apply _)
  given writes: OWrites[MoreThanTest] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String])(Tuple.fromProductTyped(_))
}

object MoreThanOrEqualsTest {
  given reads: Reads[MoreThanOrEqualsTest] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String])(MoreThanOrEqualsTest.apply _)
  given writes: OWrites[MoreThanOrEqualsTest] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String])(Tuple.fromProductTyped(_))
}

object LessThanTest {
  given reads: Reads[LessThanTest] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String])(LessThanTest.apply _)
  given writes: OWrites[LessThanTest] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String])(Tuple.fromProductTyped(_))
}

object LessThanOrEqualsTest {
  given reads: Reads[LessThanOrEqualsTest] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String])(LessThanOrEqualsTest.apply _)
  given writes: OWrites[LessThanOrEqualsTest] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String])(Tuple.fromProductTyped(_))
}

object ContainsTest {
  given reads: Reads[ContainsTest] =
    ((JsPath \ "left").read[String] and (JsPath \ "right").read[String])(ContainsTest.apply _)
  given writes: OWrites[ContainsTest] =
    ((JsPath \ "left").write[String] and (JsPath \ "right").write[String])(Tuple.fromProductTyped(_))
}

case class Choice(override val next: Seq[String], tests: Seq[ChoiceTest]) extends PopulatedStanza with Evaluate {
  def eval(labels: Labels): (String, Labels, Option[RuntimeError]) =
    tests.zipWithIndex
      .find { case (x, _) => x.eval(labels) }
      .fold((next.last, labels, None)) { case (_, y) => (next(y), labels, None) } // Errors ignored here as tests against unknown labels are legal
}

object Choice {
  def buildChoice(next: Seq[String], tests: Seq[ChoiceTest]): Choice = Choice(next, tests)

  given reads: Reads[Choice] =
    ((JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and (JsPath \ "tests").read[Seq[ChoiceTest]])(buildChoice _)

  given writes: OWrites[Choice] =
    ((JsPath \ "next").write[Seq[String]] and (JsPath \ "tests").write[Seq[ChoiceTest]])(Tuple.fromProductTyped(_))

  def apply(stanza: ChoiceStanza): Choice =
    Choice(
      stanza.next,
      stanza.tests.map { t =>
        t.test match {
          case Equals => EqualsTest(t.left, t.right)
          case NotEquals => NotEqualsTest(t.left, t.right)
          case MoreThan => MoreThanTest(t.left, t.right)
          case MoreThanOrEquals => MoreThanOrEqualsTest(t.left, t.right)
          case LessThan => LessThanTest(t.left, t.right)
          case LessThanOrEquals => LessThanOrEqualsTest(t.left, t.right)
          case Contains => ContainsTest(t.left, t.right)
        }
      }
    )
}
