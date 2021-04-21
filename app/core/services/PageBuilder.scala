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

package core.services

import javax.inject.{Inject, Singleton}
import core.models.ocelot._
import core.models.ocelot.stanzas._
import core.models.ocelot.errors._
import play.api.Logger
import scala.annotation.tailrec

@Singleton
class PageBuilder @Inject() (val placeholders: Placeholders) extends ProcessPopulation with PlaceholderProvider {
  val logger: Logger = Logger(this.getClass)

  def buildPage(key: String, process: Process): Either[GuidanceError, Page] = {

    @tailrec
    def collectStanzas(keys: Seq[String],
                       pageStanza: Option[PageStanza] = None,
                       ids: Seq[String] = Nil,
                       stanzas: Seq[Stanza] = Nil,
                       next: Seq[String] = Nil): Either[GuidanceError, (Option[PageStanza], Seq[String], Seq[Stanza], Seq[String])] =
      keys match {
        case Nil => Right((pageStanza, ids, stanzas, next))                                                  // End Page
        case key +: xs if ids.contains(key) => collectStanzas(xs, pageStanza, ids, stanzas, next)            // Already encountered, but potentially more paths
        case key +: xs =>
          (stanza(key, process), xs ) match {
            case (Right(s: PageStanza), _) if ids.nonEmpty => collectStanzas(xs, pageStanza, ids, stanzas, key +: next) // End page but potentially more paths
            case (Right(s: PageStanza), _) => collectStanzas(xs ++ s.next, Some(s), ids :+ key, stanzas :+ s, next)     // Beginning of page
            case (Right(EndStanza), _) => collectStanzas(xs, pageStanza, ids :+ key, stanzas :+ EndStanza, next)        // End page but potentially more paths
            case (Right(s: Stanza), _) if ids.isEmpty => Left(PageStanzaMissing(key))                                   // None PageStanza at start of page
            case (Right(s: Stanza), _) => collectStanzas(xs ++ s.next, pageStanza, ids :+ key, stanzas :+ s, next)      // None PageStanza within page
            case (Left(err), _) => Left(err)
          }
      }

    collectStanzas(List(key)) match {
      case Right((Some(p), ids, _, _)) if p.url.isEmpty || p.url.equals("/") => Left(PageUrlEmptyOrInvalid(ids.head))
      case Right((Some(p), ids, stanzas, next)) =>
        val ks: Seq[KeyedStanza] = ids.zip(stanzas).map(t => KeyedStanza(t._1, t._2))
        Right(Page(ks.head.key, p.url, ks, next))
      case Left(err) => Left(err)
    }
  }

  def pages(process: Process, start: String = Process.StartStanzaId): Either[List[GuidanceError], Seq[Page]] =
    pagesByKeys(List(start), Nil)(process) match {
      case Left(err) => Left(List(err))
      case Right(pages) => Right(pages)
    }

  @tailrec
  private def pagesByKeys(keys: Seq[String], acc: Seq[Page])(implicit process: Process): Either[GuidanceError, Seq[Page]] =
    keys match {
      case Nil => Right(acc)
      case key +: xs if !acc.exists(_.id == key) =>
        buildPage(key, process) match {
          case Right(page) =>
            pagesByKeys(page.next ++ xs ++ page.linked, acc :+ page)
          case Left(err) =>
            logger.error(s"Page building failed with error - $err")
            Left(err)
        }
      case _ +: xs => pagesByKeys(xs, acc)
    }
}
