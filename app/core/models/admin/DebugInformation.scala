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

package core.models.admin

import core.models.ocelot.Labels
import core.models.ocelot.ScalarLabel
import core.models.ocelot.ListLabel

case class DebugLabelRow(name: String, dataType: String, initialValue: Option[String], updatedValue: Option[String])

case class DebugInformation(processPageStructure: ProcessPageStructure, preRenderLabels: Labels, postRenderLabels: Labels) {
  val allPostRenderLabels = postRenderLabels.labelMap ++ postRenderLabels.updatedLabels
  val names: List[String] = (preRenderLabels.labelMap.keySet.toList ++ allPostRenderLabels.keySet.toList).distinct

  lazy val labels: List[DebugLabelRow] = {
    val all: List[DebugLabelRow] = names.map{n =>
      allPostRenderLabels(n) match {
        case s: ScalarLabel => DebugLabelRow(n, "Scalar", preRenderLabels.value(n), postRenderLabels.value(n))
        case l: ListLabel => DebugLabelRow(n, "List", preRenderLabels.valueAsList(n).map(_.mkString(",")), postRenderLabels.valueAsList(n).map(_.mkString(",")))
      }
    }
    all.filterNot(_.initialValue.isDefined) ::: all.filter(_.initialValue.isDefined) // Show new labels (undefined initial value) first in list
  }
}
