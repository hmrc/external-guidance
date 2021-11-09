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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Json, JsObject}
import core.services.{DefaultTodayProvider, Timescales, PageBuilder}

class PrototypeJsonSpec extends AnyWordSpec with Matchers {

  trait Test {
    val jsObject: JsObject = Json.parse(PrototypeJson.json).as[JsObject]
    val process: Process = jsObject.as[Process]
    val pageBuilder = new PageBuilder(new Timescales(new DefaultTodayProvider))
  }

  "Prototype Json" must {

    "Parse into a valid Process object" in new Test {
      pageBuilder.pages(process) match {
        case Right(pages) => succeed
        case Left(err) => fail(s"Invalid json ${err}")
      }
    }
  }
}
