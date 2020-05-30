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

package utils

object Constants {
  val REVIEW_TYPE_2I: String = "2i-review"
  val REVIEW_TYPE_FACT_CHECK: String = "fact-check"
  val INITIAL_PAGE_REVIEW_STATUS: String = "NotStarted"
  val REVIEW_COMPLETE_STATUS: String = "Complete"
  val STATUS_PUBLISHED: String = "Published"
  val STATUS_SUBMITTED_FOR_2I_REVIEW: String = "SubmittedFor2iReview"
  val STATUS_SUBMITTED_FOR_FACT_CHECK: String = "SubmittedForFactCheck"
  val STATUS_WITH_DESIGNER_FOR_UPDATE: String = "WithDesignerForUpdate"
  val STATUS_APPROVED_FOR_PUBLISHING: String = "ApprovedForPublishing"

}
