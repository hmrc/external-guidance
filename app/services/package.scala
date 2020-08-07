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

import models.errors.{Error, ValidationError}
import java.util.UUID
import models.RequestOutcome
import models.ocelot.{Page, Process}
import play.api.libs.json._
import play.api.Logger

package object services {

  def validateUUID(id: String): Option[UUID] = {
    val format = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    if (id.matches(format)) Some(UUID.fromString(id)) else None
  }

  def validateProcessId(id: String): Either[Error, String] = {
    val format = "^[a-z]{3}[0-9]{5}$"
    if (id.matches(format)) Right(id) else Left(ValidationError)
  }

  def guidancePages(pageBuilder: PageBuilder, jsValue: JsValue): RequestOutcome[(Process, Seq[Page])] =
    jsValue.validate[Process].fold(err => {
      Logger(getClass).error(s"Process validation has failed with error $err")
      Left(ValidationError)
      }, process => pageBuilder.pages(process).fold(err => Left(Error(List(err))), p => Right((process,p))))

}
