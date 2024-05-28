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

import javax.inject.{Inject, Singleton}
import core.models.ocelot._
import scala.util.matching.Regex

@Singleton
class Rates @Inject() () extends LabelledDataExpansion {
  private val SectionId: Int = 1
  private val RateId: Int = 2
  private val YearId: Int = 3
  private val RateRegex: Regex = "\\[rate:([^:]+):([^:]+)(?::(\\d\\d\\d\\d))?\\]".r

  private[services] def referencedIds(s: String): List[String] =
    RateRegex.findAllMatchIn(s).toList.flatMap{m =>
      (Option(m.group(SectionId)), Option(m.group(RateId)), Option(m.group(YearId))) match {
        case (Some(s), Some(r), None) => List(rateId(s, r))
        case (Some(s), Some(r), Some(y)) => List(rateId(s, r, Some(y)))
        case _ => Nil
      }
    }

  def expand(str: String, process: Process): String =
    RateRegex.replaceAllIn(str, m => {
      (Option(m.group(SectionId)), Option(m.group(RateId)), Option(m.group(YearId))) match {
        case (Some(s), Some(r), year) => process.rates.get(rateId(s, r, year)).fold(m.group(0))(_.bigDecimal.toPlainString)
        case _ => m.group(0)
      }
    })

  private def rateId(s: String, r: String, y: Option[String] = None): String = y.fold(s"$s-$r")(year => s"$s-$r-$year")
}
