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

package services

import javax.inject.{Inject, Singleton}
import core.services.PageBuilder
import core.models.ocelot._
import core.models.ocelot.stanzas._
import core.models.ocelot.errors._
import play.api.Logger
import scala.annotation.tailrec

case class PageVertex(id: String, url: String, next: Seq[String], flows: List[String], links: Seq[String])

object PageVertex {
  def apply(p: Page): PageVertex = {
    val (next, flows) = p.stanzas.collectFirst{
      case s: Sequence =>
        val flws: List[String] = s.next.init.toList
        (p.next.filterNot(flws.contains(_)), flws)
    }.getOrElse((p.next, Nil))
    PageVertex(p.id, p.url, next, flows, p.linked)
  }
}

@Singleton
class ValidatingPageBuilder @Inject() (pageBuilder: PageBuilder){
  val logger: Logger = Logger(getClass)
  val ReservedUrls: List[String] = List("/session-timeout", "/session-restart")

  def pagesWithValidation(process: Process, start: String = Process.StartStanzaId): Either[List[GuidanceError], Seq[Page]] = {

    pageBuilder.pages(process, start).fold[Either[List[GuidanceError], Seq[Page]]](Left(_),
      pages => {
        implicit val stanzaMap: Map[String, Stanza] = process.flow
        val vertices: List[PageVertex] = pages.map(PageVertex(_)).toList
        checkForSequencePageReuse(vertices) ++
        checkQuestionPages(pages, Nil) ++
        duplicateUrlErrors(pages.reverse, Nil) ++
        checkDateInputErrorCallouts(pages, Nil) ++
        checkExclusiveSequenceTypeError(pages, Nil) ++
        checkExclusiveSequencePages(pages, Nil) ++
        checkForUseOfReservedUrls(pages, Nil) ++
        detectUnsupportedPageRedirect(pages) match {
          case Nil => Right(pages.head +: pages.tail.sortWith((x,y) => x.id < y.id))
          case errors => Left(errors)
        }
      }
    )
  }

  @tailrec
  private def checkDateInputErrorCallouts(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] = {
    // Sufficient: 3 stacked callouts with messages containing 0,1 and 2 embedded parameters
    def checkCalloutSufficiency(p: Page): List[GuidanceError] = {
      val callouts: Seq[ErrorCallout] = p.keyedStanzas
                                         .map(_.stanza)
                                         .collect{case e: ErrorCallout => e}

      callouts
       .map(co => EmbeddedParameterRegex.findAllIn(co.text.english).length)
       .sorted match {
          case List(0,1,2) if callouts(1).stack && callouts(2).stack => Nil
          case _ => List(IncompleteDateInputPage(p.id))
       }
    }

    pages match {
      case Nil => errors
      case p +: xs => p.keyedStanzas.find(
          _.stanza match {
            case _: DateInput => true
            case _ => false
        }) match {
        case Some(_) => checkDateInputErrorCallouts(xs, checkCalloutSufficiency(p) ++ errors)
        case None => checkDateInputErrorCallouts(xs, errors)
      }
    }
  }

  private def detectUnsupportedPageRedirect(pages: Seq[Page]): Seq[GuidanceError] = {
    val pageIds = pages.map(_.id)

    @tailrec
    def traverse(keys: Seq[String], pageStanzas: Map[String, Stanza], seen: List[String]): Option[String] =
      keys match {
        case Nil => None
        case x +: xs if seen.contains(x) => traverse(xs, pageStanzas, seen)
        case x +: xs => pageStanzas.get(x) match {
          case None => traverse(xs, pageStanzas, x :: seen)
          case Some(_: DataInput) => traverse(xs, pageStanzas, x :: seen)
          case Some(s: Choice) if s.next.exists(n => pageIds.contains(n)) => Some(x)
          case Some(s: Stanza) => traverse(s.next ++ xs, pageStanzas, x :: seen)
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
  private def checkForUseOfReservedUrls(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x :: xs if ReservedUrls.contains(x.url) => checkForUseOfReservedUrls(xs, UseOfReservedUrl(x.id) :: errors)
      case _ :: xs => checkForUseOfReservedUrls(xs, errors)
    }

  @tailrec
  private def checkQuestionPages(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x +: xs =>
        x.keyedStanzas.find(
          _.stanza match {
            case _: Question => true
            case _ => false
        }) match {
          case None => checkQuestionPages(xs, errors)
          case Some(q) =>
            val anyErrors = checkQuestionFollowers(q.stanza.next, x.keyedStanzas.map(k => (k.key, k.stanza)).toMap, Nil)
            checkQuestionPages(xs, anyErrors ++ errors)
        }
    }

  @tailrec
  private def checkExclusiveSequenceTypeError(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case p :: xs if p.stanzas.collectFirst{case _: ExclusiveSequence => ()}.fold(false)(_ => true) &&
                      p.stanzas.collectFirst{case _: TypeErrorCallout => ()}.fold(true)(_ => false) =>
        checkExclusiveSequenceTypeError(xs, IncompleteExclusiveSequencePage(p.id) :: errors)
      case _ :: xs =>
        checkExclusiveSequenceTypeError(xs, errors)
    }

  @tailrec
  private def checkExclusiveSequencePages(pages: Seq[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x +: xs => x.keyedStanzas.collectFirst{case KeyedStanza(key, exSeq: ExclusiveSequence) => (key, exSeq)} match {
          case Some((key, exclusiveSequence)) =>
            if(exclusiveSequence.exclusiveOptions.size > 1) {
              checkExclusiveSequencePages(xs, MultipleExclusiveOptions(key) :: errors)
            } else {
              checkExclusiveSequencePages(xs, errors)
            }
          case None => checkExclusiveSequencePages(xs, errors)
        }
    }

  private def checkForSequencePageReuse(connections: List[PageVertex])(implicit stanzaMap: Map[String, Stanza]): List[GuidanceError] = {
    val vertexMap = connections.map(pv => (pv.id, pv)).toMap
    val mainFlow: List[String] = mainFlowPageIds(vertexMap)
    val sequencePageIds = for{
      pv <- connections.filterNot(_.flows.isEmpty)              // Sequences
      flw <- pv.flows                                           // Flow Ids
      p <- pageGraph(findPageIds(List(flw)), vertexMap, mainFlow) // Pages below sequences
    } yield p.id

    sequencePageIds.groupBy(x => x).toList.collect{case m if m._2.length > 1 => PageOccursInMultiplSequenceFlows(m._1)}
  }

  private def mainFlowPageIds(vertexMap: Map[String, PageVertex])(implicit stanzaMap: Map[String, Stanza]): List[String] =
    pageGraph(List(Process.StartStanzaId), vertexMap).map(_.id)

  @tailrec
  // Given a list of stanza ids, find all connected pages (wont follow links)
  private def findPageIds(ids: List[String], seen: List[String] = Nil, acc: List[String] = Nil)(implicit stanzaMap: Map[String, Stanza]): List[String] =
    ids match {
      case Nil => acc
      case x :: xs if seen.contains(x) => findPageIds(xs, seen, acc)
      case x :: xs =>
        stanzaMap(x) match {
          case _: PageStanza => findPageIds(xs, x :: seen, x :: acc)
          case EndStanza => findPageIds(xs, x :: seen, acc)
          case s => findPageIds(s.next.toList.filterNot(seen.contains(_)) ++ xs, x :: seen, acc)
        }
    }

  @tailrec
  // Given a list of page ids, find all connected pages (wont follow links)
  private def pageGraph(keys: List[String],
                        vertices: Map[String, PageVertex],
                        ignore: List[String] = Nil,
                        dontFollowFlows: Boolean = true,
                        seen: List[String] = Nil,
                        acc: List[PageVertex] = Nil)(implicit stanzaMap: Map[String, Stanza]): List[PageVertex] =
    keys match {
      case Nil => acc
      case x :: xs if seen.contains(x) => pageGraph(xs, vertices, ignore, dontFollowFlows, seen, acc)
      case x :: xs if !ignore.contains(x) =>
        val v = vertices(x)
        if (v.flows.isEmpty || dontFollowFlows) {
          pageGraph(xs ++ v.next.filterNot(seen.contains(_)), vertices, ignore, dontFollowFlows, x :: seen, v :: acc)
        } else {
          val flowStartPages = findPageIds(v.flows.filterNot(seen.contains(_)))
          pageGraph(xs ++ v.next.filterNot(seen.contains(_)) ++ flowStartPages, vertices, ignore, dontFollowFlows, x :: seen, v :: acc)
        }
      case x :: xs => pageGraph(xs, vertices, ignore, dontFollowFlows, x :: seen, acc)
    }

}
