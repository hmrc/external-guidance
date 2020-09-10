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
import models.ocelot.{Page, Process}
import play.api.Logger
import scala.annotation.tailrec

case class KeyedStanza(key: String, stanza: Stanza)

@Singleton
class PageBuilder extends ProcessPopulation {
  val logger: Logger = Logger(this.getClass)

  private val pageLinkRegex = s"\\[link:.+?:(\\d+|${Process.StartStanzaId})\\]".r
  private val hintRegex = "\\[hint:([^\\]])+\\]".r

  private def pageLinkIds(str: String): Seq[String] = pageLinkRegex.findAllMatchIn(str).map(_.group(1)).toList

  def buildPage(key: String, process: Process): Either[GuidanceError, Page] = {

    @tailrec
    def collectStanzas(key: String, acc: Seq[KeyedStanza], linkedAcc: Seq[String]): Either[GuidanceError, (Seq[KeyedStanza], Seq[String], Seq[String])] =
      stanza(key, process) match {
        case Right(q: Question) => Right((acc :+ KeyedStanza(key, q), q.next, linkedAcc))
        case Right(EndStanza) => Right((acc :+ KeyedStanza(key, EndStanza), Nil, linkedAcc))
        case Right(p: PageStanza) if acc.nonEmpty => Right((acc, acc.last.stanza.next, linkedAcc))
        case Right(p: PageStanza) => collectStanzas(p.next.head, acc :+ KeyedStanza(key, p), linkedAcc)
        case Right(i: Instruction) => collectStanzas(i.next.head, acc :+ KeyedStanza(key, i), linkedAcc ++ pageLinkIds(i.text.langs.head) ++ i.linkIds)
        case Right(c: Callout) => collectStanzas(c.next.head, acc :+ KeyedStanza(key, c), linkedAcc)
        case Right(v: ValueStanza) => collectStanzas(v.next.head, acc :+ KeyedStanza(key, v), linkedAcc)
        case Right(cs: CalculationStanza) => Left(UnknownStanza(key, "CalculationStanza"))
        case Right(v: ChoiceStanza) => Left(UnknownStanza(key, "ChoiceStanza"))
        case Right(i: InputStanza) => Left(UnknownStanza(key, "InputStanza"))
        case Left(err) => Left(err)
      }

    collectStanzas(key, Nil, Nil) match {
      case Right((ks, next, linked)) =>
        ks.head.stanza match {
          case p: PageStanza if p.url.isEmpty || p.url.equals("/") => Left(PageUrlEmptyOrInvalid(ks.head.key))
          case p: PageStanza => Right(Page(ks.head.key, p.url, ks.map(_.stanza), next, linked))
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
