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

package models.errors

import play.api.libs.json.{Json, OFormat}

case class Error(code: String, message: Option[String], messages: Option[List[ErrorDetail]] = None)

object Error {
  val UnprocessableEntity = "UNPROCESSABLE_ENTITY"
  def apply(code: String, msg: String): Error = Error(code, Some(msg),  None)
  def apply(code: String, details: List[ErrorDetail]): Error = Error(code, None, Some(details))
  def apply(details: List[ErrorDetail]): Error = Error(UnprocessableEntity, None, Some(details)) 
  implicit val formats: OFormat[Error] = Json.format[Error]
}

object InternalServiceError extends Error("INTERNAL_SERVER_ERROR",  Some("An error occurred whilst processing your request."))
object DatabaseError extends Error("DATABASE_ERROR",  Some("An error occurred whilst accessing the database."))
object ValidationError extends Error("VALIDATION_ERROR",  Some("Input data failed validation test."))
object InvalidProcessError extends Error("BAD_REQUEST",  Some("The input process is invalid"))
object InternalServerError extends Error("INTERNAL_SERVER_ERROR",  Some("An unexpected error has occurred"))
object NotFoundError extends Error("NOT_FOUND_ERROR",  Some("The resource requested could not be found."))
object StaleDataError extends Error("STALE_DATA_ERROR",  Some("The resource requested has been changed elsewhere."))
object MalformedResponseError extends Error("BAD_REQUEST",  Some("The response received could not be parsed"))
object BadRequestError extends Error("BAD_REQUEST_ERROR",  Some("The request is invalid."))
object IncompleteDataError extends Error("INCOMPLETE_DATA_ERROR", Some("Data is not in the required state for the requested action."))
