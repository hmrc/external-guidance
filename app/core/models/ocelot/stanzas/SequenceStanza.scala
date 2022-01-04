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

import core.models.ocelot.{KeyedStanza, labelReferences, Page, Labels, Phrase, stripHintPlaceholder, asListOfPositiveInt}
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
      (js \ "next").validate[Seq[String]](minLength[Seq[String]](3)) and
      (js \ "options").validate[Seq[Int]](minLength[Seq[Int]](2)) and
      (js \ "label").validateOpt[String] and
      (js \ "stack").validate[Boolean]).tupled match {
      case err: JsError => err
      case JsSuccess((_: Int, next: Seq[String], options: Seq[Int], _: Option[String], _: Boolean), _) if next.length != (options.length + 1) =>
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

object Sequence {
  def apply(s: SequenceStanza, text: Phrase, options: Seq[Phrase], exclusive: Option[Phrase]): Sequence = {
    Sequence(text, s.next, options, exclusive, s.label, s.stack)
  }
}

case class Sequence(text: Phrase,
                    override val next: Seq[String],
                    options: Seq[Phrase],
                    exclusive: Option[Phrase],
                    label: Option[String],
                    stack: Boolean) extends VisualStanza with Populated with DataInput {
  override val labelRefs: List[String] = labelReferences(text.english) ++ options.flatMap(a => labelReferences(a.english))
  override val labels: List[String] = label.fold(List.empty[String])(l => List(l))

  def eval(value: String, page: Page, labels: Labels): (Option[String], Labels) =
    validInput(value).fold[(Option[String], Labels)]((None, labels)){checkedItems =>
      asListOfPositiveInt(checkedItems).fold[(Option[String], Labels)]((None, labels)){checked => {
        val chosenOptions: List[Phrase] = checked.flatMap{idx =>
          options.lift(idx).fold(exclusive.fold(List.empty[Phrase])(ep => List(stripHintPlaceholder(ep))))(sp => List(stripHintPlaceholder(sp)))
        }
        // Collect any Evaluate stanzas following this Sequence for use when the Continuation is followed
        val continuationStanzas: Map[String, Stanza] = page.keyedStanzas.dropWhile(_.stanza != this)
                                                           .collect{case ks @ KeyedStanza(_, _: Stanza with Evaluate) => (ks.key, ks.stanza)}
                                                           .toMap

        // push the flows and Continuation corresponding to the checked items, then
        // nextFlow and redirect to the first flow (setting list and first flow label)
        label.fold(labels){l =>
          labels.updateList(s"${l}_seq", chosenOptions.map(_.english), chosenOptions.map(_.welsh))
        }.pushFlows(checked.flatMap(idx => next.lift(idx).fold[List[String]](Nil)(List(_))),
                    next.last,
                    label,
                    chosenOptions,
                    continuationStanzas)
      }}
    }

  def validInput(value: String): Option[String] =
    asListOfPositiveInt(value).fold[Option[String]](None){l =>
      if (l.forall(options.indices.contains) ||
          l.length == 1 && exclusive.fold(false)(_ => l.headOption.contains(options.length))) Some(value) else None
    }
}
