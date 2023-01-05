/*
 * Copyright 2023 HM Revenue & Customs
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

import core.models.ocelot.RunMode
import core.models.ocelot.errors.EGError
import core.models.ocelot.errors.{GuidanceError, RuntimeError}

case class Error(code: String, errors: List[EGError] = Nil, runMode: Option[RunMode] = None, stanzaId: Option[String] = None)

object InternalServerError extends Error("INTERNAL_SERVER_ERROR")
object DatabaseError extends Error("DATABASE_ERROR")
object DuplicateKeyError extends Error("DUPLICATE_KEY_ERROR")
object ValidationError extends Error("VALIDATION_ERROR")
object InvalidProcessError extends Error("INVALID_PROCESS")
object NotFoundError extends Error("NOT_FOUND")
object StaleDataError extends Error("STALE_DATA_ERROR")
object MalformedResponseError extends Error("MALFORMED_RESPONSE")
object BadRequestError extends Error("BAD_REQUEST")
object IncompleteDataError extends Error("INCOMPLETE_DATA_ERROR")
object AuthenticationError extends Error("AUTHENTICATION_ERROR")
object ExpectationFailedError extends Error("EXPECTATION_FAILED")
object ForbiddenError extends Error("FORBIDDEN")
object UpgradeRequiredError extends Error("UPGRADE_REQUIRED")
object IllegalPageSubmissionError extends Error("ILLEGAL_PAGE_SUBMISSION")
object SessionNotFoundError extends Error("SESSION_NOT_FOUND")
object TransactionFaultError extends Error("TRANSACTION_FAULT")

object Error {
  val UnprocessableEntity = "UNPROCESSABLE_ENTITY"
  val ExecutionError = "EXECUTION_ERROR"

  def apply(error: RuntimeError, runMode: RunMode, stanzaId: Option[String]): Error = Error(ExecutionError, List(error), Some(runMode), stanzaId)
  def apply(errors: List[GuidanceError]): Error = Error(UnprocessableEntity, errors, None, None)
  def apply(error: GuidanceError): Error = Error(List(error))
}

