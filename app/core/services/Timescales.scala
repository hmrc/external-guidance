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
import java.time.LocalDate
import scala.util.matching.Regex
import Regex._

trait TimescaleProvider {
  val timescales: Timescales
}

trait TodayProvider {
  def now: LocalDate
}

@Singleton
class DefaultTodayProvider extends TodayProvider {
  def now: LocalDate = LocalDate.now
}

@Singleton
class Timescales @Inject() (tp: TodayProvider) {
  val TaxYearStartDay: Int = 6
  val TaxYearStartMonth: Int = 4
  val TaxYearPattern: String = "CY([\\-+]\\d+)?"
  val timescalePattern = s"\\[timescale:(?:(?:($TimescaleIdPattern):days)|(?:($DatePattern)|(today)|($TaxYearPattern))(?::(long|short))?)\\]"
  val timescaleRegex: Regex = timescalePattern.r

  def long(date: LocalDate): Int = date.getYear
  def short(date: LocalDate): Int = long(date) % 100
  def cy(offset: Int, when: LocalDate): LocalDate = {
    val candidateStartDate = when.withMonth(TaxYearStartMonth).withDayOfMonth(TaxYearStartDay)
    candidateStartDate.minusYears(-offset + (if (when.isBefore(candidateStartDate)) 1 else 0))
  }

  private val TimescaleIdGroup: Int = 1
  private val DateLiteralGroup: Int = 2
  private val TodayGroup: Int = 3
  private val CyOffsetGroup: Int = 5
  private val LongOrShortGroup: Int = 6

  def expand(str: String, todaysDate: LocalDate = tp.now): String = {
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

    timescaleRegex.replaceAllIn(str,m => Option(m.group(TimescaleIdGroup)).fold(dateTimescale(m))(tsId => timescaleDays(tsId).getOrElse(m.group(0))))
  }
}
