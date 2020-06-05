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

import javax.inject.{Inject, Singleton}
import models.errors._
import models.{ApprovalProcessPageReview, ApprovalProcessStatusChange}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.ReviewService
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import utils.Constants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ProcessReviewController @Inject() (reviewService: ReviewService, cc: ControllerComponents) extends BackendController(cc) {

  def approval2iReviewInfo(id: String): Action[AnyContent] = Action.async { _ =>
    reviewService.approval2iReviewInfo(id).map {
      case Right(data) => Ok(Json.toJson(data).as[JsObject])
      case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
      case Left(Errors(StaleDataError :: Nil)) => NotFound(Json.toJson(StaleDataError))
      case Left(Errors(BadRequestError :: Nil)) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

  def approval2iReviewComplete(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    def save(statusChangeInfo: ApprovalProcessStatusChange): Future[Result] = {
      reviewService.change2iReviewStatus(id, StatusSubmittedFor2iReview, statusChangeInfo).map {
        case Right(_) => NoContent
        case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
        case Left(errors) => InternalServerError(Json.toJson(errors))
      }
    }

    request.body.validate[ApprovalProcessStatusChange] match {
      case JsSuccess(statusChangeInfo, _) => save(statusChangeInfo)
      case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  def approval2iReviewPageInfo(id: String, pageUrl: String): Action[AnyContent] = Action.async { _ =>
    reviewService.approval2iReviewPageInfo(id, pageUrl).map {
      case Right(data) => Ok(Json.toJson(data).as[JsObject])
      case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
      case Left(Errors(StaleDataError :: Nil)) => NotFound(Json.toJson(StaleDataError))
      case Left(Errors(BadRequestError :: Nil)) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

  def approval2iReviewPageComplete(id: String, pageUrl: String): Action[JsValue] = Action.async(parse.json) { request =>
    def save(reviewInfo: ApprovalProcessPageReview): Future[Result] =
      reviewService.approval2iReviewPageComplete(id, pageUrl, reviewInfo).map {
        case Right(_) => NoContent
        case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
        case Left(errors) => InternalServerError(Json.toJson(errors))
      }

    request.body.validate[ApprovalProcessPageReview] match {
      case JsSuccess(pageInfo, _) => save(pageInfo)
      case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  def approvalFactCheckInfo(id: String): Action[AnyContent] = Action.async { _ =>
    reviewService.approvalFactCheckInfo(id).map {
      case Right(data) => Ok(Json.toJson(data).as[JsObject])
      case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
      case Left(Errors(StaleDataError :: Nil)) => NotFound(Json.toJson(StaleDataError))
      case Left(Errors(BadRequestError :: Nil)) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

  def approvalFactCheckComplete(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    def save(statusChangeInfo: ApprovalProcessStatusChange): Future[Result] = {
      reviewService.changeFactCheckStatus(id, StatusSubmittedForFactCheck, statusChangeInfo).map {
        case Right(_) => NoContent
        case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
        case Left(errors) => InternalServerError(Json.toJson(errors))
      }
    }

    request.body.validate[ApprovalProcessStatusChange] match {
      case JsSuccess(statusChangeInfo, _) => save(statusChangeInfo)
      case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  def approvalFactCheckPageInfo(id: String, pageUrl: String): Action[AnyContent] = Action.async { _ =>
    reviewService.approvalFactCheckPageInfo(id, pageUrl).map {
      case Right(data) => Ok(Json.toJson(data).as[JsObject])
      case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
      case Left(Errors(StaleDataError :: Nil)) => NotFound(Json.toJson(StaleDataError))
      case Left(Errors(BadRequestError :: Nil)) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

}
