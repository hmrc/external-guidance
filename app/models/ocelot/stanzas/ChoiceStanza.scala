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

package models.ocelot.stanzas

import models.ocelot.labelReferences
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._


case class ChoiceTest(left: String, test: TestType, right: String)

object ChoiceTest {
  implicit val reads: Reads[ChoiceTest] =
    (
      (JsPath \ "left").read[String] and
      (JsPath \ "test").read[TestType] and
      (JsPath \ "right").read[String]
    )(ChoiceTest.apply _)

  implicit val writes: OWrites[ChoiceTest] =
    (
      (JsPath \ "left").write[String] and
      (JsPath \ "test").write[TestType] and
      (JsPath \ "right").write[String]
    )(unlift(ChoiceTest.unapply))
}

case class ChoiceStanza(
  override val next: Seq[String],
  tests: Seq[ChoiceTest],
  stack: Boolean
) extends NonVisualStanza {
  override val labelRefs: List[String]  = tests.flatMap(op => labelReferences(op.left) ++ labelReferences(op.right)).toList
}

object ChoiceStanza {

  implicit val reads: Reads[ChoiceStanza] =
    (
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](2)) and
      (JsPath \ "tests").read[Seq[ChoiceTest]](minLength[Seq[ChoiceTest]](1)) and
      (JsPath \ "stack").read[Boolean]
    )(ChoiceStanza.apply _)

  implicit val writes: OWrites[ChoiceStanza] =
    (
      (JsPath \ "next").write[Seq[String]] and
      (JsPath \ "tests").write[Seq[ChoiceTest]] and
      (JsPath \ "stack").write[Boolean]
    )(unlift(ChoiceStanza.unapply))

}