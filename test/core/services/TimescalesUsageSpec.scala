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
import core.models.ocelot._

class TimescalesUsageSpec extends BaseSpec with ProcessJson {
  val tids: Seq[String] = Seq(
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

  val ps: List[Phrase] = Range(0, tids.length)
                          .toList
                          .map(i =>Phrase(s"Text [timescale:${tids(i)}:days] $i", s"Welsh: Text [timescale:${tids(i)}:days] $i"))
  val eps: List[Phrase] = Range(0, tids.length)
                          .toList
                          .map(i =>Phrase(s"Text [timescale:${tids(i)}:days] $i[exclusive]", s"Welsh: Text [timescale:${tids(i)}:days] $i[exclusive]"))

  trait Test {
    val timescales = new Timescales(new DefaultTodayProvider)
  }

  "Timescales" must {
    "Detect all referencedIds in a process flow" in new Test {
      val process: Process = rawOcelotTimescalesJson.as[Process]

      timescales.referencedNonPhraseIds(process.flow) shouldBe List("JRSProgChaseCB", "CHBFLCertabroad")
    }

    "Detect all referencedIds in a process phrases" in new Test {
      val process: Process = rawOcelotTimescalesJson.as[Process]

      timescales.referencedIds(process.phrases) shouldBe List("JRSRefCB")
    }

    "Detect all referencedIds in a list of phrases" in new Test {
      timescales.referencedIds(ps) shouldBe tids
      timescales.referencedIds(eps) shouldBe tids
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
