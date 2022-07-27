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

package models.errors

import play.api.libs.json.{Json, OFormat}
import core.models.errors.Error
import core.models.ocelot.errors._

case class ErrorReport(message: String, stanza: String)
case class OcelotError(code: String, messages: List[ErrorReport] = Nil)

object OcelotError {
  def apply(error: Error): OcelotError = OcelotError(error.code, fromGuidanceErrors(error.errors.collect{case e: GuidanceError => e}))
  def apply(errorReports: List[ErrorReport]): OcelotError = OcelotError(Error.UnprocessableEntity, errorReports)
  def apply(errorReport: ErrorReport): OcelotError = OcelotError(List(errorReport))

  implicit val erformat: OFormat[ErrorReport] = Json.format[ErrorReport]
  implicit val formats: OFormat[OcelotError] = Json.format[OcelotError]
}