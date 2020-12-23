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

import models.ocelot.{Label, Labels, Phrase, asCurrency, asCurrencyPounds, asDate, stringFromDate, labelReferences}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class InputStanza(
  ipt_type: InputType,
  override val next: Seq[String],
  name: Int,
  help: Option[Int],
  label: String,
  placeholder: Option[Int],
  stack: Boolean
) extends VisualStanza {
  override val labels = List(Label(label, None))
}

object InputStanza {

  implicit val reads: Reads[InputStanza] =
    ((JsPath \ "ipt_type").read[InputType] and
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
      (JsPath \ "name").read[Int] and
      (JsPath \ "help").readNullable[Int] and
      (JsPath \ "label").read[String] and
      (JsPath \ "placeholder").readNullable[Int] and
      (JsPath \ "stack").read[Boolean])(InputStanza.apply _)

  implicit val writes: OWrites[InputStanza] =
    (
      (JsPath \ "ipt_type").write[InputType] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "name").write[Int] and
        (JsPath \ "help").writeNullable[Int] and
        (JsPath \ "label").write[String] and
        (JsPath \ "placeholder").writeNullable[Int] and
        (JsPath \ "stack").write[Boolean]
    )(unlift(InputStanza.unapply))

}

trait Input extends VisualStanza with Populated with DataInput {
  val name: Phrase
  val help: Option[Phrase]
  val label: String
  val placeholder: Option[Phrase]
  val stack: Boolean

  override val labelRefs: List[String] = labelReferences(name.langs(0)) ++ help.fold[List[String]](Nil)(h => labelReferences(h.langs(0)))
  def eval(value: String, labels: Labels): (Option[String], Labels) = (Some(next(0)), labels.update(label, value))
}

case class CurrencyInput(
  override val next: Seq[String],
  name: Phrase,
  help: Option[Phrase],
  label: String,
  placeholder: Option[Phrase],
  stack: Boolean
) extends Input {
  def validInput(value: String): Option[String] = asCurrency(value).map(_.toString)
}

case class CurrencyPoundsOnlyInput(
  override val next: Seq[String],
  name: Phrase,
  help: Option[Phrase],
  label: String,
  placeholder: Option[Phrase],
  stack: Boolean
) extends Input {
  def validInput(value: String): Option[String] = asCurrencyPounds(value).map(_.toString)
}

case class DateInput(
  override val next: Seq[String],
  name: Phrase,
  help: Option[Phrase],
  label: String,
  placeholder: Option[Phrase],
  stack: Boolean
) extends Input {
  def validInput(value: String): Option[String] = asDate(value).map(stringFromDate(_))
}

object Input {
  def apply(stanza: InputStanza, name: Phrase, help: Option[Phrase], placeholder: Option[Phrase]): Option[Input] =
    stanza.ipt_type match {
      case Currency => Some(CurrencyInput(stanza.next, name, help, stanza.label, placeholder, stanza.stack))
      case CurrencyPoundsOnly => Some(CurrencyPoundsOnlyInput(stanza.next, name, help, stanza.label, placeholder, stanza.stack))
      case Date => Some(DateInput(stanza.next, name, help, stanza.label, placeholder, stanza.stack))
      // .... Add additional input types when needed
      case _ => None
    }
}
