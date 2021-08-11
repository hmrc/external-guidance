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

import javax.inject.{Inject, Singleton}
import core.models.errors.{ValidationError, InternalServerError => ServerError}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.TimescalesService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.Logger
import controllers.actions.AllRolesAction
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton()
class TimescalesController @Inject() (timescaleService: TimescalesService, cc: ControllerComponents, allRolesAction: AllRolesAction) extends BackendController(cc) {

  val logger: Logger = Logger(getClass)

  def save(): Action[JsValue] = allRolesAction.async(parse.json) { implicit request =>
    val timescales: JsValue = request.body
    logger.warn(s"Received timescales update")
    timescaleService.save(timescales).map {
      case Right(id) => NoContent
      case Left(ValidationError) =>
        logger.error(s"Failed to save of updated timescales due to ValidationError")
        BadRequest(Json.toJson(ValidationError))
      case Left(err) =>
        logger.error(s"Failed to save of updated timescales due to $err, returning internal server error")
        InternalServerError(Json.toJson(ServerError))
    }
  }

  def get(): Action[AnyContent] = Action.async { _ =>
    timescaleService.get().map {
      case Right(timescales) => Ok(timescales)
      case Left(_) => InternalServerError(Json.toJson(ServerError))
    }
  }
}
