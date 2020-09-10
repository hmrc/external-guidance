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

package models.ocelot

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, Writes, __}
import models.ocelot.stanzas._

case class Process(meta: Meta, flow: Map[String, Stanza], phrases: Vector[Phrase], links: Vector[Link]) {

  lazy val phraseOption: Int => Option[Phrase] = phrases.lift
  lazy val linkOption: Int => Option[Link] = links.lift
  lazy val title: Phrase = meta.titlePhrase.fold(Phrase(meta.title, meta.title)){
    titleIndex => phraseOption(titleIndex).getOrElse(Phrase(meta.title, meta.title))
  }
  lazy val startUrl: Option[String] = flow.get(Process.StartStanzaId).collect{case ps: PageStanza => ps.url}
}

object Process {
  val StartStanzaId = "start"

  implicit val reads: Reads[Process] = (
    (__ \ "meta").read[Meta] and
      (__ \ "flow").read[Map[String, Stanza]] and
      (__ \ "phrases").read[Vector[Phrase]] and
      (__ \ "links").read[Vector[Link]]
  )(Process.apply _)

  implicit val writes: Writes[Process] = (
    (__ \ "meta").write[Meta] and
      (__ \ "flow").write[Map[String, Stanza]] and
      (__ \ "phrases").write[Vector[Phrase]] and
      (__ \ "links").write[Vector[Link]]
  )(unlift(Process.unapply))
}
