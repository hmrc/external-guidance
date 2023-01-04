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

package core.services

import base.BaseSpec
import core.models.ocelot.errors._
import core.models.ocelot.stanzas._
import core.models.ocelot._
import play.api.libs.json._

class PageBuilderErrorsSpec extends BaseSpec with ProcessJson {
  // Define instance of class used in testing
  val pageBuilder: PageBuilder = new PageBuilder(new Timescales(new DefaultTodayProvider))

  val meta: Meta = Json.parse(prototypeMetaSection).as[Meta]

  case object DummyStanza extends Stanza {
    override val next: Seq[String] = Seq("1")
  }

  "PageBuilder error handling" must {

    val flow = Map(
      Process.StartStanzaId -> ValueStanza(List(Value(ScalarType, "PageUrl", "/blah")), Seq("1"), false),
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
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pages(process) match {
        case Left(List(PageStanzaMissing("start"))) => succeed
        case Left(err) => fail(s"Missing PageStanza, failed with $err")
        case x => fail(s"Missing PageStanza with $x")
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
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pages(process) match {
        case Left(List(PageUrlEmptyOrInvalid(Process.StartStanzaId))) => succeed
        case Left(err) => fail(s"Missing ValueStanza containing PageUrl value not detected, failed with $err")
        case _ => fail(s"Missing ValueStanza containing PageUrl value not detected")
      }
    }

    "detect PageUrlEmptyOrInvalid error when PageStanza url is /" in {
      val invalidProcess = invalidOnePageJson.as[Process]

      pageBuilder.pages(invalidProcess) match {
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
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pages(process) match {
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
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pages(process) match {
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
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh: Some Text"))), Vector[Link]())

      pageBuilder.pages(process) match {
        case Left(List(PhraseNotFound(id, 2))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(2) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(2)")
      }
    }

    "detect PhraseNotFound in InputStanza name" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InputStanza(Currency, Seq("end"), 2, Some(3), "Label", None, false),
        "end" -> EndStanza
      )
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh: Some Text"))), Vector[Link]())

      pageBuilder.pages(process) match {
        case Left(List(PhraseNotFound(id, 2))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(2) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(2)")
      }
    }

    "detect PhraseNotFound in InputStanza help" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InputStanza(Currency, Seq("end"), 0, Some(3), "Label", None, false),
        "end" -> EndStanza
      )
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh: Some Text"))), Vector[Link]())

      pageBuilder.pages(process) match {
        case Left(List(PhraseNotFound(id, 3))) => succeed
        case Left(err) => fail(s"Missing PhraseNotFound(3) with error $err")
        case Right(_) => fail(s"Missing PhraseNotFound(3)")
      }
    }

    "detect PhraseNotFound in InputStanza placeholder" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("Blah", Seq("1"), false),
        "1" -> InputStanza(Currency, Seq("end"), 0, Some(0), "Label", Some(3), false),
        "end" -> EndStanza
      )
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh: Some Text"))), Vector[Link]())

      pageBuilder.pages(process) match {
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
      val process = Process(metaSection, flow, Vector[Phrase](Phrase(Vector("Some Text", "Welsh: Some Text"))), Vector[Link]())

      pageBuilder.pages(process) match {
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

      pageBuilder.pages(process) match {
        case Left(List(LinkNotFound(id, 1))) => succeed
        case Left(err) => fail(s"Missing LinkNotFound error. Actual error raised is $err")
        case Right(_) => fail("Page building terminated successfully when LinkNotFound error expected")
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
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )
      pageBuilder.pages(process) match {
        case Left(List(MissingWelshText("2", _, "Some Text1"))) => succeed
        case Left(err) => fail(s"MissingWelshText error not detected, failed with $err")
        case _ => fail(s"MissingWelshText not detected")
      }
    }

    "detect an erroneous timescale" in {
      val jsObject = inValidOnePageWithTimescalesJson
      val result = jsObject.as[JsObject].validate[Process].fold(
        errs => Left(GuidanceError.fromJsonValidationErrors(errs)),
        process => {
          pageBuilder.pages(process, process.startPageId).fold(errs => Left(errs),
            pages => Right((process, pages, jsObject))
          )}
      )
      result.fold(
        {
          case x :: xs if x.equals(TimescalesParseError("RepayReim", "error.expected.jsnumber", "")) => {
            succeed
          }
          case errs => {
            fail(s"Failed with errors: $errs")}
        }, _ => fail)
    }

    "detect UnknownCalloutType" in {
      val guidanceErrors: List[GuidanceError] =
        List(
          UnknownInputType("34", "UnknownInputType"),
          LinksParseError("0", "error.path.missing", ""),
          PhrasesParseError("5", "error.minLength","2"),
          UnknownStanza("2", "UnknownStanza"),
          FlowParseError("5", """'type' is undefined on object: {"next":["end"],"noteType":"Error","stack":false,"text":59}""", """/flow/5"""),
          UnknownCalloutType("4", "UnknownType"),
          UnknownValueType("33", "AnUnknownType"),
          MetaParseError("ocelot", "error.path.missing", ""))

      val jsObject = assortedParseErrorsJson
      val result = jsObject.as[JsObject].validate[Process].fold(
        errs => Left(GuidanceError.fromJsonValidationErrors(errs)),
        process => {
          pageBuilder.pages(process, process.startPageId).fold(errs => Left(errs),
            pages => Right((process, pages, jsObject))
        )}
      )

      result.fold(
        {
          case errors if errors == guidanceErrors => succeed
          case errs => fail(s"Failed with errors: $errs")
        }, _ => fail)
    }

    "pass as valid exclusive sequence with a single exclusive option" in {

      val flow: Map[String, Stanza] = Map(
        Process.StartStanzaId -> PageStanza("/start", Seq("10"), stack = false),
        "10" -> CalloutStanza(TypeError, 2, Seq("1"), false),
        "1" -> InstructionStanza(0, Seq("2"), None, stack = false),
        "2" -> SequenceStanza(1, Seq("3", "5", "7"), Seq(2, 3), None, stack = false),
        "3" -> PageStanza("/page-1", Seq("4"), stack = false),
        "4" -> InstructionStanza(0, Seq("end"), None, stack = false),
        "5" -> PageStanza("/page-2", Seq("6"), stack = false),
        "6" -> InstructionStanza(0, Seq("end"), None, stack = false),
        "7" -> PageStanza("/page-3", Seq("8"), stack = false),
        "8" -> InstructionStanza(0, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )

      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Exclusive sequence stanza", "Welsh: Exclusive sequence stanza")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector(
            "Some Text3 [exclusive][hint:Selecting this checkbox will deselect the other checkboxes]",
            "Welsh: Some Text3 [exclusive][hint:Welsh: Selecting this checkbox will deselect the other checkboxes]"))
        ),
        Vector[Link]()
      )

      pageBuilder.pages(process) match {
        case Right(_) => succeed
        case Left(err) => fail(s"Valid sequence stanza definition should pass validation, but failed with error $err")
      }

    }

  }

}
