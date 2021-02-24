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

import core.models.ocelot.stanzas.Stanza
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class KeyedStanza(key: String, stanza: Stanza)

object KeyedStanza {
  implicit val reads: Reads[KeyedStanza] = (
    (__ \ "key").read[String] and
      (__ \ "stanza").read[Stanza]
  )(KeyedStanza.apply _)

  implicit val writes: Writes[KeyedStanza] = (
    (__ \ "key").write[String] and
      (__ \ "stanza").write[Stanza]
  )(unlift(KeyedStanza.unapply))
}

case class Page(id: String, url: String, keyedStanzas: Seq[KeyedStanza], next: Seq[String]) {
  val stanzas: Seq[Stanza] = keyedStanzas.map(_.stanza)
  val linked: Seq[String] = keyedStanzas.flatMap(_.stanza.links)
  val labels: Seq[Label] = keyedStanzas.flatMap(_.stanza.labels)
  val labelRefs: Seq[String] = keyedStanzas.flatMap(_.stanza.labelRefs).distinct
}