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

import core.models.ocelot.stanzas.Stanza
import play.api.i18n.Lang

trait Flows {
  def pushFlows(flowNext: List[String],
                continue: String,
                labelName: Option[String],
                labelValues: List[Phrase],
                stanzas: Map[String, Stanza]): (Option[String], Labels)
  def nextFlow: Option[(String, Labels)]
  def activeFlow: Option[FlowStage]
  def continuationPool: Map[String, Stanza]

  // Persistence access
  def flowStack: List[FlowStage]
  def poolUpdates: Map[String, Stanza]  // Changes to initial pool
}

// Timescale defns
trait TimescaleDefns {
  def timescaleDays(id: String): Option[Int]
}

trait Messages {
  def msg(id: String, param: Seq[Any] = Seq.empty[Any]): String
}

trait Mode {
  val runMode: RunMode
}

trait Encrypter {
  def encrypt(text: String): String
}

trait Labels extends Flows with TimescaleDefns with Messages with Mode with Encrypter {
  def value(name: String): Option[String]
  def valueAsList(name: String): Option[List[String]]
  def displayValue(name: String)(implicit lang: Lang): Option[String]
  def displayListValue(name: String)(implicit lang: Lang): Option[List[String]]
  def update(name: String, english: String): Labels
  def update(name: String, english: String, welsh: String): Labels
  def updateList(name: String, english: List[String]): Labels
  def updateList(name: String, english: List[String], welsh: List[String]): Labels

  // Persistence access
  def updatedLabels: Map[String, Label]
  def labelMap:Map[String, Label]
  def flush(): Labels
}

object IdentityEncrypter extends Encrypter {
  def encrypt(plaintext: String): String = plaintext
}

private[ocelot] class LabelCacheImpl(labels: Map[String, Label],
                                     cache: Map[String, Label],
                                     stack: List[FlowStage],
                                     pool: Map[String, Stanza],
                                     poolCache: Map[String, Stanza],
                                     timescales: Map[String, Int],
                                     messages: (String, Seq[Any]) => String,
                                     val runMode: RunMode,
                                     encrypter: Encrypter) extends Labels {

  // Labels
  def value(name: String): Option[String] = label(name).collect{case s: ScalarLabel => s.english.headOption.getOrElse("")}
  def valueAsList(name: String): Option[List[String]] = label(name).collect{case l: ListLabel => l.english}
  def displayValue(name: String)(implicit lang: Lang): Option[String] = label(name).map{lbl =>
    lang.code match {
      case "cy" if lbl.welsh.nonEmpty => lbl.welsh.mkString(",")
      case _ => lbl.english.mkString(",")
    }
  }
  def displayListValue(name: String)(implicit lang: Lang): Option[List[String]] = label(name).map{lbl =>
    lang.code match {
      case "cy" if lbl.welsh.nonEmpty => lbl.welsh
      case _ => lbl.english
    }
  }
  def update(name: String, english: String): Labels =
    new LabelCacheImpl(labels, updateOrAddScalarLabel(name, english, None), stack, pool, poolCache, timescales, messages, runMode, encrypter)
  def update(name: String, english: String, welsh: String): Labels =
    new LabelCacheImpl(labels, updateOrAddScalarLabel(name, english, Some(welsh)), stack, pool, poolCache, timescales, messages, runMode, encrypter)
  def updateList(name: String, english: List[String]): Labels =
    new LabelCacheImpl(labels, updateOrAddListLabel(name, english), stack, pool, poolCache, timescales, messages, runMode, encrypter)
  def updateList(name: String, english: List[String], welsh: List[String]): Labels =
    new LabelCacheImpl(labels, updateOrAddListLabel(name, english, welsh), stack, pool, poolCache, timescales, messages, runMode, encrypter)

  // Persistence access
  def updatedLabels: Map[String, Label] = cache
  def labelMap:Map[String, Label] = labels
  def flush(): Labels = new LabelCacheImpl(labels ++ cache.toList, Map(), stack, pool, poolCache, timescales, messages, runMode, encrypter)

  // Label ops
  private def label(name: String): Option[Label] = cache.get(name).fold(labels.get(name))(Some(_))

  private def updateOrAddScalarLabel(name: String, english: String, welsh: Option[String]): Map[String, Label] =
    cache + (name -> cache.get(name).fold[Label]
      (ScalarLabel(name, List(english), welsh.fold[List[String]](Nil)(w => List(w))))
      (l => ScalarLabel(l.name, List(english), welsh.fold[List[String]](Nil)(w => List(w)))))

  private def updateOrAddListLabel(name: String, english: List[String], welsh: List[String] = Nil): Map[String, Label] =
    cache + (name -> cache.get(name).fold[Label](ListLabel(name, english, welsh))(l => ListLabel(l.name, english, welsh)))

  // Flows
  def pushFlows(flowNext: List[String],
                continue: String,
                labelName: Option[String], // Flow label
                labelValues: List[Phrase], // Flow label values assigned to the flow label when traversing the associated flow
                stanzas: Map[String, Stanza]): (Option[String], Labels) =
    flowNext.zipWithIndex.map{
      case (nxt, idx) => Flow(nxt, labelName.map(LabelValue(_, labelValues(idx))))
    } match {
      case Nil => (Some(continue), this)
      case x :: xs =>
        (Some(x.next),
        x.labelValue.fold(
          new LabelCacheImpl(labels, cache, x :: xs ++ (Continuation(continue) :: stack), pool, poolCache ++ stanzas, timescales, messages, runMode, encrypter)
        )
        (lv => new LabelCacheImpl(labels,
                                  updateOrAddScalarLabel(lv.name, lv.value.english, Some(lv.value.welsh)),
                                  x :: xs ++ (Continuation(continue) :: stack),
                                  pool,
                                  poolCache ++ stanzas,
                                  timescales,
                                  messages,
                                  runMode,
                                  encrypter))
        )
    }

  def nextFlow: Option[(String, Labels)] = // Remove head of flow stack and update flow label if required
    stack match {
      case Nil => None
      case _ :: Nil => None
      case _ :: (y: Flow) :: xs =>
        Some(
          (y.next,
           y.labelValue.fold(new LabelCacheImpl(labels, cache, stack.tail, pool, poolCache, timescales, messages, runMode, encrypter))
                               (lv => new LabelCacheImpl(labels,
                                                         updateOrAddScalarLabel(lv.name, lv.value.english, Some(lv.value.welsh)),
                                                         y :: xs,
                                                         pool,
                                                         poolCache,
                                                         timescales,
                                                         messages,
                                                         runMode,
                                                         encrypter)))
        )
      case _ :: (c: Continuation) :: xs => Some((c.next, new LabelCacheImpl(labels, cache, xs, pool, poolCache, timescales, messages, runMode, encrypter)))
    }

  def activeFlow: Option[FlowStage] = stack.headOption
  def continuationPool: Map[String, Stanza] = pool ++ poolCache

  // Persistence access
  def flowStack: List[FlowStage] = stack
  def poolUpdates: Map[String, Stanza] = poolCache

  // Timescales defns
  def timescaleDays(id: String): Option[Int] = timescales.get(id)

  // Messages
  def msg(id: String, param: Seq[Any]): String = messages(id, param)

  // Encrypter
  def encrypt(text: String): String = encrypter.encrypt(text)
}

object LabelCache {
  // TEST only
  def apply(): Labels = new LabelCacheImpl(Map(),Map(), Nil,Map(),Map(),Map(),(_,_) => "", Published, IdentityEncrypter)
  def apply(labels: List[Label]): Labels =
    new LabelCacheImpl(labels.map(l => (l.name -> l)).toMap,Map(), Nil,Map(),Map(),Map(),(_,_) => "", Published, IdentityEncrypter)
  def apply(labels: Map[String, Label]): Labels = new LabelCacheImpl(labels,Map(), Nil,Map(),Map(),Map(),(_,_) => "", Published, IdentityEncrypter)
  def apply(labels: Map[String, Label], cache: Map[String, Label]): Labels =
    new LabelCacheImpl(labels, cache,Nil, Map(),Map(),Map(), (_,_) => "", Published, IdentityEncrypter)

  def apply(labels: Map[String, Label],
            cache: Map[String, Label],
            stack: List[FlowStage],
            pool: Map[String, Stanza],
            timescales: Map[String, Int],
            messages: (String, Seq[Any]) => String,
            runMode: RunMode,
            encrypter: Encrypter): Labels = new LabelCacheImpl(labels, cache, stack, pool, Map(), timescales, messages, runMode, encrypter)
}
