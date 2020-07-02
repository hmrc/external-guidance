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

import models.ocelot.{Phrase, Process, Link => OcelotLink}
import models.ui._

import scala.annotation.tailrec
import scala.util.matching.Regex
import scala.util.matching.Regex._

object TextBuilder {

  def fromPhrase(txt: Phrase)(implicit urlMap: Map[String, String]): Text = {
    val isEmpty: TextItem => Boolean = _.isEmpty

    val (enTexts, enMatches) = fromPattern(placeholdersPattern, txt.langs(0))
    val (cyTexts, cyMatches) = fromPattern(placeholdersPattern, txt.langs(1))

    val en = merge(enTexts.map(Words(_)), placeholdersToItems(enMatches), Nil, isEmpty)
    val cy = merge(cyTexts.map(Words(_)), placeholdersToItems(cyMatches), Nil, isEmpty)
    Text(en, cy)
  }

  // Parses a string potentially containing a hint pattern[hint:<Text Hint>]
  // The text before the first hint (if any) and if so the first hint will be
  // returned. All subsequent text and hints will be ignored and lost
  def singleTextWithOptionalHint(txt: Phrase): (Text, Option[Text]) = {
    val (enTexts, enMatches) = fromPattern(answerHintPattern, txt.langs(0))
    val (cyTexts, cyMatches) = fromPattern(answerHintPattern, txt.langs(1))

    val enHint = enMatches.headOption.map(enM => enM.group(1))
    val cyHint = cyMatches.headOption.map(cyM => cyM.group(1))
    val hint = enHint.map(en => Text(en, cyHint.getOrElse("")))
    (Text(enTexts.head, cyTexts.head), hint)
  }

  private def placeholdersToItems(matches: List[Match])(implicit urlMap: Map[String, String]): List[TextItem] =
    matches.map { m =>
      Option(m.group(1)).fold[TextItem](
        if (OcelotLink.isLinkableStanzaId(m.group(3))) {
          Link(urlMap(m.group(3)), m.group(2))
        } else {
          Link(m.group(3), m.group(2))
        }
      )(_ => Words(m.group(1), true))
    }

  private def fromPattern(pattern: Regex, text: String): (List[String], List[Match]) =
    (pattern.split(text).toList, pattern.findAllMatchIn(text).toList)

  def placeholderTxtsAndMatches(text: String): (List[String], List[Match]) = fromPattern(placeholdersPattern, text)

  @tailrec
  def merge[A, B](txts: List[A], links: List[A], acc: Seq[A], isEmpty: A => Boolean): Seq[A] =
    (txts, links) match {
      case (Nil, Nil) => acc
      case (t :: txs, l :: lxs) if isEmpty(t) => merge(txs, lxs, acc :+ l, isEmpty)
      case (t :: txs, l :: lxs) => merge(txs, lxs, (acc :+ t) :+ l, isEmpty)
      case (t, Nil) => acc ++ t
      case (Nil, l) => acc ++ l
    }

  val answerHintPattern: Regex = """\[hint:([^\]]+)\]""".r
  val placeholdersPattern: Regex = s"\\[bold:([^\\]]+)\\]|\\[link:([^\\]]+?):(\\d+|${Process.StartStanzaId}|https?:[a-zA-Z0-9\\/\\.\\-\\?_\\.=&]+)\\]".r
}
