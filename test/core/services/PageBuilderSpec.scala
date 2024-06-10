/*
 * Copyright 2024 HM Revenue & Customs
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
import core.models.ocelot.errors._
import core.models.ocelot.stanzas._
import core.models.ocelot._
import play.api.libs.json._

import base.TestConstants

trait PageDefns extends TestConstants {
  val phrases: Vector[Phrase] = Vector(
    Phrase(Vector("Text 0", "Welsh: Text 0")),
    Phrase(Vector("Text 1", "Welsh: Text 1")),
    Phrase(Vector("Text 2", "Welsh: Text 2")),
    Phrase(Vector("Text 3", "Welsh: Text 3")),
    Phrase(Vector("Text 4", "Welsh: Text 4")),
    Phrase(Vector("Text 5", "Welsh: Text 5")),
    Phrase(Vector("Text 6", "Welsh: Text 6")),
    Phrase(Vector("Text 7", "Welsh: Text 7")),
    Phrase(Vector("Text 8", "Welsh: Text 8"))
  )

  val rawExclusiveSequencePhrases: Vector[Phrase] = phrases ++ Vector(
    Phrase(Vector("What kind of fruit do you like?", "Welsh: What kind of fruit do you like?")),
    Phrase(Vector(
      "Other [exclusive][hint:Selecting this checkbox will deselect the other checkboxes]",
      "Welsh: Other [exclusive][hint:Welsh: Selecting this checkbox will deselect the other checkboxes]"
    )),
    Phrase(Vector("Apples", "Welsh: Apples")),
    Phrase(Vector("Bananas", "Welsh: Bananas")),
    Phrase(Vector("Oranges", "Welsh: Oranges"))
  )

  val exclusiveSequencePhrases: Vector[Phrase] = phrases ++ Vector(
    Phrase(Vector("What kind of fruit do you like?", "Welsh: What kind of fruit do you like?")),
    Phrase(Vector(
      "Other [hint:Selecting this checkbox will deselect the other checkboxes]",
      "Welsh: Other [hint:Welsh: Selecting this checkbox will deselect the other checkboxes]"
    )),
    Phrase(Vector("Apples", "Welsh: Apples")),
    Phrase(Vector("Bananas", "Welsh: Bananas")),
    Phrase(Vector("Oranges", "Welsh: Oranges"))
  )

  val links: Vector[Link] = Vector(Link(0, "http://my.com/news", "MyCOM Daily News", window = true))

  // Define stanzas used in simple question page test
  val pageOneUrl = "/page/1"
  val pageTwoUrl = "/page/2"
  val pageThreeUrl = "/page/3"

  // Question page - BEFORE
  val pageOnePageStanza: PageStanza = PageStanza(pageOneUrl, Seq("1"), stack = false)
  val pageOneInstructionStanza: InstructionStanza = InstructionStanza(0, Seq("2"), None, stack = false)
  val pageOneCalloutStanza: CalloutStanza = CalloutStanza(SubTitle, 1, Seq("3"), stack = false)
  val pageOneQuestionStanza: QuestionStanza = QuestionStanza(two, Seq(three, four), Seq("4", "6"), None, stack = false)

  val pageOneInputStanza: InputStanza = InputStanza(Currency, Seq("4"), two, Some(three), "label", None, stack = false)
  val pageOneDateInputStanza: InputStanza = InputStanza(Date, Seq("4"), two, Some(three), "label", None, stack = false)
  val pageOneNumberInputStanza: InputStanza = InputStanza(Number, Seq("4"), two, Some(three), "label", None, stack = false)
  val pageOneTextInputStanza: InputStanza = InputStanza(Txt, Seq("4"), two, Some(three), "label", None, stack = false)
  val pageOneSequenceStanza: SequenceStanza = SequenceStanza(seven, Seq("4", "6"), Seq(eight), None, stack = false )
  val pageOneSequenceStanzaMissingTitle: SequenceStanza = SequenceStanza(oneHundred, Seq("4", "6"), Seq(eight), None, stack = false)
  val pageOneSequenceStanzaMissingOption: SequenceStanza = SequenceStanza(seven, Seq("4", "6"), Seq(oneHundred), None, stack = false)

  val pageOnePageStanzaAlternate: PageStanza = PageStanza(pageOneUrl, Seq("9"), stack = false)
  val pageOneTypeErrorCallout: TypeErrorCallout = TypeErrorCallout(Phrase("Invalid selection", "Invalid selection"), Seq("1"), stack = false)
  val pageOneExclusiveSequenceStanza: SequenceStanza = SequenceStanza(
    nine,
    Seq("4", "4", "4", "4", "6"),
    Seq(ten, eleven, twelve, thirteen),
    None,
    stack = false)

  // Question page - After
  val pageOneInstruction: Instruction = Instruction(phrases(0), Seq("2"), None, stack = false)
  val pageOneCallout: SubTitleCallout = SubTitleCallout(phrases(1), Seq("3"), stack = false)
  val pageOneQuestion: Question = Question(phrases(two), Seq(phrases(three), phrases(four)), Seq("4", "6"), None, stack = false)
  val pageOneInput: CurrencyInput = CurrencyInput(Seq("4"), phrases(two), Some(phrases(three)), "label", None, stack = false)
  val pageOneDateInput: DateInput = DateInput(Seq("4"), phrases(two), Some(phrases(three)), "label", None, stack = false)
  val pageOneNumberInput: NumberInput = NumberInput(Seq("4"), phrases(two), Some(phrases(three)), "label", None, stack = false)
  val pageOneTextInput: TextInput = TextInput(Seq("4"), phrases(two), Some(phrases(three)), "label", None, stack = false)
  val pageOneNonExclusiveSequence: Sequence = Sequence(
    phrases(seven),
    Seq("4", "6"),
    Seq(phrases(eight)),
    None,
    None,
    stack = false)

  val pageOneExclusiveSequence: Sequence = Sequence(
    exclusiveSequencePhrases(nine),
    Seq("4", "4", "4", "4", "6"),
    Seq(
      exclusiveSequencePhrases(eleven),
      exclusiveSequencePhrases(twelve),
      exclusiveSequencePhrases(thirteen)
    ),
    Some(
      Phrase(
        "Other [hint:Selecting this checkbox will deselect the other checkboxes]",
        "Welsh: Other [hint:Welsh: Selecting this checkbox will deselect the other checkboxes]")
    ),
    None,
    stack = false
  )

  // First answer page BEFORE
  val pageTwoPageStanza: PageStanza = PageStanza(pageTwoUrl, Seq("5"), stack = false)
  val pageTwoInstructionStanza: InstructionStanza = InstructionStanza(0, Seq("end"), None, stack = false)
  // First answer page AFTER
  val pageTwoInstruction: Instruction = Instruction(phrases(0), Seq("end"), None, stack = false)

  // Second answer page BEFORE
  val pageThreePageStanza: PageStanza = PageStanza(pageThreeUrl, Seq("7"), stack = false)
  val pageThreeInstructionStanza: InstructionStanza = InstructionStanza(five, Seq("8"), Some(0), stack = false)
  val pageThreeCalloutStanza: CalloutStanza = CalloutStanza(Lede, six, Seq("end"), stack = false)
  // Second answer page AFTER
  val pageThreeInstruction: Instruction = Instruction(phrases(five), Seq("8"), Some(links(0)), stack = false)
  val pageThreeCallout: LedeCallout = LedeCallout(phrases(six), Seq("end"), stack = false)

  val standardSequenceFlow: Map[String, Stanza] =  Map(
    Process.StartStanzaId -> pageOnePageStanza,
    "1" -> pageOneInstructionStanza,
    "2" -> pageOneSequenceStanza,
    "4" -> pageTwoPageStanza,
    "5" -> pageTwoInstructionStanza,
    "6" -> pageThreePageStanza,
    "7" -> pageThreeInstructionStanza,
    "8" -> pageThreeCalloutStanza,
    "end" -> EndStanza)
  val exclusiveSequenceFlow: Map[String, Stanza] =  Map(
    Process.StartStanzaId -> pageOnePageStanzaAlternate,
    "9" -> pageOneTypeErrorCallout,
    "1" -> pageOneInstructionStanza,
    "2" -> pageOneExclusiveSequenceStanza,
    "4" -> pageTwoPageStanza,
    "5" -> pageTwoInstructionStanza,
    "6" -> pageThreePageStanza,
    "7" -> pageThreeInstructionStanza,
    "8" -> pageThreeCalloutStanza,
    "end" -> EndStanza)
}

class PageBuilderSpec extends BaseSpec with ProcessJson with PageDefns {
  val timescales: Timescales = new Timescales(new DefaultTodayProvider)
  var rates: Rates = new Rates()
  val pageBuilder = new PageBuilder(new LabelledData(timescales, rates))
  val meta: Meta = Json.parse(prototypeMetaSection).as[Meta]

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
      pageId1 -> PageStanza("/start", Seq("1"), stack = false),
      "1" -> InstructionStanza(3, Seq("2"), None, stack = false),
      "2" -> QuestionStanza(1, Seq(2, 1), Seq(pageId2, pageId4), None, stack = false),
      pageId2 -> PageStanza("/this4", Seq("5"), stack = false),
      "5" -> InstructionStanza(1, Seq("end"), Some(2), stack = false),
      pageId3 -> PageStanza("/this6", Seq("7"), stack = false),
      "7" -> InstructionStanza(2, Seq("8"), None, stack = false),
      "8" -> QuestionStanza(1, Seq(2, 3), Seq(pageId4, pageId6), None, stack = false),
      pageId4 -> PageStanza("/this9", Seq("16"), stack = false),
      "16" -> InstructionStanza(3, Seq("10"), None, stack = false),
      "10" -> InstructionStanza(2, Seq("end"), None, stack = false),
      pageId5 -> PageStanza("/this11", Seq("12"), stack = false),
      "12" -> InstructionStanza(0, Seq("13"), None, stack = false),
      "13" -> QuestionStanza(1, Seq(2, 3), Seq(pageId6, pageId2), None, stack = false),
      pageId6 -> PageStanza("/this14", Seq("15"), stack = false),
      "15" -> InstructionStanza(0, Seq("end"), None, stack = false),
      pageId7 -> PageStanza("/this15", Seq("18"), stack = false),
      "18" -> InstructionStanza(0, Seq("end"), None, stack = false),
      "end" -> EndStanza
    )

    private val phrases = Vector[Phrase](
      Phrase(Vector("Some Text", "Welsh: Some Text")),
      Phrase(Vector(s"Some Text1 [link:Link to stanza 17:$pageId7]", s"Welsh, Some Text1 [link:Link to stanza 17:$pageId7]")),
      Phrase(Vector(s"Some [link:PageId3:$pageId3] Text2", s"Welsh: Some [link:PageId3:$pageId3] Text2")),
      Phrase(Vector(s"Some [link:Link to stanza 11:$pageId5] Text3", s"Welsh: Some [link:Link to stanza 11:$pageId5] Text3"))
    )

    private val links = Vector(Link(0, pageId3, "", window = false), Link(1, pageId6, "", window = false), Link(2, Process.StartStanzaId, "Back to the start", window = false))

    val processWithLinks = Process(metaSection, flow, phrases, links)
  }

  trait IhtTest extends Test with IhtJson {
    val ihtProcess = ihtJsonShort.as[Process]
  }

  "services" must {
    "determine unique set of case sensitive labels from a collection of pages" in new IhtTest {
      val labels = Seq(
        "Properties",
        "Money",
        "Household",
        "Motor Vehicles",
        "Private pension",
        "Trust",
        "Foreign assets",
        "Other assets",
        "Mortgage_debt",
        "funeral_expenses",
        "other_debts",
        "left to spouse",
        "registered charity",
        "nil rate band",
        "more than 100k",
        "Value of Assets",
        "Value of Debts",
        "Additional Info",
        "IHT result")

      pageBuilder.pages(ihtProcess, "start") match {
        case Right(pages) => uniqueLabels(pages) shouldBe labels
        case Left(err) => fail(s"Failed with $err")
      }
    }

    "determine unique set of label references from a collection of pages" in new IhtTest {
      val labelsReferenced = Seq("Properties",
        "Money",
        "Value of Assets",
        "Household",
        "Motor Vehicles",
        "Private pension",
        "Trust",
        "Foreign assets",
        "Other assets",
        "Mortgage_debt",
        "funeral_expenses",
        "Value of Debts",
        "other_debts",
        "left to spouse",
        "registered charity",
        "Additional Info",
        "nil rate band",
        "IHT result")

      pageBuilder.pages(ihtProcess, "start") match {
        case Right(pages) => uniqueLabelRefs(pages) shouldBe labelsReferenced
        case Left(err) => fail(s"Failed with $err")
      }
    }
  }

  "PageBuilder" must {

    "Make it possible to validate label references across a sequence of pages" in new IhtTest {
      pageBuilder.pages(ihtProcess, "start") match {
        case Right(pages) =>
          val labels = uniqueLabels(pages)
          uniqueLabelRefs(pages).forall(lr => labels.exists(_.equals(lr))) shouldBe true
        case Left(err) => fail(s"Failed with $err")
      }
    }

    "be not buildable from non-existent key" in {

      val process: Process = prototypeJson.as[Process]

      pageBuilder.buildPage("unknown", process) match {
        case Right(_) => fail("Invalid key should not return a page")
        case Left(err) => succeed
      }
    }

    "Ensure all urls have a leading /" in {

      val process: Process = invalidOnePageJson.as[Process]

      pageBuilder.buildPage("start", process) match {
        case Right(Page(_,url,_,_, _)) if url.startsWith("/") => succeed
        case Right(_) => fail("Url should be prefixed with a / char")
        case Left(err) => fail(s"Url should be prefixed with a / char, failed with unexpected err $err")
      }
    }

    "Sequence of connected pages" must {

      "not be extractable from a Process using an invalid start key" in {

        val process: Process = prototypeJson.as[Process]

        pageBuilder.pages(process, "unknown") match {
          case Right(_) => fail("""Should fail with StanzaNotFound("unknown")""")
          case Left(List(err)) if err == StanzaNotFound("unknown") => succeed
          case Left(wrongErr) => fail(s"""Should fail with StanzaNotFound("unknown") $wrongErr""")
        }
      }

      "be extractable from a Process using key 'start''" in {

        val process: Process = prototypeJson.as[Process]

        pageBuilder.pages(process) match {
          case Right(pages) =>
            pages shouldNot be(Nil)

            pages.length shouldBe 28

          case Left(err) => fail(s"GuidanceError $err")
        }

      }

      "return pages in order beginning with 'start' page" in {

        val process: Process = prototypeJson.as[Process]

        pageBuilder.pages(process) match {
          case Right(pages) =>
            pages shouldNot be(Nil)

            pages.head.id shouldBe Process.StartStanzaId

          case Left(err) => fail(s"First page must be the requested start page")
        }

      }

      "return pages in order beginning with nominated start page" in {

        val process: Process = prototypeJson.as[Process]

        pageBuilder.pages(process, "120") match {
          case Right(pages) =>
            pages shouldNot be(Nil)

            pages.head.id shouldBe "120"

          case Left(err) => fail(s"GuidanceError $err")
        }

      }

      "correctly identify the pages in a Process accounting fro every stanza" in {

        val process: Process = prototypeJson.as[Process]
        val expectedPageIds: List[String] = List(Process.StartStanzaId,"26","36","37","39","46","53","60","70","77","120","80","83",
                                                 "90","97","102","109","113","121","124","127","131","159","138","143","151","157","158")

        pageBuilder.pages(process) match {
          case Right(pages) =>
            pages.length shouldBe expectedPageIds.length
            pages.forall(p => expectedPageIds.contains(p.id)) shouldBe true

          case Left(err) => fail(s"GuidanceError error $err")
        }

      }

      "consist of one page when only page exists" in {
        val process: Process = validOnePageJson.as[Process]
        pageBuilder.pages(process, "start") match {
          case Right(pages) =>
            pages shouldNot be(Nil)

            pages.length shouldBe 1

          case Left(err) => fail(s"GuidanceError $err")
        }

      }

      "confirm one page elements" in {
        val onePage: Map[String, Stanza] = Map(
            Process.StartStanzaId -> PageStanza("/blah", Seq("1"), stack = false),
            "1" -> InstructionStanza(0, Seq("2"), None, stack = false),
            "2" -> InstructionStanza(1, Seq("end"), None, stack = false),
            "end" -> EndStanza)

        val process: Process = Process(
          meta,
          onePage,
          Vector[Phrase](Phrase(Vector("Some Text", "Welsh: Some Text")), Phrase(Vector("Some Text1", "Welsh: Some Text1"))),
          Vector[Link]()
        )

        pageBuilder.pages(process) match {
          case Right(pages) =>
            pages shouldNot be(Nil)
            pages.length shouldBe 1

          case Left(err) => fail(s"GuidanceError $err")
        }
      }

      "follows links to pages identified by stanza id " in new Test {

        pageBuilder.pages(processWithLinks) match {
          case Right(pages) =>
            pages.length shouldBe 7

            val pageMap = pages.map(p => (p.id, p.linked)).toMap

            pageIds.forall(pageMap.contains) shouldBe true
            pageMap(pageId1) shouldBe List(pageId5)
            pageMap(pageId2) shouldBe List(pageId7, pageId1)
            pageMap(pageId3) shouldBe List(pageId3)
            pageMap(pageId4) shouldBe List(pageId5, pageId3)
            pageMap(pageId5) shouldBe Nil
            pageMap(pageId6) shouldBe Nil

          case Left(err) => fail(s"GuidanceError $err")
        }
      }

    }

    "When processing a simple question page" must {
      val simpleQuestionFlow: Map[String, Stanza] = Map(
          Process.StartStanzaId -> pageOnePageStanza,
          "1" -> pageOneInstructionStanza,
          "2" -> pageOneCalloutStanza,
          "3" -> pageOneQuestionStanza,
          "4" -> pageTwoPageStanza,
          "5" -> pageTwoInstructionStanza,
          "6" -> pageThreePageStanza,
          "7" -> pageThreeInstructionStanza,
          "8" -> pageThreeCalloutStanza,
          "end" -> EndStanza)
      val process: Process = Process(meta, simpleQuestionFlow, phrases, links)

      pageBuilder.pages(process) match {

        case Right(pages) =>
          "Determine the correct number of pages to be displayed" in {
            pages shouldNot be(Nil)
            pages.length shouldBe 3
          }

          val indexedSeqOfPages = pages.toIndexedSeq

          // Test contents of individual pages

          "Define the question page correctly" in {
            indexedSeqOfPages(0).id shouldBe Process.StartStanzaId
            indexedSeqOfPages(0).stanzas.size shouldBe 4
            indexedSeqOfPages(0).stanzas shouldBe Seq(pageOnePageStanza, pageOneInstruction, pageOneCallout, pageOneQuestion)
            indexedSeqOfPages(0).next shouldBe Seq("6", "4")
          }

          "Define the first answer page correctly" in {
            indexedSeqOfPages(2).id shouldBe "4"
            indexedSeqOfPages(2).stanzas.size shouldBe 3
            indexedSeqOfPages(2).stanzas(0) shouldBe pageTwoPageStanza
            indexedSeqOfPages(2).stanzas(1) shouldBe pageTwoInstruction
            indexedSeqOfPages(2).stanzas.last shouldBe EndStanza
            indexedSeqOfPages(2).next shouldBe Nil
          }

          "Define the second answer page correctly" in {
            indexedSeqOfPages(1).id shouldBe "6"
            indexedSeqOfPages(1).stanzas.size shouldBe 4
            indexedSeqOfPages(1).stanzas(0) shouldBe pageThreePageStanza
            indexedSeqOfPages(1).stanzas(1) shouldBe pageThreeInstruction
            indexedSeqOfPages(1).stanzas(2) shouldBe pageThreeCallout
            indexedSeqOfPages(1).stanzas.last shouldBe EndStanza

            indexedSeqOfPages(1).next shouldBe Nil
          }

        case Left(err) => //fail(s"Flow error $err")
      }
    }

    "When processing a simple input page one" must {

      "Confirm content of page" in {
        val simpleInputFlow: Map[String, Stanza] = Map(
            Process.StartStanzaId -> pageOnePageStanza,
            "1" -> pageOneInstructionStanza,
            "2" -> pageOneCalloutStanza,
            "3" -> pageOneInputStanza,
            "4" -> pageTwoPageStanza,
            "5" -> pageTwoInstructionStanza,
            "6" -> pageThreePageStanza,
            "7" -> pageThreeInstructionStanza,
            "8" -> pageThreeCalloutStanza,
            "end" -> EndStanza)
        val process: Process = Process(meta, simpleInputFlow, phrases, links)

        pageBuilder.pages(process) match {
          case Right(pages) =>
            pages shouldNot be(Nil)
            pages.length shouldBe 2
            val page = pages.head
            // Test contents of individual pages
            page.id shouldBe Process.StartStanzaId
            page.stanzas.size shouldBe 4
            page.stanzas shouldBe Seq(pageOnePageStanza, pageOneInstruction, pageOneCallout, pageOneInput)
            page.next shouldBe Seq("4")

          case Left(err) => fail(s"Flow error $err")
        }
      }
    }

    "When processing a simple date input page" must {
      val simpleDateInputFlow: Map[String, Stanza] = Map(
          Process.StartStanzaId -> pageOnePageStanza,
          "1" -> pageOneInstructionStanza,
          "2" -> pageOneDateInputStanza,
          "4" -> pageTwoPageStanza,
          "5" -> pageTwoInstructionStanza,
          "6" -> pageThreePageStanza,
          "7" -> pageThreeInstructionStanza,
          "8" -> pageThreeCalloutStanza,
          "end" -> EndStanza)
      val process: Process = Process(meta, simpleDateInputFlow, phrases, links)

      pageBuilder.pages(process) match {
        case Right(pages) =>
          "Determine the correct number of pages to be displayed" in {
            pages shouldNot be(Nil)
            pages.length shouldBe 2
          }

          // Test contents of individual pages
          "Define the input page correctly" in {
            val page = pages.head
            page.id shouldBe Process.StartStanzaId
            page.stanzas.size shouldBe 3
            page.stanzas shouldBe Seq(pageOnePageStanza, pageOneInstruction, pageOneDateInput)
            page.next shouldBe Seq("4")
          }

        case Left(err) => fail(s"Flow error $err")
      }
    }

    "When processing a simple text input page" must {
      val simpleTextInputFlow: Map[String, Stanza] = Map(
          Process.StartStanzaId -> pageOnePageStanza,
          "1" -> pageOneInstructionStanza,
          "2" -> pageOneTextInputStanza,
          "4" -> pageTwoPageStanza,
          "5" -> pageTwoInstructionStanza,
          "6" -> pageThreePageStanza,
          "7" -> pageThreeInstructionStanza,
          "8" -> pageThreeCalloutStanza,
          "end" -> EndStanza)
      val process: Process = Process(meta, simpleTextInputFlow, phrases, links)

      pageBuilder.pages(process) match {
        case Right(pages) =>
          "Determine the correct number of pages to be displayed" in {
            pages shouldNot be(Nil)
            pages.length shouldBe 2
          }

          // Test contents of individual pages
          "Define the input page correctly" in {
            val page = pages.head
            page.id shouldBe Process.StartStanzaId
            page.stanzas.size shouldBe 3
            page.stanzas shouldBe Seq(pageOnePageStanza, pageOneInstruction, pageOneTextInput)
            page.next shouldBe Seq("4")
          }
        case Left(err) => fail(s"Flow error $err")
      }
    }

    "When processing a simple number input page" must {
      val simpleNumberInputFlow: Map[String, Stanza] = Map(
          Process.StartStanzaId -> pageOnePageStanza,
          "1" -> pageOneInstructionStanza,
          "2" -> pageOneNumberInputStanza,
          "4" -> pageTwoPageStanza,
          "5" -> pageTwoInstructionStanza,
          "6" -> pageThreePageStanza,
          "7" -> pageThreeInstructionStanza,
          "8" -> pageThreeCalloutStanza,
          "end" -> EndStanza)
      val process: Process = Process(meta, simpleNumberInputFlow, phrases, links)

      pageBuilder.pages(process) match {
        case Right(pages) =>
          "Determine the correct number of pages to be displayed" in {
            pages shouldNot be(Nil)
            pages.length shouldBe 2
          }

          // Test contents of individual pages
          "Define the input page correctly" in {
            val page = pages.head
            page.id shouldBe Process.StartStanzaId
            page.stanzas.size shouldBe 3
            page.stanzas shouldBe Seq(pageOnePageStanza, pageOneInstruction, pageOneNumberInput)
            page.next shouldBe Seq("4")
          }
        case Left(err) => fail(s"Flow error $err")
      }
    }

    "when processing a simple sequence page" must {
      val process: Process = Process(meta, standardSequenceFlow, phrases, links)

      pageBuilder.pages(process) match {
        case Right(pages) =>
          "Determine the correct number of pages to be displayed" in {
            pages shouldNot be(Nil)
            pages.length shouldBe 3
          }

          // Test content of page containing sequence input component
          "define the simple sequence page correctly" in {
            val page = pages.head
            page.id shouldBe Process.StartStanzaId
            page.stanzas.size shouldBe 3
            page.stanzas shouldBe Seq(pageOnePageStanza, pageOneInstruction, pageOneNonExclusiveSequence)
            page.next shouldBe Seq("6", "4")
          }

        case Left(err) => fail(s"Flow error $err")
      }
    }

    "when processing a sequence with a missing title phrase" must {
      val sequenceWithMissingTitleFlow: Map[String, Stanza] =  Map(
        Process.StartStanzaId -> pageOnePageStanza,
        "1" -> pageOneInstructionStanza,
        "2" -> pageOneSequenceStanzaMissingTitle,
        "4" -> pageTwoPageStanza,
        "5" -> pageTwoInstructionStanza,
        "6" -> pageThreePageStanza,
        "7" -> pageThreeInstructionStanza,
        "8" -> pageThreeCalloutStanza,
        "end" -> EndStanza)
      val process: Process = Process(meta, sequenceWithMissingTitleFlow, phrases, links)

      "return a phrase not found error" in {
        pageBuilder.pages(process) match {
          case Right(_) => fail("A sequence should not be created when the title phrase is undefined")
          case Left(err) => err shouldBe List(PhraseNotFound("2", oneHundred))
        }

      }
    }

    "when processing a sequence with a missing option phrase" must {
      val sequenceWithMissingOptionFlow: Map[String, Stanza] =  Map(
        Process.StartStanzaId -> pageOnePageStanza,
        "1" -> pageOneInstructionStanza,
        "2" -> pageOneSequenceStanzaMissingOption,
        "4" -> pageTwoPageStanza,
        "5" -> pageTwoInstructionStanza,
        "6" -> pageThreePageStanza,
        "7" -> pageThreeInstructionStanza,
        "8" -> pageThreeCalloutStanza,
        "end" -> EndStanza)
      val process: Process = Process(meta, sequenceWithMissingOptionFlow, phrases, links)

      "return a phrase not found error" in {
        pageBuilder.pages(process) match {
          case Right(_) => fail("A sequence should not be created when one, or more, option phrases are undefined")
          case Left(err) => err shouldBe List(PhraseNotFound("2", oneHundred))
        }

      }

    }

    "correctly determine the page title for a non-exclusive sequence page" in {
      case class Dummy(id: String, pageUrl: String, pageTitle: String)
      val process: Process = Process(meta, standardSequenceFlow, phrases, links)

      pageBuilder.pages(process) match {

        case Right(pages) =>
          val pageInfo = fromPageDetails(pages)(Dummy)
          pageInfo.length shouldBe 1
          pageInfo.head.id shouldBe "start"
          pageInfo.head.pageUrl shouldBe "/page/1"
          pageInfo.head.pageTitle shouldBe "Text 7"

        case Left(err) => fail(s"Flow error $err")
      }
    }

    "when processing an exclusive sequence page" must {
      val process: Process = Process(meta, exclusiveSequenceFlow, rawExclusiveSequencePhrases, links)

      pageBuilder.pages(process) match {
        case Right(pages) =>
          "Determine the correct number of pages to be displayed" in {
            pages shouldNot be(Nil)
            pages.length shouldBe 3
          }

          "define an exclusive sequence page correctly" in {
            val page = pages.head
            page.id shouldBe Process.StartStanzaId
            page.stanzas.size shouldBe 4
            page.stanzas shouldBe Seq(pageOnePageStanzaAlternate, pageOneTypeErrorCallout, pageOneInstruction, pageOneExclusiveSequence)
          }

        case Left(err) => fail(s"Flow error $err")
      }
    }


    "correctly determine the page title for an exclusive sequence page" in {
      case class Dummy(id: String, pageUrl: String, pageTitle: String)
      val process: Process = Process(meta, exclusiveSequenceFlow, rawExclusiveSequencePhrases, links)

      pageBuilder.pages(process) match {

        case Right(pages) =>

            val pageInfo = fromPageDetails(pages)(Dummy)

            pageInfo.length shouldBe 1
            pageInfo.head.id shouldBe "start"
            pageInfo.head.pageUrl shouldBe "/page/1"
            pageInfo.head.pageTitle shouldBe "What kind of fruit do you like?"

        case Left(err) => fail(s"Flow error $err")
      }

    }

  } // End of PageBuilder must

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

      pageBuilder.pages(process) match {
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

      pageBuilder.pages(process) match {
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

      pageBuilder.pages(process) match {
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

      pageBuilder.pages(process) match {
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

      pageBuilder.pages(process) match {
        case Right(_) => fail( "PageBuilder should not create a row from a row stanza with an invalid phrase identifier")
        case Left(List(PhraseNotFound("2", PageBuilderSpec.this.five))) => succeed
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

      pageBuilder.pages(process) match {
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
      pageBuilder.pages(process) match {
        case Right(pages) if pages.length == 2 => succeed
        case Right(pages) => fail(s"Page count is incorrect, found ${pages.length} pages")
        case Left(err) => fail(s"FAIL ${err.toString}")
      }
    }
  }

  "When parsing a process" should  {
    "determine the page title" in new Test {

      case class Dummy(id: String, pageUrl: String, pageTitle: String)

      pageBuilder.pages(Json.parse(processWithCallouts).as[Process]) match {
        case Right(pages) =>
          val pageInfo = fromPageDetails(pages)(Dummy)

          pageInfo shouldNot be(Nil)
          pageInfo.length shouldBe 7

          pageInfo(0).id shouldBe "start"
          pageInfo(0).pageUrl shouldBe "/example-page-1"
          pageInfo(0).pageTitle shouldBe "External Guidance Testing process"

          pageInfo(1).id shouldBe "13"
          pageInfo(1).pageUrl shouldBe "/example-page-2"
          pageInfo(1).pageTitle shouldBe "User role"

          pageInfo(2).id shouldBe "19"
          pageInfo(2).pageUrl shouldBe "/example-page-3"
          pageInfo(2).pageTitle shouldBe "Who reviews and approves the g2uid1ance produced by the designer?"

          pageInfo(6).id shouldBe "31"
          pageInfo(6).pageUrl shouldBe "/example-page-7"
          pageInfo(6).pageTitle shouldBe "Congratulations"

        case _ => fail()
      }
    }

    "determine the input page title" in new Test {

      case class Dummy(id: String, pageUrl: String, pageTitle: String)

      pageBuilder.pages(validOnePageProcessWithProcessCodeJson.as[Process]) match {
        case Right(pages) =>
          val pageInfo = fromPageDetails(pages)(Dummy)

          pageInfo shouldNot be(Nil)
          pageInfo.length shouldBe 1

          pageInfo(0).id shouldBe "start"
          pageInfo(0).pageUrl shouldBe "/feeling-bad"
          pageInfo(0).pageTitle shouldBe "Do you have a tea bag?"

        case _ => fail()
      }
    }

    "determine the Your Call page title" in new Test {

      case class Dummy(id: String, pageUrl: String, pageTitle: String)
      val flow = Map(
        Process.StartStanzaId -> PageStanza("/this", Seq("1"), stack = false),
        "1" -> CalloutStanza(YourCall, 2, Seq("2"), stack = false),
        "2" -> InstructionStanza(0, Seq("4"), None, stack = false),
        "4" -> PageStanza("/that", Seq("5"), stack = false),
        "5" -> QuestionStanza(1, Seq(2, 3), Seq("end", "end"), None, stack = false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pages(process) match {
        case Right(pages) =>
          val pageInfo = fromPageDetails(pages)(Dummy)

          pageInfo shouldNot be(Nil)
          pageInfo.length shouldBe 2

          pageInfo(0).id shouldBe "start"
          pageInfo(0).pageUrl shouldBe "/this"
          pageInfo(0).pageTitle shouldBe "Some Text2"

        case Left(err) =>
          fail(s"Failed with error $err")
      }
    }

    "determine the date input page title" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("/this", Seq("1"), stack = false),
        "1" -> InstructionStanza(0, Seq("2"), None, stack = false),
        "2" -> InputStanza(Date, Seq("end"), 1, None, "date", None, stack = false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )
      case class Dummy(id: String, pageUrl: String, pageTitle: String)
      pageBuilder.pages(process) match {
        case Right(pages) =>
          val pageInfo = fromPageDetails(pages)(Dummy)

          pageInfo shouldNot be(Nil)
          pageInfo.length shouldBe 1

          pageInfo(0).id shouldBe "start"
          pageInfo(0).pageUrl shouldBe "/this"
          pageInfo(0).pageTitle shouldBe "Some Text1"

        case Left(err) =>
          fail(s"Failed with error $err")
      }
    }
  }

  "ProcessPopulation" must {
    val phrasesValid = Vector[Phrase](
      Phrase(Vector("Some Text", "Welsh: Some Text")),
      Phrase(Vector(s"Some Text", s"Welsh, Some Text")),
      Phrase(Vector(s"Some Text2[width:4][norepeat]", s"Welsh: Some Text2[width:4][norepeat]"))
    )

    val phrasesInvalid = Vector[Phrase](
      Phrase(Vector("Some Text", "Welsh: Some Text")),
      Phrase(Vector(s"Some Text", s"Welsh, Some Text")),
      Phrase(Vector(s"Some Text2[width:17]", s"Welsh: Some Text2[width:17]"))
    )


    "accept and use a valid input field width and norepeat option" in new Test {
      val simpleTextInputFlow: Map[String, Stanza] = Map(
          Process.StartStanzaId -> PageStanza("/start", Seq("1"), stack = false),
          "1" -> InstructionStanza(0, Seq("2"), None, stack = false),
          "2" -> InputStanza(Txt, Seq("end"), 2, None, "label", None, stack = false),
          "end" -> EndStanza)
      val process: Process = Process(meta, simpleTextInputFlow, phrasesValid, Vector())

      pageBuilder.pages(process) match {
        case Right(pages) =>
            pages shouldNot be(Nil)
            pages.length shouldBe 1

            pages(0).id shouldBe Process.StartStanzaId
            pages(0).stanzas.size shouldBe 4
            pages(0).stanzas(2) match {
              case i: Input =>
                i.width shouldBe "4"
                i.dontRepeatName shouldBe true
              case _ => fail()
            }

        case Left(err) => fail(s"Flow error $err")
      }
    }

    "accept and use an invalid input field width" in new Test {
      val simpleTextInputFlow: Map[String, Stanza] = Map(
          Process.StartStanzaId -> PageStanza("/start", Seq("1"), stack = false),
          "1" -> InstructionStanza(0, Seq("2"), None, stack = false),
          "2" -> InputStanza(Txt, Seq("end"), 2, None, "label", None, stack = false),
          "end" -> EndStanza)
      val process: Process = Process(meta, simpleTextInputFlow, phrasesInvalid, Vector())

      pageBuilder.pages(process) match {
        case Left(List(InvalidFieldWidth("2"))) => succeed
        case _ => fail()
      }
    }

  }

}
