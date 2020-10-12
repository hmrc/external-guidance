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

package services

import base.BaseSpec
import models.errors.{Error => MainError, ProcessError}
import models.ocelot.errors._
import models.ocelot.stanzas._
import models.ocelot._
import play.api.libs.json._
import utils.StanzaHelper


class PageBuilderSpec extends BaseSpec with ProcessJson with StanzaHelper {

  // Define instance of class used in testing
  val pageBuilder: PageBuilder = new PageBuilder()

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
      Phrase(Vector("Some Text", "Welsh, Some Text")),
      Phrase(Vector(s"Some Text1 [link:Link to stanza 17:$pageId7]", s"Welsh, Some Text1 [link:Link to stanza 17:$pageId7]")),
      Phrase(Vector(s"Some [link:PageId3:$pageId3] Text2", s"Welsh, Some [link:PageId3:$pageId3] Text2")),
      Phrase(Vector(s"Some [link:Link to stanza 11:$pageId5] Text3", s"Welsh, Some [link:Link to stanza 11:$pageId5] Text3"))
    )

    private val links = Vector(Link(0, pageId3, "", false), Link(1, pageId6, "", false), Link(2, Process.StartStanzaId, "Back to the start", false))

    val processWithLinks = Process(metaSection, flow, phrases, links)
  }

  "PageBuilder error handling" must {

    val flow = Map(
      Process.StartStanzaId -> ValueStanza(List(Value(Scalar, "PageUrl", "/blah")), Seq("1"), false),
      "1" -> InstructionStanza(0, Seq("2"), None, false),
      "2" -> DummyStanza
    )

    "detect StanzaNotFound error" in {
      val process = Process(metaSection, flow, Vector[Phrase](), Vector[Link]())

      pageBuilder.buildPage("4", process) match {
        case Left(StanzaNotFound("4")) => succeed
        case _ => fail("Unknown stanza not detected")
      }
    }

    "detect PageStanzaMissing error when stanza routes to page not starting with PageStanza" in {
      val flow = Map(
        Process.StartStanzaId -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(1, Seq(2, 3), Seq("4", "5"), None, false),
        "4" -> InstructionStanza(0, Seq("end"), None, false),
        "5" -> InstructionStanza(0, Seq("end"), None, false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh, Some Text")),
          Phrase(Vector("Some Text1", "Welsh, Some Text1")),
          Phrase(Vector("Some Text2", "Welsh, Some Text2")),
          Phrase(Vector("Some Text3", "Welsh, Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(PageStanzaMissing("start"))) => succeed
        case Left(err) => fail(s"Missing PageStanza, failed with $err")
        case x => fail(s"Missing PageStanza with $x")
      }
    }

    "detect VisualStanzasAfterQuestion error when Question stanzas followed by UI stanzas" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("/url", Seq("1"), true),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(1, Seq(2, 3), Seq("4", "5"), None, false),
        "4" -> InstructionStanza(0, Seq("end"), None, false),
        "5" -> InstructionStanza(0, Seq("end"), None, false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh, Some Text")),
          Phrase(Vector("Some Text1", "Welsh, Some Text1")),
          Phrase(Vector("Some Text2", "Welsh, Some Text2")),
          Phrase(Vector("Some Text3", "Welsh, Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(VisualStanzasAfterQuestion("4"))) => succeed
        case Left(err) => fail(s"Should generate VisualStanzasAfterQuestion, failed with $err")
        case x => fail(s"Should generate VisualStanzasAfterQuestion, returned $x")
      }
    }

    "detect PageUrlEmptyOrInvalid error when PageValue is present but url is blank" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("", Seq("1"), false),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(1, Seq(2, 3), Seq("4", "5"), None, false),
        "4" -> InstructionStanza(0, Seq("end"), None, false),
        "5" -> InstructionStanza(0, Seq("end"), None, false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh, Some Text")),
          Phrase(Vector("Some Text1", "Welsh, Some Text1")),
          Phrase(Vector("Some Text2", "Welsh, Some Text2")),
          Phrase(Vector("Some Text3", "Welsh, Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(PageUrlEmptyOrInvalid(Process.StartStanzaId))) => succeed
        case Left(err) => fail(s"Missing ValueStanza containing PageUrl value not detected, failed with $err")
        case _ => fail(s"Missing ValueStanza containing PageUrl value not detected")
      }
    }

    "detect PageUrlEmptyOrInvalid error when PageStanza url is /" in {
      val invalidProcess = invalidOnePageJson.as[Process]

      pageBuilder.pagesWithValidation(invalidProcess) match {
        case Left(List(PageUrlEmptyOrInvalid("4"))) => succeed
        case Left(err) => fail(s"PageStanza url equal to / not detected, failed with $err")
        case _ => fail(s"PageStanza url equal to / not detected")
      }
    }

    "detect PhraseNotFound in QuestionStanza text" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(four, Seq(two, three), Seq("4", "5"), None, false),
        "4" -> InstructionStanza(0, Seq("end"), None, false),
        "5" -> InstructionStanza(0, Seq("end"), None, false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh, Some Text")),
          Phrase(Vector("Some Text1", "Welsh, Some Text1")),
          Phrase(Vector("Some Text2", "Welsh, Some Text2")),
          Phrase(Vector("Some Text3", "Welsh, Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(PhraseNotFound(id, four))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(4) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(4)")
      }
    }

    "detect PhraseNotFound in QuestionStanza answers" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(1, Seq(four, three), Seq("4", "5"), None, false),
        "4" -> InstructionStanza(0, Seq("end"), None, false),
        "5" -> InstructionStanza(0, Seq("end"), None, false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh, Some Text")),
          Phrase(Vector("Some Text1", "Welsh, Some Text1")),
          Phrase(Vector("Some Text2", "Welsh, Some Text2")),
          Phrase(Vector("Some Text3", "Welsh, Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(PhraseNotFound(id, four))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(4) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(4)")
      }
    }

    "detect PhraseNotFound in InstructionStanza" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InstructionStanza(2, Seq("end"), None, false),
        "end" -> EndStanza
      )
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh, Some Text"))), Vector[Link]())

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(PhraseNotFound(id, 2))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(2) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(2)")
      }
    }

    "detect PhraseNotFound in InputStanza name" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InputStanza(Currency, Seq("end"), 2, 3, "Label", None, false),
        "end" -> EndStanza
      )
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh, Some Text"))), Vector[Link]())

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(PhraseNotFound(id, 2))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(2) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(2)")
      }
    }

    "detect PhraseNotFound in InputStanza help" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InputStanza(Currency, Seq("end"), 0, 3, "Label", None, false),
        "end" -> EndStanza
      )
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh, Some Text"))), Vector[Link]())

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(PhraseNotFound(id, 3))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(3) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(3)")
      }
    }

    "detect PhraseNotFound in InputStanza placeholder" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InputStanza(Currency, Seq("end"), 0, 0, "Label", Some(3), false),
        "end" -> EndStanza
      )
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh, Some Text"))), Vector[Link]())

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(PhraseNotFound(id, 3))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(3) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(3)")
      }
    }

    "detect PhraseNotFound in CalloutStanza" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> CalloutStanza(Title, 2, Seq("end"), false),
        "end" -> EndStanza
      )
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh, Some Text"))), Vector[Link]())

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(PhraseNotFound(id, 2))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(2) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(2)")
      }
    }

    "detect LinkNotFound(1) in InstructionStanza" in {

      val flow: Map[String, Stanza] = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> InstructionStanza(1, Seq("end"), Some(1), false),
        "end" -> EndStanza
      )

      // Create process with single link
      val process: Process = Process(
        metaSection,
        flow,
        Vector[Phrase](Phrase(Vector("First English phrase", "First Welsh Phrase")), Phrase(Vector("Second English Phrase", "Second Welsh Phrase"))),
        Vector[Link](Link(0, "http://my.com/search", "MyCOM Search Engine", false))
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(LinkNotFound(id, 1))) => succeed
        case Left(err) => fail(s"Missing LinkNotFound error. Actual error raised is $err")
        case Right(_) => fail("Page building terminated successfully when LinkNotFound error expected")
      }
    }

    "detect DuplicatePageUrl" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("/this", Seq("1"), false),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(1, Seq(2, 3), Seq("4", "5"), None, false),
        "4" -> PageStanza("/this", Seq("5"), false),
        "5" -> PageStanza("/that", Seq("end"), false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh, Some Text")),
          Phrase(Vector("Some Text1", "Welsh, Some Text1")),
          Phrase(Vector("Some Text2", "Welsh, Some Text2")),
          Phrase(Vector("Some Text3", "Welsh, Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(DuplicatePageUrl("4", "/this"))) => succeed
        case Left(err) => fail(s"DuplicatePageUrl error not detected, failed with $err")
        case res => fail(s"DuplicatePageUrl not detected $res")
      }
    }

    "detect multiple DuplicatePageUrl" in {
      duplicateUrlsJson.validate[Process] match {
        case JsSuccess(process, _) =>
          pageBuilder.pagesWithValidation(process) match {
            case Left(List(DuplicatePageUrl("6","/feeling-bad"), DuplicatePageUrl("8","/feeling-good"))) => succeed
            case Left(err) => fail(s"DuplicatePageUrl error not detected, failed with $err")
            case res => fail(s"DuplicatePageUrl not detected $res")
          }

        case JsError(errs) => fail(s"Errors reported $errs")
      }

    }

    "detect MissingWelshText" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("/this", Seq("1"), false),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(1, Seq(2, 3), Seq("4", "5"), None, false),
        "4" -> PageStanza("/that", Seq("5"), false),
        "5" -> InstructionStanza(0, Seq("end"), None, false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh, Some Text")),
          Phrase(Vector("Some Text1", "")),
          Phrase(Vector("Some Text2", "Welsh, Some Text2")),
          Phrase(Vector("Some Text3", "Welsh, Some Text3"))
        ),
        Vector[Link]()
      )
      pageBuilder.pagesWithValidation(process) match {
        case Left(List(MissingWelshText("2", _, "Some Text1"))) => succeed
        case Left(err) => fail(s"MissingWelshText error not detected, failed with $err")
        case _ => fail(s"MissingWelshText not detected")
      }
    }

    "detect UnknownCalloutType" in {
      val processErrors: List[ProcessError] =
        List(
          ProcessError("Unsupported InputStanza type UnknownInputType found at stanza id 34","34"),
          ProcessError("Process Links section parse error, reason: error.path.missing, index: 0",""),
          ProcessError("Process Phrases section parse error, reason: error.minLength, index: 5",""),
          ProcessError("Unsupported stanza type UnknownStanza found at stanza id 2","2"),
          ProcessError("""Process Flow section parse error, reason: 'type' is undefined on object:"""+
           """ {"next":["end"],"noteType":"Error","stack":false,"text":59}, stanzaId: 5, target: /flow/5""","5"),
          ProcessError("Unsupported CalloutStanza type UnknownType found at stanza id 4","4"),
          ProcessError("Unsupported ValueStanza Value type AnUnknownType found at stanza id 33","33"),
          ProcessError("Process Meta section parse error, reason: error.path.missing, target: ocelot",""))
      guidancePages(new PageBuilder(), assortedParseErrorsJson).fold(
        errs => errs match {
          case MainError(MainError.UnprocessableEntity, None,Some(errors)) if errors == processErrors => succeed
          case _ => fail(s"Failed with errors: $errs")
        }, _ => fail)
    }
  }

  trait IhtTest extends Test with IhtJson {
    val ihtProcess = ihtJsonShort.as[Process]
  }

  "services" must {
    "determine unique set of case sensitive labels from a collection of pages" in new IhtTest {
      val labels = Seq(Label("Properties",Some("0"),None),
                       Label("Money",Some("0"),None),
                       Label("Household",Some("0"),None),
                       Label("Motor Vehicles",Some("0"),None),
                       Label("Private pension",Some("0"),None),
                       Label("Trust",Some("0"),None),
                       Label("Foreign assets",Some("0"),None),
                       Label("Other assets",Some("0"),None),
                       Label("Mortgage_debt",Some("0"),None),
                       Label("funeral_expenses",Some("0"),None),
                       Label("other_debts",Some("0"),None),
                       Label("left to spouse",Some("0"),None),
                       Label("registered charity",Some("0"),None),
                       Label("nil rate band",Some("0"),None),
                       Label("more than 100k",None,None),
                       Label("Value of Assets",None,None),
                       Label("Value of Debts",None,None),
                       Label("Additional Info",None,None),
                       Label("IHT result",None,None))

      pageBuilder.pagesWithValidation(ihtProcess, "start") match {
        case Right(pages) => services.uniqueLabels(pages) shouldBe labels
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

      pageBuilder.pagesWithValidation(ihtProcess, "start") match {
        case Right(pages) => services.uniqueLabelRefs(pages) shouldBe labelsReferenced
        case Left(err) => fail(s"Failed with $err")
      }
    }
  }

  "PageBuilder" must {

    "Make it possible to validate label references across a sequence of pages" in new IhtTest {
      pageBuilder.pagesWithValidation(ihtProcess, "start") match {
        case Right(pages) =>
          val labels = services.uniqueLabels(pages)
          services.uniqueLabelRefs(pages).forall(lr => labels.exists(_.name == lr)) shouldBe true
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
        case Right(Page(_,url,_,_)) if url.startsWith("/") => succeed
        case Right(_) => fail("Url should be prefixed with a / char")
        case Left(err) => fail(s"Url should be prefixed with a / char, failed with unexpected err $err")
      }
    }

    "Sequence of connected pages" must {

      "not be extractable from a Process using an invalid start key" in {

        val process: Process = prototypeJson.as[Process]

        pageBuilder.pagesWithValidation(process, "unknown") match {
          case Right(_) => fail("""Should fail with StanzaNotFound("unknown")""")
          case Left(List(err)) if err == StanzaNotFound("unknown") => succeed
          case Left(wrongErr) => fail(s"""Should fail with StanzaNotFound("unknown") $wrongErr""")
        }
      }

      "be extractable from a Process using key 'start''" in {

        val process: Process = prototypeJson.as[Process]

        pageBuilder.pagesWithValidation(process) match {
          case Right(pages) =>
            pages shouldNot be(Nil)

            pages.length shouldBe 28

          case Left(err) => fail(s"GuidanceError $err")
        }

      }

      "return pages in order beginning with 'start' page" in {

        val process: Process = prototypeJson.as[Process]

        pageBuilder.pagesWithValidation(process) match {
          case Right(pages) =>
            pages shouldNot be(Nil)

            pages.head.id shouldBe Process.StartStanzaId

          case Left(err) => fail(s"First page must be the requested start page")
        }

      }

      "return pages in order beginning with nominated start page" in {

        val process: Process = prototypeJson.as[Process]

        pageBuilder.pagesWithValidation(process, "120") match {
          case Right(pages) =>
            pages shouldNot be(Nil)

            pages.head.id shouldBe "120"

          case Left(err) => fail(s"GuidanceError $err")
        }

      }

      "correctly identify the pages in a Process accounting fro every stanza" in {

        val process: Process = prototypeJson.as[Process]

        pageBuilder.pagesWithValidation(process) match {
          case Right(pages) =>
            testPagesInPrototypeJson(pages)

          case Left(err) => fail(s"GuidanceError error $err")
        }

      }

      "consist of one page when only page exists" in {
        val process: Process = validOnePageJson.as[Process]
        pageBuilder.pagesWithValidation(process, "start") match {
          case Right(pages) =>
            pages shouldNot be(Nil)

            pages.length shouldBe 1

          case Left(err) => fail(s"GuidanceError $err")
        }

      }

      "confirm one page elements" in {

        val process: Process = Process(
          meta,
          onePage,
          Vector[Phrase](Phrase(Vector("Some Text", "Welsh, Some Text")), Phrase(Vector("Some Text1", "Welsh, Some Text1"))),
          Vector[Link]()
        )

        pageBuilder.pagesWithValidation(process) match {
          case Right(pages) =>
            pages shouldNot be(Nil)

            pages.length shouldBe 1

          case Left(err) => fail(s"GuidanceError $err")
        }
      }

      "follows links to pages identified by stanza id " in new Test {

        pageBuilder.pagesWithValidation(processWithLinks) match {
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

      val process: Process = Process(meta, simpleQuestionPage, phrases, links)

      pageBuilder.pagesWithValidation(process) match {

        case Right(pages) =>
          "Determine the correct number of pages to be displayed" in {

            pages shouldNot be(Nil)

            pages.length shouldBe 3
          }

          val indexedSeqOfPages = pages.toIndexedSeq

          // Test contents of individual pages
          testSqpQp(indexedSeqOfPages(0))

          testSqpFap(indexedSeqOfPages(1))

          testSqpSap(indexedSeqOfPages(2))

        case Left(err) => //fail(s"Flow error $err")
      }
    }
  }

  "When processing a 2 page flow separated by a PageStanza" must {
    val process: Process = Process(
      meta,
      twoPagesSeperatedByValueStanza,
      Vector(Phrase(Vector("Some Text", "Welsh, Some Text")), Phrase(Vector("Some Text1", "Welsh, Some Text1"))),
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

  "When parsing a process" should  {
    "determine the page title" in new Test {

      case class Dummy(id: String, pageUrl: String, pageTitle: String)

      pageBuilder.pagesWithValidation(Json.parse(processWithCallouts).as[Process]) match {
        case Right(pages) =>
          val pageInfo = pageBuilder.fromPageDetails(pages)(Dummy(_,_,_))

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

        case _ => fail
      }
    }

  }
  def testPagesInPrototypeJson(pages: Seq[Page]): Unit = {

    val expectedPageIds: List[String] = List(
      Process.StartStanzaId,
      "26",
      "36",
      "37",
      "39",
      "46",
      "53",
      "60",
      "70",
      "77",
      "120",
      "80",
      "83",
      "90",
      "97",
      "102",
      "109",
      "113",
      "121",
      "124",
      "127",
      "131",
      "159",
      "138",
      "143",
      "151",
      "157",
      "158"
    )

    pages.length shouldBe expectedPageIds.length

    pages.forall(p => expectedPageIds.contains(p.id)) shouldBe true
  }

  /**
    * Test question page in simple question page test
    *
    * @param firstPage
    */
  def testSqpQp(page: Page): Unit = {

    "Define the question page correctly" in {

      page.id shouldBe Process.StartStanzaId
      page.stanzas.size shouldBe 4

      page.stanzas shouldBe Seq(sqpQpPageStanza, sqpQpInstruction, sqpQpCallout, sqpQpQuestion)

      page.next shouldBe Seq("6", "4")
    }

  }

  /**
    * Test first answer page in simple question page test
    *
    * @param secondPage
    */
  def testSqpFap(page: Page): Unit = {

    "Define the first answer page correctly" in {

      page.id shouldBe "4"
      page.stanzas.size shouldBe 3

      page.stanzas(0) shouldBe sqpFapPageStanza
      page.stanzas(1) shouldBe sqpFapInstruction
      page.stanzas.last shouldBe EndStanza

      page.next shouldBe Nil
    }

  }

  /**
    * Test second answer page in simple question page
    *
    * @param thirdPage
    */
  def testSqpSap(page: Page): Unit = {

    "Define the second answer page correctly" in {

      page.id shouldBe "6"
      page.stanzas.size shouldBe 4

      page.stanzas(0) shouldBe sqpSapPageStanza
      page.stanzas(1) shouldBe sqpSapInstruction
      page.stanzas(2) shouldBe sqpSapCallout
      page.stanzas.last shouldBe EndStanza

      page.next shouldBe Nil
    }

  }
}
