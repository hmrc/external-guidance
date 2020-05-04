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
import models.errors.{BadRequestError, Errors, InternalServiceError, NotFoundError}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.ApprovalService
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import scala.concurrent.ExecutionContext.Implicits.global

import models.{ApprovalProcess, ApprovalProcessMeta}
import repositories.formatters.{ApprovalProcessFormatter, ApprovalProcessMetaFormatter}

@Singleton
class ApprovalController @Inject() (approvalService: ApprovalService, cc: ControllerComponents) extends BackendController(cc) {

  implicit val apFormat: Format[ApprovalProcess] = ApprovalProcessFormatter.mongoFormat
  implicit val apmFormat: Format[ApprovalProcessMeta] = ApprovalProcessMetaFormatter.mongoFormat

  def save: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val process = request.body.as[JsObject]

    approvalService.save(process).map {
      case Right(id) => Created(Json.obj("id" -> id))
      case Left(Errors(BadRequestError :: Nil)) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

  def get(id: String): Action[AnyContent] = Action.async { _ =>
    approvalService.getById(id).map {
      case Right(approvalProcess) => Ok(Json.toJson(approvalProcess))
      case Left(Errors(NotFoundError :: Nil)) => NotFound(Json.toJson(NotFoundError))
      case Left(Errors(BadRequestError :: Nil)) => BadRequest(Json.toJson(BadRequestError))
      case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

  def listForAdminHomePage: Action[AnyContent] = Action.async { _ =>
    approvalService.listForHomePage().map {
      case Right(list) =>
        Ok(Json.toJson(list))
      case _ => InternalServerError(Json.toJson(InternalServiceError))
    }
  }

}
