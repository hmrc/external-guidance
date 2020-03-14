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

import play.api.libs.json.JsObject

import scala.concurrent.Future

class ScratchRepository {
  val dummyId: UUID = UUID.fromString("265e0178-cbe1-42ab-8418-7120ce6d0925")
  def save(process: JsObject): Future[UUID] = Future.successful(dummyId)
}
