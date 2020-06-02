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
import models.ocelot.{Process, ProcessJson}
import play.api.libs.json.Json
import utils.ProcessUtils._

class ProcessUtilsSpec extends UnitSpec with ProcessJson {

  val process: Process = prototypeJson.as[Process]

  val processWithNoPageStanzas: Process = Json
    .parse(
      """
      |{
      |  "meta": {
      |    "title": "Customer wants to make a cup of tea",
      |    "id": "oct90001",
      |    "ocelot": 1,
      |    "lastAuthor": "000000",
      |    "lastUpdate": 1500298931016,
      |    "version": 4,
      |    "filename": "oct90001.js"
      |  },
      |  "howto": [],
      |  "contacts": [],
      |  "links": [],
      |  "flow": {
      |    "3": {
      |      "type": "InstructionStanza",
      |      "text": 1,
      |      "next": [
      |        "2"
      |      ],
      |      "stack": true
      |    },
      |    "2": {
      |      "type": "InstructionStanza",
      |      "text": 0,
      |      "next": [
      |        "end"
      |      ],
      |      "stack": true
      |    },
      |    "end": {
      |      "type": "EndStanza"
      |    }
      |  },
      |  "phrases": [
      |    ["no - they don’t have a cup", "Welsh, no - they don’t have a cup"]
      |  ]
      |}
    """.stripMargin
    )
    .as[Process]

  "Calling the extract Pages" when {
    "the process contains PageStanzas" should {

      "return a list of pages" in {

        val list = extractPages(process)
        list.size shouldBe 28

      }
    }

    "the process does not contain PageStanzas" should {
      "return an empty list" in {
        val list = extractPages(processWithNoPageStanzas)
        list.size shouldBe 0
      }
    }
  }

}
