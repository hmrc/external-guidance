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
import models.ocelot.stanzas.{Callout, Instruction, Question, Title}
import models.ocelot.{Page, Phrase, Process, ProcessJson}
import play.api.libs.json.Json
import utils.ProcessUtils._

import scala.collection.immutable.Nil

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

  "Calling the extractPageInfo method" when {
    "the page has a callout stanza" should {

      "return the correct title" in {
        val page = Page("id", "url", Seq(Callout(Title, Phrase(Vector("title1", "title2")), Seq("string"), stack = false)), Seq("1"), Seq("2"))
        val reviewInfo = extractPageInfo(page)
        reviewInfo shouldNot be(Nil)
        reviewInfo.id shouldBe "id"
        reviewInfo.pageUrl shouldBe "url"
        reviewInfo.pageTitle shouldBe "title1"
      }
    }
    "the page has a question stanza" should {
      "return the correct title" in {
        val page = Page("id",
          "url",
          Seq(
            Question(
              Phrase(Vector("title1", "title2")),
              Seq(Phrase(Vector("title1", "title2"))),
              Seq("string"),
              stack = false)),
          Seq("1"),
          Seq("2")
        )
        val reviewInfo = extractPageInfo(page)
        reviewInfo shouldNot be(Nil)
        reviewInfo.id shouldBe "id"
        reviewInfo.pageUrl shouldBe "url"
        reviewInfo.pageTitle shouldBe "title1"
      }
    }

    "the page does not have a question or callout stanza" should {
      "return the url as the title" in {
        val page = Page("id",
          "url",
          Seq(
            Instruction(
              Phrase(Vector("title1", "title2")),
              Seq("string"),
              None,
              stack = false)),
          Seq("1"),
          Seq("2")
        )
        val reviewInfo = extractPageInfo(page)
        reviewInfo shouldNot be(Nil)
        reviewInfo.id shouldBe "id"
        reviewInfo.pageUrl shouldBe "url"
        reviewInfo.pageTitle shouldBe "url"
      }
    }
  }
}
