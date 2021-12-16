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

package core.services

import javax.inject.{Inject, Singleton}
import core.models.ocelot._
import core.models.ocelot.stanzas._
import java.time.LocalDate
import scala.util.matching.Regex
import Regex._

trait TimescaleExpansion {
  def expand(str: String, timescaleDefns: Map[String, Int]): String
}

trait TodayProvider {
  def now: LocalDate
}

@Singleton
class DefaultTodayProvider extends TodayProvider {
  def now: LocalDate = LocalDate.now
}

@Singleton
class Timescales @Inject() (tp: TodayProvider) extends TimescaleExpansion {

  // Page Analysis
  private val DateAddTimescaleId: Int = 3
  private val TimescaleId: Int = 4

  def referencedIds(p: Phrase): List[String] = (referencedIds(p.english) ++ referencedIds(p.welsh)).distinct
  def referencedIds(p: Seq[Phrase]): List[String] = p.toList.flatMap(referencedIds)

  def referencedNonPhraseIds(flow: Map[String, Stanza]): List[String] =
    flow.values.toList.flatMap{
      case s: CalculationStanza => referencedIds(s.calcs.toList.flatMap(c => List(c.left, c .right)))
      case s: ValueStanza => referencedIds(s.values.toList.flatMap(c => List(c.value)))
      case s: ChoiceStanza => referencedIds(s.tests.toList.flatMap(c => List(c.left, c .right)))
      case _ => Nil
    }

  private[services] def referencedIds(stringList: List[String]): List[String] = stringList.flatMap(referencedIds)
  private[services] def referencedIds(s: String): List[String] = TimescaleIdUsageRegex.findAllMatchIn(s).toList.flatMap{m =>
    Option(m.group(DateAddTimescaleId)).fold{
      Option(m.group(TimescaleId)).fold(List.empty[String]){id => List(id)}
    }(id => List(id))
  }

  private val TaxYearStartDay: Int = 6
  private val TaxYearStartMonth: Int = 4
  private val TaxYearPattern: String = "CY([\\-+]\\d+)?"
  private val timescalesPattern = s"\\[timescale:(?:(?:($TimescaleIdPattern):days)|(?:($DatePattern)|(today)|($TaxYearPattern))(?::(long|short))?)\\]"
  private val timescalesRegex: Regex = timescalesPattern.r

  private[services] def long(date: LocalDate): Int = date.getYear
  private[services] def short(date: LocalDate): Int = long(date) % 100
  private[services] def cy(offset: Int, when: LocalDate): LocalDate = {
    val candidateStartDate = when.withMonth(TaxYearStartMonth).withDayOfMonth(TaxYearStartDay)
    candidateStartDate.minusYears(-offset + (if (when.isBefore(candidateStartDate)) 1 else 0))
  }

  private val TimescaleIdGroup: Int = 1
  private val DateLiteralGroup: Int = 2
  private val TodayGroup: Int = 3
  private val CyOffsetGroup: Int = 5
  private val LongOrShortGroup: Int = 6

  def expand(str: String, timescaleDefns: Map[String, Int]): String = expand(str, timescaleDefns, tp.now)
  private[services] def expand(str: String, timescaleDefns: Map[String, Int], todaysDate: LocalDate): String = {
    def longOrShort(m: Match, when: LocalDate): String = Option(m.group(LongOrShortGroup)).fold(stringFromDate(when)){
      case "long" => long(when).toString
      case "short" => short(when).toString
    }

    def dateTimescale(m: Match): String =
      Option(m.group(DateLiteralGroup)).fold{
        Option(m.group(TodayGroup)).fold{
          Option(m.group(CyOffsetGroup)).fold{
            longOrShort(m, cy(0, todaysDate))
          }{offset => longOrShort(m, cy(offset.toInt, todaysDate))}
        }{_ => longOrShort(m, todaysDate)}
      }{literal => literal}

    timescalesRegex.replaceAllIn(str, m =>
      Option(m.group(TimescaleIdGroup)).fold(dateTimescale(m)){tsId =>
        timescaleDefns.getOrElse(tsId, m.group(0)).toString
      }
    )
  }
}
