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

package services

import javax.inject.{Inject, Singleton}
import core.services.PageBuilder
import core.models.ocelot._
import core.models.ocelot.stanzas._
import core.models.ocelot.errors._
import play.api.Logger
import core.models.{GuidanceCheckLevel, Strict}

import scala.annotation.tailrec

case class PageVertex(
  id: String,
  url: String,
  next: Seq[String],
  flows: List[String],
  links: Seq[String],
  endPage: Boolean = false
)

object PageVertex {
  def apply(p: Page): PageVertex = {
    val (next, flows) = p.stanzas.collectFirst{
      case s: Sequence =>
        val flws: List[String] = s.next.init.toList
        (p.next.filterNot(flws.contains(_)), flws)
    }.getOrElse((p.next, Nil))
    PageVertex(p.id, p.url, next ++ p.buttonLinked, flows, p.linked, p.endPage)
  }
}

@Singleton
class ValidatingPageBuilder @Inject() (val pageBuilder: PageBuilder){
  val logger: Logger = Logger(getClass)

  def pagesWithValidation(process: Process,
                          start: String = Process.StartStanzaId,
                          checkLevel: GuidanceCheckLevel = Strict): Either[List[GuidanceError], Seq[Page]] =
    pageBuilder.pages(process, start).fold[Either[List[GuidanceError], Seq[Page]]](Left(_),
      pages => {
        implicit val stanzaMap: Map[String, Stanza] = process.flow
        val vertices: List[PageVertex] = pages.map(PageVertex(_)).toList
        val vertexMap = vertices.map(pv => (pv.id, pv)).toMap
        val mainFlow: List[String] = pageGraph(List(Process.StartStanzaId), vertexMap).map(_.id)

        checkSequenceErrors(vertices, vertexMap, mainFlow) ++
        (if (checkLevel == Strict) {
          checkDateInputErrorCallouts(pages, Nil).distinct ++
          confirmInputPageErrorCallouts(pages, Nil) ++
          confirmPageTitles(pages, Nil)
        } else Nil) ++
        checkPhraseLinkIdUsage(pages) ++
        checkDataInputPages(pages, Nil) ++
        duplicateUrlErrors(pages.reverse, Nil) ++
        checkExclusiveSequenceTypeError(pages, Nil) ++
        checkForUseOfReservedUrls(pages, Nil) ++
        checkForInvalidLabelNames(pages, Nil) ++
        detectUnsupportedPageRedirect(pages) match {
          case Nil => Right(pages.head +: pages.tail.sortWith((x,y) => x.id < y.id))
          case errors => Left(errors)
        }
      }
    )

  private def checkPhraseLinkIdUsage(pages: List[Page]): List[GuidanceError] =
    pages.flatMap{p =>
      p.keyedStanzas.collect{
        case KeyedStanza(id, s: Question) if !validPhrase(s.text) => LanguageLinkIdsDiffer(id)
        case KeyedStanza(id, s: Instruction) if !validPhrase(s.text) => LanguageLinkIdsDiffer(id)
        case KeyedStanza(id, s: Callout) if !validPhrase(s.text) => LanguageLinkIdsDiffer(id)
        case KeyedStanza(id, s: Sequence) if !validPhrase(s.text) => LanguageLinkIdsDiffer(id)
        case KeyedStanza(id, s: Input) if !validPhrase(s.name) => LanguageLinkIdsDiffer(id)
        case KeyedStanza(id, s: Row) if !s.cells.collect{case cell if !validPhrase(cell) => id}.isEmpty => LanguageLinkIdsDiffer(id)
      }
    }


  private def validPhrase(phrase: Phrase): Boolean =
    pageLinkIds(phrase.english).sorted == pageLinkIds(phrase.welsh).sorted

  private def checkSequenceErrors(vertices: List[PageVertex], vertexMap: Map[String, PageVertex], mainFlow: List[String])
                            (implicit stanzaMap: Map[String, Stanza]): List[GuidanceError] =
    checkForMinimumTwoPageFlows(vertices, vertexMap) match {
      case Nil =>
        Nil
      case errors =>
          checkForSequencePageReuse(vertices, vertexMap, mainFlow) match {
          case Nil =>
            Nil
          case reuseErrors =>
            val seqErrors = reuseErrors.filter(e => errors.exists(x => x.id == e.id))
            errors.filter(e => reuseErrors.exists(x => x.id ==e.id)) ++ seqErrors
        }
    }

  @tailrec
  private def confirmInputPageErrorCallouts(pages: List[Page], errors: List[GuidanceError]): List[GuidanceError] = {

    @tailrec
    def inputCalloutState(stanzas: List[PopulatedStanza],
                          requiredError: Boolean = false,
                          typeError: Boolean = false,
                          input: Option[DataInput] = None):(Boolean, Boolean, Option[DataInput]) =
      stanzas match {
        case Nil => (requiredError, typeError, input)
        case (s: DataInput) :: xs => inputCalloutState(xs, requiredError, typeError, Some(s))
        case (r: ErrorCallout) :: xs => inputCalloutState(xs, requiredError = true, typeError = typeError, input)
        case (t: TypeErrorCallout) :: xs => inputCalloutState(xs, requiredError, typeError = true, input)
        case x :: xs => inputCalloutState(xs, requiredError, typeError, input)
      }

      pages match {
        case Nil => errors
        case x :: xs =>
          inputCalloutState(x.stanzas.toList) match {
            case (_, _, None) => confirmInputPageErrorCallouts(xs, errors)
            case (true, _, Some(x: Question)) => confirmInputPageErrorCallouts(xs, errors)
            case (true, _, Some(x: Sequence)) => confirmInputPageErrorCallouts(xs, errors)
            case (true, _, Some(x: TextInput)) => confirmInputPageErrorCallouts(xs, errors)
            case (true, _, Some(x: DataInput)) => confirmInputPageErrorCallouts(xs, errors)
            case (_, _, _) => confirmInputPageErrorCallouts(xs, IncompleteInputPage(x.id) :: errors)
          }
      }
  }

  @tailrec
  private def confirmPageTitles(pages: List[Page], errors: List[GuidanceError]): List[GuidanceError] = {
    def missingPageTitle(p: Page): Boolean =
      p.stanzas.collectFirst{
        case _: TitleCallout => ()
        case _: DataInputStanza => ()
        case _: YourCallCallout => ()
      }.isEmpty

    pages match {
      case Nil => errors
      case x :: xs if missingPageTitle(x) => confirmPageTitles(xs, MissingTitle(x.id) :: errors)
      case x :: xs => confirmPageTitles(xs, errors)
    }
  }

  @tailrec
  private def checkForInvalidLabelNames(pages: List[Page], errors: List[GuidanceError]): List[GuidanceError] = {
    def stanzaLabelNameErrors(ks: KeyedStanza): List[GuidanceError] = ks.stanza.labels.collect{case l if !labelNameValid(l) => InvalidLabelName(ks.key)}
    pages match {
      case Nil => errors
      case p :: xs => checkForInvalidLabelNames(xs, p.keyedStanzas.toList.flatMap(stanzaLabelNameErrors) ++ errors)
    }
  }

  @tailrec
  private def checkDateInputErrorCallouts(pages: List[Page], errors: List[GuidanceError]): List[GuidanceError] = {
    def checkCalloutSufficiency(pId: String, callouts: Seq[Callout]): List[GuidanceError] =
      callouts
       .map(co => (co, EmbeddedParameterRegex.findAllIn(co.text.english).length))
       match {
                      // Sufficient: 3 stacked callouts with messages containing 0,1 and 2 embedded parameters
          case cos if cos.size == 3 && cos(1)._1.stack && cos(2)._1.stack && List(0,1,2).forall(cos.map(_._2).contains) => Nil
          case cos => List(IncompleteDateInputPage(pId))
       }

    pages match {
      case Nil => errors
      case p :: xs => p.stanzas.collect{case d: DateInput => d} match {
        case Nil => checkDateInputErrorCallouts(xs, errors)
        case _ =>
        checkDateInputErrorCallouts(xs, checkCalloutSufficiency(p.id, p.stanzas.collect{case e: ErrorCallout => e}) ++
                                        checkCalloutSufficiency(p.id, p.stanzas.collect{case t: TypeErrorCallout => t}) ++ errors)
      }
    }
  }

  private def detectUnsupportedPageRedirect(pages: List[Page]): Seq[GuidanceError] = {
    val pageIds = pages.map(_.id)

    @tailrec
    def traverse(keys: List[String], pageStanzas: Map[String, Stanza], seen: List[String]): Option[String] =
      keys match {
        case Nil => None
        case x :: xs if seen.contains(x) => traverse(xs, pageStanzas, seen)
        case x :: xs => pageStanzas.get(x) match {
          case None => traverse(xs, pageStanzas, x :: seen)
          case Some(_: DataInput) => traverse(xs, pageStanzas, x :: seen)
          case Some(s: Choice) if s.next.exists(n => pageIds.contains(n)) => Some(x)
          case Some(s: Stanza) => traverse(s.next.toList ++ xs, pageStanzas, x :: seen)
        }
      }

    pages.flatMap{p =>
      traverse(List(p.id), p.keyedStanzas.map(ks => (ks.key, ks.stanza)).toMap, Nil).map(id => PageRedirectNotSupported(id))
    }
  }

  @tailrec
  private def duplicateUrlErrors(pages: List[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x :: xs if xs.exists(_.url == x.url) => duplicateUrlErrors(xs, DuplicatePageUrl(x.id, x.url) :: errors)
      case _ :: xs => duplicateUrlErrors(xs, errors)
    }

  @tailrec
  private def checkForUseOfReservedUrls(pages: List[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x :: xs if Process.ReservedUrls.contains(x.url) => checkForUseOfReservedUrls(xs, UseOfReservedUrl(x.id) :: errors)
      case _ :: xs => checkForUseOfReservedUrls(xs, errors)
    }

  @tailrec
  private def checkExclusiveSequenceTypeError(pages: List[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case p :: xs if p.stanzas.collectFirst{case seq: Sequence => seq}.fold(false)(seq => seq.exclusive.nonEmpty) &&
                      p.stanzas.collectFirst{case _: TypeErrorCallout => ()}.fold(true)(_ => false) =>
        checkExclusiveSequenceTypeError(xs, IncompleteExclusiveSequencePage(p.id) :: errors)
      case _ :: xs =>
        checkExclusiveSequenceTypeError(xs, errors)
    }

  private def checkForMinimumTwoPageFlows(vertices: List[PageVertex], vertexMap: Map[String, PageVertex]): List[AllFlowsMustContainMultiplePages] = {

    val flowPageVertices: List[PageVertex] = for {
      pv <- vertices.filterNot(_.flows.isEmpty)                   // Sequences
      flw <- pv.flows                                             // Flow Ids
      p <- vertexMap.get(flw)                                     // First pages of flows
    } yield p

    //filtering for pages that contain and end of flow
    flowPageVertices.collect{case p if p.endPage => AllFlowsMustContainMultiplePages(p.id)}.distinct
  }

  private def checkForSequencePageReuse(vertices: List[PageVertex], vertexMap: Map[String, PageVertex], mainFlow: List[String])
                                       (implicit stanzaMap: Map[String, Stanza]): List[PageOccursInMultiplSequenceFlows] = {
    val sequencePageIds: List[String] = for{
      pv <- vertices.filterNot(_.flows.isEmpty)                 // Sequences
      flw <- pv.flows                                           // Flow Ids
      p <- pageGraph(findPageIds(List(flw)), vertexMap, mainFlow) // Pages below sequences
    } yield p.id

    sequencePageIds.groupBy(x => x).toList.collect{case m if m._2.length > 1 => PageOccursInMultiplSequenceFlows(m._1)}
  }

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
      case Process.EndStanzaId :: xs => pageGraph(xs, vertices, ignore, dontFollowFlows, seen, acc)
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

  @tailrec
  private def checkDataInputPages(pages: List[Page], errors: List[GuidanceError]): List[GuidanceError] =
    pages match {
      case Nil => errors
      case x :: xs =>
        x.keyedStanzas.find(
          _.stanza match {
            case _: DataInput => true
            case _ => false
          }) match {
          case None => checkDataInputPages(xs, errors)
          case Some(d) =>
            val stanzaMap: Map[String, Stanza] = x.keyedStanzas.map(ks => (ks.key, ks.stanza)).toMap
            val leadingStanzaIds: List[String] = stanzasBeforeDataInput(List(x.keyedStanzas.head.key), stanzaMap, Nil, Nil)
            val anyErrors = checkDataInputFollowers(d.stanza.next.toList, stanzaMap, leadingStanzaIds, Nil)
            checkDataInputPages(xs, anyErrors ++ errors)
        }
    }

  @tailrec
  private def stanzasBeforeDataInput(p: List[String], stanzaMap: Map[String, Stanza], seen: List[String], acc: List[String]): List[String] =
    p match {
      case Nil => acc.reverse
      case x :: xs if seen.contains(x) => stanzasBeforeDataInput(xs, stanzaMap, seen, acc)
      case x :: xs if !stanzaMap.contains(x) => stanzasBeforeDataInput(xs, stanzaMap, x :: seen, acc)
      case x :: xs => stanzaMap(x) match {
        case _:DataInput => stanzasBeforeDataInput(xs, stanzaMap, x +: seen, acc)
        case other => stanzasBeforeDataInput(xs ++ other.next, stanzaMap, x :: seen, x :: acc)
      }
    }

  @tailrec
  private def checkDataInputFollowers( p: List[String],
                                       keyedStanzas: Map[String, Stanza],
                                       leadingStanzaIds: List[String],
                                       seen: List[String]): List[GuidanceError] =
    p match {
      case Nil => Nil
      case x :: xs if seen.contains(x) => checkDataInputFollowers(xs, keyedStanzas, leadingStanzaIds, seen)
      case x :: _ if leadingStanzaIds.contains(x) && leadingStanzaIds.indexOf(x) != 1 => List(ErrorRedirectToFirstNonPageStanzaOnly(x))
      case x :: xs if leadingStanzaIds.contains(x) => checkDataInputFollowers(xs, keyedStanzas, leadingStanzaIds, x :: seen)
      case x :: xs if !keyedStanzas.contains(x) => checkDataInputFollowers(xs, keyedStanzas, leadingStanzaIds, x :: seen)
      case x :: _ if keyedStanzas(x).visual => List(VisualStanzasAfterDataInput(x))
      case x :: xs => checkDataInputFollowers(keyedStanzas(x).next.toList ++ xs, keyedStanzas, leadingStanzaIds, x :: seen)
    }

}
