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

package models.errors

import base.UnitSpec
import play.api.libs.json.Json

class ErrorsSpec extends UnitSpec {

  "Contructing errors" should {
    "Create an error containing details of many errors" in {
      val details = List(ErrorDetail("First","id"), ErrorDetail("Second", "id2"))
      val err = Error("Blah", details)

      err shouldBe Error("Blah", None, Some(details))

      val err2 = Error(details)

      err2 shouldBe Error(Error.UnprocessableEntity, None, Some(details))
    }
  }

  "Serialising a single error into JSON" should {
    "generate the correct JSON" in {
      val expected = Json.parse(
        """
          |{
          |  "code": "SOME_CODE",
          |  "message": "some message"
          |}
        """.stripMargin
      )

      val error = Error("SOME_CODE", "some message")

      val result = Json.toJson(error)

      result shouldBe expected
    }
  }

  "Serialising multiple errors into JSON" should {
    "generate the correct JSON" in {
      // val expected = Json.parse(
      //   """
      //     |{
      //     |  "errors": [
      //     |    {
      //     |      "code": "SOME_CODE_1",
      //     |      "message": "some message 1"
      //     |    },
      //     |    {
      //     |      "code": "SOME_CODE_2",
      //     |      "message": "some message 2"
      //     |    }
      //     |  ]
      //     |}
      //   """.stripMargin
      // )

      // val errors = Errors(
      //   Seq(
      //     Error("SOME_CODE_1", "some message 1"),
      //     Error("SOME_CODE_2", "some message 2")
      //   )
      // )

      // val result = Json.toJson(errors)

      // result shouldBe expected
    }
  }
}
