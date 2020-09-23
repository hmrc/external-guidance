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

import models.ocelot.{labelReferences, Phrase}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class QuestionStanza(text: Int, answers: Seq[Int], override val next: Seq[String], stack: Boolean) extends Stanza

object QuestionStanza {

  implicit val questionReads: Reads[QuestionStanza] =
    ((JsPath \ "text").read[Int] and
      (JsPath \ "answers").read[Seq[Int]] and
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
      (JsPath \ "stack").read[Boolean])(QuestionStanza.apply _)

  implicit val questionWrites: OWrites[QuestionStanza] =
    (
      (JsPath \ "text").write[Int] and
        (JsPath \ "answers").write[Seq[Int]] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "stack").write[Boolean]
    )(unlift(QuestionStanza.unapply))

}

case class Question(text: Phrase, answers: Seq[Phrase], override val next: Seq[String], stack: Boolean) extends Stanza with Populated {
  override val labelRefs: List[String] = labelReferences(text.langs(0)) ++ answers.flatMap(a => labelReferences(a.langs(0)))

}

object Question {

  def apply(stanza: QuestionStanza, text: Phrase, answers: Seq[Phrase]): Question =
    Question(text, answers, stanza.next, stanza.stack)
}
