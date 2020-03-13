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

package data

import play.api.libs.json.{JsValue, Json}

object ExamplePayloads {

  val simpleValidProcess: JsValue = Json.parse(
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
      |    "1": {
      |      "type": "ValueStanza",
      |      "values": [
      |        {
      |          "type": "scalar",
      |          "label": "PageUrl",
      |          "value": "/feeling-bad"
      |        }
      |      ],
      |      "next": ["3"],
      |      "stack": true
      |    },
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
      |    ["Ask the customer if they have a tea bag", "Welsh, Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh, Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh, Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh, No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh, Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh, Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh, yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh, no - they don’t have a cup"]
      |  ]
      |}
    """.stripMargin
  )
}
