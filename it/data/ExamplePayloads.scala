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

import java.time.{LocalDate, ZoneOffset}
import java.util.UUID

import models.{ApprovalProcessStatusChange, PageReview, ProcessReview}
import play.api.libs.json.{JsObject, JsValue, Json}
import utils.Constants._

object ExamplePayloads {

  val simpleValidProcessString: String =
    """
      |{
      |  "meta": {
      |    "title": "Customer wants to make a cup of tea",
      |    "id": "oct90001",
      |    "ocelot": 1,
      |    "lastAuthor": "000000",
      |    "lastUpdate": 1500298931016,
      |    "version": 4,
      |    "filename": "oct90001.js"
      |  },
      |  "howto": [],
      |  "contacts": [],
      |  "links": [],
      |  "flow": {
      |    "1": {
      |      "type": "ValueStanza",
      |      "values": [
      |        {
      |          "type": "scalar",
      |          "label": "PageUrl",
      |          "value": "/feeling-bad"
      |        }
      |      ],
      |      "next": ["3"],
      |      "stack": true
      |    },
      |    "3": {
      |      "type": "InstructionStanza",
      |      "text": 1,
      |      "next": [
      |        "2"
      |      ],
      |      "stack": true
      |    },
      |    "2": {
      |      "type": "InstructionStanza",
      |      "text": 0,
      |      "next": [
      |        "end"
      |      ],
      |      "stack": true
      |    },
      |    "end": {
      |      "type": "EndStanza"
      |    }
      |  },
      |  "phrases": [
      |    ["Ask the customer if they have a tea bag", "Welsh, Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh, Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh, Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh, No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh, Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh, Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh, yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh, no - they don’t have a cup"]
      |  ]
      |}
    """.stripMargin

  val simpleValidProcess: JsValue = Json.parse(simpleValidProcessString)

  val dateSubmitted: LocalDate = LocalDate.of(2020, 3, 3)
  val submittedDateInMilliseconds: Long = dateSubmitted.atStartOfDay(ZoneOffset.UTC).toInstant.toEpochMilli

  val validApprovalProcessJson: JsValue = Json.parse(
    s"""
      |{
      |  "meta" : {
      |    "id" : "oct90001",
      |    "title" : "This is the title",
      |    "status" : "$STATUS_SUBMITTED_FOR_2I_REVIEW",
      |    "dateSubmitted" : {"$$date": $submittedDateInMilliseconds}
      |  },
      |  "process" : $simpleValidProcessString
      |}
      """.stripMargin
  )

  val expectedApprovalProcessJson: JsValue = Json.parse(
    s"""
      |{
      |  "_id" : "oct90001",
      |  "meta" : {
      |    "id" : "oct90001",
      |    "title" : "This is the title",
      |    "status" : "$STATUS_SUBMITTED_FOR_2I_REVIEW",
      |    "dateSubmitted" : {"$$date": $submittedDateInMilliseconds}
      |  },
      |  "process" : $simpleValidProcessString
      |}
      """.stripMargin
  )

  val validId = "oct90001"

  val processReviewInfo: ProcessReview =
    ProcessReview(
      UUID.randomUUID(),
      validId,
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

  val statusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", STATUS_SUBMITTED_FOR_FACT_CHECK)

  val statusChangeJson: JsValue = Json.toJson(statusChangeInfo)

  val invalidStatusChangeJson: JsObject = Json.obj()
}
