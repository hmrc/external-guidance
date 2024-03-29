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

package controllers

import javax.inject.{Inject, Singleton}
import core.models.errors.{ValidationError, InternalServerError => ServerError}
import models.errors.OcelotError
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{ApprovalService, PublishedService, TimescalesService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.Logger
import controllers.actions.AllRolesAction

import scala.concurrent.{ExecutionContext, Future}
import models.requests.IdentifierRequest

@Singleton()
class TimescalesController @Inject() (timescaleService: TimescalesService,
                                      publishService: PublishedService,
                                      approvalService: ApprovalService,
                                      cc: ControllerComponents,
                                      allRolesAction: AllRolesAction)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(getClass)

  def save(): Action[JsValue] = allRolesAction.async(parse.json) { implicit request: IdentifierRequest[JsValue] =>
    publishService.getTimescalesInUse().flatMap{
      case Left(err) =>
        logger.error(s"Unable to retreive list of timescales within published guidance, $err")
        Future.successful(InternalServerError(Json.toJson(OcelotError(ServerError))))
      case Right(publishedInUse) =>
        approvalService.getTimescalesInUse().flatMap{
          case Left(err) =>
            logger.error(s"Unable to retreive list of timescales within for-approval guidance, $err")
            Future.successful(InternalServerError(Json.toJson(OcelotError(ServerError))))
          case Right(approvalInUse) =>
            timescaleService.save(request.body, request.credId, request.name, request.email, (publishedInUse ++ approvalInUse).distinct).map {
              case Right(response) =>
                logger.warn(s"TIMESCALES: ${response.count} Timescale definitions received from ${request.name} (${request.credId}), email ${request.email}")
                Accepted(Json.toJson(response))
              case Left(ValidationError) =>
                logger.error(s"Failed to save of updated timescales due to ValidationError")
                BadRequest(Json.toJson(OcelotError(ValidationError)))
              case Left(err) =>
                logger.error(s"Failed to save of updated timescales due to $err, returning internal server error")
                InternalServerError(Json.toJson(OcelotError(ServerError)))
            }
        }
    }
  }

  def details: Action[AnyContent] = allRolesAction.async { _ =>
    timescaleService.details().map {
      case Right(response) => Ok(Json.toJson(response))
      case Left(_) => InternalServerError(Json.toJson(OcelotError(ServerError)))
    }
  }

  def get: Action[AnyContent] = Action.async { _ =>
    timescaleService.get().map {
      case Right(response) => Ok(Json.toJson(response))
      case Left(_) => InternalServerError(Json.toJson(OcelotError(ServerError)))
    }
  }

}
