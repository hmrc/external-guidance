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
import models.ocelot.stanzas._
import models.ocelot.errors._
import models.ocelot.{Label, Page, Process}
import play.api.Logger
import scala.annotation.tailrec

case class KeyedStanza(key: String, stanza: Stanza)

@Singleton
class PageBuilder extends ProcessPopulation {
  val logger: Logger = Logger(this.getClass)

  def buildPage(key: String, process: Process): Either[GuidanceError, Page] = {
    @tailrec
    def collectStanzas(key: String,
                       acc: Seq[KeyedStanza],
                       labelsAcc: Seq[Label],
                       linkedAcc: Seq[String]): Either[GuidanceError, (Seq[KeyedStanza], Seq[Label], Seq[String])] =
      stanza(key, process) match {
        case Right(s: PageStanza) if acc.nonEmpty => Right((acc, labelsAcc, linkedAcc))
        // Partially enable Calculation and Choice stanzas to allow collection of labels, both will be ignore during build of UI
        // Calculation and Choice stanza will generate if found in the place of a PageStanza e.e. after a Question
        case Right(s: CalculationStanza) if acc.nonEmpty => collectStanzas(s.next.head, acc :+ KeyedStanza(key, s), labelsAcc ++ s.labels, linkedAcc)
        case Right(s: ChoiceStanza) if acc.nonEmpty => collectStanzas(s.next.head, acc :+ KeyedStanza(key, s), labelsAcc ++ s.labels, linkedAcc)
        case Right(s: CalculationStanza) => Left(UnknownStanza(key, "CalculationStanza"))
        case Right(s: ChoiceStanza) => Left(UnknownStanza(key, "ChoiceStanza"))
        case Right(s: Stanza with PageTerminator) => Right((acc :+ KeyedStanza(key, s), labelsAcc, linkedAcc))
        case Right(s: PopulatedStanza) => collectStanzas(s.next.head, acc :+ KeyedStanza(key, s), labelsAcc ++ s.labels, linkedAcc ++ s.links)
        case Right(s: Stanza) => collectStanzas(s.next.head, acc :+ KeyedStanza(key, s), labelsAcc ++ s.labels, linkedAcc)
        case Left(err) => Left(err)
      }

    collectStanzas(key, Nil, Nil, Nil) match {
      case Right((ks, labels, linked)) =>
        ks.head.stanza match {
          case p: PageStanza if p.url.isEmpty || p.url.equals("/") => Left(PageUrlEmptyOrInvalid(ks.head.key))
          case p: PageStanza => Right(Page(ks.head.key, p.url, ks.map(_.stanza), ks.last.stanza.next, linked, labels))
          case _ => Left(PageStanzaMissing(ks.head.key))
        }
      case Left(err) => Left(err)
    }
  }

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

    @tailrec
    def duplicateUrlErrors(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] =
      pages match {
        case Nil => errors
        case x :: xs if xs.exists(_.url == x.url) => duplicateUrlErrors(xs, DuplicatePageUrl(x.id, x.url) :: errors)
        case x :: xs => duplicateUrlErrors(xs, errors)
      }

    pagesByKeys(List(start), Nil).fold(
      err => Left(List(err)),
      pages => {
        duplicateUrlErrors(pages.reverse, Nil) match {
          case Nil => Right(pages)
          case duplicates => Left(duplicates)
        }
      }
    )
  }

  def fromPageDetails[A](pages: Seq[Page])(f: (String, String, String) => A): List[A] =
    pages.toList.flatMap { page =>
      page.stanzas.collectFirst {
        case Callout(Title, text, _, _) =>
          f(page.id, page.url, text.langs(0))
        case q: Question =>
          f(page.id, page.url, hintRegex.replaceAllIn(q.text.langs(0), ""))
      }
    }
}
