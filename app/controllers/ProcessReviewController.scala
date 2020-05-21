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
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.ReviewService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext.Implicits.global

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
}
