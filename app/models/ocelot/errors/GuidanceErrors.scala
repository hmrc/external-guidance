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

import play.api.libs.json._

sealed trait GuidanceError
sealed trait MetaError extends GuidanceError
sealed trait FlowError extends GuidanceError
sealed trait PhrasesError extends GuidanceError
sealed trait LinksError extends GuidanceError

case class UnknownStanza(id: String, typeName: String) extends FlowError
case class UnknownCalloutType(id: String, typeName: String) extends FlowError
case class UnknownValueType(id: String, typeName: String) extends FlowError
case class StanzaNotFound(id: String) extends FlowError
case class PageStanzaMissing(id: String) extends FlowError
case class PageUrlEmptyOrInvalid(id: String) extends FlowError
case class PhraseNotFound(id: String, index: Int) extends FlowError
case class LinkNotFound(id: String, index: Int) extends FlowError
case class DuplicatePageUrl(id: String, url: String) extends FlowError
case class MissingWelshText(id: String, index: String, english: String) extends FlowError
case class ParseError(jsPath: JsPath, errs: Seq[JsonValidationError]) extends GuidanceError
case class FlowParseError(msg: String) extends FlowError
case class MetaParseError(msg: String) extends MetaError
case class PhrasesParseError(msg: String) extends PhrasesError
case class LinksParseError(msg: String) extends LinksError

object GuidanceError {

  def fromJsonValidationError(err: (JsPath, Seq[JsonValidationError])): GuidanceError = {
    val (jsPath, errs) = err
    jsPath.path.lift(1).fold(ParseError(jsPath, errs): GuidanceError)(pth => {
      errs.head.messages.head match {
        case "CalloutType" => UnknownCalloutType(pth.toJsonString.drop(1), errs.head.args(0).toString)
        case "Stanza" => UnknownStanza(pth.toJsonString.drop(1), errs.head.args(0).toString)
        case "ValueType" => UnknownValueType(pth.toJsonString.drop(1), errs.head.args(0).toString)
        case _ => ParseError(jsPath, errs)
      }
    })
  }

  def fromJsonValidationErrors(jsErrors: Seq[(JsPath, Seq[JsonValidationError])]): List[GuidanceError] =  
    jsErrors.map(fromJsonValidationError).toList
}
