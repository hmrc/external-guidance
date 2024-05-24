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
import core.models.ocelot.ProcessJson
import core.models.ocelot.Process

class RatesSpec extends BaseSpec with ProcessJson {
  val today: LocalDate = LocalDate.of(2024, 6, 24)
  val earlyYearToday: LocalDate = LocalDate.of(2018, 2, 12)
  val rates: Rates = new Rates(new TodayProvider{ def now: LocalDate = today })

  val process: Process = prototypeJson.as[Process].copy(rates =
    Map(
      "section1-rate1-2020" -> 32,
      "section1-rate1-2021" -> 33,
      "section1-rate2-2018" -> 765.9,
      "section1-rate2-2020" -> 34,
      "section1-rate2-2024" -> 319,
      "section1-rate2-2021" -> 35.4,
      "section1-rate3-2020" -> 36.5,
      "section1-rate3-2021" -> 0.4,
      "section2-rate1-2020" -> 33.6,
      "section2-rate1-2021" -> 47
    )
  )

  "Rates.expand" must {
    "expand valid rate placeholder" in {
      rates.expand("Rate: [rate:section1:rate2:2021]", process) shouldBe s"Rate: 35.4"
    }

    "leave placeholder unchanged if rate reference invalid" in {
      rates.expand("Rate: [rate:section1:rate2:2025]", process) shouldBe s"Rate: [rate:section1:rate2:2025]"
    }

    "expand valid rate placeholder with default year" in {
      rates.expand("Rate: [rate:section1:rate2]", process) shouldBe s"Rate: 319"
    }

    "expand multiple rate placeholder within a phrase" in {
      val expected = "A rate 319 is followed by 35.4 and also 765.9"
      rates.expand("A rate [rate:section1:rate2] is followed by [rate:section1:rate2:2021] and also [rate:section1:rate2:2018]", process) shouldBe expected
    }
  }

  "Rates.expand with overridden TodayProvider" must {
    val rates: Rates = new Rates(new TodayProvider {def now: LocalDate = earlyYearToday})

    "expand valid rate placeholder" in {
      rates.expand("Rate: [rate:section1:rate2]", process) shouldBe s"Rate: 765.9"
    }
  }

  "Rates.referenceIds" must {
    "Find all rate ids used within a phrase" in {
      val expected = List("section1-rate2", "section1-rate2-2021", "section1-rate2-2018")
      rates.referencedIds("A rate [rate:section1:rate2] is followed by [rate:section1:rate2:2021] and also [rate:section1:rate2:2018]") shouldBe expected
    }
  }
}
