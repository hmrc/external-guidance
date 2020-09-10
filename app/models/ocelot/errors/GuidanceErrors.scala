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
case class UnknownCalcOperationType(id: String, typeName: String) extends FlowError
case class UnknownTestType(id: String, typeName: String) extends FlowError
case class UnknownInputType(id: String, typeName: String) extends FlowError
case class StanzaNotFound(id: String) extends FlowError
case class PageStanzaMissing(id: String) extends FlowError
case class PageUrlEmptyOrInvalid(id: String) extends FlowError
case class PhraseNotFound(id: String, index: Int) extends FlowError
case class LinkNotFound(id: String, index: Int) extends FlowError
case class DuplicatePageUrl(id: String, url: String) extends FlowError
case class MissingWelshText(id: String, index: String, english: String) extends FlowError
case class ParseError(jsPath: JsPath, errs: Seq[JsonValidationError]) extends GuidanceError
case class FlowParseError(id: String, msg: String, arg: String) extends FlowError
case class MetaParseError(id: String, msg: String, arg: String) extends MetaError
case class PhrasesParseError(id: String, msg: String, arg: String) extends PhrasesError
case class LinksParseError(id: String, msg: String, arg: String) extends LinksError

object GuidanceError {

  // Some flow errors add a hint the to JsonValidationError message to indicate that an
  // unsupported type/stanza or option has been found. Other validation errors are
  // converted to a general parse error for the containing section
  def fromJsonValidationError(err: (JsPath, Seq[JsonValidationError])): GuidanceError = {
    val (jsPath, errs) = err
    jsPath.path.lift(0).fold[GuidanceError](ParseError(jsPath, errs))( root => {
      val id = jsPath.path.lift(1).fold("Unknown")(_.toString.drop(1))
      errs.headOption.fold[GuidanceError](ParseError(jsPath, errs))( err => {
        val msg = err.message
        val arg = err.args.headOption.fold("")(_.toString)
        root.toString match {
          case "/flow" =>
            errs.headOption.fold[GuidanceError](FlowParseError(id, msg, jsPath.toString))( err =>
              err.messages.headOption.collect{
                case "CalloutType" => UnknownCalloutType(id, arg)
                case "Stanza" => UnknownStanza(id, arg)
                case "ValueType" => UnknownValueType(id, arg)
                case "TestType" => UnknownTestType(id, arg)
                case "InputType" => UnknownInputType(id, arg)
                case "CalcOperationType" => UnknownCalcOperationType(id, arg)
              }.getOrElse(FlowParseError(id, msg, jsPath.toString))
            )
          case "/meta" => MetaParseError(id, msg, arg)
          case "/phrases" => PhrasesParseError(id.dropRight(1), msg, arg)
          case "/links" => LinksParseError(id.dropRight(1), msg, arg)
        }
      })
      })
  }

  def fromJsonValidationErrors(jsErrors: Seq[(JsPath, Seq[JsonValidationError])]): List[GuidanceError] =
    jsErrors.map(fromJsonValidationError).toList
}
