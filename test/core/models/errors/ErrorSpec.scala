/*
 * Copyright 2022 HM Revenue & Customs
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

package core.models.errors

import base.BaseSpec

import core.models.ocelot.Published
import core.models.ocelot.errors.{GuidanceError, UnsupportedUiPatternError, InvalidFieldWidth}

class ErrorSpec extends BaseSpec {
  import Error._

  "Error companion object" should {
    "translate RuntimeError into Error" in {
      val error: Error = Error(UnsupportedUiPatternError, Published, Some("stanzaId"))
      error shouldBe Error(ExecutionError, List(UnsupportedUiPatternError), Some(Published), Some("stanzaId"))
    }

    "translate List[GuidanceError] into Error" in {
      val errors: List[GuidanceError] = List(InvalidFieldWidth("id"))
      Error(errors) shouldBe Error(UnprocessableEntity, errors, None, None)
    }

    "translate GuidanceError into Error" in {
      val error: GuidanceError = InvalidFieldWidth("id")
      Error(error) shouldBe Error(UnprocessableEntity, List(error), None, None)
    }
  }
}
