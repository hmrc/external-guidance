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

import java.time.{LocalDate, ZoneOffset}

import play.api.libs.json.{JsObject, Json}

trait ApprovalProcessJson {

  val validId = "oct90001"
  val dateSubmitted: LocalDate = LocalDate.of(2020, 3, 3)
  val submittedDateInMilliseconds: Long = dateSubmitted.atStartOfDay(ZoneOffset.UTC).toInstant.toEpochMilli

  val approvalProcessMeta: ApprovalProcessMeta = ApprovalProcessMeta("oct90001", "This is the title", "SubmittedFor2iReview", dateSubmitted)
  val approvalProcess: ApprovalProcess = ApprovalProcess(validId, approvalProcessMeta, Json.obj())
  val approvalProcessSummary: ApprovalProcessSummary = ApprovalProcessSummary("oct90001", "This is the title", dateSubmitted, "SubmittedFor2iReview")

  val validApprovalProcessJson: JsObject = Json
    .parse(
      """
      |{
      |  "_id" : "oct90001",
      |  "meta" : {
      |    "id" : "oct90001",
      |    "title" : "This is the title",
      |    "status" : "SubmittedFor2iReview",
      |    "dateSubmitted" : {"$date": placeholder}
      |  },
      |  "process" : {
      |  }
      |}
    """.stripMargin.replace("placeholder", submittedDateInMilliseconds.toString)
    )
    .as[JsObject]

}
