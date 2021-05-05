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

class PageBuilderErrorsSpec extends BaseSpec with ProcessJson {
  // Define instance of class used in testing
  val pageBuilder: ValidatingPageBuilder = new ValidatingPageBuilder(new PageBuilder(new Placeholders(new DefaultTodayProvider)))

  val meta: Meta = Json.parse(prototypeMetaSection).as[Meta]

  case object DummyStanza extends Stanza {
    override val next: Seq[String] = Seq("1")
  }

  "PageBuilder error handling" must {

    "detect IncompleteDateInputPage error" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("/url1", Seq("1"), stack = true),
        "1" -> InstructionStanza(0, Seq("2"), None, stack = false),
        "2" -> InputStanza(Currency, Seq("3"), 1, Some(2), "Label", None, stack = false),
        "3" -> PageStanza("/url2", Seq("4"), stack = false),
        "4" -> InstructionStanza(3, Seq("5"), None, stack = false),
        "5" -> InputStanza(Date, Seq("6"), four, Some(five), "Label", None, stack = false),
        "6" -> PageStanza("/url3", Seq("7"), stack = false),
        "7" -> InstructionStanza(six, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3")),
          Phrase(Vector("Some Text4", "Welsh: Some text4")),
          Phrase(Vector("Some Text5", "Welsh: Some text5")),
          Phrase(Vector("Some Text6", "Welsh: Some text6"))
        ),
        Vector[Link]()
      )
      pageBuilder.pagesWithValidation(process, Process.StartStanzaId) match {
        case Left(Seq(IncompleteDateInputPage("3"))) => succeed
        case err => fail(s"IncompleteDateInputPage not detected $err")
      }
    }

    "detect VisualStanzasAfterDataInput error when Question stanzas followed by UI stanzas" in {
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
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(VisualStanzasAfterDataInput("4"))) => succeed
        case Left(err) => fail(s"Should generate VisualStanzasAfterDataInput, failed with $err")
        case x => fail(s"Should generate VisualStanzasAfterDataInput, returned $x")
      }
    }

    "detect VisualStanzasAfterDataInput with possible loop in post Question stanzas" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("/url", Seq("1"), true),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(1, Seq(2, 3, four, five), Seq("4", "5", "6", "7"), None, false),
        "4" -> Choice(ChoiceStanza(Seq("5","6"), Seq(ChoiceStanzaTest("[label:X]", LessThanOrEquals, "8")), false)),
        "5" -> ValueStanza(List(Value(ScalarType, "PageUrl", "/blah")), Seq("4"), false),
        "6" -> InstructionStanza(0, Seq("end"), None, false),
        "7" -> InstructionStanza(0, Seq("end"), None, false),
        "end" -> EndStanza
      )
      val process = Process(
        metaSection,
        flow,
        Vector[Phrase](
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3")),
          Phrase(Vector("Some Text4", "Welsh: Some Text4")),
          Phrase(Vector("Some Text5", "Welsh: Some Text5"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(VisualStanzasAfterDataInput("6"))) => succeed
        case Left(err) => fail(s"Should generate VisualStanzasAfterDataInput, failed with $err")
        case x => fail(s"Should generate VisualStanzasAfterDataInput, returned $x")
      }
    }

    "not detect visual stanzas after data input error for standard date input pattern" in {

      val flow = Map(
        Process.StartStanzaId -> PageStanza("/page-1", Seq("1"), stack = false),
        "1" -> CalloutStanza(Error, 0, Seq("2"), stack = false),
        "2" -> CalloutStanza(Error, 1, Seq("3"), stack = true),
        "3" -> CalloutStanza(Error, 2, Seq("4"), stack = true),
        "4" -> CalloutStanza(TypeError, 3, Seq("5"), stack = false),
        "5" -> ChoiceStanza(
          Seq("6", "7"),
          Seq(ChoiceStanzaTest("[label:ErrorCode]", Equals, "out_of_range")),
          stack = false
        ),
        "7" -> InputStanza(Date, Seq("8"), four, None, "date_label", None, stack = false),
        "6" -> CalloutStanza(ValueError, five, Seq("7"), stack = false),
        "8" -> ChoiceStanza(
          Seq("1", "9"),
          Seq(ChoiceStanzaTest("[label:date_label", MoreThan, "[timescale:today]")),
          stack = false
        ),
        "9" -> PageStanza("/page-2", Seq("10"), stack = false),
        "10" -> InstructionStanza(six, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )

      val phrases: Vector[Phrase] = Vector(
        Phrase("Date of birth must include a {0} and {1}", "Welsh, Date of birth must include a {0} and {1}"),
        Phrase("Date of birth must include a {0}", "Welsh, Date of birth must include a {0}"),
        Phrase("You must enter a date", "Welsh, You must enter a date"),
        Phrase("You must enter a real date", "Welsh, You must enter a real date"),
        Phrase("What is your date of birth?", "Welsh, What is your date of birth?"),
        Phrase(
          "Date of birth must be on or after 1 January 1900 and not in the future",
          "Welsh, Date of birth must be on or after 1 January 1900 and not in the future"),
        Phrase("The second page", "Welsh, The second page")
      )

      val process = Process(
        metaSection,
        flow,
        phrases,
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Right(_) => succeed
        case Left(err) => fail(s"Should not detect any errors, failed with $err")
      }
    }

    "detect visual stanzas after data input error for standard date input pattern" in {

      val flow = Map(
        Process.StartStanzaId -> PageStanza("/page-1", Seq("1"), stack = false),
        "1" -> CalloutStanza(Error, 0, Seq("2"), stack = false),
        "2" -> CalloutStanza(Error, 1, Seq("3"), stack = true),
        "3" -> CalloutStanza(Error, 2, Seq("4"), stack = true),
        "4" -> CalloutStanza(TypeError, 3, Seq("5"), stack = false),
        "5" -> ChoiceStanza(
          Seq("6", "7"),
          Seq(ChoiceStanzaTest("[label:ErrorCode]", Equals, "out_of_range")),
          stack = false
        ),
        "7" -> InputStanza(Date, Seq("8"), four, None, "date_label", None, stack = false),
        "6" -> CalloutStanza(ValueError, five, Seq("7"), stack = false),
        "8" -> CalloutStanza(SubSection, six, Seq("9"), stack = false),
        "9" -> ChoiceStanza(
          Seq("1", "10"),
          Seq(ChoiceStanzaTest("[label:date_label", MoreThan, "[timescale:today]")),
          stack = false
        ),
        "10" -> PageStanza("/page-2", Seq("11"), stack = false),
        "11" -> InstructionStanza(seven, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )

      val phrases: Vector[Phrase] = Vector(
        Phrase("Date of birth must include a {0} and {1}", "Welsh, Date of birth must include a {0} and {1}"),
        Phrase("Date of birth must include a {0}", "Welsh, Date of birth must include a {0}"),
        Phrase("You must enter a date", "Welsh, You must enter a date"),
        Phrase("You must enter a real date", "Welsh, You must enter a real date"),
        Phrase("What is your date of birth?", "Welsh, What is your date of birth?"),
        Phrase(
          "Date of birth must be on or after 1 January 1900 and not in the future",
          "Welsh, Date of birth must be on or after 1 January 1900 and not in the future"),
        Phrase("Page footer", "Welsh, Page footer"),
        Phrase("The second page", "Welsh, The second page")
      )

      val process = Process(
        metaSection,
        flow,
        phrases,
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(VisualStanzasAfterDataInput("8"))) => succeed
        case Left(err) => fail(s"Should generate VisualStanzasAfterDataInput, failed with $err")
        case x => fail(s"Should generate VisualStanzasAfterDataInput, returned $x")
      }
    }

    "detect a visual stanza after an exclusive sequence input component" in {

      val flow: Map[String, Stanza] = Map(
        Process.StartStanzaId -> PageStanza("/page-1", Seq("1"), stack = false),
        "1" -> CalloutStanza(Title, 0, Seq("1.5"), stack = false),
        "1.5" -> CalloutStanza(TypeError, nine, Seq("2"), stack = false),
        "2" -> SequenceStanza(1, Seq("3", "5", "7"), Seq(2, 3, four), None, stack = false),
        "3" -> PageStanza("/page-2", Seq("4"), stack = false),
        "4" -> CalloutStanza(Title, five, Seq("end"), stack = false),
        "5" -> PageStanza("/page-3", Seq("6"), stack = false),
        "6" -> InstructionStanza(six, Seq("end"), None, stack = false),
        "7"-> InstructionStanza(seven, Seq("8"), None, stack = false),
        "8" -> PageStanza("/page-4", Seq("9"), stack = false),
        "9" -> InstructionStanza(eight, Seq("end"), None, stack = false),
        "end" -> EndStanza
      )

      val phrases: Vector[Phrase] = Vector(
        Phrase("Sequence example", "Welsh: Sequence example"),
        Phrase("Select your favourite type of sweet", "Welsh: Select your favourite type of sweet"),
        Phrase("Wine gums", "Welsh: Wine gums"),
        Phrase("Strawberry bonbons", "Welsh: Strawberry bonbons"),
        Phrase("Something else [exclusive]", "Welsh: Something else [exclusive]"),
        Phrase("You like wine gums", "Welsh: You like wine gums"),
        Phrase("You like strawberry bonbons", "Welsh: You like strawberry bonbons"),
        Phrase("Some random stuff", "Welsh: Some random stuff"),
        Phrase("You like something else", "Welsh, You like something else"),
        Phrase("You must select an option", "Welsh: You must select an option")
      )

      val process = Process(
        metaSection,
        flow,
        phrases,
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(VisualStanzasAfterDataInput("7"))) => succeed
        case Left(err) => fail(s"Should generate VisualStanzasAfterDataInput, failed with $err")
        case x => fail(s"Should generate VisualStanzasAfterDataInput, returned $x")
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
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "Welsh: Some Text1")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(DuplicatePageUrl("4", "/this"))) => succeed
        case Left(err) => fail(s"DuplicatePageUrl error not detected, failed with $err")
        case res => fail(s"DuplicatePageUrl not detected $res")
      }
    }

    "detect UseOfReservedUrl" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("/this", Seq("1"), false),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(1, Seq(2, 3), Seq("4", "5"), None, false),
        "4" -> PageStanza("/session-restart", Seq("5"), false),
        "5" -> PageStanza("/session-timeout", Seq("end"), false),
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

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(UseOfReservedUrl("4"), UseOfReservedUrl("5"))) => succeed
        case Left(err) => fail(s"UseOfReservedUrl error not detected, failed with $err")
        case Right(res) => fail(s"UseOfReservedUrl not detected $res")
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
          Phrase(Vector("Some Text", "Welsh: Some Text")),
          Phrase(Vector("Some Text1", "")),
          Phrase(Vector("Some Text2", "Welsh: Some Text2")),
          Phrase(Vector("Some Text3", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )
      pageBuilder.pagesWithValidation(process) match {
        case Left(List(MissingWelshText("2", _, "Some Text1"))) => succeed
        case Left(err) => fail(s"MissingWelshText error not detected, failed with $err")
        case _ => fail(s"MissingWelshText not detected")
      }
    }

    "detect InconsistentQuestionError" in {
      val flow = Map(
        Process.StartStanzaId -> PageStanza("/this", Seq("1"), false),
        "1" -> InstructionStanza(0, Seq("2"), None, false),
        "2" -> QuestionStanza(1, Seq(2, 3), Seq("4"), None, false),
        "4" -> PageStanza("/that", Seq("5"), false),
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
      pageBuilder.pagesWithValidation(process) match {
        case Left(List(InconsistentQuestionError("2"))) => succeed
        case Left(err) => fail(s"InconsistentQuestionError error not detected, failed with $err")
        case _ => fail(s"InconsistentQuestionError not detected")
      }
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
          Phrase(Vector("Some Text3 [exclusive]", "Welsh: Some Text3"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Right(_) => succeed
        case Left(err) => fail(s"Valid sequence stanza definition should pass validation, but failed with error $err")
      }

    }

    "detect multiple exclusive options in a sequence stanza" in {

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
          Phrase(Vector("Some Text2 [exclusive:Hint]", "Welsh: Some Text2 [exclusive:Welsh: Hint]")),
          Phrase(Vector("Some Text3 [exclusive:Hint]", "Welsh: Some Text3 [exclusive:Welsh: Hint]"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(MultipleExclusiveOptions("2"))) => succeed
        case Left(err) => fail(s"Failed to detect multiple exclusive options. Instead failed with error $err")
        case _ => fail("Failed to detect multiple exclusive options")
      }
    }

    "detect missing TypeError callout in an exclusive sequence stanza" in {

      val flow: Map[String, Stanza] = Map(
        Process.StartStanzaId -> PageStanza("/start", Seq("1"), stack = false),
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
          Phrase(Vector("Some Text3 [exclusive: Hint]", "Welsh: Some Text3 [exclusive:Welsh: Hint]"))
        ),
        Vector[Link]()
      )

      pageBuilder.pagesWithValidation(process) match {
        case Left(List(IncompleteExclusiveSequencePage(Process.StartStanzaId))) => succeed
        case Left(err) => fail(s"Failed to detect missing TypeError callout. Instead failed with error $err")
        case _ => fail("Failed to detect missing TypeError callout")
      }
    }

  }

}
