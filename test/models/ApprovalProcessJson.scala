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

package models

import java.time.{LocalDate, ZonedDateTime}
import java.util.UUID

import data.ProcessData._
import play.api.libs.json.{JsObject, Json}
import utils.Constants._

trait ApprovalProcessJson {

   val localZoneID = ZonedDateTime.now.getZone

  val validId = "oct90001"
  val dateSubmitted: LocalDate = LocalDate.of(2020, 3, 3)
  val submittedDateInMilliseconds: Long = dateSubmitted.atStartOfDay(localZoneID).toInstant.toEpochMilli

  val approvalProcessMeta: ApprovalProcessMeta =
    ApprovalProcessMeta(validId, "This is the title", StatusSubmitted, dateSubmitted, dateSubmitted.atStartOfDay(localZoneID), processCode = "processCode")
  val approvalProcess: ApprovalProcess = ApprovalProcess(validId, approvalProcessMeta, Json.obj())
  val approvalProcessWithValidProcess = approvalProcess.copy(process = process90087Json)

  val approvalProcessSummary: ApprovalProcessSummary =
    ApprovalProcessSummary(validId, "This is the title", dateSubmitted, StatusSubmittedFor2iReview, ReviewType2i)

  val validApprovalProcessJson: JsObject = Json
    .parse(
      s"""
      |{
      |  "_id" : "$validId",
      |  "meta" : {
      |    "id" : "$validId",
      |    "title" : "This is the title",
      |    "status" : "$StatusSubmitted",
      |    "dateSubmitted" : {"$$date": $submittedDateInMilliseconds},
      |    "lastModified" : {"$$date": $submittedDateInMilliseconds},
      |    "ocelotDateSubmitted" : 1,
      |    "ocelotVersion" : 1,
      |    "reviewType" : "$ReviewType2i",
      |    "processCode" : "processCode"
      |  },
      |  "process" : {
      |  },
      |  "version" : 1
      |}
    """.stripMargin
    )
    .as[JsObject]

  val validApprovalProcessWithoutAnIdJson: JsObject = Json
    .parse(
      s"""
        |{
        |  "meta" : {
        |    "id" : "$validId",
        |    "title" : "This is the title",
        |    "status" : "$StatusSubmitted",
        |    "dateSubmitted" : {"$$date": $submittedDateInMilliseconds},
        |    "lastModified" : {"$$date": $submittedDateInMilliseconds},
        |    "reviewType" : "$ReviewType2i",
        |    "processCode" : "processCode"
        |  },
        |  "process" : {
        |  },
        |  "version" : 1
        |}
    """.stripMargin
    )
    .as[JsObject]

  val process2iReviewSummary: String =
    s"""
      |{
      |    "id": "$validId",
      |    "title": "Telling HMRC about extra income",
      |    "lastUpdated": "2020-05-10",
      |    "status" : "$StatusSubmitted",
      |    "pages": [
      |        {
      |            "id": "id1",
      |            "title": "how-did-you-earn-extra-income",
      |            "status": "$InitialPageReviewStatus",
      |            "comment": ""
      |        },
      |        {
      |            "id": "id2",
      |            "title": "sold-goods-or-services/did-you-only-sell-personal-possessions",
      |            "status": "$InitialPageReviewStatus",
      |            "comment": ""
      |        },
      |        {
      |            "id": "id3",
      |            "title": "sold-goods-or-services/have-you-made-a-profit-of-6000-or-more",
      |            "status": "$InitialPageReviewStatus",
      |            "comment": ""
      |        },
      |        {
      |            "id": "id4",
      |            "title": "sold-goods-or-services/have-you-made-1000-or-more",
      |            "status": "$InitialPageReviewStatus",
      |            "comment": ""
      |        },
      |        {
      |            "id": "id5",
      |            "title": "sold-goods-or-services/you-do-not-need-to-tell-hmrc",
      |            "status": "$InitialPageReviewStatus",
      |            "comment": ""
      |        },
      |        {
      |            "id": "id6",
      |            "title": "rent-a-property/do-you-receive-any-income",
      |            "status": "$InitialPageReviewStatus",
      |            "comment": ""
      |        },
      |        {
      |            "id": "id7",
      |            "title": "rent-a-property/have-you-rented-out-a-room",
      |            "status": "$InitialPageReviewStatus",
      |            "comment": ""
      |        }
      |    ]
      |}
      |""".stripMargin

  val process2iReviewSummaryJson: JsObject = Json.parse(process2iReviewSummary).as[JsObject]

  val validReviewId: String = "276cc289-a852-4af2-95ae-4bafa1c1835c"

  val reviewBody: String =
    s"""
       |	"ocelotId" : "$validId",
       |	"version" : 5,
       |	"reviewType" : "$ReviewTypeFactCheck",
       |	"title" : "Customer wants to make a cup of tea",
       |	"lastUpdated" : "2020-05-29",
       |	"result" : "",
       |	"completionDate" : null,
       |	"completionUser" : null,
       |	"pages" : [
       |		{
       |			"id" : "1",
       |			"pageUrl" : "/feeling-bad",
       |			"pageTitle" : "title",
       |			"result" : "",
       |			"status" : "$InitialPageReviewStatus",
       |			"comment" : "",
       |			"updateDate" : {"$$date":1590760487000},
       |			"updateUser" : ""
       |		}
       |	]
    """.stripMargin

  val review: String =
    s"""
      |{
      |	"_id" : "$validReviewId",
      | $reviewBody
      |}
    """.stripMargin

  val validApprovalProcessReviewJson: JsObject = Json.parse(review).as[JsObject]

  val reviewWithoutId: String =
    s"""
       |{
       |  $reviewBody
       |}
    """.stripMargin

  val validApprovalProcessReviewWithNoIdJson: JsObject = Json.parse(reviewWithoutId).as[JsObject]

  val approvalProcessReview: ApprovalProcessReview =
    ApprovalProcessReview(
      UUID.randomUUID(),
      validId,
      1,
      ReviewType2i,
      "Title",
      List(ApprovalProcessPageReview("id", "url", "pageUrl")),
      LocalDate.now(),
      ReviewCompleteStatus,
      Some(ZonedDateTime.now())
    )
}
