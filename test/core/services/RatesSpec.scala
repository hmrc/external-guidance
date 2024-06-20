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
import core.models.ocelot.ProcessJson
import core.models.ocelot.Process
import java.time.LocalDate

class RatesSpec extends BaseSpec with ProcessJson {
  val rates: Rates = new Rates()
  import Rates._
  val process: Process = rawOcelotRatesJson.as[Process].copy(rates =
    Map(
      s"section1${KeySeparator}rate1${KeySeparator}2020" -> 32,
      s"section1${KeySeparator}rate1${KeySeparator}2021" -> 33,
      s"section1${KeySeparator}rate2${KeySeparator}2018" -> 765.9,
      s"section1${KeySeparator}rate2${KeySeparator}2020" -> 34,
      s"section1${KeySeparator}rate2" -> 319,
      s"section1${KeySeparator}rate2${KeySeparator}2021" -> 35.4,
      s"section1${KeySeparator}rate3${KeySeparator}2020" -> 36.5,
      s"section1${KeySeparator}rate3${KeySeparator}2021" -> 0.4,
      s"section2${KeySeparator}rate1${KeySeparator}2020" -> 33.6,
      s"section2${KeySeparator}rate1${KeySeparator}2021" -> 47,
      s"section2${KeySeparator}rate1${KeySeparator}CY" -> 4.89,
      s"section2${KeySeparator}rate1${KeySeparator}CY-3" -> 0.67,
      s"section2${KeySeparator}rate1:sub1" -> 3.67,
      s"section2${KeySeparator}rate1:sub1${KeySeparator}2021" -> 1.2,
      s"section2${KeySeparator}rate1:sub2${KeySeparator}2021" -> 2.3,
      s"section2${KeySeparator}rate1:sub1${KeySeparator}CY" -> 3.4,
      s"section2${KeySeparator}rate1:sub2${KeySeparator}CY-1" -> 4.5
    )
  )
  val today: LocalDate = LocalDate.of(2020, 6, 24)
  val earlyYearToday: LocalDate = LocalDate.of(2018, 2, 12)
  val taxStartForNow = LocalDate.of(2020, 4, 6)
  val taxYearForNow = taxStartForNow.getYear
  val todayProvider: TodayProvider = new TodayProvider{
                            def now = earlyYearToday
                            def year: String = now.getYear().toString
                          }


  "Rates.expand" must {
    "expand valid rate placeholder" in {
      rates.expand("Rate: [rate:section1:rate2:2021]", process) shouldBe "Rate: 35.4"
    }

    "expand valid rate with CY year placeholder" in {
      rates.expand("Rate: [rate:section2:rate1:CY]", process) shouldBe "Rate: 4.89"
    }

    "expand valid rate with CY and offset year placeholder" in {
      rates.expand("Rate: [rate:section2:rate1:CY-3]", process) shouldBe "Rate: 0.67"
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

    "expand valid rate placeholder with subrate usage" in {
      rates.expand("Rate: [rate:section2:rate1:sub2:2021]", process) shouldBe "Rate: 2.3"
    }

    "expand valid rate placeholder with subrate and CY year" in {
      rates.expand("Rate: [rate:section2:rate1:sub1:CY]", process) shouldBe "Rate: 3.4"
    }

    "expand valid rate placeholder with subrate and CY and offset year" in {
      rates.expand("Rate: [rate:section2:rate1:sub2:CY-1]", process) shouldBe "Rate: 4.5"
    }

    "leave placeholder unchanged if rate (with subrate) reference unknown" in {
      rates.expand("Rate: [rate:section1:rate2:sub2:2025]", process) shouldBe s"Rate: [rate:section1:rate2:sub2:2025]"
    }

    "expand valid rate with subrate placeholder with default year" in {
      rates.expand("Rate: [rate:section2:rate1:sub1]", process) shouldBe s"Rate: 3.67"
    }

  }

  "Rates.referencedNonPhraseIds" must {
    "Find all rate ids used within non-phrase text" in {
      val expected = List(s"TaxNic${KeySeparator}CTC${KeySeparator}2010", s"Legacy${KeySeparator}higherrate", s"Legacy${KeySeparator}basicrate${KeySeparator}2016")
      rates.referencedNonPhraseIds(process.flow) shouldBe expected
    }
  }

  "Rates.referenceIds" must {
    "Find all rate ids used within a phrase" in {
      val expected = List(s"section1${KeySeparator}rate2", s"section1${KeySeparator}rate2${KeySeparator}2021", s"section1${KeySeparator}rate2${KeySeparator}2018")
      rates.referencedIds("A rate [rate:section1:rate2] is followed by [rate:section1:rate2:2021] and also [rate:section1:rate2:2018]") shouldBe expected
    }
  }

  "Rates.reverseRateId" must {
    "Correctly split a valid rateId" in {
      rates.reverseRateId(s"Sector${KeySeparator}rate${KeySeparator}2001") shouldBe Some(("Sector", "rate", Some("2001")))
    }
  }

  "Rates.fullRateId" must {
    "Correctly complete rateId with implicit year" in {
      rates.fullRateId(s"Sector${KeySeparator}rate", todayProvider) shouldBe s"Sector${KeySeparator}rate${KeySeparator}${todayProvider.year}"
    }

    "Correctly complete rateId with CY year" in {
      rates.fullRateId(s"Sector${KeySeparator}rate${KeySeparator}CY", todayProvider) shouldBe s"Sector${KeySeparator}rate${KeySeparator}${todayProvider.cyYear()}"
    }

    "Correctly complete rateId with CY arithmetic year" in {
      rates.fullRateId(s"Sector${KeySeparator}rate${KeySeparator}CY-1", todayProvider) shouldBe s"Sector${KeySeparator}rate${KeySeparator}${todayProvider.cyYear(Some("-1"))}"
    }

  }

}
