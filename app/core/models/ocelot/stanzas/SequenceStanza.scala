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

import core.models.ocelot.{labelReferences, ScalarLabel, Page, Labels, Label, Phrase, asListOfInt, exclusiveOptionRegex}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsSuccess, JsError, JsValue, JsonValidationError, JsPath, OWrites, Reads}
import core.models.ocelot.KeyedStanza

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
      case JsSuccess((text: Int, next: Seq[String], options: Seq[Int], label: Option[String], stack: Boolean), _) if next.length != (options.length + 1) =>
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

trait Sequence extends VisualStanza with Populated with DataInput {
  val text: Phrase
  val options: Seq[Phrase]
  val label: Option[String]
  override val labelRefs: List[String] = labelReferences(text.english) ++ options.flatMap(a => labelReferences(a.english))
  override val labels: List[Label] = label.fold[List[Label]](Nil)(l => List(ScalarLabel(l)))

  def eval(value: String, page: Page, labels: Labels): (Option[String], Labels) =
    validInput(value).fold[(Option[String], Labels)]((None, labels)){checkedItems =>
      asListOfInt(checkedItems).fold[(Option[String], Labels)]((None, labels)){checked => {
        val chosenOptions: List[String] = checked.flatMap(idx => options.lift(idx).fold[List[String]](Nil)(p => List(p.english)))
        // Collect any Evaluate stanzas following this Sequence for use when the Continuation is followed
        val continuationStanzas: Map[String, Stanza] = page.keyedStanzas
          .dropWhile{ks => ks.stanza match {
              case s: Sequence => false
              case _ => true
            }
          }
          .drop(1)  // Drop the Sequence
          .collect{case ks @ KeyedStanza(_, s: Stanza with Evaluate) => (ks.key, ks.stanza)}
          .toMap
        // push the flows and Continuation corresponding to the checked items, then
        // nextFlow and redirect to the first flow (setting list and first flow label)
        label.fold(labels)(l => labels.updateList(s"${l}_seq", chosenOptions))
          .pushFlows(checked.flatMap(idx => next.lift(idx).fold[List[String]](Nil)(List(_))), next.last, label, chosenOptions, continuationStanzas)
        }
      }
    }

  def validInput(value: String): Option[String] =
    asListOfInt(value).fold[Option[String]](None)(l => if (l.forall(options.indices.contains(_))) Some(value) else None)
}

object Sequence {
  def apply(s: SequenceStanza, text: Phrase, options: Seq[Phrase]): Sequence =
    if (options.exists(o => exclusiveOptionRegex.findFirstMatchIn(o.english).nonEmpty))
      ExclusiveSequence(text, s.next, options, s.label, s.stack)
    else
      NonExclusiveSequence(text, s.next, options, s.label, s.stack)
}

case class NonExclusiveSequence(text: Phrase,
                                override val next: Seq[String],
                                options: Seq[Phrase],
                                label: Option[String],
                                stack: Boolean) extends Sequence

case class ExclusiveSequence(text: Phrase,
                             override val next: Seq[String],
                             options: Seq[Phrase],
                             label: Option[String],
                             stack: Boolean) extends Sequence {
  lazy val exclusiveOptions: Seq[Phrase] = options.filter{p => exclusiveOptionRegex.findFirstMatchIn(p.english).nonEmpty}
  val nonExclusiveOptions: Seq[Phrase] = options.filterNot{p => exclusiveOptionRegex.findFirstMatchIn(p.english).nonEmpty}
}
