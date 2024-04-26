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
import core.models.ocelot.stanzas._
import core.models.ocelot.errors._
import play.api.Logger
import scala.annotation.tailrec

@Singleton
class PageBuilder @Inject() (val timescales: Timescales) extends ProcessPopulation(timescales) {
  val logger: Logger = Logger(getClass)

  def buildPage(key: String, process: Process): Either[GuidanceError, Page] = {

    @tailrec
    def collectStanzas(keys: List[String],
                       pageStanza: Option[PageStanza] = None,
                       ids: Seq[String] = Nil,
                       stanzas: Seq[PopulatedStanza] = Nil,
                       next: Seq[String] = Nil,
                       endFound: Boolean = false): Either[GuidanceError, (Option[PageStanza], Seq[String], Seq[PopulatedStanza], Seq[String], Boolean)] =
      keys match {
        case Nil => Right((pageStanza, ids, stanzas, next, endFound))                                        // End Page
        case id :: xs if ids.contains(id) => collectStanzas(xs, pageStanza, ids, stanzas, next, endFound)    // Already encountered, possibly more paths
        case id :: xs =>
          (stanza(id, process), xs ) match {
            case (Right(_: PageStanza), _) if ids.nonEmpty => collectStanzas(xs, pageStanza, ids, stanzas, id +: next, endFound) // End, possibly more paths
            case (Right(s: PageStanza), _) => collectStanzas(xs ++ s.next, Some(s), ids :+ id, stanzas :+ s, next, endFound)     // Beginning of page
            case (Right(EndStanza), _) => collectStanzas(xs, pageStanza, ids :+ id, stanzas :+ EndStanza, next, endFound = true) // End, possibly more paths
            case (Right(_: PopulatedStanza), _) if ids.isEmpty => Left(PageStanzaMissing(id))                                    // No PageStanza at start
            case (Right(s: PopulatedStanza), _) => collectStanzas(xs ++ s.next, pageStanza, ids :+ id, stanzas :+ s, next, endFound) // Within-page stanza
            case (Left(err), _) => Left(err)
          }
      }

    collectStanzas(List(key)) match {
      case Right((Some(p), ids, _, _, _)) if p.url.isEmpty || p.url.equals("/") => Left(PageUrlEmptyOrInvalid(ids.head))
      case Right((Some(p), ids, stanzas, next, endPage)) =>
        val ks: Seq[KeyedStanza] = ids.zip(stanzas).map(t => KeyedStanza(t._1, t._2))
        Right(Page(ks.head.key, p.url, ks, next, endPage))
      case Left(err) => Left(err)
      case Right((None, _, _, _, _)) => Left(PageStanzaMissing(key))
    }
  }

  def pages(process: Process, start: String = Process.StartStanzaId): Either[List[GuidanceError], List[Page]] =
    pagesByKeys(List(start), Nil)(process) match {
      case Left(err) => Left(List(err))
      case Right(pages) => Right(pages)
    }

  @tailrec
  private def pagesByKeys(keys: List[String], acc: List[Page])(implicit process: Process): Either[GuidanceError, List[Page]] =
    keys match {
      case Nil => Right(acc)
      case key :: xs if !acc.exists(_.id == key) =>
        buildPage(key, process) match {
          case Right(page) =>
            pagesByKeys(page.next.toList ++ xs ++ page.linked, acc :+ page)
          case Left(err) =>
            logger.error(s"Page building failed with error - $err")
            Left(err)
        }
      case _ :: xs => pagesByKeys(xs, acc)
    }
}
