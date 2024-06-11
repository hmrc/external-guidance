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

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.{ApprovalReviewService, LabelledDataService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import models.Constants._
import core.models.errors.{BadRequestError, DuplicateKeyError, Error}
import core.models.errors.{NotFoundError, ValidationError, InternalServerError => ServerError}
import models.errors.{DuplicateProcessCodeError, OcelotError}
import play.api.libs.json.Json.toJson

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApprovalController @Inject() (allRolesAction: AllRolesAction,
                                    approvalService: ApprovalReviewService,
                                    labelledDataService: LabelledDataService,
                                    cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(getClass)

  def saveFor2iReview: Action[JsValue] = Action.async(parse.json) { implicit request =>
    saveProcess(request.body, ReviewType2i)
  }

  def saveForFactCheck: Action[JsValue] = Action.async(parse.json) { implicit request =>
    saveProcess(request.body, ReviewTypeFactCheck)
  }

  def saveProcess(jsProcess: JsValue, reviewType: String): Future[Result] =
    jsProcess.validate[JsObject].fold(errs => {
      logger.error(s"Unable to parse incoming json as a JsObject, Errors: $errs")
      Future.successful(BadRequest(toJson(OcelotError(BadRequestError))))
    }, process =>
      approvalService.save(process, reviewType, StatusSubmitted).map {
        case Right(id) =>
          logger.info(s"Saved process for $reviewType with id $id")
          Created(Json.obj("id" -> id))
        case Left(err @ Error(Error.UnprocessableEntity, details, _, _)) =>
          logger.error(s"Failed to save process for approval due to process errors $details")
          UnprocessableEntity(toJson(OcelotError(err)))
        case Left(DuplicateKeyError) =>
          logger.error(s"Failed to save process for approval due to duplicate processCode")
          UnprocessableEntity(toJson(OcelotError(DuplicateProcessCodeError)))
        case Left(ValidationError) =>
          logger.error(s"Failed to save process for approval due to validation errors")
          BadRequest(toJson[OcelotError](OcelotError(BadRequestError)))
        case Left(BadRequestError) => BadRequest(toJson(OcelotError(BadRequestError)))
        case Left(err) =>
          logger.error(s"Unexpected error $err, returning InternalServerError")
          InternalServerError(toJson(OcelotError(ServerError)))
      }
    )

  def get(id: String): Action[AnyContent] = Action.async { _ =>
    approvalService.getById(id).flatMap {
      case Right(approvalProcess) =>
        labelledDataService.updateProcessLabelledDataTablesAndVersions(approvalProcess).map{
          case Right(result) => Ok(result)
          case Left(_) => InternalServerError(toJson(OcelotError(ServerError)))
        }
      case Left(NotFoundError) => Future.successful(NotFound(toJson(OcelotError(NotFoundError))))
      case Left(BadRequestError) => Future.successful(BadRequest(toJson(OcelotError(BadRequestError))))
      case Left(_) => Future.successful(InternalServerError(toJson(OcelotError(ServerError))))
    }
  }

  def getByProcessCode(processCode: String): Action[AnyContent] = Action.async { _ =>
    approvalService.getByProcessCode(processCode).flatMap {
      case Right(approvalProcess) =>
        labelledDataService.updateProcessLabelledDataTablesAndVersions(approvalProcess).map{
          case Right(result) => Ok(result)
          case Left(_) => InternalServerError(toJson(OcelotError(ServerError)))
        }
      case Left(NotFoundError) => Future.successful(NotFound(toJson(OcelotError(NotFoundError))))
      case Left(BadRequestError) => Future.successful(BadRequest(toJson(OcelotError(BadRequestError))))
      case Left(_) => Future.successful(InternalServerError(toJson(OcelotError(ServerError))))
    }
  }

  def approvalSummaryList: Action[AnyContent] = allRolesAction.async { implicit request =>
    approvalService.approvalSummaryList(request.roles).map {
      case Right(list) => Ok(list)
      case _ => InternalServerError(toJson(OcelotError(ServerError)))
    }
  }

  def list: Action[AnyContent] = Action.async { _ =>
    approvalService.list() map {
      case Right(summaries) => Ok(summaries)
      case Left(BadRequestError) => BadRequest(toJson(OcelotError(BadRequestError)))
      case Left(_) => InternalServerError(toJson(OcelotError(ServerError)))
    }
  }

}
