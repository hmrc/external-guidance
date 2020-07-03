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

package utils



import models._
import models.errors.{BadRequestError, Errors}
import models.ocelot.Process
import play.api.Logger
import play.api.libs.json.{JsError, JsObject, JsSuccess}

object ProcessUtils {

  val logger = Logger(getClass)

  def validateProcess(jsonProcess: JsObject): RequestOutcome[Process] =
    jsonProcess.validate[Process] match {
      case JsSuccess(process, _) =>
        Right(process)
      case JsError(errors) =>
        logger.error(s"Parsing process failed with the following error(s): $errors")
        Left(Errors(BadRequestError))
    }

  def createApprovalProcess(id: String, title: String, status: String, jsonProcess: JsObject): ApprovalProcess =
    ApprovalProcess(id, ApprovalProcessMeta(id, title, status), jsonProcess)

}
