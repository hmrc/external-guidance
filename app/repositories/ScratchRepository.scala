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

package repositories

import java.util.UUID


import javax.inject.{Inject, Singleton}
import models.ScratchProcess

import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import play.api.libs.json.{Format, JsObject, Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoComponent
import repositories.formatters.ScratchProcessFormatter
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ScratchRepository {
  def save(process: JsObject): Future[UUID]
  def getByUuid(uuid: String): Future[Option[JsObject]]
}

@Singleton
class ScratchRepositoryImpl @Inject() (mongoComponent: ReactiveMongoComponent)
    extends ReactiveRepository[ScratchProcess, UUID](
      collectionName = "scratchProcesses",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = ScratchProcessFormatter.mongoFormat,
      idFormat = implicitly[Format[UUID]]
    )
    with ScratchRepository {

  def save(process: JsObject): Future[UUID] = {
    implicit val writer: OFormat[ScratchProcess] = ScratchProcessFormatter.mongoFormat
    val document = ScratchProcess(UUID.randomUUID(), process)
    collection.insert(ordered = false).one(document).map(_ => document.id)
  }

  def getByUuid(uuid: String): Future[Option[JsObject]] =
    collection.find(Json.obj("_id" -> uuid), None)
              .one[ScratchProcess]
              .map(_.map(_.process))

}


