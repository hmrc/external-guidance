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

import models.errors.{Errors, ValidationError}
import java.util.UUID
import models.RequestOutcome
import models.ocelot.errors._
import models.ocelot.{Page, Process}
import models.errors.{ValidationError, Error => ExternalGuidanceError, _}
import play.api.libs.json._
import play.api.Logger

package object services {

  def validateUUID(id: String): Option[UUID] = {
    val format = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    if (id.matches(format)) Some(UUID.fromString(id)) else None
  }

  def validateProcessId(id: String): Either[Errors, String] = {
    val format = "^[a-z]{3}[0-9]{5}$"
    if (id.matches(format)) Right(id) else Left(Errors(ValidationError))
  }
  
  def toProcessError(flowError: FlowError): ProcessError = flowError match {
    case UnknownStanzaType(unknown) => ProcessError(s"Unsupported stanza $unknown found at id = ??", "")
    case StanzaNotFound(id) => ProcessError(s"Missing stanza at id = $id", id)
    case PageStanzaMissing(id) => ProcessError(s"PageSanza expected but missing at id = $id", id)
    case PageUrlEmptyOrInvalid(id) => ProcessError(s"PageStanza URL empty or invalid at id = $id", id)
    case PhraseNotFound(index) => ProcessError(s"Referenced phrase at index $index on stanza id = ?? is missing", "")
    case LinkNotFound(index) => ProcessError(s"Referenced link at index $index on stanza id = ?? is missing" , "")
    case DuplicatePageUrl(id, url) => ProcessError(s"Duplicate page url $url found on stanza id = $id", id)
    case MissingWelshText(index, english) => ProcessError(s"Welsh text at index $index on stanza id = ?? is empty", "")
  }

  def toError(flowErrors: List[FlowError]): ExternalGuidanceError = ExternalGuidanceError(flowErrors.map(toProcessError).toList)

  def toGuidancePages(pageBuilder: PageBuilder, jsValue: JsValue): RequestOutcome[(Process, Seq[Page])] =
    jsValue.validate[Process].fold(err => {
      Logger(getClass).error(s"Process validation has failed with error $err")
      Left(Errors(ValidationError))
      }, process => pageBuilder.pages(process).fold(err => Left(Errors(toError(List(err)))), p => Right((process,p))))

}
