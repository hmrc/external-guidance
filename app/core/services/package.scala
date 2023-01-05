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

package core

import core.models.ocelot._
import core.models.errors.{Error, ValidationError}
import core.models.ocelot.stanzas.{TitleCallout, YourCallCallout, Question, Sequence, Input}
import java.util.UUID

package object services {
  val processIdformat: String = "^[a-z]{3}[0-9]{5}$"
  val uuidFormat: String = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
  val TimeMillisFormat: String = "^\\d{13}$"
  def validateUUID(id: String): Option[UUID] = if (id.matches(uuidFormat)) Some(UUID.fromString(id)) else None
  def validateProcessId(id: String): Either[Error, String] = if (id.matches(processIdformat)) Right(id) else Left(ValidationError)
  def uniqueLabels(pages: Seq[Page]):Seq[String] = pages.flatMap(p => p.labels).distinct
  def uniqueLabelRefs(pages: Seq[Page]): Seq[String] = pages.flatMap(_.labelRefs)
  def isTimeValueInMilliseconds(value: String): Boolean = value.matches(TimeMillisFormat)

  def fromPageDetails[A](pages: Seq[Page])(f: (String, String, String) => A): List[A] =
  pages.toList.flatMap { page =>
    page.stanzas.collectFirst {
      case TitleCallout(text, _, _) =>
        f(page.id, page.url, text.english)
      case YourCallCallout(text, _, _) =>
        f(page.id, page.url, text.english)
      case i: Question =>
        f(page.id, page.url, hintRegex.replaceAllIn(i.text.english, ""))
      case i: Sequence =>
        f(page.id, page.url, hintRegex.replaceAllIn(i.text.english, ""))
      case i: Input =>
        f(page.id, page.url, hintRegex.replaceAllIn(i.name.english, ""))
    }
  }
}
