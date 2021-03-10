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

  def pages(process: Process, start: String = Process.StartStanzaId): Either[List[GuidanceError], Seq[Page]] = {
    @tailrec
    def pagesByKeys(keys: Seq[String], acc: Seq[Page]): Either[GuidanceError, Seq[Page]] =
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

    pagesByKeys(List(start), Nil) match {
      case Left(err) => Left(List(err))
      case Right(pages) => Right(pages)
    }
  }

  def pagesWithValidation(process: Process, start: String = Process.StartStanzaId): Either[List[GuidanceError], Seq[Page]] =
    pages(process, start).fold[Either[List[GuidanceError], Seq[Page]]](Left(_),
      pages => {
        checkQuestionPages(pages, Nil) ++
        duplicateUrlErrors(pages.reverse, Nil) ++
        //detectSharedStanzaUsage(pages) ++
        detectUnsupportedPageRedirect(pages) match {
          case Nil => Right(pages.head +: pages.tail.sortWith((x,y) => x.id < y.id))
          case errors =>
            Left(errors)
        }
      }
    )

  def fromPageDetails[A](pages: Seq[Page])(f: (String, String, String) => A): List[A] =
    pages.toList.flatMap { page =>
      page.stanzas.collectFirst {
        case TitleCallout(text, _, _) =>
          f(page.id, page.url, text.english)
        case YourCallCallout(text, _, _) =>
          f(page.id, page.url, text.english)
        case i: Input =>
          f(page.id, page.url, hintRegex.replaceAllIn(i.name.english, ""))
        case i: Question =>
          f(page.id, page.url, hintRegex.replaceAllIn(i.text.english, ""))
        case i: Sequence =>
          f(page.id, page.url, hintRegex.replaceAllIn(i.text.english, ""))
      }
    }

  private def detectSharedStanzaUsage(pages: Seq[Page]): Seq[GuidanceError] = {
    val dataInputByPage: Seq[(String, Seq[String])] = pages.map(p => (p.id, p.keyedStanzas.collect{case KeyedStanza(id, _: DataInput) => id}))
    dataInputByPage.flatMap(_._2).distinct.flatMap{ id =>
      dataInputByPage.collect{case(pId, stanzas) if stanzas.contains(id) => (id, pId)}
    }.groupBy(t => t._1)
     .collect{case (k, p) if p.length > 1 => SharedDataInputStanza(k, p.map(_._2))}
     .toSeq
  }

  private def detectUnsupportedPageRedirect(pages: Seq[Page]): Seq[GuidanceError] = {
    val pageIds = pages.map(_.id)

    @tailrec
    def traverse(keys: Seq[String], page: Map[String, Stanza], seen: List[String]): Option[String] =
      keys match {
        case Nil => None
        case x +: xs if seen.contains(x) => traverse(xs, page, seen)
        case x +: xs => page.get(x) match {
          case None => traverse(xs, page, x :: seen)
          case Some(_: DataInput) => traverse(xs, page, x :: seen)
          case Some(s: Choice) if s.next.exists(n => pageIds.contains(n)) => Some(x)
          case Some(s: Stanza) => traverse(s.next ++ xs, page, x :: seen)
        }
      }

    pages.flatMap{p =>
      traverse(Seq(p.id), p.keyedStanzas.map(ks => (ks.key, ks.stanza)).toMap, Nil).map(id => PageRedirectNotSupported(id))
    }
  }

  @tailrec
  private def duplicateUrlErrors(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x +: xs if xs.exists(_.url == x.url) => duplicateUrlErrors(xs, DuplicatePageUrl(x.id, x.url) :: errors)
      case _ +: xs => duplicateUrlErrors(xs, errors)
    }

  @tailrec
  private def checkQuestionFollowers(p: Seq[String], keyedStanzas: Map[String, Stanza], seen: Seq[String]): List[GuidanceError] =
    p match {
      case Nil => Nil
      case x +: xs if seen.contains(x) => checkQuestionFollowers(xs, keyedStanzas, seen)
      case x +: xs if !keyedStanzas.contains(x) => checkQuestionFollowers(xs, keyedStanzas, x +: seen)
      case x +: _ if keyedStanzas.contains(x) && keyedStanzas(x).visual => List(VisualStanzasAfterQuestion(x))
      case x +: xs => checkQuestionFollowers(keyedStanzas(x).next ++ xs, keyedStanzas, x +: seen)
    }

  @tailrec
  private def checkQuestionPages(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x +: xs =>
        x.keyedStanzas.find(
          _.stanza match {
            case q: Question => true
            case _ => false
        }) match {
          case None => checkQuestionPages(xs, errors)
          case Some(q) =>
            val anyErrors = checkQuestionFollowers(q.stanza.next, x.keyedStanzas.map(k => (k.key, k.stanza)).toMap, Nil)
            checkQuestionPages(xs, anyErrors ++ errors)
        }
    }
}
