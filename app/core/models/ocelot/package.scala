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

package core.models

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}
import scala.util.Try
import scala.util.matching.Regex

package object ocelot {
  val labelPattern = "\\[label:([A-Za-z0-9\\s\\-_]+)(:(currency|currencyPoundsOnly|date|number))?\\]"
  val boldPattern = s"\\[bold:($labelPattern|[^\\]]+)\\]"
  val linkToPageOnlyPattern = s"\\[link:(.+?):(\\d+|${Process.StartStanzaId})\\]"
  val toPageLinkPattern = s"\\[(button|link)(-same|-tab)?:([^\\]]+?):(\\d+|${Process.StartStanzaId})\\]"
  val linkPattern = s"\\[(button|link)(-same|-tab)?:(.+?):(\\d+|${Process.StartStanzaId}|https?:[a-zA-Z0-9\\/\\.\\-\\?_\\.=&#]+)\\]"
  val commaSeparatedIntsPattern = "^\\d+\\s*(?:,\\s*\\d+\\s*)*$"
  val hintRegex: Regex = "\\[hint:([^\\]]+)\\]".r
  val pageLinkRegex: Regex = s"${toPageLinkPattern}".r
  val labelRefRegex: Regex = s"\\[label:([A-Za-z0-9\\s\\-_]+)(:(currency))?\\]".r
  val inputCurrencyRegex: Regex = "^-?£?(\\d{1,3}(,\\d{3})*|\\d+)(\\.(\\d{1,2})?)?$".r
  val inputCurrencyPoundsRegex: Regex = "^-?£?(\\d{1,3}(,\\d{3})*|\\d+)$".r
  val integerRegex: Regex = "^\\d+$".r
  val listOfintegerRegex: Regex = s"$commaSeparatedIntsPattern".r
  val anyIntegerRegex: Regex = "^[\\-]?\\d+$".r
  val EmbeddedParameterRegex: Regex = """\{(\d)\}""".r

  val DateOutputFormat = "d MMMM uuuu"
  val ignoredCurrencyChars: Seq[Char] = Seq(' ','£', ',')
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/uuuu", java.util.Locale.UK).withResolverStyle(ResolverStyle.STRICT)
  def plSingleGroupCaptures(regex: Regex, str: String, index: Int = 1): List[String] = regex.findAllMatchIn(str).map(_.group(index)).toList
  def pageLinkIds(str: String): List[String] = plSingleGroupCaptures(pageLinkRegex, str, 4)
  def pageLinkIds(phrases: Seq[Phrase]): List[String] = phrases.flatMap(phrase => pageLinkIds(phrase.english)).toList
  def labelReferences(str: String): List[String] = plSingleGroupCaptures(labelRefRegex, str)
  def labelReference(str: String): Option[String] = plSingleGroupCaptures(labelRefRegex, str).headOption
  def asTextString(value: String): Option[String] = value.trim.headOption.fold[Option[String]](None)(_ => Some(value.trim))
  def asCurrency(value: String): Option[BigDecimal] = inputCurrencyRegex.findFirstIn(value.filterNot(c => c==' '))
                                                                        .map(s => BigDecimal(s.filterNot(ignoredCurrencyChars.contains(_))))
  def asCurrencyPounds(value: String): Option[BigDecimal] = inputCurrencyPoundsRegex.findFirstIn(value.filterNot(c => c==' '))
                                                                        .map(s => BigDecimal(s.filterNot(ignoredCurrencyChars.contains(_))))
  def asDate(value: String): Option[LocalDate] = Try(LocalDate.parse(value.trim, dateFormatter)).map(d => d).toOption
  def stringFromDate(when: LocalDate): String = when.format(dateFormatter)
  def asInt(value: String): Option[Int] = integerRegex.findFirstIn(value).map(_.toInt)
  def asListOfInt(value: String): Option[List[Int]] = listOfintegerRegex.findFirstIn(value).map(s => s.split(",").toList.map(el => el.trim.toInt))
  def asAnyInt(value: String): Option[Int] = anyIntegerRegex.findFirstIn(value).map(_.toInt)

  val pageLinkOnlyPattern: String = s"^${linkToPageOnlyPattern}$$"
  val boldOnlyPattern: String = s"^${boldPattern}$$"
  def isLinkOnlyPhrase(phrase: Phrase): Boolean =phrase.english.matches(pageLinkOnlyPattern)
  def isBoldOnlyPhrase(phrase: Phrase): Boolean =phrase.english.matches(boldOnlyPattern)
}
