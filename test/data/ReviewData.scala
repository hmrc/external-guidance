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

package data

import java.time.LocalDate
import java.util.UUID

import models.{ApprovalProcessStatusChange, PageReview, ProcessReview}
import play.api.libs.json.{JsObject, JsValue, Json}
import models.Constants._

trait ReviewData {

  val validProcessIdForReview = "oct90001"

  val processReviewInfo: ProcessReview =
    ProcessReview(
      UUID.randomUUID().toString,
      validProcessIdForReview,
      1,
      ReviewType2i,
      "Telling HMRC about extra income",
      LocalDate.of(2020, 5, 10),
      List(
        PageReview("id1", "title1", "/how-did-you-earn-extra-income", InitialPageReviewStatus, None),
        PageReview("id2", "title2", "sold-goods-or-services/did-you-only-sell-personal-possessions", InitialPageReviewStatus, None),
        PageReview("id3", "title3", "sold-goods-or-services/have-you-made-a-profit-of-6000-or-more", InitialPageReviewStatus, None),
        PageReview("id4", "title4", "sold-goods-or-services/have-you-made-1000-or-more", InitialPageReviewStatus, None),
        PageReview("id5", "title5", "sold-goods-or-services/you-do-not-need-to-tell-hmrc", InitialPageReviewStatus, None),
        PageReview("id6", "title6", "rent-a-property/do-you-receive-any-income", InitialPageReviewStatus, None),
        PageReview("id7", "title7", "rent-a-property/have-you-rented-out-a-room", InitialPageReviewStatus, None)
      )
    )
  val reviewInfoJson: JsObject = Json.toJson(processReviewInfo).as[JsObject]

  val statusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusSubmittedForFactCheck)
  val statusChange2iReviewInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusWithDesignerForUpdate)

  val statusChangeJson: JsValue = Json.toJson(statusChangeInfo)

  val invalidStatusChangeJson: JsObject = Json.obj()
}
