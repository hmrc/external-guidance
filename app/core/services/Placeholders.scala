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

trait PlaceholderProvider {
  val placeholders: Placeholders
}

trait TodayProvider {
  def now: LocalDate
}

@Singleton
class DefaultTodayProvider extends TodayProvider {
  def now: LocalDate = LocalDate.now
}

@Singleton
class Placeholders @Inject() (tp: TodayProvider) {
  val TaxYearStartDay: Int = 6
  val TaxYearStartMonth: Int = 4
  val timescalePattern = "\\[timescale:((\\d{1,2}\\/\\d{1,2}\\/\\d{4})|(today|CY([\\-+]\\d+)?)(:(long|short))?)\\]"
  val timescaleRegex: Regex = s"${timescalePattern}".r

  def long(date: LocalDate): Int = date.getYear
  def short(date: LocalDate): Int = long(date) % 100
  def cy(offset: Int, when: LocalDate): LocalDate = {
    val candidateStartDate = when.withMonth(TaxYearStartMonth).withDayOfMonth(TaxYearStartDay)
    candidateStartDate.minusYears(-offset + (if (when.isBefore(candidateStartDate)) 1 else 0))
  }

  private val DateLiteralGroup: Int = 2
  private val TodayOrCyGroup: Int = 3
  private val CyOffsetGroup: Int = 4
  private val LongOrShortGroup: Int = 6

  def translate(str: String, todaysDate: LocalDate = tp.now): String = {
    def longOrShort(m: Match, when: LocalDate): String = Option(m.group(LongOrShortGroup)).fold(stringFromDate(when)){
      case "long" => long(when).toString
      case "short" => short(when).toString
    }

    timescaleRegex.replaceAllIn(str,{m =>
      Option(m.group(DateLiteralGroup)).fold[String]{
        Option(m.group(TodayOrCyGroup)) match {
          case Some("today") => longOrShort(m, todaysDate)
          case Some(_) => longOrShort(m, Option(m.group(CyOffsetGroup)).fold(cy(0, todaysDate))(offset => cy(offset.toInt, todaysDate)))
          case None => Option(m.matched).getOrElse("") // Should never occur, however group() can return null!!
        }
      }{literal => literal}
    })
  }
}