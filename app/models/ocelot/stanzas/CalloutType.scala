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

package models.ocelot.stanzas

import play.api.libs.json._

sealed trait CalloutType

case object Title extends CalloutType

case object SubTitle extends CalloutType

case object Error extends CalloutType

case object Lede extends CalloutType

case object Section extends CalloutType

object CalloutType {

  implicit val reads: Reads[CalloutType] = new Reads[CalloutType] {

    override def reads(json: JsValue): JsResult[CalloutType] = json match {
      case JsString("Title") => JsSuccess(Title, __)
      case JsString("SubTitle") => JsSuccess(SubTitle, __)
      case JsString("Lede") => JsSuccess(Lede, __)
      case JsString("Error") => JsSuccess(Error, __)
      case JsString("Section") => JsSuccess(Section, __)
      case _ => JsError("Invalid Callout type")
    }
  }
}
