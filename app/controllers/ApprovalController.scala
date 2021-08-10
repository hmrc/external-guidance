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

package controllers

import controllers.actions.AllRolesAction
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.ApprovalService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import models.Constants._
import core.models.errors.{BadRequestError, DuplicateKeyError, Error, NotFoundError, ValidationError, InternalServerError => ServerError}
import play.api.libs.json.Json.toJson

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApprovalController @Inject() (allRolesAction: AllRolesAction, approvalService: ApprovalService, cc: ControllerComponents) extends BackendController(cc) {

  val logger: Logger = Logger(getClass)

  def saveFor2iReview: Action[JsValue] = Action.async(parse.json) { implicit request =>
    saveProcess(request.body.as[JsObject], ReviewType2i)
  }

  def saveForFactCheck: Action[JsValue] = Action.async(parse.json) { implicit request =>
    saveProcess(request.body.as[JsObject], ReviewTypeFactCheck)
  }

  def saveProcess(process: JsObject, reviewType: String): Future[Result] =
    approvalService.save(process, reviewType, StatusSubmitted).map {
      case Right(id) =>
        logger.info(s"Saved process for $reviewType with id $id")
        Created(Json.obj("id" -> id))
      case Left(err @ Error(Error.UnprocessableEntity, _, Some(details))) =>
        logger.error(s"Failed to save process for approval due to process errors $details")
        UnprocessableEntity(toJson(err))
      case Left(DuplicateKeyError) =>
        logger.error(s"Failed to save process for approval due to duplicate processCode")
        UnprocessableEntity(toJson(Error(Error.UnprocessableEntity, "Duplicate ProcessCode - the process has the same processCode as an existing process")))
      case Left(ValidationError) =>
        logger.error(s"Failed to save process for approval due to validation errors")
        BadRequest(toJson(BadRequestError))
      case Left(BadRequestError) => BadRequest(toJson(BadRequestError))
      case Left(_) => InternalServerError(toJson(ServerError))
    }

  def get(id: String): Action[AnyContent] = Action.async { _ =>
    approvalService.getById(id).map {
      case Right(approvalProcess) => Ok(approvalProcess)
      case Left(NotFoundError) => NotFound(toJson(NotFoundError))
      case Left(BadRequestError) => BadRequest(toJson(BadRequestError))
      case Left(_) => InternalServerError(toJson(ServerError))
    }
  }

  def getByProcessCode(processCode: String): Action[AnyContent] = Action.async { _ =>

    approvalService.getByProcessCode(processCode).map {
      case Right(approvalProcess) => Ok(approvalProcess)
      case Left(NotFoundError) => NotFound(toJson(NotFoundError))
      case Left(BadRequestError) => BadRequest(toJson(BadRequestError))
      case Left(_) => InternalServerError(toJson(ServerError))
    }
  }

  def approvalSummaryList: Action[AnyContent] = allRolesAction.async { implicit request =>
    approvalService.approvalSummaryList(request.roles).map {
      case Right(list) => Ok(list)
      case _ => InternalServerError(toJson(ServerError))
    }
  }
}
