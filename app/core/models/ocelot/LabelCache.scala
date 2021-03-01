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

import core.models.ocelot.stanzas.Stanza
import play.api.i18n.Lang

trait Labels {
  def value(name: String): Option[String]
  def valueAsList(name: String): Option[List[String]]
  def displayValue(name: String)(implicit lang: Lang): Option[String]
  def update(name: String, english: String): Labels
  def update(name: String, english: String, welsh: String): Labels
  def updateList(name: String, english: List[String]): Labels
  def updateList(name: String, english: List[String], welsh: List[String]): Labels
  def pushFlows(flowNext: Seq[String], continue: String, labelName: Option[String], labelValues: Seq[String], stanzas: Map[String, Stanza]): Labels
  def takeFlow: Option[(String, Map[String, Stanza], Labels)]
  // Persistence access
  def updatedLabels: Map[String, Label]
  def labelMap:Map[String, Label]
  def flowStack: List[FlowStage]
  def flush(): Labels
}

private class LabelCacheImpl(labels: Map[String, Label], cache: Map[String, Label], stack: List[FlowStage]) extends Labels {

  def value(name: String): Option[String] = label(name).collect{case s: ScalarLabel => s.english.headOption.getOrElse("")}
  def valueAsList(name: String): Option[List[String]] = label(name).collect{case l: ListLabel => l.english}
  def displayValue(name: String)(implicit lang: Lang): Option[String] = label(name).map{lbl =>
    lang.code match {
      case "cy" if(lbl.welsh.nonEmpty) => lbl.welsh.mkString(",")
      case _ => lbl.english.mkString(",")
    }
  }
  def update(name: String, english: String): Labels = new LabelCacheImpl(labels, updateOrAddScalarLabel(name, english, None), stack)
  def update(name: String, english: String, welsh: String): Labels = new LabelCacheImpl(labels, updateOrAddScalarLabel(name, english, Some(welsh)), stack)
  def updateList(name: String, english: List[String]): Labels = new LabelCacheImpl(labels, updateOrAddListLabel(name, english), stack)
  def updateList(name: String, english: List[String], welsh: List[String]): Labels =
    new LabelCacheImpl(labels, updateOrAddListLabel(name, english, welsh), stack)
  def updatedLabels: Map[String, Label] = cache

  def pushFlows(flowNext: Seq[String], continue: String, labelName: Option[String], labelValues: Seq[String], stanzas: Map[String, Stanza]): Labels =
    flowNext.zipWithIndex.map{
      case (nxt, idx) => Flow(nxt, labelName.map(LabelValue(_, labelValues.lift(idx).fold[Option[String]](None)(v => Some(v)))))
    } match {
      case Nil => this
      case flows => new LabelCacheImpl(labels, cache, flows.toList ++ (Continuation(continue, stanzas) :: stack))
    }

  def takeFlow: Option[(String, Map[String, Stanza], Labels)] = // Remove head of flow stack and update flow label if required
    stack.headOption.map{
      case f: Flow =>
      (f.next,
       Map(),
       f.labelValue.flatMap(lv => lv.value.map(v => new LabelCacheImpl(labels, updateOrAddScalarLabel(lv.name, v, None), stack.tail)))
        .getOrElse(new LabelCacheImpl(labels, cache, stack.tail))
      )
      case c: Continuation => (c.next, c.stanzas, new LabelCacheImpl(labels, cache, stack.tail))
    }

  // Persistence access
  def labelMap:Map[String, Label] = labels
  def flowStack: List[FlowStage] = stack
  def flush(): Labels = new LabelCacheImpl(labels ++ cache.toList, Map(), stack)

  private def label(name: String): Option[Label] = cache.get(name).fold(labels.get(name))(Some(_))

  private def updateOrAddScalarLabel(name: String, english: String, welsh: Option[String]): Map[String, Label] = {
    cache + (name -> cache.get(name).fold[Label]
      (ScalarLabel(name, List(english), welsh.fold[List[String]](Nil)(w => List(w))))
      (l => ScalarLabel(l.name, List(english), welsh.fold[List[String]](Nil)(w => List(w)))))
  }

  private def updateOrAddListLabel(name: String, english: List[String], welsh: List[String] = Nil): Map[String, Label] =
    cache + (name -> cache.get(name).fold[Label](ListLabel(name, english, welsh))(l => ListLabel(l.name, english, welsh)))
}

object LabelCache {
  def apply(): Labels = new LabelCacheImpl(Map(), Map(), Nil)
  def apply(labels: Map[String, Label]): Labels = new LabelCacheImpl(labels, Map(), Nil)
  def apply(labels: Map[String, Label], cache: Map[String, Label]): Labels = new LabelCacheImpl(labels, cache, Nil)
  def apply(labels: Map[String, Label], cache: Map[String, Label], stack: List[FlowStage]): Labels = new LabelCacheImpl(labels, cache, stack)
}
