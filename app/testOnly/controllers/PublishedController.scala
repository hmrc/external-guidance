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

package testOnly.controllers

import javax.inject.{Inject, Singleton}
import core.models.ocelot.Process
import models.errors.OcelotError
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import repositories.PublishedRepository
import testOnly.repositories.{PublishedRepository => TestPublishedRepository}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import java.time.{LocalDate, ZoneId}
import scala.util.{Try, Success}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PublishedController @Inject() (publishedRepo: PublishedRepository, testRepo: TestPublishedRepository, cc: ControllerComponents)
  (implicit ec: ExecutionContext) extends BackendController(cc) {

  def postAtDate(day: String, month: String, year: String): Action[JsValue] = Action.async(parse.json) { request =>
    def save(process: Process, when: LocalDate): Future[Result] = {
      publishedRepo.save(process.meta.id, "system", process.meta.processCode, Json.toJson(process).as[JsObject], process.meta.version).flatMap {
        case Right(id) =>
          testRepo.setPublishedDate(id, when.atStartOfDay(ZoneId.of("UTC"))).map{
            case Right(_) => Created(id)
            case Left(err) => InternalServerError(Json.toJson(OcelotError(err)))
          }
        case Left(err) => Future.successful(InternalServerError(Json.toJson(OcelotError(err))))
      }
    }

    Try{
      LocalDate.of(year.toInt, month.toInt, day.toInt)
    } match {
      case Success(when) =>
        request.body.validate[Process] match {
          case JsSuccess(p, _) =>
            p.valueStanzaPassPhrase.fold(save(p, when)){passPhrase =>
              save(p.copy(meta = p.meta.copy(passPhrase = Some(passPhrase))), when)
            }
          case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
        }
      case _ => Future.successful(InternalServerError(Json.toJson(OcelotError("Invalid date"))))
    }
  }

  def post(): Action[JsValue] = Action.async(parse.json) { request =>
    def save(process: Process): Future[Result] = {
      publishedRepo.save(process.meta.id, "system", process.meta.processCode, Json.toJson(process).as[JsObject], process.meta.version).map {
        case Right(id) => Created(id)
        case Left(err) => InternalServerError(Json.toJson(OcelotError(err)))
      }
    }

    request.body.validate[Process] match {
      case JsSuccess(p, _) =>
        p.valueStanzaPassPhrase.fold(save(p))(passPhrase => save(p.copy(meta = p.meta.copy(passPhrase = Some(passPhrase)))))
      case errors: JsError => Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async {
    testRepo.delete(id).map {
      case Right(_) => NoContent
      case Left(errors) => InternalServerError(Json.toJson(OcelotError(errors)))
    }
  }
}
