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

package core.models.ocelot.stanzas

import core.models.ocelot.{labelReferences, Phrase}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, OWrites, Reads}

case class RowStanza (cells: Seq[Int], override val next: Seq[String],  stack: Boolean) extends VisualStanza

object RowStanza {

  implicit val rowReads: Reads[RowStanza] =
    (
      (JsPath \ "cells").read[Seq[Int]] and
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
      (JsPath \ "stack").read[Boolean]
    )(RowStanza.apply _)

  implicit val rowWrites: OWrites[RowStanza] =
    (
      (JsPath \ "cells").write[Seq[Int]] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "stack").write[Boolean]

    )(unlift(RowStanza.unapply))
}

case class Row( cells: Seq[Phrase],
                override val next: Seq[String],
                stack: Boolean = false,
                override val links: List[String] = Nil) extends VisualStanza with Populated {

  override val labelRefs: List[String] = cells.toList.flatMap(c => labelReferences(c.english))
}

object Row {
  def apply(stanza: RowStanza, cells: Seq[Phrase], linkIds: List[String] ): Row = Row(cells, stanza.next, stanza.stack, linkIds)
}
