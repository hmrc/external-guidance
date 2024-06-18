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

object Rates {
  val KeySeparator: String = "!"
}

@Singleton
class Rates @Inject() () extends LabelledDataExpansion with LabelledDataReferencing {
  import Rates.KeySeparator
  // Sector       1
  // Rate        2
  // CY (no subrate)         3
  // +-N (no subrate)        4
  // Year(no subrate)        5
  // subrate (with)          6
  // CY (subrate in use).    7
  // +-N (subrate in use).   8
  // Year (subrate in use)   9
  // subrate no year/CY      10

  private val SectorId: Int = 1
  private val RateId: Int = 2
  private val CYNoSubId = 3
  private val OffsetNoSubId = 4
  private val YearNoSubId = 5
  private val SubWithYearId = 6
  private val CYSubId: Int = 7
  private val OffsetSubId: Int = 8
  private val YearSubId: Int = 9
  private val SubOnlyId: Int = 10
  private val NamePattern = "([^:]+)"
  private val CYPattern = "(CY([\\-+]\\d{1,2})?)"
  private val YearPattern = s"(?:${CYPattern}|(\\d{4}))"
  private val RatePattern = s"\\[rate:${NamePattern}:${NamePattern}(?::(?:${YearPattern}|(?:${NamePattern}:${YearPattern})|${NamePattern}))?\\]"
  private val RateRegex: Regex = RatePattern.r

  private val LookupSectorId = 1
  private val LookupRateId = 2
  private val LookupYearId = 3
  private val LookupRateIdRegex: Regex = s"^([^$KeySeparator]+)$KeySeparator([^$KeySeparator]+)(?:$KeySeparator([^$KeySeparator]+))?$$".r
  private val LookupRateIdFixedYearRegex: Regex = s"^([^$KeySeparator]+)$KeySeparator([^$KeySeparator]+)$KeySeparator([^$KeySeparator]+)$$".r

  private val PrefixId: Int = 1
  private val CYId: Int = 2
  private val OffsetId: Int = 3
  private val RateIdCYSuffixRegex: Regex = s"^(.+?)$CYPattern$$".r
  private val TaxYearStartDay: Int = 6
  private val TaxYearStartMonth: Int = 4

  private def matchToKey(grp: Int => Option[String], default: String): String =
    grp(SectorId).fold(default){sector =>
      grp(RateId).fold(default){rate =>
        grp(CYNoSubId).fold{
          grp(YearNoSubId).fold{
            grp(SubWithYearId).fold{
              grp(SubOnlyId).fold(rateId(sector, rate))(sub =>rateId(sector, s"${rate}:${sub}"))
            }{sub =>
              grp(CYSubId).fold(rateId(sector, s"${rate}:${sub}", grp(YearSubId))){_ =>
                grp(OffsetSubId).fold(rateId(sector, s"${rate}:${sub}", Some("CY"))){offset =>
                  rateId(sector, s"${rate}:${sub}", Some(s"CY$offset"))
                }
              }
            }
          }{y => rateId(sector, rate, Some(y))}
        }{_ =>
          grp(OffsetNoSubId).fold(rateId(sector, rate, Some("CY"))){offset =>
            rateId(sector, rate, Some(s"CY$offset"))
          }
        }
      }
    }

  private[services] def referencedIds(s: String): List[String] = RateRegex.findAllMatchIn(s).map(m => matchToKey(matchGroup(m), m.group(0))).toList

  def expand(str: String, process: Process): String =
    RateRegex.replaceAllIn(str, m => process.rates.get(matchToKey(matchGroup(m), m.group(0))).fold(m.group(0))(_.bigDecimal.toPlainString))

  def rateId(s: String, r: String, y: Option[String] = None): String = y.fold(s"$s$KeySeparator$r")(year => s"$s$KeySeparator$r$KeySeparator$year")
  def reverseRateId(id: String): Option[(String, String, Option[String])] =
    LookupRateIdRegex.findFirstMatchIn(id).map(m => (m.group(LookupSectorId), m.group(LookupRateId), Option(m.group(LookupYearId))))
  def reverseRateFixedYearId(id: String): Option[(String, String, String)] =
    LookupRateIdFixedYearRegex.findFirstMatchIn(id).map(m => (m.group(LookupSectorId), m.group(LookupRateId), m.group(LookupYearId)))

  def fullRateId(id: String, tp: TodayProvider): String = {
    def matchKey(grp: Int => Option[String]): Option[(String, Option[String])] =
      grp(PrefixId).fold[Option[(String, Option[String])]](None){prefix =>
        grp(CYId).fold[Option[(String, Option[String])]](Some((prefix, None))){_ => Some((prefix, grp(OffsetId)))}
      }

    RateIdCYSuffixRegex.findFirstMatchIn(id).map{m =>
      matchKey(matchGroup(m)).fold(id){
        case (prefix, dcy) => s"${prefix}${KeySeparator}${taxYear(dcy, tp)}"
      }
    }.getOrElse(id)
  }

  private def taxYear(off: Option[String], tp: TodayProvider): String = {
    val offset:Int = off.fold(0)(_.toInt)
    val candidateStartDate = tp.now.withMonth(TaxYearStartMonth).withDayOfMonth(TaxYearStartDay)
    candidateStartDate.minusYears(-offset + (if (tp.now.isBefore(candidateStartDate)) 1 else 0)).getYear().toString
  }
}
