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

package core.models.ocelot.stanzas

import core.models.ocelot.labelReference
import core.models.ocelot.{Labels, operandValue, labelReferences}
import play.api.libs.functional.syntax.*
import play.api.libs.json.Reads.*
import play.api.libs.json.*

import scala.annotation.tailrec
import core.models.ocelot.errors.RuntimeError

case class Value(valueType: ValueType, label: String, value: String)

object Value {

  given reads: Reads[Value] =
    ((__ \ "type").read[ValueType] and
      (__ \ "label").read[String] and
      (__ \ "value").read[String])(Value.apply _)

  given writes: Writes[Value] =
    (
      (__ \ "type").write[ValueType] and
        (__ \ "label").write[String] and
        (__ \ "value").write[String]
    )(Tuple.fromProductTyped(_))
}

case class ValueStanza(values: List[Value], override val next: Seq[String], stack: Boolean) extends PopulatedStanza with Evaluate {
  override val labels: List[String] = values.map(v => v.label)
  override val labelRefs: List[String] = values.flatMap(v => labelReferences(v.value))

  def eval(originalLabels: Labels): (String, Labels, Option[RuntimeError]) = {

    def assignValue(v: Value, labels: Labels): Labels = v.valueType match {
        case ScalarType => labels.update(v.label, operandValue(v.value)(using labels).getOrElse(""))
        case ListType => labels.updateList(v.label, labelReference(v.value).fold[List[String]]
          (if(v.value.isEmpty) Nil else v.value.split(",").toList)(lr => labels.valueAsList(lr).getOrElse(Nil)))
      }

    @tailrec
    def assignValToLabels(vs: List[Value], l: Labels): Labels =
      vs match {
        case Nil => l
        case x :: xs =>
          assignValToLabels(xs, assignValue(x, l))
      }

    (next.head, assignValToLabels(values, originalLabels), None)
  }
}

object ValueStanza {

  given reads: Reads[ValueStanza] =
    ((__ \ "values").read[List[Value]](minLength[List[Value]](1)) and
      (__ \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
      (__ \ "stack").read[Boolean])(ValueStanza.apply _)

  given writes: OWrites[ValueStanza] =
    (
      (__ \ "values").write[List[Value]] and
        (__ \ "next").write[Seq[String]] and
        (__ \ "stack").write[Boolean]
    )(Tuple.fromProductTyped(_))

}
