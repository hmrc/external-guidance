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

import base.BaseSpec
import java.time.LocalDate

class TimescalesSpec extends BaseSpec {
  val today: LocalDate = LocalDate.of(2020, 6, 24)
  val earlyYearToday: LocalDate = LocalDate.of(2018, 2, 12)
  val taxStartForNow = LocalDate.of(2020, 4, 6)
  val taxYearForNow = taxStartForNow.getYear
  val pls: Timescales = new Timescales(new DefaultTodayProvider)

  "Timescales" must {
    "determine long and short year from a date" in {
      pls.long(today) shouldBe 2020
      pls.short(today) shouldBe 20
    }

    "determine tax year start date from a current date" in {
      pls.cy(0, today) shouldBe taxStartForNow
    }

    "determine previous and future tax year from a current date" in {
      for(i <- Range(0, 10)) {
        pls.cy(i, today).getYear shouldBe taxYearForNow + i
        pls.cy(-i, today).getYear shouldBe taxYearForNow -i
      }
    }

    "determine start of current tax year when today is first day of tax year" in {
      pls.cy(0, taxStartForNow) shouldBe taxStartForNow
    }

    "expand today" in {
      pls.expand("Today is [timescale:today]", today) shouldBe s"Today is 24/6/2020"
    }

    "expand today:long" in {
      pls.expand("Today is [timescale:today:long]", today) shouldBe s"Today is 2020"
    }

    "expand today:short" in {
      pls.expand("Today is [timescale:today:short]", today) shouldBe s"Today is 20"
    }

    "expand CY" in {
      pls.expand("Tax year start date: [timescale:CY]", today) shouldBe s"Tax year start date: 6/4/2020"
    }

    "expand CY with -/- offsets" in {
      pls.expand("Tax year start date: [timescale:CY-1]", today) shouldBe s"Tax year start date: 6/4/2019"
      pls.expand("Tax year start date: [timescale:CY-2]", today) shouldBe s"Tax year start date: 6/4/2018"
      pls.expand("Tax year start date: [timescale:CY+1]", today) shouldBe s"Tax year start date: 6/4/2021"
      pls.expand("Tax year start date: [timescale:CY+2]", today) shouldBe s"Tax year start date: 6/4/2022"
    }

    "expand CY:long" in {
      pls.expand("Tax year start: [timescale:CY:long]", today) shouldBe s"Tax year start: 2020"
    }

    "expand CY:short" in {
      pls.expand("Tax year start: [timescale:CY:short]", today) shouldBe s"Tax year start: 20"
    }

    "expand date literals" in {
      pls.expand("A date literal: [timescale:23/5/1999]", today) shouldBe s"A date literal: 23/5/1999"
    }

    "expand valid timescale days" in {
      pls.expand("Timescale in days: [timescale:NTCReAwardManAward:days]", today) shouldBe s"Timescale in days: 14"
    }

    "expand another valid timescale days" in {
      pls.expand("Timescale in days: [timescale:CHBIntCorrCOC:days]", today) shouldBe s"Timescale in days: 147"
    }

    "expand invalid timescale days" in {
      pls.expand("Timescale in days: [timescale:UNKNOWN:days]", today) shouldBe s"Timescale in days: [timescale:UNKNOWN:days]"
    }

  }

  "Timescales with overridden TodayProvider" must {
    val pls: Timescales = new Timescales(new TodayProvider {def now: LocalDate = earlyYearToday})

    "expand today" in {
      pls.expand("Today is [timescale:today]") shouldBe s"Today is 12/2/2018"
    }

    "expand today:long" in {
      pls.expand("Today is [timescale:today:long]") shouldBe s"Today is 2018"
    }

    "expand today:short" in {
      pls.expand("Today is [timescale:today:short]") shouldBe s"Today is 18"
    }

    "expand CY" in {
      pls.expand("Tax year start date: [timescale:CY]") shouldBe s"Tax year start date: 6/4/2017"
    }

    "expand CY with -/- offsets" in {
      pls.expand("Tax year start date: [timescale:CY-1]") shouldBe s"Tax year start date: 6/4/2016"
      pls.expand("Tax year start date: [timescale:CY-2]") shouldBe s"Tax year start date: 6/4/2015"
      pls.expand("Tax year start date: [timescale:CY+1]") shouldBe s"Tax year start date: 6/4/2018"
      pls.expand("Tax year start date: [timescale:CY+2]") shouldBe s"Tax year start date: 6/4/2019"
    }

  }
}