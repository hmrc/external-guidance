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

package controllers

import controllers.actions.AllRolesAction
import models.errors.OcelotError
import javax.inject.{Inject, Singleton}
import core.models.errors.{BadRequestError, NotFoundError, InternalServerError => ServerError}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.ArchiveService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._

@Singleton
class ArchivedController @Inject() (archivedService: ArchiveService,
                                    cc: ControllerComponents,
                                    identify: AllRolesAction) extends BackendController(cc) {
  import Json._

  def get(id: String): Action[AnyContent] = Action.async {
    archivedService.getById(id).map {
      case Right(archived) => Ok(archived.process)
      case Left(BadRequestError) => BadRequest(toJson(OcelotError(BadRequestError)))
      case Left(NotFoundError) => NotFound(toJson(OcelotError(NotFoundError)))
      case Left(_) => InternalServerError(toJson(OcelotError(ServerError)))
    }
  }

  def list: Action[AnyContent] = identify.async { _ =>
    archivedService.list map {
      case Right(summaries) => Ok(summaries)
      case Left(BadRequestError) => BadRequest(toJson(OcelotError(BadRequestError)))
      case Left(_) => InternalServerError(toJson(OcelotError(ServerError)))
    }
  }

}