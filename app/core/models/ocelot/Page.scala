/*
 * Copyright 2022 HM Revenue & Customs
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

import core.models.ocelot.stanzas.PopulatedStanza

case class KeyedStanza(key: String, stanza: PopulatedStanza)

case class Page(id: String, url: String, keyedStanzas: Seq[KeyedStanza], next: Seq[String], endPage: Boolean = false) {
  val buttonLinked: Seq[String] = keyedStanzas.flatMap(_.stanza.buttonLinks)
  val linked: Seq[String] = keyedStanzas.flatMap(_.stanza.links).distinct
  val stanzas: Seq[PopulatedStanza] = keyedStanzas.map(_.stanza)
  val labels: Seq[String] = keyedStanzas.flatMap(_.stanza.labels).distinct
  val labelRefs: Seq[String] = keyedStanzas.flatMap(_.stanza.labelRefs).distinct
}
