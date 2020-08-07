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

package controllers

import controllers.actions.IdentifierAction
import javax.inject.{Inject, Singleton}
import models.errors.{BadRequestError, ValidationError, InternalServiceError, NotFoundError, Error}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.ApprovalService
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import utils.Constants._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.errors._

@Singleton
class ApprovalController @Inject() (
                                     identify: IdentifierAction,
                                     approvalService: ApprovalService,
                                     cc: ControllerComponents) extends BackendController(cc) {

  def saveFor2iReview: Action[JsValue] = Action.async(parse.json) { implicit request =>
    saveProcess(request.body.as[JsObject], ReviewType2i)
  }

  def saveForFactCheck: Action[JsValue] = Action.async(parse.json) { implicit request =>
    saveProcess(request.body.as[JsObject], ReviewTypeFactCheck)
  }

  def saveProcess(process: JsObject, reviewType: String): Future[Result] = {
    approvalService.save(process, reviewType, StatusSubmitted).map {
      case Right(id) => Created(Json.obj("id" -> id))
      case Left(err @ Error(Error.UnprocessableEntity, _, _)) => UnprocessableEntity(Json.toJson(err))
      case Left(ValidationError) => BadRequest(Json.toJson(BadRequestError))
      case Left(BadRequestError) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

  def get(id: String): Action[AnyContent] = Action.async { _ =>
    approvalService.getById(id).map {
      case Right(approvalProcess) => Ok(approvalProcess)
      case Left(NotFoundError) => NotFound(Json.toJson(NotFoundError))
      case Left(BadRequestError) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

  def approvalSummaryList: Action[AnyContent] = identify.async { implicit request =>
    approvalService.approvalSummaryList(request.roles).map {
      case Right(list) => Ok(list)
      case _ => InternalServerError(Json.toJson(InternalServiceError))
    }
  }
}
