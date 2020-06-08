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
    getReviewInfo(id, ReviewType2i)
  }

  def approvalFactCheckInfo(id: String): Action[AnyContent] = Action.async { _ =>
    getReviewInfo(id, ReviewTypeFactCheck)
  }

  private def getReviewInfo(id: String, reviewType: String): Future[Result] = {
    reviewService.approvalReviewInfo(id, reviewType).map {
      case Right(data) => Ok(Json.toJson(data).as[JsObject])
      case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
      case Left(Errors(StaleDataError :: Nil)) => NotFound(Json.toJson(StaleDataError))
      case Left(Errors(BadRequestError :: Nil)) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

  def approval2iReviewComplete(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    def save(statusChangeInfo: ApprovalProcessStatusChange): Future[Result] = {
      reviewService.twoEyeReviewComplete(id, statusChangeInfo).map {
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

  def approvalFactCheckComplete(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    def save(statusChangeInfo: ApprovalProcessStatusChange): Future[Result] = {
      reviewService.factCheckComplete(id, statusChangeInfo).map {
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
    pageReviewInfo(id, pageUrl, ReviewType2i)
  }

  def approvalFactCheckPageInfo(id: String, pageUrl: String): Action[AnyContent] = Action.async { _ =>
    pageReviewInfo(id, pageUrl, ReviewTypeFactCheck)
  }

  private def pageReviewInfo(id: String, pageUrl: String, reviewType: String): Future[Result] = {
    reviewService.approvalPageInfo(id, pageUrl, reviewType).map {
      case Right(data) => Ok(Json.toJson(data).as[JsObject])
      case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
      case Left(Errors(StaleDataError :: Nil)) => NotFound(Json.toJson(StaleDataError))
      case Left(Errors(BadRequestError :: Nil)) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

  def approval2iReviewPageComplete(id: String, pageUrl: String): Action[JsValue] = Action.async(parse.json) { request =>
    pageReviewComplete(id, pageUrl, ReviewType2i, request.body)
  }

  def approvalFactCheckPageComplete(id: String, pageUrl: String): Action[JsValue] = Action.async(parse.json) { request =>
    pageReviewComplete(id, pageUrl, ReviewTypeFactCheck, request.body)
  }

  private def pageReviewComplete(id: String, pageUrl: String, reviewType: String, reviewJson: JsValue): Future[Result] = {
    def save(reviewInfo: ApprovalProcessPageReview): Future[Result] =
      reviewService.approvalPageComplete(id, pageUrl, reviewType, reviewInfo).map {
        case Right(_) => NoContent
        case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
        case Left(errors) => InternalServerError(Json.toJson(errors))
      }

    reviewJson.validate[ApprovalProcessPageReview] match {
      case JsSuccess(pageInfo, _) => save(pageInfo)
      case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

}
