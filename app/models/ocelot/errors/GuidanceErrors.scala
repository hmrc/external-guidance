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
import play.api.libs.json._

sealed trait GuidanceError
sealed trait MetaError extends GuidanceError
sealed trait FlowError extends GuidanceError
sealed trait PhrasesError extends GuidanceError
sealed trait LinksError extends GuidanceError

case class UnknownStanzaType(unknown: Stanza) extends FlowError
case class UnknownStanza(id: String, msg: String) extends FlowError
case class UnknownCalloutType(id: String, msg: String) extends FlowError
case class UnknownValueType(id: String, msg: String) extends FlowError
case class StanzaNotFound(id: String) extends FlowError
case class PageStanzaMissing(id: String) extends FlowError
case class PageUrlEmptyOrInvalid(id: String) extends FlowError
case class PhraseNotFound(index: Int) extends FlowError
case class LinkNotFound(index: Int) extends FlowError
case class DuplicatePageUrl(id: String, url: String) extends FlowError
case class MissingWelshText(index: String, english: String) extends FlowError
case class PhraseError(id: String, msg: String) extends PhrasesError
case class MetaElementError(id: String, msg: String) extends MetaError
case class LinkError(id: String, msg: String) extends LinksError
case class UnknownParseError(msg: String) extends GuidanceError
case class UnknownFlowError(msg: String) extends FlowError
case class UnknownMetaError(msg: String) extends MetaError
case class UnknownPhrasesError(msg: String) extends PhrasesError
case class UnknownLinksError(msg: String) extends LinksError

object UnknownParseError {
  def apply(jsPath: JsPath, errs: Seq[JsonValidationError]): UnknownParseError =
    UnknownParseError(s"${jsPath.toString} : ${errs.toString}")
  
}

object GuidanceError {

  implicit def toGuidanceError(err: (JsPath, Seq[JsonValidationError])): GuidanceError = {
    val (jsPath, errs) = err
    println(jsPath.toString)
    jsPath.path.lift(1).fold(UnknownParseError(jsPath, errs): GuidanceError)(pth => {
      println(s"${pth.toString}")
      val stanzaId = pth.toJsonString.drop(1)
      errs.head.messages.head match {
        case "CalloutType" => UnknownCalloutType(stanzaId, s"Unknown CalloutStanza type found at stanza id $stanzaId")
        case "Stanza" => UnknownStanza(stanzaId, s"Unknown stanza type found at stanza id $stanzaId")
        case "ValueType" => UnknownValueType(stanzaId, s"Unknown ValueStanza type found at stanza id $stanzaId")
        case _ => UnknownParseError(jsPath, errs)
      }
    })
  }  

  implicit val toProcessError: GuidanceError => ProcessError = {
    case e: UnknownStanzaType => ProcessError(s"Unsupported stanza ${e.unknown} found at id = ??", "")
    case e: StanzaNotFound => ProcessError(s"Missing stanza at id = ${e.id}", e.id)
    case e: PageStanzaMissing => ProcessError(s"PageSanza expected but missing at id = ${e.id}", e.id)
    case e: PageUrlEmptyOrInvalid => ProcessError(s"PageStanza URL empty or invalid at id = ${e.id}", e.id)
    case e: PhraseNotFound => ProcessError(s"Referenced phrase at index ${e.index} on stanza id = ?? is missing", "")
    case e: LinkNotFound => ProcessError(s"Referenced link at index ${e.index} on stanza id = ?? is missing", "")
    case e: DuplicatePageUrl => ProcessError(s"Duplicate page url ${e.url} found on stanza id = ${e.id}", e.id)
    case e: MissingWelshText => ProcessError(s"Welsh text at index ${e.index} on stanza id = ?? is empty", "")
    case e: UnknownStanza => ProcessError(e.msg, e.id)
    case e: UnknownCalloutType => ProcessError(e.msg, e.id)
    case e: UnknownValueType => ProcessError(e.msg, e.id)
    case e: PhraseError => ProcessError(e.msg, e.id)
    case e: MetaElementError => ProcessError(e.msg, e.id)
    case e: LinkError => ProcessError(e.msg, e.id)  
    case e: UnknownParseError => ProcessError(e.msg, "")
    case e: UnknownFlowError => ProcessError(e.msg, "")
    case e: UnknownMetaError => ProcessError(e.msg, "")
    case e: UnknownPhrasesError => ProcessError(e.msg, "")
    case e: UnknownLinksError => ProcessError(e.msg, "")

  }

  implicit def f(l: List[GuidanceError]): List[ProcessError] = l.map(f => f)

}
