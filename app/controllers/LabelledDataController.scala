/*
 * Copyright 2024 HM Revenue & Customs
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
import services.{ApprovalReviewService, PublishedService, LabelledDataService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.Logger
import controllers.actions.AllRolesAction
import scala.concurrent.{ExecutionContext, Future}
import models.requests.IdentifierRequest
import models.LabelledDataId

@Singleton()
class LabelledDataController @Inject() (labelledDataService: LabelledDataService,
                                        publishService: PublishedService,
                                        approvalService: ApprovalReviewService,
                                        cc: ControllerComponents,
                                        allRolesAction: AllRolesAction)(implicit ec: ExecutionContext) extends BackendController(cc) {

  val logger: Logger = Logger(getClass)

  def save(dataId: LabelledDataId): Action[JsValue] = allRolesAction.async(parse.json) { implicit request: IdentifierRequest[JsValue] =>
    publishService.getDataInUse(dataId).flatMap{
      case Left(err) =>
        logger.error(s"Unable to retreive list of $dataId within published guidance, $err")
        Future.successful(InternalServerError(Json.toJson(OcelotError(ServerError))))
      case Right(publishedInUse) =>
        approvalService.getDataInUse(dataId).flatMap{
          case Left(err) =>
            logger.error(s"Unable to retreive list of $dataId within for-approval guidance, $err")
            Future.successful(InternalServerError(Json.toJson(OcelotError(ServerError))))
          case Right(approvalInUse) =>
            labelledDataService.save(dataId, request.body, request.credId, request.name, request.email, (publishedInUse ++ approvalInUse).distinct).map {
              case Right(response) =>
                logger.warn(s"$dataId: ${response.count} definitions received from ${request.name} (${request.credId}), email ${request.email}")
                Accepted(Json.toJson(response))
              case Left(ValidationError) =>
                logger.error(s"Failed to save of updated $dataId due to ValidationError")
                BadRequest(Json.toJson(OcelotError(ValidationError)))
              case Left(err) =>
                logger.error(s"Failed to save of updated $dataId due to $err, returning internal server error")
                InternalServerError(Json.toJson(OcelotError(ServerError)))
            }
        }
    }
  }

  def details(dataId: LabelledDataId): Action[AnyContent] = allRolesAction.async { _ =>
    labelledDataService.details(dataId).map {
      case Right(response) => Ok(Json.toJson(response))
      case Left(_) => InternalServerError(Json.toJson(OcelotError(ServerError)))
    }
  }

  def get(dataId: LabelledDataId): Action[AnyContent] = Action.async { _ =>
    labelledDataService.get(dataId).map {
      case Right(jsValue) => Ok(jsValue)
      case Left(_) => InternalServerError(Json.toJson(OcelotError(ServerError)))
    }
  }

}
