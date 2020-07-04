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

package models.ocelot

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Link(id: Int, dest: String, title: String, window: Boolean)

object Link {

  def isLinkableStanzaId(dest: String): Boolean = dest.equals(Process.StartStanzaId) || dest.forall(_.isDigit)

  implicit val reads: Reads[Link] = (
    (__ \ "id").read[Int] and
      (__ \ "dest").read[String] and
      (__ \ "title").read[String] and
      (__ \ "window").read[Boolean]
  )(Link.apply _)
}
