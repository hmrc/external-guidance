/*
 * Copyright 2023 HM Revenue & Customs
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

import core.models.ocelot.{labelReferences, pageLinkIds, Phrase}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

sealed trait Callout extends VisualStanza {
  val text: Phrase
  override val labelRefs: List[String] = labelReferences(text.english)
  override val links: List[String] = pageLinkIds(text.english)
}

sealed trait Heading

case class CalloutStanza(noteType: CalloutType, text: Int, override val next: Seq[String], stack: Boolean) extends Stanza

object CalloutStanza {
  implicit val calloutReads: Reads[CalloutStanza] =
    ((JsPath \ "noteType").read[CalloutType] and
      (JsPath \ "text").read[Int] and
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
      (JsPath \ "stack").read[Boolean])(CalloutStanza.apply _)

  implicit val owrites: OWrites[CalloutStanza] =
    (
      (JsPath \ "noteType").write[CalloutType] and
        (JsPath \ "text").write[Int] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "stack").write[Boolean]
    )(unlift(CalloutStanza.unapply))
}

object Callout {
  def apply(stanza: CalloutStanza, text: Phrase): Callout =
    stanza.noteType match {
      case Title => TitleCallout(text, stanza.next, stanza.stack)
      case SubTitle => SubTitleCallout(text, stanza.next, stanza.stack)
      case Section => SectionCallout(text, stanza.next, stanza.stack)
      case SubSection => SubSectionCallout(text, stanza.next, stanza.stack)
      case Lede => LedeCallout(text, stanza.next, stanza.stack)
      case Error => ErrorCallout(text, stanza.next, stanza.stack)
      case ValueError => ValueErrorCallout(text, stanza.next, stanza.stack)
      case TypeError => TypeErrorCallout(text, stanza.next, stanza.stack)
      case Important => ImportantCallout(text, stanza.next, stanza.stack)
      case YourCall => YourCallCallout(text, stanza.next, stanza.stack)
      case NumListItem => NumberedListItemCallout(text, stanza.next, stanza.stack)
      case NumCircListItem => NumberedCircleListItemCallout(text, stanza.next, stanza.stack)
      case Note => NoteCallout(text, stanza.next, stanza.stack)
    }
}

case class TitleCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout with Heading {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class SubTitleCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout with Heading {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class SectionCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout with Heading {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class SubSectionCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout with Heading {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class LedeCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class ErrorCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class ValueErrorCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class TypeErrorCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class ImportantCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class YourCallCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class NumberedListItemCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class NumberedCircleListItemCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}
case class NoteCallout(text: Phrase, override val next: Seq[String], stack: Boolean) extends Callout {
  override def rendered(expand: Phrase => Phrase): VisualStanza = copy(text = expand(text))
}

