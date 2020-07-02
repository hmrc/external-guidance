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

///*
// * Copyright 2020 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package services
//
//import models.ocelot._
//import models.ocelot.stanzas._
//import models.ui._
//import play.api.data.FormError
//
//class UIBuilderSpec extends BaseSpec with ProcessJson {
//
//  trait QuestionTest {
//
//    implicit val urlMap: Map[String, String] =
//      Map(
//        Process.StartStanzaId -> "/blah",
//        "3" -> "dummy-path",
//        "4" -> "dummy-path/question",
//        "5" -> "dummy-path/blah",
//        "6" -> "dummy-path/anotherquestion",
//        "34" -> "dummy-path/next"
//      )
//    val answerDestinations = Seq("4", "5", "6")
//    val questionPhrase: Phrase = Phrase(Vector("Some Text", "Welsh, Some Text"))
//    val questionHintString = "A hint!!"
//    val questionWithHintPhrase: Phrase = Phrase(Vector(s"Some Text[hint:${questionHintString}]", s"Welsh, Some Text[hint:${questionHintString}]"))
//
//    val answers =
//      Seq(Phrase(Vector("Some Text", "Welsh, Some Text")), Phrase(Vector("Some Text", "Welsh, Some Text")), Phrase(Vector("Some Text", "Welsh, Some Text")))
//    val question: models.ocelot.stanzas.Question = Question(questionPhrase, answers, answerDestinations, false)
//
//    val stanzas = Seq(
//      PageStanza("/blah", Seq("1"), false),
//      Callout(Error, Phrase(Vector("Some Text", "Welsh, Some Text")), Seq("3"), false),
//      Callout(Section, Phrase(Vector("Some Text", "Welsh, Some Text")), Seq("4"), false),
//      Instruction(Phrase(Vector("Some Text", "Welsh, Some Text")), Seq("end"), None, false)
//    )
//
//    val page = Page(Process.StartStanzaId, "/test-page", stanzas :+ Question(questionPhrase, answers, answerDestinations, false), Seq(""), Nil)
//
//    val pageWithQuestionHint =
//      Page(Process.StartStanzaId, "/test-page", stanzas :+ Question(questionWithHintPhrase, answers, answerDestinations, false), Seq(""), Nil)
//    val uiBuilder: UIBuilder = new UIBuilder()
//
//    val four: Int = 4
//  }
//
//  "UIBulider Question processing" must {
//
//    "Ignore Error Callouts when there are no errors" in new QuestionTest {
//      uiBuilder.fromStanzaPage(page, None)(urlMap) match {
//        case s: QuestionPage if s.question.errorMsgs.isEmpty => succeed
//        case s: QuestionPage => fail("No error messages should be included on page")
//        case _ => fail("Should return QuestionPage")
//      }
//    }
//
//    "Include Error messages when there are errors" in new QuestionTest {
//      val formError = new FormError("test-page", List("error.required"))
//      val formData = Some(FormData("test-page", Map(), List(formError)))
//
//      uiBuilder.fromStanzaPage(page, formData)(urlMap) match {
//        case s: QuestionPage if s.question.errorMsgs.isEmpty => fail("No error messages found on page")
//        case s: QuestionPage => succeed
//        case _ => fail("Should return QuestionPage")
//      }
//    }
//
//    "Maintain order of components within a Question" in new QuestionTest {
//      uiBuilder.fromStanzaPage(page, None) match {
//        case q: QuestionPage =>
//          q.question.body(0) match {
//            case h: H3 => succeed
//            case _ => fail("Ordering of question body components not maintained")
//          }
//          q.question.body(1) match {
//            case h: Paragraph => succeed
//            case _ => fail("Ordering of question body components not maintained")
//          }
//
//        case _ => fail("Page should be a Question page")
//      }
//
//    }
//
//    "Include a question hint appended to the question text" in new QuestionTest {
//      uiBuilder.fromStanzaPage(pageWithQuestionHint)(urlMap) match {
//        case s: QuestionPage if s.question.hint == Some(Text(questionHintString, questionHintString)) => succeed
//        case s: QuestionPage => fail("No hint found within Question")
//        case _ => fail("Should return QuestionPage")
//      }
//    }
//
//  }
//
//  trait Test extends ProcessJson {
//
//    val lang0 = Vector("Some Text", "Welsh, Some Text")
//    val lang1 = Vector("Some Text1", "Welsh, Some Text1")
//    val lang2 = Vector("Some Text2", "Welsh, Some Text2")
//    val lang3 = Vector("Some Text3", "Welsh, Some Text3")
//    val lang4 = Vector("Some Text4", "Welsh, Some Text4")
//
//    val ltxt1 = Text(Words("This is a ", true), Words("Welsh, This is a ", true))
//    val ltxt2 = Text(" followed by ", " Welsh, followed by ")
//    val ltxt3 = Text(" and nothing", " Welsh, and nothing")
//    val link1TxtEn = "A link"
//    val link1TxtCy = "Welsh, A link"
//    val link2TxtEn = "Another Link"
//    val link2TxtCy = "Welsh, Another Link"
//    val link2StartEn = "Back to beginning"
//    val link2StartCy = "Back to beginning"
//    val link1Txt2En = "A link at start of phrase"
//    val link1Txt2Cy = "Welsh, A link at start of phrase"
//    val link2Txt2En = "Another Link at end of phrase"
//    val link2Txt2Cy = "Welsh, Another Link at end of phrase"
//
//    val pageLink1TextEn = "A page link"
//    val pageLink1TextCy = "Welsh, A page link"
//    val pageLink2TextEn = "Another page link"
//    val pageLink2TextCy = "Welsh, Another page link"
//    val q1 = Vector("Do you agree?", "Welsh, Do you agree?")
//    val ans1 = Vector("Yes", "Welsh, Yes")
//    val ans2 = Vector("No", "Welsh, Yes")
//    val ans3 = Vector("Not sure", "Welsh, Yes")
//
//    val ans1WithHint = Vector("Yes[hint:You agree with the assertion]", "Welsh, Yes[hint:Welsh, You agree with the assertion]")
//    val ans2WithHint = Vector("No[hint:You DONT agree with the assertion]", "Welsh, Yes[hint:Welsh, You DONT agree with the assertion]")
//    val ans3WithHint = Vector("Not sure[hint:You dont know]", "Welsh, Yes[hint:Welsh, You dont know]")
//
//    val hint1 = Text("You agree with the assertion", "Welsh, You agree with the assertion")
//    val hint2 = Text("You DONT agree with the assertion", "Welsh, You DONT agree with the assertion")
//    val hint3 = Text("You dont know", "Welsh, You dont know")
//
//    val link1En = Link("https://www.bbc.co.uk", link1TxtEn, false)
//    val link2En = Link("https://www.gov.uk", link2TxtEn, false)
//    val link2_1En = Link("https://www.bbc.co.uk", link1Txt2En, false)
//    val link2_2En = Link("https://www.gov.uk", link2Txt2En, false)
//    val link3En = Link("dummy-path/blah", lang4(0), false)
//    val link4En = Link("https://www.bbc.co.uk", lang4(0), false)
//
//    val pageLink1En = Link("dummy-path/next", pageLink1TextEn)
//    val pageLink2En = Link("dummy-path", pageLink2TextEn)
//
//    val startLinkEn = Link("/blah", link2StartEn)
//    val startLinkCy = Link("/blah", link2StartCy)
//
//    val link1Cy = Link("https://www.bbc.co.uk", link1TxtCy, false)
//    val link2Cy = Link("https://www.gov.uk", link2TxtCy, false)
//    val link2_1Cy = Link("https://www.bbc.co.uk", link1Txt2Cy, false)
//    val link2_2Cy = Link("https://www.gov.uk", link2Txt2Cy, false)
//    val link3Cy = Link("dummy-path/blah", lang4(1), false)
//    val link4Cy = Link("https://www.bbc.co.uk", lang4(1), false)
//
//    val pageLink1Cy = Link("dummy-path/next", pageLink1TextCy)
//    val pageLink2Cy = Link("dummy-path", pageLink2TextCy)
//
//    implicit val urlMap: Map[String, String] =
//      Map(
//        Process.StartStanzaId -> "/blah",
//        "3" -> "dummy-path",
//        "4" -> "dummy-path/question",
//        "5" -> "dummy-path/blah",
//        "6" -> "dummy-path/anotherquestion",
//        "34" -> "dummy-path/next"
//      )
//    val answerDestinations = Seq("4", "5", "6")
//    val answerDestinationUrls = Seq("dummy-path/question", "dummy-path/blah", "dummy-path/anotherquestion")
//
//    val txtWithLinks = Phrase(
//      Vector(
//        "[bold:This is a ][link:A link:https://www.bbc.co.uk] followed by [link:Another Link:https://www.gov.uk] and nothing",
//        "[bold:Welsh, This is a ][link:Welsh, A link:https://www.bbc.co.uk] Welsh, followed by [link:Welsh, Another Link:https://www.gov.uk] Welsh, and nothing"
//      )
//    )
//
//    val txtWithLinks2 = Phrase(
//      Vector(
//        "[link:A link at start of phrase:https://www.bbc.co.uk] followed by [link:Another Link at end of phrase:https://www.gov.uk]",
//        "[link:Welsh, A link at start of phrase:https://www.bbc.co.uk] Welsh, followed by [link:Welsh, Another Link at end of phrase:https://www.gov.uk]"
//      )
//    )
//
//    val txtWithPageLinks = Phrase(
//      Vector(
//        "[bold:This is a ][link:A page link:34] followed by [link:Another page link:3] and nothing",
//        "[bold:Welsh, This is a ][link:Welsh, A page link:34] Welsh, followed by [link:Welsh, Another page link:3] Welsh, and nothing"
//      )
//    )
//
//    val txtWithAllLinks = Phrase(
//      Vector(
//        "[link:A link at start of phrase:https://www.bbc.co.uk] followed by [link:A page link:34][link:Back to beginning:start]",
//        "[link:Welsh, A link at start of phrase:https://www.bbc.co.uk] Welsh, followed by [link:Welsh, A page link:34][link:Back to beginning:start]"
//      )
//    )
//
//    val linkInstructionStanza = Instruction(Phrase(lang4), Seq("end"), Some(models.ocelot.Link(7, "5", "", false)), false)
//    val hyperLinkInstructionStanza = Instruction(Phrase(lang4), Seq("end"), Some(models.ocelot.Link(7, "https://www.bbc.co.uk", "", false)), false)
//    val embeddedLinkInstructionStanza = Instruction(txtWithLinks, Seq("end"), None, false)
//    val embeddedLinkInstructionStanza2 = Instruction(txtWithLinks2, Seq("end"), None, false)
//    val embeddedPageLinkInstructionStanza = Instruction(txtWithPageLinks, Seq("end"), None, false)
//    val embeddedAllLinkInstructionStanza = Instruction(txtWithAllLinks, Seq("end"), None, false)
//
//    val questionPhrase: Phrase = Phrase(q1)
//    val answers = Seq(Phrase(ans1), Phrase(ans2), Phrase(ans3))
//    val answersWithHints = Seq(Phrase(ans1WithHint), Phrase(ans2WithHint), Phrase(ans3WithHint))
//    val question: models.ocelot.stanzas.Question = Question(questionPhrase, answers, answerDestinations, false)
//    val questionWithAnswerHints: models.ocelot.stanzas.Question = Question(questionPhrase, answersWithHints, answerDestinations, false)
//
//    val initialStanza = Seq(
//      PageStanza("/blah", Seq("1"), false),
//      Instruction(Phrase(lang2), Seq("2"), None, false),
//      Callout(Title, Phrase(lang0), Seq("3"), false),
//      Callout(SubTitle, Phrase(lang1), Seq("4"), false),
//      Callout(Lede, Phrase(lang2), Seq("5"), false),
//      Instruction(Phrase(lang3), Seq("end"), None, false)
//    )
//
//    val stanzasWithQuestion = Seq(
//      PageStanza("/blah", Seq("1"), false),
//      Instruction(Phrase(lang2), Seq("2"), None, false),
//      Instruction(Phrase(lang3), Seq("3"), None, false),
//      question
//    )
//
//    val stanzasWithQuestionAndHints = Seq(
//      PageStanza("/blah", Seq("1"), false),
//      Instruction(Phrase(lang2), Seq("2"), None, false),
//      Instruction(Phrase(lang3), Seq("3"), None, false),
//      questionWithAnswerHints
//    )
//
//    val questionPage = Page(Process.StartStanzaId, "/blah", stanzasWithQuestion, Seq(""), Nil)
//    val questionPageWithHints = Page(Process.StartStanzaId, "/blah", stanzasWithQuestionAndHints, Seq(""), Nil)
//
//    val stanzas = initialStanza ++ Seq(linkInstructionStanza, EndStanza)
//    val stanzasWithHyperLink = initialStanza ++ Seq(hyperLinkInstructionStanza, EndStanza)
//    val stanzasWithEmbeddedLinks = initialStanza ++ Seq(embeddedLinkInstructionStanza, EndStanza)
//    val stanzasWithEmbeddedLinks2 = initialStanza ++ Seq(embeddedLinkInstructionStanza2, EndStanza)
//    val stanzasWithEmbeddedPageLinks = initialStanza ++ Seq(embeddedPageLinkInstructionStanza, EndStanza)
//    val stanzasWithEmbeddedAllLinks = initialStanza ++ Seq(embeddedAllLinkInstructionStanza, EndStanza)
//    val page = Page(Process.StartStanzaId, "/test-page", stanzas, Seq(""), Nil)
//    val hyperLinkPage = Page(Process.StartStanzaId, "/test-page", stanzasWithHyperLink, Seq(""), Nil)
//
//    val textItems = ltxt1 +
//      Text(link1En, link1Cy) +
//      ltxt2 +
//      Text(link2En, link2Cy) +
//      ltxt3
//    val textItems2 = Text(link2_1En, link2_1Cy) + ltxt2 + Text(link2_2En, link2_2Cy)
//
//    val pageLinkTextItems = ltxt1 +
//      Text(pageLink1En, pageLink1Cy) +
//      ltxt2 +
//      Text(pageLink2En, pageLink2Cy) +
//      ltxt3
//    val allLinksTextItems = Text(link2_1En, link2_1Cy) + ltxt2 + Text(pageLink1En, pageLink1Cy) + Text(startLinkEn, startLinkCy)
//
//    val pageWithEmbeddLinks = page.copy(stanzas = stanzasWithEmbeddedLinks)
//    val pageWithEmbeddLinks2 = page.copy(stanzas = stanzasWithEmbeddedLinks2)
//    val pageWithEmbeddPageLinks = page.copy(stanzas = stanzasWithEmbeddedPageLinks)
//    val pageWithEmbeddAllLinks = page.copy(stanzas = stanzasWithEmbeddedAllLinks)
//
//    val brokenLinkPhrase = Phrase(Vector("Hello [link:Blah Blah:htts://www.bbc.co.uk]", "Welsh, Hello [link:Blah Blah:htts://www.bbc.co.uk]"))
//    // for multi page testing
//    val pageBuilder: PageBuilder = new PageBuilder()
//    val stanzaPages = pageBuilder.pages(prototypeJson.as[Process]).right.get
//    val prototypeUrlMap = stanzaPages.map(p => (p.id, p.url)).toMap
//
//    // Define instance of class to be used in tests
//    val uiBuilder: UIBuilder = new UIBuilder()
//
//    val four: Int = 4
//    val five: Int = 5
//  }
//
//  "UIBuilder" must {
//
//    "convert and Ocelot page into a UI page with the same url" in new Test {
//
//      uiBuilder.fromStanzaPage(page) match {
//        case p if p.urlPath == page.url => succeed
//        case p => fail(s"UI page urlPath set incorrectly to ${p.urlPath}")
//      }
//    }
//
//    "convert 1st Callout type Title to H1" in new Test {
//      val uiPage = uiBuilder.fromStanzaPage(page)
//      uiPage.components(1) mustBe models.ui.H1(Text(lang0))
//    }
//
//    "convert 2nd Callout type SubTitle to H2" in new Test {
//
//      val uiPage = uiBuilder.fromStanzaPage(page)
//      uiPage.components(2) mustBe models.ui.H2(Text(lang1))
//    }
//
//    "convert Callout type Lede to lede Paragraph" in new Test {
//
//      val uiPage = uiBuilder.fromStanzaPage(page)
//      uiPage.components(3) mustBe models.ui.Paragraph(Text(lang2), true)
//    }
//
//    "convert Simple instruction to Paragraph" in new Test {
//
//      val uiPage = uiBuilder.fromStanzaPage(page)
//      uiPage.components(four) mustBe models.ui.Paragraph(Text(lang3), false)
//    }
//
//    "convert Link instruction to Paragraph" in new Test {
//
//      val uiPage = uiBuilder.fromStanzaPage(page)
//      val en = Link("dummy-path/blah", lang4(0))
//      val cy = Link("dummy-path/blah", lang4(1))
//      uiPage.components(five) mustBe models.ui.Paragraph(Text(en, cy), false)
//    }
//
//    "convert page with instruction stanza containing a sequence of Text and Link items" in new Test {
//
//      val uiPage = uiBuilder.fromStanzaPage(pageWithEmbeddLinks)
//      uiPage.components(five) mustBe models.ui.Paragraph(textItems, false)
//    }
//
//    "convert page with instruction stanza containing a sequence of TextItems beginning and ending with HyperLinks" in new Test {
//      val uiPage = uiBuilder.fromStanzaPage(pageWithEmbeddLinks2)
//      uiPage.components(5) mustBe models.ui.Paragraph(textItems2, false)
//    }
//
//    "convert page with instruction stanza text containing PageLinks and Text" in new Test {
//      val uiPage = uiBuilder.fromStanzaPage(pageWithEmbeddPageLinks)
//      uiPage.components(5) mustBe models.ui.Paragraph(pageLinkTextItems, false)
//    }
//
//    "convert a sequence of stanza pages into a map of UI pages by url" in new Test {
//      implicit val stanzaToUrlMap: Map[String, String] = stanzaPages.map(p => (p.id, p.url)).toMap
//      val pageMap = uiBuilder.pages(stanzaPages)
//
//      pageMap.keys.toList.length mustBe stanzaPages.length
//
//      stanzaPages.foreach { p =>
//        pageMap.contains(p.url) mustBe true
//      }
//    }
//
//    "convert page with instruction stanza text containing PageLinks, HyperLinks and Text" in new Test {
//      val uiPage = uiBuilder.fromStanzaPage(pageWithEmbeddAllLinks)
//      uiPage.components(five) mustBe models.ui.Paragraph(allLinksTextItems, false)
//    }
//
//    "convert page including a PageLink instruction stanza" in new Test {
//      val uiPage = uiBuilder.fromStanzaPage(page)
//      uiPage.components(five) mustBe models.ui.Paragraph(Text(link3En, link3Cy), false)
//    }
//
//    "convert page including a Link instruction stanza" in new Test {
//      val uiPage = uiBuilder.fromStanzaPage(hyperLinkPage)
//      uiPage.components(five) mustBe models.ui.Paragraph(Text(link4En, link4Cy), false)
//    }
//
//    "convert a question page into a Seq of a single Question UI object" in new Test {
//      val uiPage = uiBuilder.fromStanzaPage(questionPage)
//
//      uiPage.components.length mustBe 1
//
//      uiPage.components.head match {
//        case q: models.ui.Question =>
//          q.answers.length mustBe 3
//
//          q.body.length mustBe 2
//
//          q.answers.head mustBe models.ui.Answer(Text(ans1), None, answerDestinationUrls.head)
//
//          q.answers(1) mustBe models.ui.Answer(Text(ans2), None, answerDestinationUrls(1))
//
//          q.answers(2) mustBe models.ui.Answer(Text(ans3), None, answerDestinationUrls(2))
//
//        case _ => fail("Found non question UIComponent")
//      }
//    }
//
//    "convert a question page including answer hints into a Seq of a single Question UI object" in new Test {
//      val uiPage = uiBuilder.fromStanzaPage(questionPageWithHints)
//
//      uiPage.components.length mustBe 1
//
//      uiPage.components.head match {
//        case q: models.ui.Question =>
//          q.answers.length mustBe 3
//
//          q.body.length mustBe 2
//
//          q.answers.head mustBe models.ui.Answer(Text(ans1), Some(hint1), answerDestinationUrls.head)
//
//          q.answers(1) mustBe models.ui.Answer(Text(ans2), Some(hint2), answerDestinationUrls(1))
//
//          q.answers(2) mustBe models.ui.Answer(Text(ans3), Some(hint3), answerDestinationUrls(2))
//
//        case _ => fail("Found non question UIComponent")
//      }
//    }
//
//    "Process page with a simple instruction group" in new Test {
//
//      val phrase1: Phrase = Phrase(Vector("My favourite sweets are wine gums", "Fy hoff losin yw deintgig gwin"))
//      val phrase2: Phrase = Phrase(Vector("My favourite sweets are humbugs", "Fy hoff losin yw humbugs"))
//
//      val instruction1: Instruction = Instruction(phrase1, Seq("2"), None, true)
//      val instruction2: Instruction = Instruction(phrase2, Seq("end"), None, false)
//
//      val instructionGroup: InstructionGroup = InstructionGroup(Seq(instruction1, instruction2))
//
//      val bulletPointListStanzas = Seq(
//        PageStanza("/blah", Seq("1"), false),
//        instructionGroup
//      )
//
//      val bulletPointListPage = Page(Process.StartStanzaId, "/blah", bulletPointListStanzas, Seq(""), Nil)
//
//      val uiPage = uiBuilder.fromStanzaPage(bulletPointListPage)
//
//      uiPage.components.length mustBe 1
//
//      // Check contents of bullet point list
//      val leadingTextItems: Text = Text(Words("My favourite sweets are"), Words("Fy hoff losin yw"))
//
//      val bulletPointOne: Text = Text("wine gums", "deintgig gwin")
//      val bulletPointTwo: Text = Text("humbugs", "humbugs")
//
//      uiPage.components.head match {
//        case b: BulletPointList => {
//
//          b.text mustBe leadingTextItems
//
//          b.listItems.size mustBe 2
//
//          b.listItems.head mustBe bulletPointOne
//          b.listItems.last mustBe bulletPointTwo
//        }
//        case _ => fail("Did not find bullet point list")
//      }
//    }
//
//    "Process page with a simple instruction group from prototypeJson" in new Test {
//
//      val phrase1: Phrase = Phrase(
//        Vector(
//          "In some circumstances, you do not have to tell HMRC about extra income you've made. In each tax year you can earn up to £11,000, tax free, if you are: selling goods or services (trading)",
//          "Mewn rhai amgylchiadau, nid oes rhaid i chi ddweud wrth Gyllid a Thollau EM am incwm ychwanegol rydych wedi'i wneud. Ymhob blwyddyn dreth gallwch ennill hyd at £ 11,000, yn ddi-dreth, os ydych chi: gwerthu nwyddau neu wasanaethau (masnachu)"
//        )
//      )
//      val phrase2: Phrase = Phrase(
//        Vector(
//          "In some circumstances, you do not have to tell HMRC about extra income you've made. In each tax year you can earn up to £11,000, tax free, if you are: renting land or property",
//          "Mewn rhai amgylchiadau, nid oes rhaid i chi ddweud wrth Gyllid a Thollau EM am incwm ychwanegol rydych wedi'i wneud. Ymhob blwyddyn dreth gallwch ennill hyd at £ 11,000, yn ddi-dreth, os ydych chi: rhentu tir neu eiddo"
//        )
//      )
//
//      val instruction1: Instruction = Instruction(phrase1, Seq("2"), None, true)
//      val instruction2: Instruction = Instruction(phrase2, Seq("end"), None, false)
//
//      val instructionGroup: InstructionGroup = InstructionGroup(Seq(instruction1, instruction2))
//
//      val bulletPointListStanzas = Seq(
//        PageStanza("/blah", Seq("1"), false),
//        instructionGroup
//      )
//
//      val bulletPointListPage = Page(Process.StartStanzaId, "/blah", bulletPointListStanzas, Seq(""), Nil)
//
//      val uiPage = uiBuilder.fromStanzaPage(bulletPointListPage)
//
//      uiPage.components.length mustBe 1
//
//      // Check contents of bullet point list
//      val leadingTextItems: Text = Text(
//        "In some circumstances, you do not have to tell HMRC about extra income you've made. In each tax year you can earn up to £11,000, tax free, if you are:",
//        "Mewn rhai amgylchiadau, nid oes rhaid i chi ddweud wrth Gyllid a Thollau EM am incwm ychwanegol rydych wedi'i wneud. Ymhob blwyddyn dreth gallwch ennill hyd at £ 11,000, yn ddi-dreth, os ydych chi:"
//      )
//
//      val bulletPointOne: Text = Text("selling goods or services (trading)", "gwerthu nwyddau neu wasanaethau (masnachu)")
//      val bulletPointTwo: Text = Text("renting land or property", "rhentu tir neu eiddo")
//
//      uiPage.components.head match {
//        case b: BulletPointList => {
//
//          b.text mustBe leadingTextItems
//
//          b.listItems.size mustBe 2
//
//          b.listItems.head mustBe bulletPointOne
//          b.listItems.last mustBe bulletPointTwo
//        }
//        case _ => fail("Did not find bullet point list")
//      }
//    }
//
//    "Process complex page with both instruction groups and single instructions" in new Test {
//
//      val phrase1: Phrase = Phrase(Vector("Going to the market", "Mynd i'r farchnad"))
//      val phrase2: Phrase = Phrase(Vector("Fruit and Vegetables", "Ffrwythau a llysiau"))
//      val phrase3: Phrase = Phrase(Vector("Vegetables", "Llysiau"))
//      val phrase4: Phrase = Phrase(Vector("What you can buy in our lovely vegetable market", "Beth allwch chi ei brynu yn ein marchnad llysiau hyfryd"))
//      val phrase5: Phrase = Phrase(Vector("Today we have special parsnips for sale", "Heddiw mae gennym bananas arbennig ar werth"))
//      val phrase6: Phrase = Phrase(Vector("Today we have special purple carrots for sale", "Heddiw mae gennym foron porffor arbennig ar werth"))
//      val phrase7: Phrase = Phrase(Vector("Today we have special brussels sprouts for sale", "Heddiw mae gennym ysgewyll cregyn gleision arbennig ar werth"))
//      val phrase8: Phrase = Phrase(Vector("Thank you", "Diolch"))
//
//      val titleCallout: Callout = Callout(Title, phrase1, Seq("1"), false)
//      val instruction1: Instruction = Instruction(phrase2, Seq("2"), None, false)
//      val subTitleCallout: Callout = Callout(SubTitle, phrase3, Seq("3"), false)
//      val instruction2: Instruction = Instruction(phrase4, Seq("4"), None, false)
//
//      val instructionGroupInstruction1: Instruction = Instruction(phrase5, Seq("5"), None, true)
//      val instructionGroupInstruction2: Instruction = Instruction(phrase6, Seq("6"), None, false)
//      val instructionGroupInstruction3: Instruction = Instruction(phrase7, Seq("7"), None, false)
//
//      val instructionGroup: InstructionGroup = InstructionGroup(Seq(instructionGroupInstruction1, instructionGroupInstruction2, instructionGroupInstruction3))
//
//      val instruction3: Instruction = Instruction(phrase8, Seq("8"), None, false)
//
//      // Build sequence of stanzas
//      val stanzaSeq = Seq(
//        PageStanza("/blah", Seq("1"), false),
//        titleCallout,
//        instruction1,
//        subTitleCallout,
//        instruction2,
//        instructionGroup,
//        instruction3
//      )
//
//      val complexPage = Page(Process.StartStanzaId, "/blah", stanzaSeq, Seq(""), Nil)
//
//      val complexUiPage = uiBuilder.fromStanzaPage(complexPage)
//
//      complexUiPage.components.size mustBe 6
//
//      // Check contents of bullet point list
//      val leadingTextItems: Text = Text("Today we have special", "Heddiw mae gennym")
//
//      val bulletPointOne: Text = Text("parsnips for sale", "bananas arbennig ar werth")
//      val bulletPointTwo: Text = Text("purple carrots for sale", "foron porffor arbennig ar werth")
//      val bulletPointThree: Text = Text("brussels sprouts for sale", "ysgewyll cregyn gleision arbennig ar werth")
//
//      complexUiPage.components(four) match {
//        case b: BulletPointList => {
//
//          b.text mustBe leadingTextItems
//
//          b.listItems.size mustBe 3
//
//          b.listItems.head mustBe bulletPointOne
//          b.listItems(1) mustBe bulletPointTwo
//          b.listItems.last mustBe bulletPointThree
//        }
//        case _ => fail("Did not find bullet point list")
//      }
//
//      val finalParagraph: Paragraph = Paragraph(Text("Thank you", "Diolch"))
//
//      complexUiPage.components(five) match {
//        case p: Paragraph => {
//          p mustBe finalParagraph
//        }
//        case _ => fail("The last components is not an instruction")
//      }
//    }
//
//    "Process page with multiple line bullet point list" in new Test {
//
//      val phrase1: Phrase = Phrase(Vector("You must have a tea bag", "Rhaid i chi gael bag te"))
//      val phrase2: Phrase = Phrase(Vector("You must have a cup", "Rhaid i chi gael cwpan"))
//      val phrase3: Phrase = Phrase(Vector("You must have a teaspoon", "Rhaid i chi gael llwy de"))
//      val phrase4: Phrase = Phrase(Vector("You must have water", "Rhaid i chi gael dŵr"))
//      val phrase5: Phrase = Phrase(Vector("You must have an electric kettle", "Rhaid bod gennych chi degell trydan"))
//      val phrase6: Phrase = Phrase(Vector("You must have an electricity supply", "Rhaid bod gennych gyflenwad trydan"))
//
//      val instruction1: Instruction = Instruction(phrase1, Seq("2"), None, stack = true)
//      val instruction2: Instruction = Instruction(phrase2, Seq("3"), None, stack = true)
//      val instruction3: Instruction = Instruction(phrase3, Seq("4"), None, stack = true)
//      val instruction4: Instruction = Instruction(phrase4, Seq("5"), None, stack = true)
//      val instruction5: Instruction = Instruction(phrase5, Seq("6"), None, stack = true)
//      val instruction6: Instruction = Instruction(phrase6, Seq("end"), None, stack = true)
//
//      val instructionGroup: InstructionGroup = InstructionGroup(
//        Seq(
//          instruction1,
//          instruction2,
//          instruction3,
//          instruction4,
//          instruction5,
//          instruction6
//        )
//      )
//
//      val bulletPointStanzas = Seq(PageStanza("/page-1", Seq("1"), false), instructionGroup)
//
//      val bulletPointListPage = Page(Process.StartStanzaId, "/page-1", bulletPointStanzas, Seq(""), Nil)
//
//      val uiPage = uiBuilder.fromStanzaPage(bulletPointListPage)
//
//      uiPage.components.length mustBe 1
//
//      val leadingTextItems: Text = Text("You must have", "Rhaid")
//
//      val bulletPointOne: Text = Text("a tea bag", "i chi gael bag te")
//      val bulletPointTwo: Text = Text("a cup", "i chi gael cwpan")
//      val bulletPointThree: Text = Text("a teaspoon", "i chi gael llwy de")
//      val bulletPointFour: Text = Text("water", "i chi gael dŵr")
//      val bulletPointFive: Text = Text("an electric kettle", "bod gennych chi degell trydan")
//      val bulletPointSix: Text = Text("an electricity supply", "bod gennych gyflenwad trydan")
//
//      uiPage.components.head match {
//
//        case b: BulletPointList => {
//
//          b.text mustBe leadingTextItems
//
//          b.listItems.size mustBe 6
//
//          b.listItems.head mustBe bulletPointOne
//          b.listItems(1) mustBe bulletPointTwo
//          b.listItems(2) mustBe bulletPointThree
//          b.listItems(3) mustBe bulletPointFour
//          b.listItems(four) mustBe bulletPointFive
//          b.listItems.last mustBe bulletPointSix
//        }
//        case _ => fail("Did not find bullet point list")
//      }
//
//    }
//
//  }
//
//}
