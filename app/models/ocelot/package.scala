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

package models

import scala.util.matching.Regex

package object ocelot {
  val hintRegex = "\\[hint:([^\\]])+\\]".r
  val pageLinkRegex = s"\\[link:.+?:(\\d+|${Process.StartStanzaId})\\]".r
  val labelRefRegex = s"\\[label:([0-9a-zA-Z\\s+_]+)\\]".r
  val currencyRegex = "\\d+(\\.(\\d{2})?)?".r // From Ocelot


  def plSingleGroupCaptures(regex: Regex, str: String): List[String] = regex.findAllMatchIn(str).map(_.group(1)).toList
  def pageLinkIds(str: String): List[String] = plSingleGroupCaptures(pageLinkRegex, str)
  def labelReferences(str: String): List[String] = plSingleGroupCaptures(labelRefRegex, str)
  def labelReference(str: String): Option[String] = plSingleGroupCaptures(labelRefRegex, str).headOption
  def isCurrency(str: String): Boolean = currencyRegex.findFirstIn(str).fold(false)(_=> true)
}
