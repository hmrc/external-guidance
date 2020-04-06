/*
 * Copyright 2020 HM Revenue & Customs
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

package utils

import base.UnitSpec

import utils.Validators._

import models.errors.{BadRequestError, Errors}

class ValidatorsSpec extends UnitSpec  {

  val validProcessId: String = "ext90001"
  val invalidProcessId: String = ""

  "The validators object process identifier validation" should {

    "Return a valid process id" in {

      validateProcessId(validProcessId) shouldBe Right(validProcessId)
    }

    "Return a bad request error when the process id is invalid" in {

      validateProcessId(invalidProcessId) shouldBe Left(Errors(BadRequestError))
    }
  }

}
