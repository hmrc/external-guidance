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

package core.models

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}
import scala.util.Try
import scala.util.matching.Regex
import scala.util.matching.Regex._

package object ocelot {
  val Twenty: String = "20"
  val Ten: String = "10"
  val Five: String = "5"
  val Four: String = "4"
  val Three: String = "3"
  val Two: String = "2"
  val NumberOfCYAColumns: Int = 3
  val FirstColumn: Int = 0
  val SecondColumn: Int = 1
  val ThirdColumn: Int = 2

  val TimescaleIdPattern: String = "[A-Za-z][a-zA-Z0-9_-]+"
  val DatePattern: String = "\\d{1,2}\\/\\d{1,2}\\/\\d{4}"
  val HttpUriPattern: String = "https?:[a-zA-Z0-9\\/\\.\\-\\?_\\.=&#]+"
  val JavascriptPattern: String = "javascript\\:.+?"
  val EmailPattern: String = "mailto\\:.+?"
  val StanzaIdPattern: String = s"\\d+|${Process.StartStanzaId}"
  val TenDigitIntPattern: String = "\\d{1,10}"
  val LabelNamePattern: String = "[A-Za-z0-9\\s\\-_]*[A-Za-z0-9\\-_]"
  val LabelPattern: String = s"\\[label:($LabelNamePattern)(?::(currency|currencyPoundsOnly|date|number))?\\]"
  val LabelNameRegex: Regex = s"^$LabelNamePattern$$".r
  val boldPattern: String = s"\\[bold:([^\\]]+)\\]"
  val boldLabelPattern: String = s"\\[bold:($LabelPattern|[^\\]]+)\\]"
  val SimpleTimescalePattern: String = s"\\[timescale:(?:(?:($TimescaleIdPattern):days))\\]"
  val DateAddPattern: String = s"\\[date_add:(?:($LabelNamePattern)|($DatePattern)):($TimescaleIdPattern)\\]"
  val TimescaleIdUsagePattern: String = s"(?:$DateAddPattern)|(?:$SimpleTimescalePattern)"
  val linkToPageOnlyPattern: String = s"\\[link:(.+?):($StanzaIdPattern)\\]"
  val pageLinkPattern: String = s"\\[(button|link)(-same|-tab)?:(.+?):($StanzaIdPattern)\\]"
  val buttonLinkPattern: String = s"\\[(button)(-same|-tab)?:(.+?):($StanzaIdPattern)\\]"
  val linkPattern: String = s"\\[(button|link)(-same|-tab)?:(.+?):($StanzaIdPattern|$HttpUriPattern|$JavascriptPattern|$EmailPattern)\\]"
  val timeConstantPattern: String = s"^($TenDigitIntPattern)\\s*(days?|weeks?|months?|years?)$$"
  val PositiveIntListPattern: String = s"^$TenDigitIntPattern(?:,$TenDigitIntPattern)*$$"
  val DatePlaceHolderPattern: String = s"\\[date:($DatePattern|$LabelPattern)?:(year|month|month_start|month_end|month_name|dow|dow_name|day)\\]"
  val listPattern: String = s"\\[list:($LabelNamePattern):length\\]"
  val operandPattern: String = s"^$LabelPattern|$listPattern|$DateAddPattern|$DatePlaceHolderPattern$$"
  val operandRegex: Regex = operandPattern.r
  val UiExpansionPattern: String = s"$LabelPattern|$listPattern|$DateAddPattern|$DatePlaceHolderPattern"
  val UiExpansionRegex: Regex = UiExpansionPattern.r
  val hintRegex: Regex = "\\[hint:([^\\]]+)\\]".r
  val pageLinkRegex: Regex = pageLinkPattern.r
  val buttonLinkRegex: Regex = buttonLinkPattern.r
  val labelRefRegex: Regex = LabelPattern.r
  val numericRegex: Regex = "^-?(\\d{1,3}(,\\d{3})*|\\d+)(\\.(\\d*)?)?$".r
  val inputCurrencyRegex: Regex = "^-?£?(\\d{1,3}(,\\d{3})*|\\d+)(\\.(\\d{1,2})?)?$".r
  val inputCurrencyPoundsRegex: Regex = "^-?£?(\\d{1,3}(,\\d{3})*|\\d+)$".r
  val positiveIntRegex: Regex = s"^$TenDigitIntPattern$$".r                                 // Limited to 10 decimal digits
  val listOfPositiveIntRegex: Regex = PositiveIntListPattern.r
  val anyIntegerRegex: Regex = s"^-?(\\d{1,3}(,\\d{3}){0,3}|$TenDigitIntPattern)$$".r       // Limited to 10 decimal digits or 12 comma separated
  val EmbeddedParameterGroup: Int = 1
  val EmbeddedParameterRegex: Regex = """\{(\d)\}""".r
  val ExclusivePlaceholder: String = "[exclusive]"
  val NoRepeatPlaceholder: String = "\\[norepeat\\]"
  val FieldWidthPattern: String = s"\\[width:(\\d+?)\\]"
  val ValidInputFieldWidths: List[String] = List(Twenty, Ten, Five, Four, Three, Two)
  // Match leading text, optionally followed by [width:<int>] and/or [norepeat] placeholders in any order.
  val InputOptionsPattern: String = s"^(.*?)(?:(?:(?:$FieldWidthPattern)?\\s*?($NoRepeatPlaceholder))|(?:($NoRepeatPlaceholder)?\\s*?$FieldWidthPattern))$$"
  val InputOptionsRegex: Regex = InputOptionsPattern.r
  val timeConstantRegex: Regex = timeConstantPattern.r
  val DatePlaceHolderRegex: Regex = s"^$DatePlaceHolderPattern$$".r
  val TimescaleIdUsageRegex: Regex = TimescaleIdUsagePattern.r
  val DateOutputFormat = "d MMMM uuuu"
  val ignoredNumericChars: Seq[Char] = Seq(' ', ',')
  val ignoredCurrencyChars: Seq[Char] = Seq('£') ++ ignoredNumericChars
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/uuuu", java.util.Locale.UK).withResolverStyle(ResolverStyle.STRICT)
  val pageLinkOnlyPattern: String = s"^${linkToPageOnlyPattern}$$"
  val boldOnlyPattern: String = s"^${boldPattern}$$"

  def matchGroup(m: Match)(grp: Int): Option[String] = Option(m.group(grp))
  def operandValue(str: String)(implicit labels: Labels): Option[String] =
    operandRegex.findFirstMatchIn(str).fold[Option[String]](Some(str)){m => scalarMatch(matchGroup(m), labels.value)}
  val LabelNameGroup: Int = 1
  val LabelOutputFormatGroup: Int = 2
  val ListLengthLabelNameGroup: Int = 3
  val DateAddLabelNameGroup: Int = 4
  val DateAddLiteralGroup: Int = 5
  val DateAddTimescaleIdGroup: Int = 6
  val DatePlaceholderDateLiteralGroup: Int = 7
  val DatePlaceholderLabelNameGroup: Int = 8
  val DatePlaceholderFnGroup: Int = 10

  def scalarMatch(capture: Int => Option[String], lbl: String => Option[String])(implicit labels: Labels): Option[String] =
    capture(LabelNameGroup).fold{
      capture(ListLengthLabelNameGroup).fold{
        capture(DateAddTimescaleIdGroup).fold[Option[String]]{
          capture(DatePlaceholderFnGroup).fold[Option[String]](None){fn =>
            capture(DatePlaceholderLabelNameGroup).fold[Option[String]]{
              capture(DatePlaceholderDateLiteralGroup).fold[Option[String]](None){dte => datePlaceholder(Some(dte), fn)}
            }{label => datePlaceholder(lbl(label), fn)}
          }
        }{tsId => capture(DateAddLabelNameGroup).fold(dateAdd(capture(DateAddLiteralGroup), tsId, labels)){daLabel => dateAdd(lbl(daLabel), tsId, labels)}}
      }{list => listLength(list, labels)}
    }{label => lbl(label)}

  def labelNameValid(v: String): Boolean = v match {
    case LabelNameRegex() => true
    case _ => false
  }
  private val LinkGroupID = 4
  def buttonLinkIds(str: String): List[String] = plSingleGroupCaptures(buttonLinkRegex, str, LinkGroupID)
  def buttonLinkIds(phrases: Seq[Phrase]): List[String] = phrases.flatMap(phrase => buttonLinkIds(phrase.english)).toList
  def pageLinkIds(str: String): List[String] = plSingleGroupCaptures(pageLinkRegex, str, LinkGroupID)
  def pageLinkIds(phrases: Seq[Phrase]): List[String] = phrases.flatMap(phrase => pageLinkIds(phrase.english)).toList
  def labelReferences(str: String): List[String] = plSingleGroupCaptures(labelRefRegex, str)
  def labelReference(str: String): Option[String] = plSingleGroupCaptures(labelRefRegex, str).headOption
  def listLength(listName: String, labels: Labels): Option[String] = labels.valueAsList(listName).fold[Option[String]](None){l => Some(l.length.toString)}
  def stringFromDate(when: LocalDate): String = when.format(dateFormatter)
  def stripHintPlaceholder(p: Phrase): Phrase = Phrase(hintRegex.replaceAllIn(p.english, ""), hintRegex.replaceAllIn(p.welsh, ""))
  def trimTrailing(s: String): String = s.reverse.dropWhile(_.equals(' ')).reverse

  private val TextGroup = 1
  private val WidthGroup1 = 2
  private val NoRepeatGroup1 = 3
  private val NoRepeatGroup2 = 4
  private val WidthGroup2 = 5

  def fieldAndInputOptions(s: String):(String, Boolean, Option[String]) =
    InputOptionsRegex.findFirstMatchIn(trimTrailing(s)).fold[(String, Boolean, Option[String])]((s, false, None)){m =>
      val capture = matchGroup(m) _
      capture(TextGroup).fold[(String, Boolean, Option[String])]((s, false, None)){field =>
        (capture(NoRepeatGroup1), capture(NoRepeatGroup2)) match {
          case (Some(nr1), _) => (field, true, capture(WidthGroup1))
          case (_, Some(nr2)) => (field, true, capture(WidthGroup2))
          case (_, _) => (field, false, capture(WidthGroup2))
        }
      }
    }

  def fieldAndInputOptions(p: Phrase): (Phrase, Boolean, String) = {
    val (english, dontRepeat, width) = fieldAndInputOptions(p.english)
    (Phrase(english, fieldAndInputOptions(p.welsh)._1), dontRepeat, width.getOrElse(Ten))
  }

  def fromPattern(pattern: Regex, text: String): (List[String], List[Match]) = (pattern.split(text).toList, pattern.findAllMatchIn(text).toList)
  def isLinkOnlyPhrase(phrase: Phrase): Boolean =phrase.english.matches(pageLinkOnlyPattern)
  def isBoldOnlyPhrase(phrase: Phrase): Boolean =phrase.english.matches(boldOnlyPattern)
  def stringWithOptionalHint(str: String): (String, Option[String]) = {
    val (txts, matches) = fromPattern(hintRegex, str)
    val hint = matches.headOption.map(m => m.group(1))
    (txts.mkString, hint)
  }

  def asTextString(value: String): Option[String] = value.trim.headOption.fold[Option[String]](None)(_ => Some(value.trim))
  def asNumeric(value: String): Option[BigDecimal] =
    numericRegex.findFirstIn(value.filterNot(c => c == ' ')).map(s => BigDecimal(s.filterNot(ignoredNumericChars.contains(_))))
  def asCurrency(value: String): Option[BigDecimal] =
    inputCurrencyRegex.findFirstIn(value.filterNot(c => c == ' ')).map(s => BigDecimal(s.filterNot(ignoredCurrencyChars.contains(_))))
  def asCurrencyPounds(value: String): Option[BigDecimal] =
    inputCurrencyPoundsRegex.findFirstIn(value.filterNot(c => c == ' ')).map(s => BigDecimal(s.filterNot(ignoredCurrencyChars.contains(_))))
  def asDate(value: String): Option[LocalDate] = Try(LocalDate.parse(value.filterNot(_.equals(' ')), dateFormatter)).map(d => d).toOption
  def asPositiveInt(value: String): Option[Int] = matchedInt(value, positiveIntRegex)
  def asAnyInt(value: String): Option[Int] = matchedInt(value, anyIntegerRegex)
  def asListOfPositiveInt(value: String): Option[List[Int]] = listOfPositiveIntRegex.findFirstIn(value.filterNot(_.equals(' ')))
    .flatMap(s => lOfOtoOofL(s.split(",").toList.map(asPositiveInt)))

  def datePlaceholder(date: Option[String], applyFunction: String)(implicit labels: Labels): Option[String] =
    date.flatMap { someDate =>
      asDate(someDate).flatMap(dte =>
        applyFunction match {
          case "year" => Some(dte.getYear.toString)
          case "dow_name" => Some(labels.msg(s"day.display.value.${dte.getDayOfWeek.getValue}"))
          case "month" => Some(dte.getMonthValue.toString)
          case "month_start" => Some(dte.withDayOfMonth(1).format(dateFormatter))
          case "month_end" => Some(dte.withDayOfMonth(dte.lengthOfMonth()).format(dateFormatter))
          case "month_name" => Some(labels.msg(s"month.display.value.${dte.getMonth.getValue}"))
          case "dow" => Some(dte.getDayOfWeek.getValue.toString)
          case "day" => Some(dte.getDayOfMonth.toString)
          case _ => None
        }
      )
    }

  def asTimePeriod(value: String): Option[TimePeriod] =
    timeConstantRegex.findFirstMatchIn(value.trim).flatMap{m =>
      Option(m.group(1)).fold[Option[TimePeriod]](None)(n =>
        Option(m.group(2)).fold[Option[TimePeriod]](None){
          case "day" | "days" => Some(TimePeriod(n.toInt, Day))
          case "week" | "weeks" => Some(TimePeriod(n.toInt, Week))
          case "month" | "months" => Some(TimePeriod(n.toInt, Month))
          case "year" | "years" => Some(TimePeriod(n.toInt, Year))
        }
      )
  }

  def dateAdd(date: Option[String], tsId: String, labels: Labels): Option[String] =
    labels.timescaleDays(tsId).flatMap(days => date.flatMap(asDate).map(dt => stringFromDate(dt.plusDays(days.toLong))))

  private def plSingleGroupCaptures(regex: Regex, str: String, index: Int = 1): List[String] = regex.findAllMatchIn(str).map(_.group(index)).toList
  private def matchedInt(value: String, regex: Regex): Option[Int] = regex.findFirstIn(value.filterNot(_.equals(' '))).flatMap(asInt)

  private def asInt(value: String): Option[Int] = {
    val longValue: Long = value.filterNot(_ == ',').toLong
    if (longValue < Int.MinValue || longValue > Int.MaxValue) None else Some(longValue.toInt)
  }
}
