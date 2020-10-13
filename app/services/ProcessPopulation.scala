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

package services

import models.ocelot.stanzas._
import models.ocelot.{pageLinkIds, Link, Phrase, Process}
import models.ocelot.errors._
import scala.annotation.tailrec

trait ProcessPopulation {

  def stanza(id: String, process: Process): Either[GuidanceError, Stanza] =
    process.flow.get(id) match {
      case Some(stanza) => populateStanza(id, stanza, process)
      case None => Left(StanzaNotFound(id))
    }

  private def populateStanza(id: String, stanza: Stanza, process: Process): Either[GuidanceError, Stanza] = {

    def populateInstruction(i: InstructionStanza): Either[GuidanceError, Instruction] = {
      phrase(i.text, id, process).fold(
        Left(_),
        text => {
          i.link match {
            case Some(linkIndex) => link(linkIndex).fold(Left(_), link => Right(Instruction(i, text, Some(link), pageLinkIds(text.langs.head))))
            case None => Right(Instruction(i, text, None, pageLinkIds(text.langs.head)))
          }
        }
      )
    }

    def populateInput(i: InputStanza): Either[GuidanceError, Input] =
      phrase(i.name, id, process).fold(Left(_), name =>
        optionalPhrase(i.help, id, process).fold(Left(_), help =>
          optionalPhrase(i.placeholder, id, process).fold(Left(_), placeholder =>
            Input(i, name, help, placeholder).fold[Either[GuidanceError, Input]](Left(UnknownInputType(id, i.ipt_type.toString)))(input => Right(input))
          )
        )
      )

    def link(linkIndex: Int): Either[LinkNotFound, Link] =
      process.linkOption(linkIndex).map(Right(_)).getOrElse(Left(LinkNotFound(id, linkIndex)))

    stanza match {
      case q: QuestionStanza =>
        phrases(q.text +: q.answers, Nil, id, process) match {
          case Right(texts) => Right(Question(q, texts.head, texts.tail))
          case Left(err) => Left(err)
        }
      case i: InstructionStanza => populateInstruction(i)
      case i: InputStanza => populateInput(i)
      case c: CalloutStanza => phrase(c.text, id, process).fold(Left(_), text => Right(Callout(c, text)))
      case c: ChoiceStanza => Right(Choice(c))
      case s: Stanza => Right(s)
    }
  }

  private def optionalPhrase(index: Option[Int], stanzaId: String, process: Process):Either[GuidanceError, Option[Phrase]] =
    index.fold[Either[GuidanceError, Option[Phrase]]](Right(None))(i => phrase(i, stanzaId, process).fold(Left(_), phrase => Right(Some(phrase))))

  private def phrase(phraseIndex: Int, stanzaId: String, process: Process): Either[GuidanceError, Phrase] =
    process.phraseOption(phraseIndex).fold[Either[GuidanceError, Phrase]](Left(PhraseNotFound(stanzaId, phraseIndex))){
      case Phrase(Vector(english, welsh)) if welsh.isEmpty && !english.isEmpty => Left(MissingWelshText(stanzaId, phraseIndex.toString, english))
      case p: Phrase => Right(p)
    }

  @tailrec
  private def phrases(indexes: Seq[Int], acc: Seq[Phrase], stanzaId: String, process: Process): Either[GuidanceError, Seq[Phrase]] =
    indexes match {
      case Nil => Right(acc)
      case index :: xs =>
        phrase(index, stanzaId, process) match {
          case Right(phrase) => phrases(xs, acc :+ phrase, stanzaId, process)
          case Left(err) => Left(err)
        }
    }

}
