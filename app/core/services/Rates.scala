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
class Rates @Inject() (tp: TodayProvider) extends LabelledDataExpansion {

  // Page Analysis
  private val SectionId: Int = 1
  private val RateId: Int = 2
  private val YearId: Int = 3
  private val RateRegex: Regex = "\\[rate:([^:]+):([^:]+)(?::(\\d\\d\\d\\d))?\\]".r

  def referencedIds(s: String): List[String] = ??? // TimescaleIdUsageRegex.findAllMatchIn(s).toList.flatMap{m =>
  //   Option(m.group(DateAddTimescaleId)).fold{
  //     Option(m.group(TimescaleId)).fold(List.empty[String]){id => List(id)}
  //   }(id => List(id))
  // }

  def expand(str: String, process: Process): String =
    RateRegex.replaceAllIn(str, m => {
      (Option(m.group(SectionId)), Option(m.group(RateId)), Option(m.group(YearId))) match {
        case (Some(s), Some(r), None) => process.rates.get(rateId(s, r, tp.now.getYear.toString)).fold(m.group(0))(_.bigDecimal.toPlainString)
        case (Some(s), Some(r), Some(y)) => process.rates.get(rateId(s, r, y)).fold(m.group(0))(_.bigDecimal.toPlainString)
        case _ => m.group(0)
      }
    })

  private def rateId(s: String, r: String, y: String): String = s"$s-$r-$y"
}
