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

package models

import scala.util.matching.Regex
import models.ocelot.stanzas.{Callout, Heading}

package object ocelot {
  val hintRegex = "\\[hint:([^\\]])+\\]".r
  val pageLinkOnlyRegex = s"^\\[link:(.+?):(\\d+|${Process.StartStanzaId})\\]$$".r
  val pageLinkRegex = s"\\[(button|link)(-same|-tab)?:([^\\]]+?):(\\d+|${Process.StartStanzaId})\\]".r
  val labelRefRegex = s"\\[label:([A-Za-z0-9\\s\\-_]+)(:(currency))?\\]".r
  val inputCurrencyRegex = "^-?(\\d{1,3}(,\\d{3})*|\\d+)(\\.(\\d{1,2})?)?$".r
  val integerRegex = "^\\d+$".r

  def plSingleGroupCaptures(regex: Regex, str: String, index: Int = 1): List[String] = regex.findAllMatchIn(str).map(_.group(index)).toList
  def pageLinkIds(str: String): List[String] = plSingleGroupCaptures(pageLinkRegex, str, 4)
  def pageLinkIds(phrases: Seq[Phrase]): List[String] = phrases.flatMap(phrase => pageLinkIds(phrase.langs.head)).toList
  def labelReferences(str: String): List[String] = plSingleGroupCaptures(labelRefRegex, str)
  def labelReference(str: String): Option[String] = plSingleGroupCaptures(labelRefRegex, str).headOption
  def asCurrency(value: String): Option[BigDecimal] = inputCurrencyRegex.findFirstIn(value.filterNot(_==' ')).map(s => BigDecimal(s.filterNot(_==',')))
  def asInt(value: String): Option[Int] = integerRegex.findFirstIn(value).map(_.toInt)

  def isLinkOnlyPhrase(phrase: Phrase): Boolean =
    pageLinkOnlyRegex
      .findFirstIn(phrase.langs(0))
      .fold(false)(_ => pageLinkOnlyRegex.findFirstIn(phrase.langs(1)).fold(false)(_ => true))

  def isHeadingCallout(c: Callout): Boolean = c.noteType match {
    case nt: Heading => true
    case _ => false
  }

}
