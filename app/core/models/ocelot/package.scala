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
  val labelPattern: String = "\\[label:([A-Za-z0-9\\s\\-_]+)(:(currency|currencyPoundsOnly|date|number))?\\]"
  val boldPattern: String = s"\\[bold:($labelPattern|[^\\]]+)\\]"
  val linkToPageOnlyPattern: String = s"\\[link:(.+?):(\\d+|${Process.StartStanzaId})\\]"
  val pageLinkPattern: String = s"\\[(button|link)(-same|-tab)?:(.+?):(\\d+|${Process.StartStanzaId})\\]"
  val buttonLinkPattern: String = s"\\[(button)(-same|-tab)?:(.+?):(\\d+|${Process.StartStanzaId})\\]"
  val linkPattern: String = s"\\[(button|link)(-same|-tab)?:(.+?):(\\d+|${Process.StartStanzaId}|https?:[a-zA-Z0-9\\/\\.\\-\\?_\\.=&#]+)\\]"
  val timeConstantPattern: String = "^(\\d{1,10})(day|week|month|year)$"
  val csPositiveIntPattern: String = "^\\d{1,10}(?:,\\d{1,10})*$"
  val listPattern: String = "\\[list:([A-Za-z0-9\\s\\-_]+):length\\]"
  val singleLabelOrListPattern: String = s"^$labelPattern|$listPattern$$"
  val singleLabelOrListRegex: Regex = singleLabelOrListPattern.r
  val labelAndListPattern: String = s"$labelPattern|$listPattern"
  val labelAndListRegex: Regex = labelAndListPattern.r
  val hintRegex: Regex = "\\[hint:([^\\]]+)\\]".r
  val pageLinkRegex: Regex = pageLinkPattern.r
  val buttonLinkRegex: Regex = buttonLinkPattern.r
  val labelRefRegex: Regex = labelPattern.r
  val inputCurrencyRegex: Regex = "^-?£?(\\d{1,3}(,\\d{3})*|\\d+)(\\.(\\d{1,2})?)?$".r
  val inputCurrencyPoundsRegex: Regex = "^-?£?(\\d{1,3}(,\\d{3})*|\\d+)$".r
  val positiveIntRegex: Regex = "^\\d{1,10}$".r                                 // Limited to 10 decimal digits
  val listOfPositiveIntRegex: Regex = csPositiveIntPattern.r
  val anyIntegerRegex: Regex = "^-?(\\d{1,3}(,\\d{3}){0,3}|\\d{1,10})$".r       // Limited to 10 decimal digits or 12 comma separated
  val EmbeddedParameterRegex: Regex = """\{(\d)\}""".r
  val exclusiveOptionPattern: String = "[exclusive]"
  val timeConstantRegex: Regex = timeConstantPattern.r

  val DateOutputFormat = "d MMMM uuuu"
  val ignoredCurrencyChars: Seq[Char] = Seq(' ','£', ',')
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/uuuu", java.util.Locale.UK).withResolverStyle(ResolverStyle.STRICT)
  def plSingleGroupCaptures(regex: Regex, str: String, index: Int = 1): List[String] = regex.findAllMatchIn(str).map(_.group(index)).toList
  def buttonLinkIds(str: String): List[String] = plSingleGroupCaptures(buttonLinkRegex, str, 4)
  def buttonLinkIds(phrases: Seq[Phrase]): List[String] = phrases.flatMap(phrase => buttonLinkIds(phrase.english)).toList
  def pageLinkIds(str: String): List[String] = plSingleGroupCaptures(pageLinkRegex, str, 4)
  def pageLinkIds(phrases: Seq[Phrase]): List[String] = phrases.flatMap(phrase => pageLinkIds(phrase.english)).toList
  def labelReferences(str: String): List[String] = plSingleGroupCaptures(labelRefRegex, str)
  def labelReference(str: String): Option[String] = plSingleGroupCaptures(labelRefRegex, str).headOption
  def listLength(listName: String, labels: Labels): Option[String] = labels.valueAsList(listName).fold[Option[String]](None){l => Some(l.length.toString)}
  def labelScalarMatch(m: Regex.Match, labels: Labels, lbl: String => Option[String]): Option[String] =
    Option(m.group(1)).fold[Option[String]]{
      Option(m.group(4)).fold[Option[String]](None)(list => listLength(list, labels))
    }{label => lbl(label)}
  def labelScalarValue(str: String)(implicit labels: Labels): Option[String] =
    singleLabelOrListRegex.findFirstMatchIn(str).fold[Option[String]](Some(str)){labelScalarMatch(_, labels, labels.value)}
  def asTextString(value: String): Option[String] = value.trim.headOption.fold[Option[String]](None)(_ => Some(value.trim))
  def asDecimal(value: String): Option[BigDecimal] =
    inputCurrencyRegex.findFirstIn(value.filterNot(c => c==' ')).map(s => BigDecimal(s.filterNot(ignoredCurrencyChars.contains(_))))
  def asCurrencyPounds(value: String): Option[BigDecimal] =
    inputCurrencyPoundsRegex.findFirstIn(value.filterNot(c => c==' ')).map(s => BigDecimal(s.filterNot(ignoredCurrencyChars.contains(_))))
  def asDate(value: String): Option[LocalDate] = Try(LocalDate.parse(value.filterNot(_.equals(' ')), dateFormatter)).map(d => d).toOption
  def stringFromDate(when: LocalDate): String = when.format(dateFormatter)
  def asPositiveInt(value: String): Option[Int] = matchedInt(value, positiveIntRegex)
  def asAnyInt(value: String): Option[Int] = matchedInt(value, anyIntegerRegex)
  def asListOfPositiveInt(value: String): Option[List[Int]] = listOfPositiveIntRegex.findFirstIn(value.filterNot(_.equals(' ')))
                                                                                    .flatMap(s => lOfOtoOofL(s.split(",").toList.map(asPositiveInt)))
  def asTimePeriod(value: String): Option[TimePeriod] =
    timeConstantRegex.findFirstMatchIn(value.trim).flatMap{m =>
    Option(m.group(1)).fold[Option[TimePeriod]](None)(n =>
      Option(m.group(2)).fold[Option[TimePeriod]](None){unit => unit match {
        case "day" => Some(TimePeriod(n.toInt, Day))
        case "week" => Some(TimePeriod(n.toInt, Week))
        case "month" => Some(TimePeriod(n.toInt, Month))
        case "year" => Some(TimePeriod(n.toInt, Year))
      }
    })
  }

  val pageLinkOnlyPattern: String = s"^${linkToPageOnlyPattern}$$"
  val boldOnlyPattern: String = s"^${boldPattern}$$"
  def isLinkOnlyPhrase(phrase: Phrase): Boolean =phrase.english.matches(pageLinkOnlyPattern)
  def isBoldOnlyPhrase(phrase: Phrase): Boolean =phrase.english.matches(boldOnlyPattern)

  private def matchedInt(value: String, regex: Regex): Option[Int] = regex.findFirstIn(value.filterNot(_.equals(' '))).flatMap(asInt)
  private def asInt(value: String): Option[Int] = {
    val longValue: Long = value.filterNot(_ == ',').toLong
    if (longValue < Int.MinValue || longValue > Int.MaxValue) None else Some(longValue.toInt)
  }
}
