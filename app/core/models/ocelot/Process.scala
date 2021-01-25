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

package core.models.ocelot

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, OWrites, __}
import core.models.ocelot.stanzas._

object SecuredProcess {
  val InputId: String = "passinput"
  val ChoiceId: String = "passchoice"
  val ResponseValueStanzaId: String = "passresponse"
  val SecuredProcessStartUrl = "authenticate"
  val PassPhrasePageId = "passphrasepage"
  val PassPhraseLabelName = "_GuidancePassPhrase"
  val PassPhraseResponseLabelName = "_GuidancePassPhraseResponse"
}

case class Process(meta: Meta, flow: Map[String, Stanza], phrases: Vector[Phrase], links: Vector[Link]) {
  import SecuredProcess._
  import Process._
  lazy val phraseOption: Int => Option[Phrase] = phrases.lift
  lazy val linkOption: Int => Option[Link] = links.lift
  lazy val title: Phrase = meta.titlePhrase.fold(Phrase(meta.title, meta.title))(idx => phraseOption(idx).getOrElse(Phrase(meta.title, meta.title)))
  lazy val startUrl: Option[String] = flow.get(StartStanzaId).collect{case ps: PageStanza => ps.url}
  lazy val startPageId: String = flow.get(PassPhrasePageId).fold(StartStanzaId)(_ => PassPhrasePageId)
  lazy val passPhraseResponse: Option[String] = flow.get(ResponseValueStanzaId).fold[Option[String]](None){
        case v: ValueStanza => v.values.find(_.label == PassPhraseResponseLabelName).map(_.value)
        case _ => None
      }
  lazy val passPhrase: Option[String] = flow.values
      .collect{case vs: ValueStanza => vs.values}.flatten
      .collectFirst{case Value(_, PassPhraseLabelName, phrase) => phrase}
  // true if no passphrase or passphrase page contains a value stanza with the passphrase response included and correct
  lazy val secure: Boolean = passPhrase.fold(true)(phrase => passPhraseResponse.fold(false)(_ == phrase))
}

object Process {
  val StartStanzaId = "start"

  implicit val reads: Reads[Process] = (
    (__ \ "meta").read[Meta] and
      (__ \ "flow").read[Map[String, Stanza]] and
      (__ \ "phrases").read[Vector[Phrase]] and
      (__ \ "links").read[Vector[Link]]
  )(Process.apply _)

  implicit val writes: OWrites[Process] = (
    (__ \ "meta").write[Meta] and
      (__ \ "flow").write[Map[String, Stanza]] and
      (__ \ "phrases").write[Vector[Phrase]] and
      (__ \ "links").write[Vector[Link]]
  )(unlift(Process.unapply))
}
