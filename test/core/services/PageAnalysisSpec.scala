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

class PageAnalysisSpec extends BaseSpec {
  val tids: List[String] = List(
  "JRSRefCB",
  "JRSBACs",
  "JRSHMRCcall",
  "JRSProgChaseCB",
  "CHBClaimSection",
  "CHBClaimLa",
  "CHBClaimCorr",
  "CHBDocCBO",
  "CHBDOCpost",
  "CHBCBORet",
  "CHBDOCCheck",
  "CHBE140Pcb",
  "CHBE140cb",
  "CHBE140ref",
  "CHBE140search",
  "CHBE140Write",
  "CHBE140FTE",
  "CHBE140Childabroad",
  "CHBE140Ent",
  "CHBE140RepPO",
  "CHBE140ManRecon",
  "CHBFLRepPO",
  "CHBFLHICBC",
  "CHBFLforms",
  "CHBFLManRecon",
  "CHBFLDL84TS",
  "CHBFLDL84TSNot",
  "CHBFLDL84TSProof",
  "CHBFLCertabroad",
  "CHBFLCertabroadUK",
  "CHBOtherArrears",
  "CHBOtherAck",
  "CHBOtherReply",
  "CHBOtherOnline"
  )

  val rtids = tids.reverse

  val ps: List[Phrase] = Range(0,17)
                          .toList
                          .map(i =>Phrase(s"Text [timescale:${tids(i)}:days] $i", s"Welsh: Text [timescale:${tids(i)}:days] $i"))
  val eps: List[Phrase] = Range(0,17)
                            .toList
                            .map(i =>Phrase(s"Text [timescale:${tids(i)}:days] $i[exclusive]", s"Welsh: Text [timescale:${tids(i)}:days] $i[exclusive]"))

  val ops: List[Operation] =  List(
    AddOperation(s"[date_add:MyLabel:${tids(0)}]", s"[timescale:${rtids(0)}:days]", "sum"),
    SubtractOperation(s"[date_add:MyLabel:${tids(1)}]", s"[timescale:${rtids(1)}:days]", "minus"),
    MultiplyOperation(s"[date_add:MyLabel:${tids(2)}]", s"[timescale:${rtids(2)}:days]", "times"),
    DivideOperation(s"[date_add:MyLabel:${tids(3)}]", s"[timescale:${rtids(3)}:days]", "div")
  )

  val tests: Seq[ChoiceTest] = Seq(
    EqualsTest(s"[date_add:MyLabel:${tids(0)}]", s"[date_add:MyLabel:${tids(1)}]")
  )
  // Question page - After
  def page(nxt: String, url: String): PageStanza = PageStanza(url, Seq(nxt), stack = false)
  def instruction(nxt: String, p: Phrase): Instruction = Instruction(p, Seq(nxt), None, stack = false)
  def calculation(nxt: String, ops: Seq[Operation]): Calculation = Calculation(Seq(nxt), ops)
  def choice(nxt: Seq[String], tsts: Seq[ChoiceTest]): Choice = Choice(nxt, tsts)
  def title(nxt: String, p: Phrase): TitleCallout = TitleCallout(p, Seq(nxt), stack = false)
  def subTitle(nxt: String, p: Phrase): SubTitleCallout = SubTitleCallout(p, Seq(nxt), stack = false)
  def question(nxt: String, p: Phrase, as: Seq[Phrase]): Question = Question(p, as, Seq(nxt), None, stack = false)
  def currencyInput(nxt: String, p: Phrase, h: Option[Phrase]): CurrencyInput = CurrencyInput(Seq(nxt), p, h, "label", None, stack = false)
  def dateInput(nxt: String, p: Phrase, h: Option[Phrase]): DateInput = DateInput(Seq(nxt), p, h, "label", None, stack = false)
  def fiveOptionExclusiveSequence(nxt: Seq[String]): Sequence = Sequence(
    ps(9),
    //Seq("4", "4", "4", "4", "6"),
    nxt,
    Seq(ps(10), ps(11), ps(12)),
    Some(eps(0)),
    None,
    stack = false
  )

  val values: List[Value] = List(
    Value(ScalarType, "val0", s"[date_add:MyLabel:${rtids(7)}]"),
    Value(ScalarType, "val1", s"date_add:MyLabel:${rtids(8)}]"),
    Value(ScalarType, "val2", s"date_add:MyLabel:${rtids(9)}]"),
    Value(ScalarType, "val3", s"date_add:MyLabel:${rtids(10)}]"),
    Value(ScalarType, "val4", s"date_add:MyLabel:${rtids(11)}]"),
  )

  trait Test {
    val stanzas1: List[KeyedStanza] =
      List(
        KeyedStanza(Process.StartStanzaId, page("1", "/start")),
        KeyedStanza("1", instruction("2",ps(5))),
        KeyedStanza("2", question("3",ps(1), Seq(ps(2), ps(3), ps(4))))
      )
    val stanzas2: List[KeyedStanza] =
      List(
        KeyedStanza("3", page("4", "/two")),
        KeyedStanza("4", calculation("5", Seq(ops(0), ops(2)))),
        KeyedStanza("5", ValueStanza(values, Seq("6"), false)),
        KeyedStanza("6", fiveOptionExclusiveSequence(Seq("end", "11", "end", "end", "7")))
      )
    val stanzas3: List[KeyedStanza] =
      List(
        KeyedStanza("7", page("8", "/three")),
        KeyedStanza("8", title("9",ps(8))),
        KeyedStanza("9", subTitle("10", ps(9))),
        KeyedStanza("10", currencyInput("11", ps(10), Some(ps(11)))),
        KeyedStanza("11", choice(Seq("end", "end"), tests))
      )
    val page1: Page = Page(Process.StartStanzaId, "/start", stanzas1, Seq("3"))
    val page2: Page = Page("3", "/two", stanzas2, Seq("7"))
    val page3: Page = Page("7", "/three", stanzas3, Seq("end"))

    val timescales = new Timescales(new DefaultTodayProvider)
  }

  "Timescales" must {
    "Detect all referencedIds in a page" in new Test {
      timescales.referencedIds(page1) shouldBe List("CHBClaimLa", "JRSBACs", "JRSHMRCcall", "JRSProgChaseCB", "CHBClaimSection")
      timescales.referencedIds(page2) shouldBe  List("JRSRefCB", "CHBOtherOnline", "JRSHMRCcall", "CHBOtherAck", "CHBFLDL84TSNot",
                                                      "CHBCBORet", "CHBDOCCheck", "CHBE140Pcb", "CHBE140cb")
      timescales.referencedIds(page3) shouldBe List("CHBDOCpost", "CHBCBORet", "CHBDOCCheck", "CHBE140Pcb", "JRSRefCB", "JRSBACs")
    }

    "Detect all timescales in a Phrase" in new Test {
      val p: Phrase = Phrase(s"This is a timescale [timescale:${tids(4)}:days] and this a date_add [date_add:MyLabel:${rtids(0)}] and more text",
                             s"Welsh:This is a timescale [timescale:${tids(4)}:days] and this a date_add [date_add:MyLabel:${rtids(0)}] and more text")

      timescales.referencedIds(p) shouldBe List("CHBClaimSection", "CHBOtherOnline")
    }

    "Detect all timescales in a String" in new Test {
      timescales.referencedIds(s"This is a timescale [timescale:${tids(4)}:days] and this a date_add [date_add:MyLabel:${rtids(0)}] and more text") shouldBe List("CHBClaimSection", "CHBOtherOnline")
    }
  }

}
