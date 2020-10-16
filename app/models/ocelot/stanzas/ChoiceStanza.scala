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

import models.ocelot.{asCurrency, Labels}
import models.ocelot.{labelReference, labelReferences}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class ChoiceStanzaTest(left: String, test: TestType, right: String)

object ChoiceStanzaTest {
  implicit val reads: Reads[ChoiceStanzaTest] =
    (
      (JsPath \ "left").read[String] and
      (JsPath \ "test").read[TestType] and
      (JsPath \ "right").read[String]
    )(ChoiceStanzaTest.apply _)

  implicit val writes: OWrites[ChoiceStanzaTest] =
    (
      (JsPath \ "left").write[String] and
      (JsPath \ "test").write[TestType] and
      (JsPath \ "right").write[String]
    )(unlift(ChoiceStanzaTest.unapply))
}

case class ChoiceStanza(override val next: Seq[String], tests: Seq[ChoiceStanzaTest], stack: Boolean) extends Stanza {
  override val labelRefs: List[String]  = tests.flatMap(op => labelReferences(op.left) ++ labelReferences(op.right)).toList
}

object ChoiceStanza {

  implicit val reads: Reads[ChoiceStanza] =
    (
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](2)) and
      (JsPath \ "tests").read[Seq[ChoiceStanzaTest]](minLength[Seq[ChoiceStanzaTest]](1)) and
      (JsPath \ "stack").read[Boolean]
    )(ChoiceStanza.apply _)

  implicit val writes: OWrites[ChoiceStanza] =
    (
      (JsPath \ "next").write[Seq[String]] and
      (JsPath \ "tests").write[Seq[ChoiceStanzaTest]] and
      (JsPath \ "stack").write[Boolean]
    )(unlift(ChoiceStanza.unapply))

}

sealed trait ChoiceTest {
  val left: String
  val right: String
  def eval(labels: Labels): Boolean
  def value(arg: String, labels: Labels): String = labelReference(arg).fold(arg)(ref => labels.value(ref).getOrElse(""))
  def op(f: (BigDecimal, BigDecimal) => Boolean, g: (String, String) => Boolean, labels: Labels): Boolean = {
    val x = value(left, labels)
    val y = value(right, labels)
    (asCurrency(x), asCurrency(y)) match {
      case (Some(bd1), Some(bd2)) => f(bd1, bd2)
      case _ => g(x, y)
    }
  }
}

case class EqualsTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ == _, _ == _, labels)
}
case class NotEqualsTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ != _, _ != _, labels)
}
case class MoreThanTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ > _, _ > _, labels)
}
case class MoreThanOrEqualsTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ >= _, _ >= _, labels)
}
case class LessThanTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ < _, _ < _, labels)
}
case class LessThanOrEqualsTest(left: String, right: String) extends ChoiceTest {
  def eval(labels: Labels): Boolean = op(_ <= _, _ <= _, labels)
}

case class Choice(override val next: Seq[String], tests: Seq[ChoiceTest]) extends Stanza with Evaluate {
  def eval(labels: Labels): (String, Labels) =
    tests.zipWithIndex.find{case (x ,y) => x.eval(labels)}
                      .fold((next.last, labels)){case (x,y) => (next(y), labels)}
}

object Choice {
  def apply(stanza: ChoiceStanza): Choice =
    Choice(
      stanza.next,
      stanza.tests.map{ t =>
        t.test match {
          case Equals => EqualsTest(t.left, t.right)
          case NotEquals => NotEqualsTest(t.left, t.right)
          case MoreThan => MoreThanTest(t.left, t.right)
          case MoreThanOrEquals => MoreThanOrEqualsTest(t.left, t.right)
          case LessThan => LessThanTest(t.left, t.right)
          case LessThanOrEquals => LessThanOrEqualsTest(t.left, t.right)
        }
      }
    )
}