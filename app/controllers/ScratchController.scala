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

package controllers

import javax.inject.{Inject, Singleton}
import core.models.errors.{BadRequestError, ValidationError, Error, InternalServerError => ServerError, NotFoundError}
import models.errors.OcelotError
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{TimescalesService, ScratchService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton()
class ScratchController @Inject() (scratchService: ScratchService,
                                   timescalesService: TimescalesService,
                                   cc: ControllerComponents) extends BackendController(cc) {
  val logger: Logger = Logger(getClass)
  import Json._

  def save(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[JsObject].fold(errs => {
      logger.error(s"Unable to parse incoming json as a JsObject, Errors: $errs")
      Future.successful(BadRequest(toJson(OcelotError(BadRequestError))))
    }, process => {
      scratchService.save(process).map {
        case Right(id) => Created(obj("id" -> id.toString))
        case Left(err @ Error(Error.UnprocessableEntity, details, _)) =>
          logger.error(s"Failed to save scratch process due to process errors $details")
          UnprocessableEntity(toJson(OcelotError(err)))
        case Left(ValidationError) =>
          logger.error(s"Save on scratch service returned ValidationError")
          BadRequest(toJson(OcelotError(BadRequestError)))
        case Left(BadRequestError) => BadRequest(toJson(OcelotError(BadRequestError)))
        case Left(err) => InternalServerError(toJson(OcelotError(ServerError)))
      }
    })
  }

  def get(id: String): Action[AnyContent] = Action.async { _ =>
    scratchService.getById(id).flatMap {
      case Right(process) => timescalesService.updateProcessTimescaleTable(process).map {
        case Right(result) => Ok(result)
        case Left(_) => InternalServerError(toJson(OcelotError(ServerError)))
      }
      case Left(NotFoundError) => Future.successful(NotFound(toJson(OcelotError(NotFoundError))))
      case Left(BadRequestError) => Future.successful(BadRequest(toJson(OcelotError(BadRequestError))))
      case Left(_) => Future.successful(InternalServerError(toJson(OcelotError(ServerError))))
    }
  }
}
