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

package core.models.ocelot

import play.api.i18n.Lang

trait Labels {
  def value(name: String): Option[String]
  def displayValue(name: String)(implicit lang: Lang): Option[String]
  def update(name: String, english: String): Labels
  def update(name: String, english: String, welsh: String): Labels
  def updatedLabels: Map[String, Label]
  def labelMap:Map[String, Label]
  def flush(): Labels
}

private class LabelCacheImpl(labels: Map[String, Label] = Map(), cache: Map[String, Label] = Map()) extends Labels {
  def value(name: String): Option[String] = label(name).map(_.english.getOrElse(""))
  def displayValue(name: String)(implicit lang: Lang): Option[String] = label(name).map{lbl =>
    lang.code match {
      case "en" => lbl.english.getOrElse("")
      case "cy" => lbl.welsh.fold(lbl.english.getOrElse(""))(cy => cy)
    }
  }
  def update(name: String, english: String): Labels = new LabelCacheImpl(labels, updateOrAddLabel(name, english))
  def update(name: String, english: String, welsh: String): Labels = new LabelCacheImpl(labels, updateOrAddLabel(name, english, Some(welsh)))
  def updatedLabels: Map[String, Label] = cache
  def labelMap:Map[String, Label] = labels
  def flush(): Labels = new LabelCacheImpl(labels ++ cache.toList, Map())

  private def label(name: String): Option[Label] = cache.get(name).fold(labels.get(name))(Some(_))
  private def updateOrAddLabel(name: String, english: String, welsh: Option[String] = None): Map[String, Label] =
    cache + (name -> cache.get(name).fold[Label](Label(name, Some(english), welsh))(l => Label(l.name, Some(english), welsh)))
}

object LabelCache {
  def apply(): Labels = new LabelCacheImpl()
  def apply(labels: Map[String, Label]): Labels = new LabelCacheImpl(labels)
  def apply(labels: Map[String, Label], cache: Map[String, Label]): Labels = new LabelCacheImpl(labels, cache)
}