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

package core.models

import base.BaseSpec

class CoreModelsPackageSpec extends BaseSpec {

  "List of Options conversion" must {
    "Return None if passed a empty list" in {
      lOfOtoOofL(Nil) shouldBe None
    }

    "Return Some(list) if all elements defined" in {
      lOfOtoOofL(List(Some(1), Some(2), Some(5), Some(8))) shouldBe Some(List(1, 2, 5, 8))
    }

    "Return None if not all elements defined" in {
      lOfOtoOofL(List(Some(1), Some(2), None, Some(8))) shouldBe None
    }
  }
}
