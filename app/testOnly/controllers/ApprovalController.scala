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

package testOnly.controllers

import core.models._
import core.models.errors.{BadRequestError, DuplicateKeyError, Error, ValidationError, InternalServerError => ServerError}
import models.Constants._
import models.errors.{DuplicateProcessCodeError, OcelotError}
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.ApprovalService
import testOnly.repositories.ApprovalsRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApprovalController @Inject() (approvalService: ApprovalService,
                                    testRepo: ApprovalsRepository,
                                    cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(getClass)

  def delete(id: String): Action[AnyContent] = Action.async { _ =>
    testRepo.delete(id).map {
      case Right(_) => NoContent
      case Left(_) => InternalServerError(Json.toJson(OcelotError(ServerError)))
    }
  }

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
      approvalService.save(process, reviewType, StatusSubmitted, Tolerant).map {
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

}
