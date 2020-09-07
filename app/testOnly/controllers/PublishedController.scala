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

package testOnly.controllers

import javax.inject.{Inject, Singleton}
import models.ocelot.Process
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import repositories.PublishedRepository
import testOnly.repositories.{PublishedRepository => TestPublishedRepository}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PublishedController @Inject() (publishedRepo: PublishedRepository, testRepo: TestPublishedRepository, cc: ControllerComponents)
    extends BackendController(cc) {

  def post(): Action[JsValue] = Action.async(parse.json) { request =>
    def save(process: Process): Future[Result] = {
      publishedRepo.save(process.meta.id, "system", process.meta.code, request.body.as[JsObject]).map {
        case Right(id) => Created(id)
        case Left(err) => InternalServerError(Json.toJson(err))
      }
    }

    request.body.validate[Process] match {
      case JsSuccess(process, _) => save(process)
      case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
    }

  }

  def delete(id: String): Action[AnyContent] = Action.async {
    testRepo.delete(id).map {
      case Right(_) => NoContent
      case Left(errors) => InternalServerError(Json.toJson(errors))
    }
  }
}
