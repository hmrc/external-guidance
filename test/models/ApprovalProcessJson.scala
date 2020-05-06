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

import org.joda.time.{DateTimeZone, LocalDateTime}
import play.api.libs.json.{JsObject, Json}

trait ApprovalProcessJson {

  val validId = "oct90001"
  val dateSubmitted: LocalDateTime = LocalDateTime.now()
  val submittedDateInMilliseconds: Long = dateSubmitted.toDateTime(DateTimeZone.UTC).getMillis

  val approvalProcessMeta: ApprovalProcessMeta = ApprovalProcessMeta("oct90001", "This is the title", "Ready for 2i", dateSubmitted)
  val approvalProcess: ApprovalProcess = ApprovalProcess(validId, approvalProcessMeta, Json.obj())

  val validApprovalProcessJson: JsObject = Json.parse(
    """
      |{
      |  "_id" : "oct90001",
      |  "meta" : {
      |    "id" : "oct90001",
      |    "title" : "This is the title",
      |    "status" : "Ready for 2i",
      |    "dateSubmitted" : {"$date": placeholder}
      |  },
      |  "process" : {
      |  }
      |}
    """.stripMargin.replace("placeholder", submittedDateInMilliseconds.toString)
  ).as[JsObject]

  val expectedReturnedApprovalProcessJson: JsObject = Json.parse(
    """
      |{
      |  "id" : "oct90001",
      |  "meta" : {
      |    "id" : "oct90001",
      |    "title" : "This is the title",
      |    "status" : "Ready for 2i",
      |    "dateSubmitted" : {"$date": 1500298931016}
      |  },
      |  "process" : {
      |  }
      |}
    """.stripMargin
  ).as[JsObject]

}
