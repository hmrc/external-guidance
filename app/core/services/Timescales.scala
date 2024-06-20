/*
 * Copyright 2024 HM Revenue & Customs
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
import java.time.LocalDate
import scala.util.matching.Regex
import Regex._

trait TodayProvider {
  private val TaxYearStartDay: Int = 6
  private val TaxYearStartMonth: Int = 4
  def now: LocalDate
  def year: String
  def cy(offset: Option[String] = None): LocalDate = {
    val candidateStartDate = now.withMonth(TaxYearStartMonth).withDayOfMonth(TaxYearStartDay)
    candidateStartDate.minusYears(-offset.map(_.toInt).getOrElse(0) + (if (now.isBefore(candidateStartDate)) 1 else 0))
  }
  def cyYear(offset: Option[String] = None): String = cy(offset).getYear().toString
}

@Singleton
class DefaultTodayProvider extends TodayProvider {
  def now: LocalDate = LocalDate.now
  def year: String = now.getYear().toString
}

@Singleton
class Timescales @Inject() (tp: TodayProvider) extends LabelledDataExpansion with LabelledDataReferencing{
  private val DateAddTimescaleId: Int = 3
  private val TimescaleId: Int = 4

  private[services] def referencedIds(s: String): List[String] = TimescaleIdUsageRegex.findAllMatchIn(s).toList.flatMap{m =>
    Option(m.group(DateAddTimescaleId)).fold{
      Option(m.group(TimescaleId)).fold(List.empty[String]){id => List(id)}
    }(id => List(id))
  }

  private val TaxYearPattern: String = "CY([\\-+]\\d+)?"
  private val timescalesPattern = s"\\[timescale:(?:(?:($TimescaleIdPattern):days)|(?:($DatePattern)|(today)|($TaxYearPattern))(?::(long|short))?)\\]"
  private val timescalesRegex: Regex = timescalesPattern.r

  private[services] def long(date: LocalDate): Int = date.getYear
  private[services] def short(date: LocalDate): Int = long(date) % 100

  private val TimescaleIdGroup: Int = 1
  private val DateLiteralGroup: Int = 2
  private val TodayGroup: Int = 3
  private val CyOffsetGroup: Int = 5
  private val LongOrShortGroup: Int = 6

  def expand(str: String, process: Process): String = expand(str, process.timescales, tp.now)

  private[services] def expand(str: String, timescaleDefns: Map[String, Int], todaysDate: LocalDate): String = {
    def longOrShort(m: Match, when: LocalDate): String = Option(m.group(LongOrShortGroup)).fold(stringFromDate(when)){
      case "long" => long(when).toString
      case "short" => short(when).toString
    }

    def dateTimescale(m: Match): String =
      Option(m.group(DateLiteralGroup)).fold{
        Option(m.group(TodayGroup)).fold{
          Option(m.group(CyOffsetGroup)).fold{
            longOrShort(m, tp.cy())
          }{offset => longOrShort(m, tp.cy(Some(offset)))}
        }{_ => longOrShort(m, todaysDate)}
      }{literal => literal}

    timescalesRegex.replaceAllIn(str, m => {
      Option(m.group(TimescaleIdGroup)).fold(dateTimescale(m)){tsId =>
        timescaleDefns.get(tsId).fold(m.group(0))(_.toString)
      }
    })
  }
}
