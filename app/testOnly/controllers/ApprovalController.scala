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

package testOnly.controllers

import javax.inject.{Inject, Singleton}
import core.models.errors.{BadRequestError, DuplicateKeyError, Error}
import core.models.errors.{ValidationError, InternalServerError => ServerError}
import models.errors.{OcelotError, DuplicateProcessCodeError}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import testOnly.repositories.{ApprovalProcessReviewRepository, ApprovalRepository}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import models.errors.OcelotError
import core.models._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.Constants._
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.ApprovalService
import play.api.Logger
import play.api.libs.json.Json.toJson

@Singleton
class ApprovalController @Inject() (approvalService: ApprovalService,
                                    testRepo: ApprovalRepository,
                                    testReviewRepo: ApprovalProcessReviewRepository,
                                    cc: ControllerComponents) extends BackendController(cc) {

  val logger: Logger = Logger(getClass)

  def delete(id: String): Action[AnyContent] = Action.async { _ =>
    testRepo.delete(id).flatMap {
      case Right(_) =>
        testReviewRepo.delete(id).map {
          case Right(_) => NoContent
          case Left(_) => InternalServerError(Json.toJson(OcelotError(ServerError)))
        }
      case Left(_) => Future.successful(InternalServerError(Json.toJson(OcelotError(ServerError))))
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
