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

package services

import base.BaseSpec
import core.models.ocelot.errors._
import core.models.ocelot.stanzas._
import core.models.ocelot._
import play.api.libs.json._
import core.services._

class ValidatingPageBuilderSpec extends BaseSpec with ProcessJson {

  // Define instance of class used in testing
  val pageBuilder: ValidatingPageBuilder = new ValidatingPageBuilder(new PageBuilder(new Timescales(new DefaultTodayProvider)))

  val meta: Meta = Json.parse(prototypeMetaSection).as[Meta]

  case object DummyStanza extends Stanza {
    override val next: Seq[String] = Seq("1")
  }

  trait Test {
    val pageId1 = Process.StartStanzaId
    val pageId2 = "4"
    val pageId3 = "6"
    val pageId4 = "9"
    val pageId5 = "11"
    val pageId6 = "14"
    val pageId7 = "17"
    val pageIds = Seq(pageId1, pageId2, pageId3, pageId4, pageId5, pageId6, pageId7)

    private val flow = Map(
      pageId1 -> PageStanza("/start", Seq("1"), false),
      "1" -> InstructionStanza(3, Seq("2"), None, false),
      "2" -> QuestionStanza(1, Seq(2, 1), Seq(pageId2, pageId4), None, false),
      pageId2 -> PageStanza("/this4", Seq("5"), false),
      "5" -> InstructionStanza(1, Seq("end"), Some(2), false),
      pageId3 -> PageStanza("/this6", Seq("7"), false),
      "7" -> InstructionStanza(2, Seq("8"), None, false),
      "8" -> QuestionStanza(1, Seq(2, 3), Seq(pageId4, pageId6), None, false),
      pageId4 -> PageStanza("/this9", Seq("16"), false),
      "16" -> InstructionStanza(3, Seq("10"), None, false),
      "10" -> InstructionStanza(2, Seq("end"), None, false),
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
      Phrase(Vector("Some Text", "Welsh: Some Text")),
      Phrase(Vector(s"Some Text1 [link:Link to stanza 17:$pageId7]", s"Welsh, Some Text1 [link:Link to stanza 17:$pageId7]")),
      Phrase(Vector(s"Some [link:PageId3:$pageId3] Text2", s"Welsh: Some [link:PageId3:$pageId3] Text2")),
      Phrase(Vector(s"Some [link:Link to stanza 11:$pageId5] Text3", s"Welsh: Some [link:Link to stanza 11:$pageId5] Text3")),
      Phrase(Vector("Some Text [button:HELLO:333]", "Welsh: Some Text [button:HELLO:333]"))
    )

    private val links = Vector(Link(0, pageId3, "", false), Link(1, pageId6, "", false), Link(2, Process.StartStanzaId, "Back to the start", false))

    val processWithLinks = Process(metaSection, flow, phrases, links)
  }

  trait IhtTest extends Test with IhtJson {
    val ihtProcess = ihtJsonShort.as[Process]
  }

  "ValidatingPageBuilder" must {
    "Detect shared pages between Sequence flows" in new Test {
      val flow: Map[String, Stanza] = Map(
        Process.StartStanzaId -> PageStanza("/start", Seq("2"), stack = false),
        "2" -> SequenceStanza(1, Seq("3", "5", "33"), Seq(2, 3), None, stack = false),
        "3" -> PageStanza("/page-3", Seq("4"), stack = false),
        "4" -> InstructionStanza(0, Seq("end"), None, stack = false),
        "33" -> PageStanza("/page-33", Seq("44"), stack = false),
        "44" -> InstructionStanza(0, Seq("22"), None, stack = false),
        "22" -> SequenceStanza(1, Seq("3", "5", "7"), Seq(2, 3), None, stack = false),
        "5" -> PageStanza("/page-5", Seq("6"), stack = false),
        "6" -> InstructionStanza(0, Seq("end"), None, stack = false),
        "7" -> PageStanza("/page-7", Seq("8"), stack = false),
        "8" -> InstructionStanza(0, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )
      val process = processWithLinks.copy(flow = flow)

      pageBuilder.pagesWithValidation(process) match {
        case Right(pages) => fail(s"Attempt to parse page with unsupported page redirect succeeded")
        case Left(List(PageOccursInMultiplSequenceFlows("5"), PageOccursInMultiplSequenceFlows("3"))) => succeed
        case Left(err) => fail(s"Attempt to parse page with unsupported page redirect failed with error ${err}")
      }
    }

    "Detect missing sequence flow termination caused by main flow connection into sequence flow" in new Test {
      val flow: Map[String, Stanza] = Map(
        Process.StartStanzaId -> PageStanza("/start", Seq("1"), stack = false),
        "1" -> InstructionStanza(4, Seq("2"), None, stack = false),
        "2" -> SequenceStanza(0, Seq("3", "5", "33"), Seq(2, 3), None, stack = false),
        "3" -> PageStanza("/page-3", Seq("4"), stack = false),
        "4" -> InstructionStanza(0, Seq("333"), None, stack = false),
        "333" -> PageStanza("/page-333", Seq("444"), stack = false),
        "444" -> InstructionStanza(0, Seq("8"), None, stack = false),
        "33" -> PageStanza("/page-33", Seq("44"), stack = false),
        "44" -> InstructionStanza(4, Seq("end"), None, stack = false),
        "5" -> PageStanza("/page-5", Seq("6"), stack = false),
        "6" -> InstructionStanza(0, Seq("end"), None, stack = false),
        "8" -> InstructionStanza(0, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )
      val process = processWithLinks.copy(flow = flow)

      pageBuilder.pagesWithValidation(process) match {
        case Right(pages) => fail(s"Attempt to parse page with unsupported page redirect succeeded")
        case Left(List(MissingUniqueFlowTerminator("3"))) => succeed
        case Left(err) => fail(s"Attempt to parse page with unsupported page redirect failed with error ${err}")
      }
    }


    "Detect an unsupported page redirection from a Choice stanza" in new Test {
      val invalidFlow = Map(
      pageId1 -> PageStanza("/start", Seq("1"), false),
      "1" -> InstructionStanza(2, Seq("2"), None, false),
      "2" -> QuestionStanza(1, Seq(2, 1), Seq(pageId2, pageId3), None, false),
      pageId2 -> PageStanza("/this4", Seq("55"), false),
      "55" -> ChoiceStanza(Seq("5", pageId7), Seq(ChoiceStanzaTest("yes", LessThanOrEquals, "No")), false),
      "5" -> InstructionStanza(1, Seq("end"), Some(2), false),
      pageId3 -> PageStanza("/this6", Seq("7"), false),
      "7" -> InstructionStanza(2, Seq("8"), None, false),
      "8" -> QuestionStanza(1, Seq(2, 3), Seq(pageId2, pageId7), None, false),
      pageId7 -> PageStanza("/this15", Seq("18"), false),
      "18" -> InstructionStanza(0, Seq("end"), None, false),
      "end" -> EndStanza
    )
      val testProcess = processWithLinks.copy(flow = invalidFlow)

      pageBuilder.pagesWithValidation(testProcess) match {
        case Right(pages) => fail(s"Attempt to parse page with unsupported page redirect succeeded")
        case Left(err) if err == List(PageRedirectNotSupported("55")) => succeed
        case Left(err) => fail(s"Attempt to parse page with unsupported page redirect failed with error ${err}")
      }
    }
  }

  "When processing guidance containing zero or more row stanzas" must {

    "successfully create an instance of Row from a RowStanza with a single data cell" in new Test {

      val cellDataContent: Phrase = Phrase( Vector( "Text for single data cell", "Welsh: Text for single data cell"))

      val expectedRow: Row =  new Row(Seq(cellDataContent), Seq("end"), stack = false)

      val flow = Map(
        Process.StartStanzaId -> PageStanza("/rowStanzaTest", Seq("1"), stack = false),
        "1" -> CalloutStanza(Title, 0, Seq("2"), stack = false),
        "2" -> RowStanza(Seq(1), Seq("end"), stack = false),
        "end" -> EndStanza
      )

      val process: Process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Single cell row stanza", "Welsh: Single cell row stanza")),
          cellDataContent
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Right(pages) => pages.head.stanzas(2) shouldBe expectedRow
        case Left(err) => fail(s"Attempt to parse page with single cell row stanza failed with error : ${err.toString()}")
      }
    }

    "successfully create an instance of a Row from a RowStanza with multiple data cells" in new Test {

      val cellDataContent1: Phrase = Phrase(Vector("Text for first data cell", "Welsh: Text for first data cell"))
      val cellDataContent2: Phrase = Phrase(Vector("Text for second data cell", "Welsh: Text for second data cell"))
      val cellDataContent3: Phrase = Phrase(Vector("Text for third data cell","Welsh: Text for third data cell"))

      val expectedRow: Row = new Row(
        Seq(cellDataContent1, cellDataContent2, cellDataContent3),
        Seq("3"),
        stack = false
      )

      val flow = Map(
        Process.StartStanzaId -> PageStanza("/rowStanzaTest", Seq("1"), stack = false),
        "1" -> CalloutStanza(Title, 0, Seq("2"), stack = false),
        "2" -> RowStanza(Seq( 1, 2, 3), Seq("3"), stack = false),
        "3" -> InstructionStanza(four, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )

      val process: Process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Multiple cell row stanza", "Welsh: Multiple cell row stanza")),
          cellDataContent1,
          cellDataContent2,
          cellDataContent3,
          Phrase(Vector("End of page","Welsh: End of page"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Right(pages) => pages.head.stanzas(2) shouldBe expectedRow
        case Left(err) => fail(s"Attempt to parse page with single cell row stanza failed with error : ${err.toString()}")
      }
    }

    "successfully create an instance of Row from a RowStanza with zero data cells" in new Test {

      val expectedRow: Row = new Row(
        Nil,
        Seq("3"),
        stack = false
      )

      val flow = Map(
        Process.StartStanzaId -> PageStanza("/rowStanzaTest", Seq("1"), stack = false),
        "1" -> CalloutStanza(Title,0, Seq("2"), stack = false),
        "2" -> RowStanza(Nil, Seq("3"), stack = false),
        "3" -> InstructionStanza(1, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )

      val process: Process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Multiple cell row stanza", "Welsh: Multiple cell row stanza")),
          Phrase(Vector("End of page","Welsh: End of page"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Right(pages) => pages.head.stanzas(2) shouldBe expectedRow
        case Left(err) => fail(s"Attempt to parse page with single cell row stanza failed with error : ${err.toString()}")
      }

    }

    "create an instance of Row with linked page ids when links to pages are defined in cell data" in new Test {

      val cellDataContent1: Phrase = Phrase(Vector("Cell data 1 link [link:PageId2:20]", "Welsh: Cell data 1 link [link:PageId2:20]"))
      val cellDataContent2: Phrase = Phrase(Vector("Cell data 2 link [link:PageId5:64]", "Welsh: Cell data 2 link [link:PageId5:64]"))

      val expectedRow: Row = new Row(
        Seq(cellDataContent1, cellDataContent2),
        Seq("3"),
        stack = false,
        links = List("20", "64")
      )

      val flow = Map(
        Process.StartStanzaId -> PageStanza("/rowStanzaTest-page-1", Seq("1"), stack = false),
        "1" -> CalloutStanza(Title, 0, Seq("2"), stack = false),
        "2" -> RowStanza(Seq(1, 2), Seq("3"), stack = false),
        "3" -> InstructionStanza(3, Seq("end"), Some(0), stack = false),
        "20" -> PageStanza("/rowStanzaTest-page-2", Seq("21"), stack = false),
        "21" -> InstructionStanza(3, Seq("end"), Some(1), stack = false),
        "64" -> PageStanza("/rowStanzaTest-page-3", Seq("65"), stack = false),
        "65" -> InstructionStanza(six, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )

      val process: Process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Multiple cell row stanza", "Welsh: Multiple cell row stanza")),
          cellDataContent1,
          cellDataContent2,
          Phrase(Vector("End of page","Welsh: End of page")),
          Phrase(Vector("Link to page 2", "Welsh: link to page 2")),
          Phrase(Vector("Link to page 3", "Welsh: link to page 3")),
          Phrase(Vector("End of test", "Welsh: End of test"))
        ),
        Vector[Link](
          Link(four, "20", "Link 4 title", window = false),
          Link(five, "64", "Link 5 title", window = false)
        )
      )

      pageBuilder.pagesWithValidation(process) match {
        case Right(pages) => pages.head.stanzas(2) shouldBe expectedRow
        case Left(err) => fail(s"Attempt to parse page with single cell row stanza failed with error : ${err.toString()}")
      }
    }

    "successfully detect an invalid phrase identifier in the definition of a row stanza" in new Test {

      val cellDataContent1: Phrase = Phrase(Vector("Text for first data cell", "Welsh: Text for first data cell"))
      val cellDataContent2: Phrase = Phrase(Vector("Text for second data cell", "Welsh: Text for second data cell"))
      val cellDataContent3: Phrase = Phrase(Vector("Text for third data cell","Welsh: Text for third data cell"))

      val flow = Map(
        Process.StartStanzaId -> PageStanza("/rowStanzaTest", Seq("1"), stack = false),
        "1" -> CalloutStanza(Title, 0, Seq("2"), stack = false),
        "2" -> RowStanza(Seq( 1, five, 3), Seq("3"), stack = false),
        "3" -> InstructionStanza(four, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )

      val process: Process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Multiple cell row stanza", "Welsh: Multiple cell row stanza")),
          cellDataContent1,
          cellDataContent2,
          cellDataContent3,
          Phrase(Vector("End of page","Welsh: End of page"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Right(_) => fail( "PageBuilder should not create a row from a row stanza with an invalid phrase identifier")
        case Left(List(PhraseNotFound("2", five))) => succeed
        case Left(err) => fail( s"Expected error PhraseNotFound(2, 5) but received ${err.toString}")
      }

    }

    "successfully create rows from row stanzas defined in guidance" in new Test {

      val process: Process = simpleRowStanzaProcessAsJson.as[Process]

      // Define expected single cell row
      val singleCellText: Phrase = Phrase(Vector("Text for single cell row stanza","Welsh: Text for single cell row stanza"))

      val expectedSingleCellRow: Row = new Row(Seq(singleCellText), Seq("3"), stack = false)

      // Define expected multiple cell row
      val cellOneText: Phrase = Phrase(Vector("Cell one text", "Welsh: Cell one text"))
      val cellTwoText: Phrase = Phrase(Vector("Cell two text", "Welsh: Cell two text"))
      val cellThreeText: Phrase = Phrase(Vector("Cell three text", "Welsh: Cell three text"))
      val cellFourText: Phrase = Phrase(Vector("Cell four text", "Welsh: Cell four text"))

      val expectedMultipleCellRow: Row = new Row(
        Seq(cellOneText, cellTwoText, cellThreeText, cellFourText),
        Seq("4"),
        stack = true
      )

      // Define expected zero cells row
      val expectedZeroCellRow: Row = new Row(Seq(), Seq("end"), stack = true)

      pageBuilder.pagesWithValidation(process) match {
        case Right(pages) =>
          pages.head.stanzas(2) shouldBe expectedSingleCellRow
          pages.head.stanzas(3) shouldBe expectedMultipleCellRow
          pages.head.stanzas(four) shouldBe expectedZeroCellRow
        case Left(err) => fail( s"Attempt to create pages from simple row stanza example failed with error : ${err.toString}")
      }
    }

  }

  "When processing a 2 page flow separated by a PageStanza" must {
    val twoPagesSeperatedByValueStanza: Map[String, Stanza] = Map(
        Process.StartStanzaId -> PageStanza("/blah", Seq("1"), stack = false),
        "1" -> InstructionStanza(0, Seq("2"), None, stack = false),
        "2" -> InstructionStanza(1, Seq("3"), None, stack = false),
        "3" -> PageStanza("/a", Seq("4"), stack = false),
        "4" -> InstructionStanza(0, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )
    val process: Process = Process(
      meta,
      twoPagesSeperatedByValueStanza,
      Vector(Phrase(Vector("Some Text", "Welsh: Some Text")), Phrase(Vector("Some Text1", "Welsh: Some Text1"))),
      Vector[Link]()
    )
    "result in 2 pages" in {
      pageBuilder.pagesWithValidation(process) match {
        case Right(pages) if pages.length == 2 => succeed
        case Right(pages) => fail(s"Page count is incorrect, found ${pages.length} pages")
        case Left(err) => fail(s"FAIL ${err.toString}")
      }
    }
  }

}
