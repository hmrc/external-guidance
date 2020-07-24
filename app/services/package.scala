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

import models.RequestOutcome
import models.errors.{Errors, BadRequestError, ValidationError}
import models.ocelot.Process
import play.api.Logger
import play.api.libs.json.{JsError, JsObject, JsSuccess}
import java.util.UUID

package object services {

  val logger = Logger(getClass)

  def validateUUID(id: String): Option[UUID] = {
    val format = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    if (id.matches(format)) Some(UUID.fromString(id)) else None
  }

  def validateProcessId(id: String): Either[Errors, String] = {
    val format = "^[a-z]{3}[0-9]{5}$"
    if (id.matches(format)) Right(id) else Left(Errors(ValidationError))
  }

  def validateProcess(jsonProcess: JsObject): RequestOutcome[Process] =
    jsonProcess.validate[Process] match {
      case JsSuccess(process, _) => Right(process)
      case JsError(err) =>
        logger.error(s"Parsing process failed with the following error(s): $err")
        Left(Errors(BadRequestError))
    }
}
