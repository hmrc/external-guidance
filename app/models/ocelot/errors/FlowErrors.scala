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

package models.ocelot.errors

import models.ocelot.stanzas.Stanza

trait FlowError {
  val msgKey: String
}

case class UnknownStanzaType(unknown: Stanza) extends FlowError {val msgKey: String = "error.unsupportedStanza"}
case class StanzaNotFound(id: String) extends FlowError {val msgKey: String = "error.stanzaMissing"}
case class PageStanzaMissing(id: String) extends FlowError {val msgKey: String = "error.pageStanzaMissing"}
case class PageUrlEmptyOrInvalid(id: String) extends FlowError {val msgKey: String = "error.invalidUrl"}
case class PhraseNotFound(index: Int) extends FlowError {val msgKey: String = "error.phraseNotFound"}
case class LinkNotFound(index: Int) extends FlowError {val msgKey: String = "error.linkNotFound"}
case class DuplicatePageUrl(id: String, url: String) extends FlowError {val msgKey: String = "error.duplicateUrl"}
case class MissingWelshText(index: String, english: String) extends FlowError {val msgKey: String = "error.welshMissing"}
