/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.libs.json._


class RunModeSpec extends BaseSpec {

  "RunMode serialisation" must {
    "Serialise Scratch" in {
      val m: RunMode = Scratch
      Json.toJson(m) shouldBe JsString("Scratch")
    }
    "Serialise Approval" in {
      val m: RunMode = Approval
      Json.toJson(m) shouldBe JsString("Approval")
    }
    "Serialise PageReview" in {
      val m: RunMode = PageReview
      Json.toJson(m) shouldBe JsString("PageReview")
    }
    "Serialise Published" in {
      val m: RunMode = Published
      Json.toJson(m) shouldBe JsString("Published")
    }
  }

  "RunMode deserialisation" must {
    "Deserialise Scratch" in {
      Json.parse(""""Scratch"""").as[RunMode] shouldBe Scratch
    }
    "Deserialise Approval" in {
      Json.parse(""""Approval"""").as[RunMode] shouldBe Approval
    }
    "Deserialise PageReview" in {
      Json.parse(""""PageReview"""").as[RunMode] shouldBe PageReview
    }
    "Deserialise Published" in {
      Json.parse(""""Published"""").as[RunMode] shouldBe Published
    }

    "Deserialise Unknown" in {
      try{
        Json.parse(""""Blah"""").as[RunMode]
      } catch {
        case jse: JsResultException => succeed
        case _: Throwable => fail
      }

    }

  }

}
