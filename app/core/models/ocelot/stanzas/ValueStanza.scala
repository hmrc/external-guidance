/*
 * Copyright 2021 HM Revenue & Customs
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

package core.models.ocelot.stanzas

import core.models.ocelot.{labelReferences, Label, Labels}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import scala.annotation.tailrec

case class Value(valueType: ValueType, label: String, value: String)

object Value {

  implicit val reads: Reads[Value] =
    ((__ \ "type").read[ValueType] and
      (__ \ "label").read[String] and
      (__ \ "value").read[String])(Value.apply _)

  implicit val writes: Writes[Value] =
    (
      (__ \ "type").write[ValueType] and
        (__ \ "label").write[String] and
        (__ \ "value").write[String]
    )(unlift(Value.unapply))

}

case class ValueStanza(values: List[Value], override val next: Seq[String], stack: Boolean) extends Stanza with Evaluate {
  override val labels: List[Label] = values.map(v => Label(v.label, None))
  override val labelRefs: List[String] = values.flatMap(v => labelReferences(v.value))

  def eval(originalLabels: Labels): (String, Labels) = {
    @tailrec
    def assignValtoLabels(vs: List[Value], labels: Labels): Labels =
      vs match {
        case Nil => labels
        case x :: xs => assignValtoLabels(xs, labels.update(x.label, x.value))
      }
    (next.head, assignValtoLabels(values, originalLabels))
  }
}

object ValueStanza {

  implicit val reads: Reads[ValueStanza] =
    ((__ \ "values").read[List[Value]](minLength[List[Value]](1)) and
      (__ \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
      (__ \ "stack").read[Boolean])(ValueStanza.apply _)

  implicit val writes: OWrites[ValueStanza] =
    (
      (__ \ "values").write[List[Value]] and
        (__ \ "next").write[Seq[String]] and
        (__ \ "stack").write[Boolean]
    )(unlift(ValueStanza.unapply))

}
