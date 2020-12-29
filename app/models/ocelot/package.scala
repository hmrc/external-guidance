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
import scala.util.Try
import java.time.format.{DateTimeFormatter, ResolverStyle}
import java.time.LocalDate

package object ocelot {
  val ignoredCurrencyChars: Seq[Char] = Seq(' ','£', ',')
  val hintRegex: Regex = "\\[hint:([^\\]])+\\]".r
  val pageLinkOnlyPattern: String = s"^\\[link:(.+?):(\\d+|${Process.StartStanzaId})\\]$$"
  val boldOnlyPattern: String = s"^\\[bold:(.+?)\\]$$"
  val pageLinkRegex: Regex = s"\\[(button|link)(-same|-tab)?:([^\\]]+?):(\\d+|${Process.StartStanzaId})\\]".r
  val labelRefRegex: Regex = s"\\[label:([A-Za-z0-9\\s\\-_]+)(:(currency))?\\]".r
  val inputCurrencyRegex: Regex = "^-?£?(\\d{1,3}(,\\d{3})*|\\d+)(\\.(\\d{1,2})?)?$".r
  val inputCurrencyPoundsRegex: Regex = "^-?£?(\\d{1,3}(,\\d{3})*|\\d+)$".r
  val integerRegex: Regex = "^\\d+$".r
  val anyIntegerRegex: Regex = "^[\\-]?\\d+$".r
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/uuuu", java.util.Locale.UK)
  def plSingleGroupCaptures(regex: Regex, str: String, index: Int = 1): List[String] = regex.findAllMatchIn(str).map(_.group(index)).toList
  def pageLinkIds(str: String): List[String] = plSingleGroupCaptures(pageLinkRegex, str, 4)
  def pageLinkIds(phrases: Seq[Phrase]): List[String] = phrases.flatMap(phrase => pageLinkIds(phrase.langs.head)).toList
  def labelReferences(str: String): List[String] = plSingleGroupCaptures(labelRefRegex, str)
  def labelReference(str: String): Option[String] = plSingleGroupCaptures(labelRefRegex, str).headOption
  def asCurrency(value: String): Option[BigDecimal] = inputCurrencyRegex.findFirstIn(value.filterNot(c => c==' '))
                                                                        .map(s => BigDecimal(s.filterNot(ignoredCurrencyChars.contains(_))))
  def asCurrencyPounds(value: String): Option[BigDecimal] = inputCurrencyPoundsRegex.findFirstIn(value.filterNot(c => c==' '))
                                                                        .map(s => BigDecimal(s.filterNot(ignoredCurrencyChars.contains(_))))
  def asDate(value: String): Option[LocalDate] = Try(LocalDate.parse(value.trim, dateFormatter.withResolverStyle(ResolverStyle.STRICT))).map(d => d).toOption
  def stringFromDate(when: LocalDate): String = when.format(dateFormatter)
  def asInt(value: String): Option[Int] = integerRegex.findFirstIn(value).map(_.toInt)
  def asAnyInt(value: String): Option[Int] = anyIntegerRegex.findFirstIn(value).map(_.toInt)

  def isLinkOnlyPhrase(phrase: Phrase): Boolean =phrase.langs(0).matches(pageLinkOnlyPattern)
  def isBoldOnlyPhrase(phrase: Phrase): Boolean =phrase.langs(0).matches(boldOnlyPattern)
}
