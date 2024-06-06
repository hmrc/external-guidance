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

package core.services

import core.models.ocelot.stanzas.{Stanza, CalculationStanza, ValueStanza, ChoiceStanza}
import core.models.ocelot.{Phrase, Process}

trait LabelledDataExpansion{
  def expand(text: String, process: Process): String
}

trait LabelledDataReferencing {
  def referencedIds(p: Seq[Phrase]): List[String] = p.toList.flatMap(referencedIds)
  def referencedNonPhraseIds(flow: Map[String, Stanza]): List[String] =
    flow.values.toList.flatMap{
      case s: CalculationStanza => referencedIds(s.calcs.toList.flatMap(c => List(c.left, c .right)))
      case s: ValueStanza => referencedIds(s.values.toList.flatMap(c => List(c.value)))
      case s: ChoiceStanza => referencedIds(s.tests.toList.flatMap(c => List(c.left, c .right)))
      case _ => Nil
    }.distinct

  private[services] def referencedIds(s: String): List[String]

  private[services] def referencedIds(p: Phrase): List[String] = (referencedIds(p.english) ++ referencedIds(p.welsh)).distinct
  private[services] def referencedIds(stringList: List[String]): List[String] = stringList.flatMap(referencedIds)
}
