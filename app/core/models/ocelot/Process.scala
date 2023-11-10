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

package core.models.ocelot

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, OWrites, __}
import core.models.ocelot.stanzas._

object SecuredProcess {
  val InputId: String = "passinput"
  val ChoiceId: String = "passchoice"
  val SecuredProcessStartUrl = "authenticate"
  val PassPhrasePageId = "passphrasepage"
  val PassPhraseLabelName = "_GuidancePassPhrase"
  val PassPhraseResponseLabelName = "_GuidancePassPhraseResponse"
  val EncryptedPassphraseResponseLabelName = "_GuidancePassPhraseResponse_encrypted"
}

case class Process(meta: Meta, flow: Map[String, Stanza], phrases: Vector[Phrase], links: Vector[Link], timescales: Map[String, Int] = Map()) {
  import SecuredProcess._
  import Process._
  lazy val phraseOption: Int => Option[Phrase] = phrases.lift
  lazy val linkOption: Int => Option[Link] = links.lift
  lazy val title: Phrase = meta.titlePhrase.fold(Phrase(meta.title, meta.title))(idx => phraseOption(idx).getOrElse(Phrase(meta.title, meta.title)))
  lazy val startUrl: Option[String] = flow.get(StartStanzaId).collect{case ps: PageStanza => ps.url}
  lazy val startPageId: String = flow.get(PassPhrasePageId).fold(StartStanzaId)(_ => PassPhrasePageId)
  lazy val passPhrase: Option[String] = meta.passPhrase
  lazy val encryptedPassPhrase: Option[String] = meta.encryptedPassPhrase
  lazy val valueStanzaPassPhrase: Option[String] = flow.values
      .collect{case vs: ValueStanza => vs.values}.flatten
      .collectFirst{case Value(_, PassPhraseLabelName, value) => value}
  lazy val passphraseValueStanza: Option[(String, ValueStanza)] = flow.collectFirst{
    case (k, v: ValueStanza) if v.values.exists(_.label == PassPhraseLabelName) => (k, v)
  }  
  lazy val betaPhaseBanner: Boolean = flow.values
      .collect{case vs: ValueStanza => vs.values}.flatten
      .collectFirst{case Value(_, PhaseBannerPhase, value) => value}
      .exists(_.toUpperCase().equals("YES"))
}

object Process {
  val PhaseBannerPhase = "_BetaPhaseBanner"
  val StartStanzaId = "start"
  val EndStanzaId = "end"
  val SessionTimeoutUrl = "session-timeout"
  val EndSessionUrl = "end-session"
  val SessionRestartUrl = "session-restart"
  val ReservedUrls: List[String] = List(s"/$EndSessionUrl", s"/$SessionTimeoutUrl", s"/$SessionRestartUrl", s"/${SecuredProcess.SecuredProcessStartUrl}")

  def buildProcess(m: Meta, f: Map[String, Stanza], p: Vector[Phrase], l: Vector[Link], t: Option[Map[String, Int]]): Process =
    Process(m, f, p, l, t.getOrElse(Map()))

  implicit val reads: Reads[Process] = (
    (__ \ "meta").read[Meta] and
      (__ \ "flow").read[Map[String, Stanza]] and
      (__ \ "phrases").read[Vector[Phrase]] and
      (__ \ "links").read[Vector[Link]] and
      (__ \ "timescales").readNullable[Map[String, Int]]
  )(buildProcess _)

  implicit val writes: OWrites[Process] = (
    (__ \ "meta").write[Meta] and
      (__ \ "flow").write[Map[String, Stanza]] and
      (__ \ "phrases").write[Vector[Phrase]] and
      (__ \ "links").write[Vector[Link]] and
      (__ \ "timescales").write[Map[String, Int]]
  )(unlift(Process.unapply))
}
