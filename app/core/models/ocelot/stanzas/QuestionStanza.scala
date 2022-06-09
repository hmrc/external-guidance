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

import core.models.ocelot.{labelReferences, Phrase, Labels, Page, hintRegex, asPositiveInt}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class QuestionStanza(text: Int,
                          answers: Seq[Int],
                          override val next: Seq[String],
                          label: Option[String],
                          stack: Boolean) extends VisualStanza {
  override val labels = label.fold[List[String]](Nil)(l => List(l))
}

object QuestionStanza {

  implicit val questionReads: Reads[QuestionStanza] =
    ((JsPath \ "text").read[Int] and
      (JsPath \ "answers").read[Seq[Int]] and
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
      (JsPath \ "label").readNullable[String] and
      (JsPath \ "stack").read[Boolean])(QuestionStanza.apply _)

  implicit val questionWrites: OWrites[QuestionStanza] =
    (
      (JsPath \ "text").write[Int] and
        (JsPath \ "answers").write[Seq[Int]] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "label").writeNullable[String] and
        (JsPath \ "stack").write[Boolean]
    )(unlift(QuestionStanza.unapply))

}

case class Question(text: Phrase,
                    answers: Seq[Phrase],
                    override val next: Seq[String],
                    label: Option[String],
                    stack: Boolean) extends VisualStanza with Populated with DataInput {
  override val labelRefs: List[String] = labelReferences(text.english) ++ answers.flatMap(a => labelReferences(a.english))
  override val labels: List[String] = label.fold[List[String]](Nil)(l => List(l))

  def eval(value: String, page: Page, labels: Labels): (Option[String], Labels) =
    validInput(value).fold[(Option[String], Labels)]((None, labels)){idx => {
        val answer = answers(idx.toInt)
        val english = hintRegex.split(answer.english).head.trim
        val welsh = hintRegex.split(answer.welsh).head.trim
        (Some(next(idx.toInt)), label.fold(labels)(labels.update(_, english, welsh)))
      }
    }
  def validInput(value: String): Option[String] =
    asPositiveInt(value).fold[Option[String]](None)(idx => if (answers.indices.contains(idx)) Some(idx.toString) else None)
}

object Question {
  def apply(stanza: QuestionStanza, text: Phrase, answers: Seq[Phrase]): Question =
    Question(text, answers, stanza.next, stanza.label, stanza.stack)
}
