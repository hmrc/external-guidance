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

import base.BaseSpec
import java.time.LocalDate

class RatesSpec extends BaseSpec {
  val today: LocalDate = LocalDate.of(2020, 6, 24)
  val earlyYearToday: LocalDate = LocalDate.of(2018, 2, 12)
  val taxStartForNow = LocalDate.of(2020, 4, 6)
  val taxYearForNow = taxStartForNow.getYear
  val rates: Rates = new Rates(new DefaultTodayProvider)

  "Rates" must {
    // "expand valid rate placeholder" in {
    //   rates.expand("Rate: [rate:NTCReAwardManAward:days]", timescaleMap, today) shouldBe s"Timescale in days: 14"
    // }

    // "expand another valid timescale days" in {
    //   pls.expand("Timescale in days: [timescale:CHBIntCorrCOC:days]", timescaleMap, today) shouldBe s"Timescale in days: 147"
    // }

    // "expand invalid timescale days" in {
    //   pls.expand("Timescale in days: [timescale:UNKNOWN:days]", timescaleMap, today) shouldBe s"Timescale in days: [timescale:UNKNOWN:days]"
    // }

  }

  "Rates with overridden TodayProvider" must {
    val rates: Rates = new Rates(new TodayProvider {def now: LocalDate = earlyYearToday})

    "expand today" in {
      // pls.expand("Today is [timescale:today]", timescaleMap) shouldBe s"Today is 12/2/2018"
    }
  }
}
