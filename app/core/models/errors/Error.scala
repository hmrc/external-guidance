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

package core.models.errors

import play.api.libs.json.{Json, OFormat}

case class ProcessError(message: String, stanza: String)

object ProcessError {
  implicit val formats: OFormat[ProcessError] = Json.format[ProcessError]
}

case class Error(code: String, message: Option[String], messages: Option[List[ProcessError]])

object Error {
  val UnprocessableEntity = "UNPROCESSABLE_ENTITY"
  def apply(code: String, msg: String): Error = Error(code, Some(msg), None)
  def apply(code: String, processErrors: List[ProcessError]): Error = Error(code, None, Some(processErrors))
  def apply(processErrors: List[ProcessError]): Error = Error(UnprocessableEntity, None, Some(processErrors))
  implicit val formats: OFormat[Error] = Json.format[Error]
}

object InternalServerError extends Error("INTERNAL_SERVER_ERROR", Some("An error occurred whilst processing your request."), None)
object DatabaseError extends Error("DATABASE_ERROR", Some("An error occurred whilst accessing the database."), None)
object DuplicateKeyError extends Error("DUPLICATE_KEY_ERROR", Some("An attempt was made to insert a duplicate key in the database."), None)
object ValidationError extends Error("VALIDATION_ERROR", Some("Input data failed validation test."), None)
object InvalidProcessError extends Error("BAD_REQUEST", Some("The input process is invalid"), None)
object NotFoundError extends Error("NOT_FOUND", Some("The resource requested could not be found."), None)
object StaleDataError extends Error("STALE_DATA_ERROR", Some("The resource requested has been changed elsewhere."), None)
object MalformedResponseError extends Error("BAD_REQUEST", Some("The response received could not be parsed"), None)
object BadRequestError extends Error("BAD_REQUEST", Some("The request is invalid."), None)
object IncompleteDataError extends Error("INCOMPLETE_DATA_ERROR", Some("Data is not in the required state for the requested action."), None)
object AuthenticationError extends Error("AUTHENTICATION_ERROR", Some("Not authenticated"), None)
object ExpectationFailedError extends Error("EXPECTATION_FAILED", Some("Session missing when expected"), None)
object ForbiddenError extends Error("FORBIDDEN", Some("Access to the requested resource is not allowed in this context"), None)
object UpgradeRequiredError extends Error("UPGRADE_REQUIRED", Some("Process references feature currently not supported"), None)