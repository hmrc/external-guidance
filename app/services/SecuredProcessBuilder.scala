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

import javax.inject.{Inject, Singleton}
import config.AppConfig
import core.models.ocelot.{Phrase, Process, SecuredProcess}
import core.models.ocelot.stanzas.{Txt, Equals, Stanza, PageStanza, InputStanza, ChoiceStanza, ChoiceStanzaTest, ValueStanza, Value, Scalar}

@Singleton
class SecuredProcessBuilder @Inject()(appConfig: AppConfig) {
  import SecuredProcess._
  def stanzas(nextFreePhraseIndex: Int, passPhrase: String):Seq[(String, Stanza)] = Seq(
    (PassPhrasePageId, PageStanza(s"/${SecuredProcessStartUrl}", Seq(ResponseValueStanzaId), false)),
    (ResponseValueStanzaId, ValueStanza(List(Value(Scalar, PassPhraseResponseLabelName, "")), Seq(InputId), false)),
    (InputId, InputStanza(Txt,
                Seq(ChoiceId),
                nextFreePhraseIndex,
                None,
                PassPhraseResponseLabelName,
                None,
                false)),
    (ChoiceId, ChoiceStanza(
                Seq(Process.StartStanzaId, PassPhrasePageId),
                Seq(ChoiceStanzaTest(s"[label:${PassPhraseResponseLabelName}]", Equals, passPhrase)), false))
  )

  def secure(process: Process): Process =
    process.passPhrase.fold(process){passPhrase =>
      process.copy(flow = process.flow ++ stanzas(process.phrases.length, passPhrase),
                   phrases =process.phrases ++ Vector(Phrase(appConfig.passPhrasePagePrompt, appConfig.passPhrasePagePrompt)))
    }
}
