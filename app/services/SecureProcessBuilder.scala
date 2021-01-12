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

import javax.inject.Singleton
import models.ocelot.{Phrase, Process}
import models.ocelot.stanzas.{Txt, Equals, Stanza, PageStanza, InputStanza, ChoiceStanza, ChoiceStanzaTest}

@Singleton
class SecureProcessBuilder() {
  val InputId = "passinput"
  val ChoiceId = "passchoice"
  def stanzas(initialLabelIndex: Int):Seq[(String, Stanza)] = Seq(
    (Process.PassPhrasePageId, PageStanza("/guard", Seq(InputId), false)),
    (InputId, InputStanza(Txt, Seq(ChoiceId), initialLabelIndex, None, Process.PassPhraseResponseLabelName, None, false)),
    (ChoiceId, ChoiceStanza(
                Seq(Process.StartStanzaId, Process.PassPhrasePageId),
                Seq(ChoiceStanzaTest(s"[label:${Process.PassPhraseLabelName}]", Equals, s"[label:${Process.PassPhraseResponseLabelName}]")), false))
  )

  def secure(process: Process): Process =
    process.copy(flow = process.flow ++ stanzas(process.phrases.length), phrases =process.phrases ++ Vector(Phrase("Enter passphrase", "Rhowch gyfrinair")))
}

