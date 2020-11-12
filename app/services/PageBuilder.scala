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

package services

import javax.inject.Singleton
import models.ocelot._
import models.ocelot.stanzas._
import models.ocelot.errors._
import play.api.Logger
import scala.annotation.tailrec

@Singleton
class PageBuilder extends ProcessPopulation {
  val logger: Logger = Logger(this.getClass)


  def buildPage(key: String, process: Process): Either[GuidanceError, Page] = {

    @tailrec
    def collectStanzas(keys: Seq[String],
                       ids: Seq[String],
                       stanzas: Seq[Stanza],
                       next: Seq[String]): Either[GuidanceError, (Seq[String], Seq[Stanza], Seq[String])] =
      keys match {
        case Nil => Right((ids, stanzas, next))                                                  // End Page
        case key :: xs if ids.contains(key) => collectStanzas(xs, ids, stanzas, next)            // Already encountered, but potentially more paths
        case key :: xs =>
          (stanza(key, process), xs ) match {
            case (Right(s: PageStanza), _) if ids.nonEmpty => collectStanzas(xs, ids, stanzas, key +: next) // End page but potentially more paths
            case (Right(s: PageStanza), _) => collectStanzas(xs ++ s.next, ids :+ key, stanzas :+ s, next)  // Beginning of page
            case (Right(EndStanza), _) => collectStanzas(xs, ids :+ key, stanzas :+ EndStanza, next)        // End page but potentially more paths
            case (Right(s: Stanza), _) if ids.isEmpty => Left(PageStanzaMissing(key))
            case (Right(s: Stanza), _) => collectStanzas(xs ++ s.next, ids :+ key, stanzas :+ s, next)
            case (Left(err), _) => Left(err)
          }
      }

    collectStanzas(List(key), Nil, Nil, Nil) match {
      case Right((ids, stanzas, next)) =>
        val ks: Seq[KeyedStanza] = ids.zip(stanzas).map(t => KeyedStanza(t._1, t._2))
        ks.head.stanza match {
          case p: PageStanza if p.url.isEmpty || p.url.equals("/") => Left(PageUrlEmptyOrInvalid(ks.head.key))
          case p: PageStanza => Right(Page(ks.head.key, p.url, ks, next))
          case _ => Left(PageStanzaMissing(ks.head.key))
        }
      case Left(err) => Left(err)
    }
  }

  @tailrec
  private def duplicateUrlErrors(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x :: xs if xs.exists(_.url == x.url) => duplicateUrlErrors(xs, DuplicatePageUrl(x.id, x.url) :: errors)
      case x :: xs => duplicateUrlErrors(xs, errors)
    }

  @tailrec
  private def checkQuestionFollowers(p: Seq[String], keyedStanzas: Map[String, Stanza], seen: Seq[String]): List[GuidanceError] =
    p match {
      case Nil => Nil
      case x :: xs if seen.contains(x) => checkQuestionFollowers(xs, keyedStanzas, seen)
      case x :: xs if !keyedStanzas.contains(x) => checkQuestionFollowers(xs, keyedStanzas, seen)
      case x :: xs if keyedStanzas.contains(x) && keyedStanzas(x).visual => List(VisualStanzasAfterQuestion(x))
      case x :: xs => checkQuestionFollowers(keyedStanzas(x).next ++ xs, keyedStanzas, seen)
    }

  @tailrec
  private def checkQuestionPages(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x :: xs =>
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

  def pagesWithValidation(process: Process, start: String = Process.StartStanzaId): Either[List[GuidanceError], Seq[Page]] =
    pages(process, start).fold[Either[List[GuidanceError], Seq[Page]]]( e => Left(e), pages => {
        checkQuestionPages(pages, Nil) ++ duplicateUrlErrors(pages.reverse, Nil) match {
          case Nil => Right(pages.headOption.fold(Seq.empty[Page])(h => h +: pages.tail.sortWith((x,y) => x.id < y.id)))
          case duplicates => Left(duplicates)
        }
      }
    )

  def pages(process: Process, start: String = Process.StartStanzaId): Either[List[GuidanceError], Seq[Page]] = {
    @tailrec
    def pagesByKeys(keys: Seq[String], acc: Seq[Page]): Either[GuidanceError, Seq[Page]] =
      keys match {
        case Nil => Right(acc)
        case key :: xs if !acc.exists(_.id == key) =>
          buildPage(key, process) match {
            case Right(page) =>
              pagesByKeys(page.next ++ xs ++ page.linked, acc :+ page)
            case Left(err) =>
              logger.error(s"Page building failed with error - $err")
              Left(err)
          }
        case _ :: xs => pagesByKeys(xs, acc)
      }

    pagesByKeys(List(start), Nil) match {
      case Left(err) => Left(List(err))
      case Right(pages) => Right(pages)
    }
  }

  def fromPageDetails[A](pages: Seq[Page])(f: (String, String, String) => A): List[A] =
    pages.toList.flatMap { page =>
      page.stanzas.collectFirst {
        case Callout(Title, text, _, _) =>
          f(page.id, page.url, text.langs(0))
        case Callout(YourCall, text, _, _) =>
          f(page.id, page.url, text.langs(0))
        case q: Question =>
          f(page.id, page.url, hintRegex.replaceAllIn(q.text.langs(0), ""))
        case i: Input =>
          f(page.id, page.url, hintRegex.replaceAllIn(i.name.langs(0), ""))
      }
    }
}
