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

package core.models.ocelot

import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.i18n.Lang

case class Phrase(english: String, welsh: String) {
  def value(lang: Lang): String = if (lang.code.equals("cy")) welsh else english
}

object Phrase {
  def apply(langs: Vector[String]): Phrase = Phrase(langs(0), langs(1))
  implicit val reads: Reads[Phrase] = __.read[Vector[String]](minLength[Vector[String]](2)).map(Phrase(_))

  implicit val writes: Writes[Phrase] = new Writes[Phrase] {
    override def writes(phrase: Phrase): JsValue = Json.toJson(Vector(phrase.english, phrase.welsh))
  }

  def apply(): Phrase = Phrase("", "")
}
