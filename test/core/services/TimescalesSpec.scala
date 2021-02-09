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
  val ts: Timescales = new Timescales
  val today: LocalDate = LocalDate.of(2020, 6, 24)
  val taxStartForNow = LocalDate.of(2020, 4, 6)
  val taxYearForNow = taxStartForNow.getYear

  "Timescales" must {
    "determine long and short year from a date" in {
      ts.long(today) shouldBe 2020
      ts.short(today) shouldBe 20
    }

    "determine tax year start date from a current date" in {
      ts.CY(0, today) shouldBe taxStartForNow
    }

    "determine previous and future tax year from a current date" in {
      for(i <- Range(0, 10)) {
        ts.CY(i, today).getYear shouldBe taxYearForNow + i
        ts.CY(-i, today).getYear shouldBe taxYearForNow -i
      }
    }

    "translate today" in {
      ts.translate("Today is [timescale:today]", today) shouldBe s"Today is 24/6/2020"
    }

    "translate today:long" in {
      ts.translate("Today is [timescale:today:long]", today) shouldBe s"Today is 2020"
    }

    "translate today:short" in {
      ts.translate("Today is [timescale:today:short]", today) shouldBe s"Today is 20"
    }

    "translate CY" in {
      ts.translate("Tax year start date: [timescale:CY]", today) shouldBe s"Tax year start date: 6/4/2020"
    }

    "translate CY with -/- offsets" in {
      ts.translate("Tax year start date: [timescale:CY-1]", today) shouldBe s"Tax year start date: 6/4/2019"
      ts.translate("Tax year start date: [timescale:CY-2]", today) shouldBe s"Tax year start date: 6/4/2018"
      ts.translate("Tax year start date: [timescale:CY+1]", today) shouldBe s"Tax year start date: 6/4/2021"
      ts.translate("Tax year start date: [timescale:CY+2]", today) shouldBe s"Tax year start date: 6/4/2022"
    }

    "translate CY:long" in {
      ts.translate("Tax year start: [timescale:CY:long]", today) shouldBe s"Tax year start: 2020"
    }

    "translate CY:short" in {
      ts.translate("Tax year start: [timescale:CY:short]", today) shouldBe s"Tax year start: 20"
    }

  }
}