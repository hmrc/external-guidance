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

import controllers.actions.{FactCheckerAction, TwoEyeReviewerAction}
import javax.inject.{Inject, Singleton}
import core.models.errors.{InternalServerError => ServerError, _}
import models.{ApprovalProcessPageReview, ApprovalProcessStatusChange}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.ReviewService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import models.Constants._
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ProcessReviewController @Inject() (
    factCheckerAction: FactCheckerAction,
    twoEyeReviewerAction: TwoEyeReviewerAction,
    reviewService: ReviewService,
    cc: ControllerComponents
) extends BackendController(cc) {

  val logger: Logger = Logger(getClass())

  def approval2iReviewInfo(id: String): Action[AnyContent] = twoEyeReviewerAction.async { _ =>
    getReviewInfo(id, ReviewType2i)
  }

  def approvalFactCheckInfo(id: String): Action[AnyContent] = factCheckerAction.async { _ =>
    getReviewInfo(id, ReviewTypeFactCheck)
  }

  private def getReviewInfo(id: String, reviewType: String): Future[Result] = {
    reviewService.approvalReviewInfo(id, reviewType).map {
      case Right(data) => Ok(Json.toJson(data).as[JsObject])
      case Left(NotFoundError) => NotFound(Json.toJson(NotFoundError))
      case Left(DuplicateKeyError) => BadRequest(Json.toJson(DuplicateKeyError))
      case Left(StaleDataError) => NotFound(Json.toJson(StaleDataError))
      case Left(BadRequestError) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(ServerError))
    }
  }

  def approval2iReviewConfirmAllPagesReviewed(id: String): Action[AnyContent] = twoEyeReviewerAction.async { _ =>
    reviewService.checkProcessInCorrectStateForCompletion(id, ReviewType2i).map {
      case Right(_) => NoContent
      case Left(IncompleteDataError) => BadRequest(Json.toJson(IncompleteDataError))
      case Left(StaleDataError) => NotFound(Json.toJson(StaleDataError))
      case Left(NotFoundError) => NotFound(Json.toJson(NotFoundError))
      case Left(errors) => InternalServerError(Json.toJson(errors))
    }
  }

  def approval2iReviewComplete(id: String): Action[JsValue] = twoEyeReviewerAction.async(parse.json) { request =>
    def save(statusChangeInfo: ApprovalProcessStatusChange): Future[Result] = {
      reviewService.twoEyeReviewComplete(id, statusChangeInfo).map {
        case Right(auditInfo) =>
          logger.warn(s"2i review of $id complete")
          Ok(Json.toJson(auditInfo))
        case Left(IncompleteDataError) => BadRequest(Json.toJson(IncompleteDataError))
        case Left(DuplicateKeyError) => BadRequest(Json.toJson(DuplicateKeyError))
        case Left(NotFoundError) => NotFound(Json.toJson(NotFoundError))
        case Left(StaleDataError) => NotFound(Json.toJson(StaleDataError))
        case Left(BadRequestError) => BadRequest(Json.toJson(BadRequestError))
        case Left(UpgradeRequiredError) => BadRequest(Json.toJson(UpgradeRequiredError))
        case Left(errors) => InternalServerError(Json.toJson(errors))
      }
    }
    request.body.validate[ApprovalProcessStatusChange] match {
      case JsSuccess(statusChangeInfo, _) => save(statusChangeInfo)
      case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  def approvalFactCheckComplete(id: String): Action[JsValue] = factCheckerAction.async(parse.json) { request =>
    def save(statusChangeInfo: ApprovalProcessStatusChange): Future[Result] = {
      reviewService.factCheckComplete(id, statusChangeInfo).map {
        case Right(auditInfo) =>
          logger.info(s"Fact check of $id complete")
          Ok(Json.toJson(auditInfo))
        case Left(IncompleteDataError) => BadRequest(Json.toJson(IncompleteDataError))
        case Left(NotFoundError) => NotFound(Json.toJson(NotFoundError))
        case Left(StaleDataError) => NotFound(Json.toJson(StaleDataError))
        case Left(BadRequestError) => BadRequest(Json.toJson(BadRequestError))
        case Left(errors) => InternalServerError(Json.toJson(errors))
      }
    }

    request.body.validate[ApprovalProcessStatusChange] match {
      case JsSuccess(statusChangeInfo, _) => save(statusChangeInfo)
      case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  def approval2iReviewPageInfo(id: String, pageUrl: String): Action[AnyContent] = twoEyeReviewerAction.async { _ =>
    pageReviewInfo(id, pageUrl, ReviewType2i)
  }

  def approvalFactCheckPageInfo(id: String, pageUrl: String): Action[AnyContent] = factCheckerAction.async { _ =>
    pageReviewInfo(id, pageUrl, ReviewTypeFactCheck)
  }

  private def pageReviewInfo(id: String, pageUrl: String, reviewType: String): Future[Result] = {
    reviewService.approvalPageInfo(id, s"/$pageUrl", reviewType).map {
      case Right(data) => Ok(Json.toJson(data).as[JsObject])
      case Left(NotFoundError) => NotFound(Json.toJson(NotFoundError))
      case Left(StaleDataError) => NotFound(Json.toJson(StaleDataError))
      case Left(BadRequestError) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(ServerError))
    }
  }

  def approval2iReviewPageComplete(id: String, pageUrl: String): Action[JsValue] = twoEyeReviewerAction.async(parse.json) { request =>
    pageReviewComplete(id, pageUrl, ReviewType2i, request.body)
  }

  def approvalFactCheckPageComplete(id: String, pageUrl: String): Action[JsValue] = factCheckerAction.async(parse.json) { request =>
    pageReviewComplete(id, pageUrl, ReviewTypeFactCheck, request.body)
  }

  private def pageReviewComplete(id: String, pageUrl: String, reviewType: String, reviewJson: JsValue): Future[Result] = {
    def save(reviewInfo: ApprovalProcessPageReview): Future[Result] =
      reviewService.approvalPageComplete(id, s"/$pageUrl", reviewType, reviewInfo).map {
        case Right(_) => NoContent
        case Left(NotFoundError) => NotFound(Json.toJson(NotFoundError))
        case Left(errors) => InternalServerError(Json.toJson(errors))
      }

    reviewJson.validate[ApprovalProcessPageReview] match {
      case JsSuccess(pageInfo, _) => save(pageInfo)
      case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }
}
