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

import play.api.libs.json._

case class Error(code: String, message: String)

object Error {
  implicit val writes: Writes[Error] = Json.writes[Error]
}

object InternalServiceError
    extends Error(
      "INTERNAL_SERVER_ERROR",
      "An error occurred whilst processing your request."
    )

object DatabaseError
    extends Error(
      "DATABASE_ERROR",
      "An error occurred whilst accessing the database."
    )

object NotFoundError
    extends Error(
      "NOT_FOUND_ERROR",
      "The resource requested could not be found."
    )

object StaleDataError
    extends Error(
      "STALE_DATA_ERROR",
      "The resource requested has been changed."
    )

object BadRequestError
    extends Error(
      "BAD_REQUEST_ERROR",
      "The request is invalid."
    )

object ValidationError
    extends Error(
      "VALIDATION_ERROR",
      "Input data failed validation test."
    )

object IncompleteDataError
  extends Error(
    "INCOMPLETE_DATA_ERROR",
    "Data is not in the required state for the requested action."
  )
