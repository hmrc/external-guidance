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

package models.ocelot

import base.BaseSpec
import play.api.libs.json._
import models.ocelot.stanzas.Stanza
import services.SecuredProcessBuilder

class ProcessSpec extends BaseSpec with ProcessJson {

  val oneHundred: Int = 100

  val meta: Meta = Json.parse(prototypeMetaSection).as[Meta]
  val flow: Map[String, Stanza] = Json.parse(prototypeFlowSection).as[Map[String, Stanza]]
  val phrases: Vector[Phrase] = Json.parse(prototypePhrasesSection).as[Vector[Phrase]]
  val links: Vector[Link] = Json.parse(prototypeLinksSection).as[Vector[Link]]

  val process: Process = prototypeJson.as[Process]
  val protectedProcess = validOnePageProcessWithPassPhrase.as[Process]
  val securedProcessBuilder = new SecuredProcessBuilder()

  "Process" must {

    "Deserialise from prototype json" in {

      process.meta shouldBe meta
      process.flow shouldBe flow
      process.phrases shouldBe phrases
      process.links shouldBe links

    }

    missingJsObjectAttrTests[Process](prototypeJson, List("howto", "contacts"))

    incorrectPropertyTypeJsObjectAttrTests[Process](prototypeJson, List("howto", "contacts"))
  }

  "Process phrase fn" must {
    "Return phrase valid index" in {

      val phrase = Phrase(Vector("Telling HMRC about extra income", "Welsh: Telling HMRC about extra income"))
      process.phraseOption(0) shouldBe Some(phrase)
    }

    "Return None for a invalid index" in {

      process.phraseOption(oneHundred) shouldBe None
    }
  }

  "Process passphrase" must {
    "detect passphrase when present" in {
      protectedProcess.passPhrase shouldBe Some("A not so memorable phrase")
    }

    "Not detect passphrase when not present" in {
      process.passPhrase shouldBe None
    }

    "return the process start url with an unsecured process from startUrl" in {
      process.startUrl shouldBe Some("/start")
    }

    "return the process start url with a secured process from startUrl" in {
      securedProcessBuilder.secure(protectedProcess).startUrl shouldBe Some("/feeling-bad")
    }

    "return StartStanzaId with an unsecured process from startPageId" in {
      process.startPageId shouldBe Process.StartStanzaId
    }

    "return PassPhrasePageId with a secured process from startPageId" in {
      securedProcessBuilder.secure(protectedProcess).startPageId shouldBe Process.PassPhrasePageId
    }

  }

  "Process link fn" must {

    "Return link for valid index" in {

      val link: Link = Link(0, "http://www.bbc.co.uk/news", "BBC News", window = false)

      process.linkOption(0) shouldBe Some(link)
    }

    "Return None for an invalid index" in {

      process.linkOption(ten) shouldBe None
    }
  }
}
