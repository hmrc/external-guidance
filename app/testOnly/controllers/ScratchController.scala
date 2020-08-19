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
import models.errors.InternalServiceError
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import testOnly.repositories.ScratchRepository

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ScratchController @Inject() (testRepo: ScratchRepository, cc: ControllerComponents) extends BackendController(cc) {

  def delete(id: String): Action[AnyContent] = Action.async {
    testRepo.delete(id).map {
          case Right(_) => NoContent
          case Left(_) => InternalServerError(Json.toJson(InternalServiceError))
        }
  }
}
