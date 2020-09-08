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
import models.errors.{BadRequestError, ValidationError, Error, InternalServerError => ServerError, NotFoundError}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.ScratchService
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton()
class ScratchController @Inject() (scratchService: ScratchService, cc: ControllerComponents) extends BackendController(cc) {

  val logger = Logger(getClass)

  def save(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val process = request.body.as[JsObject]

    scratchService.save(process).map {
      case Right(id) => Created(Json.obj("id" -> id.toString))
      case Left(err @ Error(Error.UnprocessableEntity, _, Some(details))) =>
        logger.error(s"Failed to save scratch process due to process errors $details")
        UnprocessableEntity(Json.toJson(err))
      case Left(ValidationError) =>
        logger.error(s"Save on scratch service returned ValidationError")
        BadRequest(Json.toJson(BadRequestError))
      case Left(BadRequestError) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(ServerError))
    }
  }

  def get(id: String): Action[AnyContent] = Action.async { _ =>
    scratchService.getById(id).map {
      case Right(process) => Ok(process)
      case Left(NotFoundError) => NotFound(Json.toJson(NotFoundError))
      case Left(BadRequestError) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(ServerError))
    }
  }
}
