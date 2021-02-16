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

import core.models.ocelot.{labelReferences, ScalarLabel, Labels, Label, Phrase, asListOfInt}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsSuccess, JsError, JsValue, JsonValidationError, JsPath, OWrites, Reads}

case class SequenceStanza(text: Int,
                          override val next: Seq[String],
                          options: Seq[Int],
                          label: Option[String],
                          stack: Boolean) extends VisualStanza

object SequenceStanza {
  implicit val reads: Reads[SequenceStanza] = (js: JsValue) =>
    ((js \ "text").validate[Int] and
      (js \ "next").validate[Seq[String]](minLength[Seq[String]](1)) and
      (js \ "options").validate[Seq[Int]] and
      (js \ "label").validateOpt[String] and
      (js \ "stack").validate[Boolean]).tupled match {
      case err: JsError => err
      case JsSuccess((text: Int, next: Seq[String], options: Seq[Int], label: Option[String], stack: Boolean), _) if next.length != (options.length+1) =>
        JsError(Seq(JsPath \ "right" -> Seq(JsonValidationError(Seq("error", "error.listlengths.inconsistent")))))
      case JsSuccess((text: Int, next: Seq[String], options: Seq[Int], label: Option[String], stack: Boolean), _) =>
        JsSuccess(SequenceStanza(text, next, options, label, stack))
    }

  implicit val writes: OWrites[SequenceStanza] =
    (
      (JsPath \ "text").write[Int] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "options").write[Seq[Int]] and
        (JsPath \ "label").writeNullable[String] and
        (JsPath \ "stack").write[Boolean]
    )(unlift(SequenceStanza.unapply))
}

case class Sequence(text: Phrase,
                    override val next: Seq[String],
                    options: Seq[Phrase],
                    label: Option[String],
                    stack: Boolean) extends VisualStanza with Populated with DataInput {
  override val labelRefs: List[String] = labelReferences(text.english) ++ options.flatMap(a => labelReferences(a.english))
  override val labels: List[Label] = label.fold[List[Label]](Nil)(l => List(ScalarLabel(l)))

  def eval(value: String, labels: Labels): (Option[String], Labels) = (None, labels) // TODO EG-1265
  def validInput(value: String): Option[String] =
    asListOfInt(value).fold[Option[String]](None)(l => if (l.forall(options.indices.contains(_))) Some(value) else None)
}

object Sequence {
  def apply(s: SequenceStanza, text: Phrase, options: Seq[Phrase]): Sequence = Sequence(text, s.next, options, s.label, s.stack)
}
