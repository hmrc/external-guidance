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

import models.ocelot.{labelReferences, Label}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

case class CalculationStanza(calcs: Seq[CalcOperation], override val next: Seq[String], stack: Boolean) extends NonVisualStanza {
  override val labels: List[Label] = calcs.map(op => Label(op.label)).toList
  override val labelRefs: List[String] = calcs.flatMap(op => labelReferences(op.left) ++ labelReferences(op.right)).toList
}

object CalculationStanza {

  implicit val calculationReads: Reads[CalculationStanza] =
    (
      (JsPath \ "calcs").read[Seq[CalcOperation]](minLength[Seq[CalcOperation]](1)) and
        (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
        (JsPath \ "stack").read[Boolean]
    )(CalculationStanza.apply _)

  implicit val calculationWrites: OWrites[CalculationStanza] =
    (
      (JsPath \ "calcs").write[Seq[CalcOperation]] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "stack").write[Boolean]
      )(unlift(CalculationStanza.unapply))
}
