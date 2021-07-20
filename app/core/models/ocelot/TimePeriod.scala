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

package core.models.ocelot

import java.time.LocalDate

trait TimeUnit

case object Day extends TimeUnit
case object Week extends TimeUnit
case object Month extends TimeUnit
case object Year extends TimeUnit

case class TimePeriod(value: Int, unit: TimeUnit)

trait TimePeriodArithmetic[A] {
  def add(tp: TimePeriod)(value: A): A
  def minus(tp: TimePeriod)(value: A): A
}

object TimePeriodArithmetic {
  implicit val dateArithmetic: TimePeriodArithmetic[LocalDate] =
    new TimePeriodArithmetic[LocalDate]{
      def add(tp: TimePeriod)(value: LocalDate): LocalDate =
        tp.unit match {
          case Day => value.plusDays(tp.value)
          case Week => value.plusWeeks(tp.value)
          case Month => value.plusMonths(tp.value)
          case Year => value.plusYears(tp.value)
        }
      def minus(tp: TimePeriod)(value: LocalDate): LocalDate =
        tp.unit match {
          case Day => value.minusDays(tp.value)
          case Week => value.minusWeeks(tp.value)
          case Month => value.minusMonths(tp.value)
          case Year => value.minusYears(tp.value)
        }
    }

  // Syntax
  implicit class TimePeriodArithmeticOps[A](value: A) {
    def add(tp: TimePeriod)(implicit a: TimePeriodArithmetic[A]): A = a.add(tp)(value)
    def minus(tp: TimePeriod)(implicit a: TimePeriodArithmetic[A]): A = a.minus(tp)(value)
  }
}
