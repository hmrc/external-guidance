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

package models.ocelot

trait Labels {
  def value(name: String): Option[String]
  def formattedValue(name: String): Option[String]
  def update(name: String, value: String): Labels
  def updatedLabels: Map[String, Label]
}

class LabelCache(labels: Map[String, Label]) extends Labels {
  def value(name: String): Option[String] = labels.get(name).map(_.value.getOrElse(""))
  def formattedValue(name: String): Option[String] = value(name)  // TODO format for type of label
  def update(name: String, value: String): Labels =  new LabelCache(updateOrAddLabel(name, value))
  def updatedLabels: Map[String, Label] = labels

  private def updateOrAddLabel(name: String, value: String): Map[String, Label] =
    labels + (name -> labels.get(name).fold(Label(name, Some(value)))(l => l.copy(value = Some(value))))
}

object LabelCache {
  def apply(): LabelCache = new LabelCache(Map())
  def apply(labels: Map[String, Label]): LabelCache = new LabelCache(labels)
}