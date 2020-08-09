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

import models.errors._
import models.ocelot.stanzas.Stanza

trait FlowError

case class UnknownStanzaType(unknown: Stanza) extends FlowError
case class StanzaNotFound(id: String) extends FlowError
case class PageStanzaMissing(id: String) extends FlowError
case class PageUrlEmptyOrInvalid(id: String) extends FlowError
case class PhraseNotFound(index: Int) extends FlowError
case class LinkNotFound(index: Int) extends FlowError
case class DuplicatePageUrl(id: String, url: String) extends FlowError
case class MissingWelshText(index: String, english: String) extends FlowError

object FlowError {
  implicit val toErrorDetails: FlowError => ErrorDetail = {
    case e: UnknownStanzaType => ErrorDetail(s"Unsupported stanza ${e.unknown} found at id = ??", "")
    case e: StanzaNotFound => ErrorDetail(s"Missing stanza at id = ${e.id}", e.id)
    case e: PageStanzaMissing => ErrorDetail(s"PageSanza expected but missing at id = ${e.id}", e.id)
    case e: PageUrlEmptyOrInvalid => ErrorDetail(s"PageStanza URL empty or invalid at id = ${e.id}", e.id)
    case e: PhraseNotFound => ErrorDetail(s"Referenced phrase at index ${e.index} on stanza id = ?? is missing", "")
    case e: LinkNotFound => ErrorDetail(s"Referenced link at index ${e.index} on stanza id = ?? is missing" , "")
    case e: DuplicatePageUrl => ErrorDetail(s"Duplicate page url ${e.url} found on stanza id = ${e.id}", e.id)
    case e: MissingWelshText => ErrorDetail(s"Welsh text at index ${e.index} on stanza id = ?? is empty", "")
  }

  implicit def f(l: List[FlowError]): List[ErrorDetail] = l.map(f => f)
}