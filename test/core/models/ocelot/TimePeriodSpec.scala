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

import base.BaseSpec
import java.time.LocalDate

class TimePeriodSpec extends BaseSpec {

  val testDate : LocalDate = LocalDate.of(1999, 9, 10)

  "A TimePeriod instance" must {
    "construct from a number of days" in {
      asTimePeriod("23455day") shouldBe Some(TimePeriod(23455, Day))
    }
    "construct from a number of weeks" in {
      asTimePeriod("23455week") shouldBe Some(TimePeriod(23455, Week))
    }
    "construct from a number of months" in {
      asTimePeriod("23455month") shouldBe Some(TimePeriod(23455, Month))
    }
    "construct from a number of years" in {
      asTimePeriod("23455year") shouldBe Some(TimePeriod(23455, Year))
    }

    "not construct when the count digit length is greater than the digit length of Int.MaxValue" in {
      asTimePeriod("12345678901day") shouldBe None
    }
  }

  import TimePeriodSupport._

  "TimePeriodSupport" must {
    "Allow addition of Days to LocalDates" in {
      testDate.add(TimePeriod(1, Day)) shouldBe LocalDate.of(1999, 9, 11)
    }

    "Allow subtraction of Days to LocalDates" in {
      testDate.minus(TimePeriod(1, Day)) shouldBe LocalDate.of(1999, 9, 9)
    }

    "Allow addition of Weeks to LocalDates" in {
      testDate.add(TimePeriod(1, Week)) shouldBe LocalDate.of(1999, 9, 17)
    }

    "Allow subtraction of Weeks to LocalDates" in {
      testDate.minus(TimePeriod(1, Week)) shouldBe LocalDate.of(1999, 9, 3)
    }

    "Allow addition of Months to LocalDates" in {
      testDate.add(TimePeriod(1, Month)) shouldBe LocalDate.of(1999, 10, 10)
    }

    "Allow subtraction of Months to LocalDates" in {
      testDate.minus(TimePeriod(1, Month)) shouldBe LocalDate.of(1999, 8, 10)
    }

    "Allow addition of Years to LocalDates" in {
      testDate.add(TimePeriod(1, Year)) shouldBe LocalDate.of(2000, 9, 10)
    }

    "Allow subtraction of Years to LocalDates" in {
      testDate.minus(TimePeriod(1, Year)) shouldBe LocalDate.of(1998, 9, 10)
    }

  }
}
