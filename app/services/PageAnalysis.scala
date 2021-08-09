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

package services

import core.models.ocelot._
import core.models.ocelot.stanzas._

object PageAnalysis {
  def timescaleIds(s: String): List[String] = TimescaleIdUsageRegex.findAllMatchIn(s).toList.flatMap{m =>
    Option(m.group(3)).fold{
      Option(m.group(4)).fold(List.empty[String]){id => List(id)}
    }(id => List(id))
  }

  def timescaleIds(p: Phrase): List[String] = (timescaleIds((p.english)) ++ timescaleIds((p.welsh))).distinct

  def timescaleIds(page: Page): List[String] =
    page.stanzas.toList.collect{
      case s: Calculation => s.calcs.flatMap(o => timescaleIds(o.left) ++ timescaleIds(o.right))
      case s: Choice => s.tests.flatMap(t => timescaleIds(t.left) ++ timescaleIds(t.right))
      case s: ValueStanza => s.values.flatMap(v => timescaleIds(v.value))
      case s: Input => timescaleIds(s.name) ++ s.help.map(timescaleIds(_)).getOrElse(Nil)
      case s: Instruction => timescaleIds(s.text)
      case s: Sequence => timescaleIds(s.text) ++ s.options.toList.flatMap(timescaleIds(_)) ++ s.exclusive.map(timescaleIds(_)).getOrElse(Nil)
      case s: Question => timescaleIds(s.text) ++ s.answers.toList.flatMap(timescaleIds(_))
      case s: Callout => timescaleIds(s.text)
      case s: Row => s.cells.toList.flatMap(timescaleIds(_))
    }.flatten.distinct
}