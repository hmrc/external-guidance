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

package services

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsObject
import repositories.ScratchRepository

import scala.concurrent.Future

@Singleton
class ScratchService @Inject() (repository: ScratchRepository) {

  def save(process: JsObject): Future[UUID] = {
    repository.save(process)
  }

  def getByUuid(uuid: String): Future[Option[JsObject]] = {
    repository.getByUuid(uuid)
  }

}
