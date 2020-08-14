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
import models.ocelot.{Link, Phrase, Process}
import models.ocelot.errors._
import scala.annotation.tailrec

trait ProcessPopulation {

  def stanza(id: String, process: Process): Either[GuidanceError, Stanza] =
    process.flow.get(id) match {
      case Some(stanza) => populateStanza(id, stanza, process)
      case None => Left(StanzaNotFound(id))
    }

  private def populateStanza(id: String, stanza: Stanza, process: Process): Either[GuidanceError, Stanza] = {

    def phrase(phraseIndex: Int): Either[GuidanceError, Phrase] =
      process.phraseOption(phraseIndex).fold(Left(PhraseNotFound(id, phraseIndex)): Either[GuidanceError, Phrase]){
        case Phrase(Vector(english, welsh)) if welsh.isEmpty && !english.isEmpty => Left(MissingWelshText(id, phraseIndex.toString, english))
        case p: Phrase => Right(p)
      }

    @tailrec
    def phrases(indexes: Seq[Int], acc: Seq[Phrase]): Either[GuidanceError, Seq[Phrase]] =
      indexes match {
        case Nil => Right(acc)
        case index :: xs =>
          phrase(index) match {
            case Right(phrase) => phrases(xs, acc :+ phrase)
            case Left(err) => Left(err)
          }
      }

    def link(linkIndex: Int): Either[LinkNotFound, Link] =
      process.linkOption(linkIndex).map(Right(_)).getOrElse(Left(LinkNotFound(id, linkIndex)))

    def populateInstruction(i: InstructionStanza): Either[GuidanceError, Instruction] = {
      phrase(i.text).fold(
        Left(_),
        text => {
          i.link match {
            case Some(linkIndex) => link(linkIndex).fold(Left(_), link => Right(Instruction(i, text, Some(link))))
            case None => Right(Instruction(i, text, None))
          }
        }
      )
    }

    stanza match {
      case q: QuestionStanza =>
        phrases(q.text +: q.answers, Nil) match {
          case Right(texts) => Right(Question(q, texts.head, texts.tail))
          case Left(err) => Left(err)
        }
      case i: InstructionStanza => populateInstruction(i)
      case c: CalloutStanza => phrase(c.text).fold(Left(_), text => Right(Callout(c, text)))
      case s: Stanza => Right(s)
    }
  }

}
