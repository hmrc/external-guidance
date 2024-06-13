/*
 * Copyright 2024 HM Revenue & Customs
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
import services.{PublishedService, LabelledDataService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PublishedController @Inject() (publishedService: PublishedService,
                                     labelledDataService: LabelledDataService,
                                     cc: ControllerComponents,
                                     identify: AllRolesAction)(implicit ec: ExecutionContext) extends BackendController(cc) {
  import Json._
  import models.PublishedProcess.Implicits._

  def get(id: String): Action[AnyContent] = Action.async {
    publishedService.getById(id).map {
      case Right(process) => Ok(toJson(process))
      case Left(BadRequestError) => BadRequest(toJson(OcelotError(BadRequestError)))
      case Left(NotFoundError) => NotFound(toJson(OcelotError(NotFoundError)))
      case Left(_) => InternalServerError(toJson(OcelotError(ServerError)))
    }
  }

  def getByProcessCode(processCode: String): Action[AnyContent] = Action.async {
    publishedService.getByProcessCode(processCode).flatMap {
      case Right(pp) => labelledDataService.updateProcessLabelledDataTablesAndVersions(pp.process).map {
        case Right(result) => Ok(result)
        case Left(_) => InternalServerError(toJson(OcelotError(ServerError)))
      }
      case Left(BadRequestError) => Future.successful(BadRequest(toJson(OcelotError(BadRequestError))))
      case Left(NotFoundError) => Future.successful(NotFound(toJson(OcelotError(NotFoundError))))
      case Left(_) => Future.successful(InternalServerError(toJson(OcelotError(ServerError))))
    }
  }

  def archive(id: String): Action[AnyContent] = identify.async { implicit request =>
    publishedService.archive(id, request.credId) map {
      case Right(_) => Ok
      case Left(BadRequestError) => BadRequest(toJson(OcelotError(BadRequestError)))
      case _ => InternalServerError(toJson(OcelotError(ServerError)))
    }
  }

  def list: Action[AnyContent] = Action.async { _ =>
    publishedService.list map {
      case Right(summaries) => Ok(summaries)
      case Left(BadRequestError) => BadRequest(toJson(OcelotError(BadRequestError)))
      case Left(_) => InternalServerError(toJson(OcelotError(ServerError)))
    }
  }

}
