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

package data

import java.time.LocalDate
import java.util.UUID

import models.{ApprovalProcessStatusChange, PageReview, ProcessReview}
import play.api.libs.json.{JsObject, JsValue, Json}
import utils.Constants._

trait ReviewData {

  val validProcessIdForReview = "oct90001"

  val processReviewInfo: ProcessReview =
    ProcessReview(
      UUID.randomUUID(),
      validProcessIdForReview,
      1,
      "Telling HMRC about extra income",
      LocalDate.of(2020, 5, 10),
      List(
        PageReview("id1", "how-did-you-earn-extra-income", INITIAL_PAGE_REVIEW_STATUS),
        PageReview("id2", "sold-goods-or-services/did-you-only-sell-personal-possessions", INITIAL_PAGE_REVIEW_STATUS),
        PageReview("id3", "sold-goods-or-services/have-you-made-a-profit-of-6000-or-more", INITIAL_PAGE_REVIEW_STATUS),
        PageReview("id4", "sold-goods-or-services/have-you-made-1000-or-more", INITIAL_PAGE_REVIEW_STATUS),
        PageReview("id5", "sold-goods-or-services/you-do-not-need-to-tell-hmrc", INITIAL_PAGE_REVIEW_STATUS),
        PageReview("id6", "rent-a-property/do-you-receive-any-income", INITIAL_PAGE_REVIEW_STATUS),
        PageReview("id7", "rent-a-property/have-you-rented-out-a-room", INITIAL_PAGE_REVIEW_STATUS)
      )
    )
  val reviewInfoJson: JsObject = Json.toJson(processReviewInfo).as[JsObject]

  val statusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", STATUS_SUBMITTED_FOR_FACT_CHECK)

  val statusChangeJson: JsValue = Json.toJson(statusChangeInfo)

  val invalidStatusChangeJson: JsObject = Json.obj()
}
