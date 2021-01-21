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

package core.services

import base.BaseSpec
import core.models.ocelot.stanzas._
import core.models.ocelot._
import play.api.libs.json._
import utils.StanzaHelper


class GraphPageBuilderSpec extends BaseSpec with ProcessJson with StanzaHelper {

  // Define instance of class used in testing
  val pageBuilder: PageBuilder = new PageBuilder()

  val meta: Meta = Json.parse(prototypeMetaSection).as[Meta]

  case object DummyStanza extends Stanza {
    override val next: Seq[String] = Seq("1")
  }

  trait GraphTest {
    val pageId1 = Process.StartStanzaId
    val pageId2 = "4"
    val pageId3 = "6"
    val pageId4 = "9"
    val pageId5 = "11"
    val pageId6 = "14"
    val pageId7 = "17"
    val pageIds = Seq(pageId1, pageId2, pageId3, pageId4, pageId5, pageId6, pageId7)
    val choiceTest = ChoiceStanzaTest("yes", LessThanOrEquals, "No")
    private val flow = Map(
      pageId1 -> PageStanza("/start", Seq("1"), false),
      "1" -> InstructionStanza(3, Seq("2"), None, false),
      "2" -> QuestionStanza(1, Seq(2, 1), Seq("3", "3"), None, false),
      "3" -> ChoiceStanza(Seq(pageId2, pageId4), Seq(choiceTest), false),
      pageId2 -> PageStanza("/this4", Seq("5"), false),
      "5" -> InstructionStanza(1, Seq("end"), Some(2), false),
      pageId3 -> PageStanza("/this6", Seq("7"), false),
      "7" -> InstructionStanza(2, Seq("8"), None, false),
      "8" -> QuestionStanza(1, Seq(2, 3), Seq(pageId4, pageId6), None, false),
      pageId4 -> PageStanza("/this9", Seq("16"), false),
      "16" -> InstructionStanza(3, Seq("161"), None, false),
      "161"-> ValueStanza(List(Value(Scalar, "error", "0")), Seq("10"), false),
      "10" -> InstructionStanza(2, Seq("100"), None, false),
      "100" -> ChoiceStanza(Seq("161", pageId6, pageId5),Seq(choiceTest), false),
      pageId5 -> PageStanza("/this11", Seq("12"), false),
      "12" -> InstructionStanza(0, Seq("13"), None, false),
      "13" -> QuestionStanza(1, Seq(2, 3), Seq(pageId6, pageId2), None, false),
      pageId6 -> PageStanza("/this14", Seq("15"), false),
      "15" -> InstructionStanza(0, Seq("end"), None, false),
      pageId7 -> PageStanza("/this15", Seq("18"), false),
      "18" -> InstructionStanza(0, Seq("end"), None, false),
      "end" -> EndStanza
    )

    private val phrases = Vector[Phrase](
      Phrase(Vector("Some Text", "Welsh, Some Text")),
      Phrase(Vector(s"Some Text1 [link:Link to stanza 17:$pageId7]", s"Welsh, Some Text1 [link:Link to stanza 17:$pageId7]")),
      Phrase(Vector(s"Some [link:PageId3:$pageId3] Text2", s"Welsh, Some [link:PageId3:$pageId3] Text2")),
      Phrase(Vector(s"Some [link:Link to stanza 11:$pageId5] Text3", s"Welsh, Some [link:Link to stanza 11:$pageId5] Text3"))
    )

    private val links = Vector(Link(0, pageId3, "", false), Link(1, pageId6, "", false), Link(2, Process.StartStanzaId, "Back to the start", false))

    val process = Process(metaSection, flow, phrases, links)
  }

  "Sequence of connected pages" must {

    "be extractable from a Process using key 'start''" in new GraphTest {

      pageBuilder.pages(process) match {
        case Right(pages) =>
          pages shouldNot be(Nil)

          pages.length shouldBe 7

        case Left(err) => fail(s"GuidanceError $err")
      }

    }

    "should contain the correct number of stanzas" in new GraphTest {

      pageBuilder.pages(process) match {
        case Right(pages) =>
          pages shouldNot be(Nil)

          pages(0).stanzas.length shouldBe 4
          pages(1).stanzas.length shouldBe 5
          pages(2).stanzas.length shouldBe 3
          pages(3).stanzas.length shouldBe 3
          pages(4).stanzas.length shouldBe 3
          pages(5).stanzas.length shouldBe 3
          pages(6).stanzas.length shouldBe 3

        case Left(err) => fail(s"GuidanceError $err")
      }

    }

    "create a set of pages with the correct next field" in new GraphTest {

      pageBuilder.pages(process) match {
        case Right(pages) =>

          val pageIds = pages.map(_.id).sorted
          val nexts = pages.flatMap(_.next).distinct.sorted

          nexts.forall(n => pageIds.contains(n)) shouldBe true

        case Left(err) => fail(s"GuidanceError $err")
      }

    }

  }

  trait Test extends ProcessJson {
    val process = prototypeJson.as[Process]
  }

  "PageBuilding" must {
    "create a set of pages with the correct next field" in new Test {

      pageBuilder.pages(process) match {
        case Right(pages) =>
          pages shouldNot be(Nil)

          pages.length shouldBe 28

          val pageIds = pages.map(_.id).sorted
          val nexts = pages.flatMap(_.next).distinct.sorted
          nexts.forall(n => pageIds.contains(n)) shouldBe true

          pageIds shouldBe nexts ++ List("start")

        case Left(err) => fail(s"GuidanceError $err")
      }

    }
  }
}