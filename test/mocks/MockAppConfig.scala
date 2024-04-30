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

package mocks

import config.AppConfig
import scala.concurrent.duration.{MINUTES, FiniteDuration}

object MockAppConfig extends AppConfig {

  val scratchExpiryHour: Int = 23
  val scratchExpiryMinutes: Int = 59
  val scratchExpiryTZ: String = "Europe/London"
  val archivedExpiryHours: Int = 720

  val designerRole: String = "Designer"
  val factCheckerRole: String = "FactChecker"
  val twoEyeReviewerRole: String = "2iReviewer"
  val fakeWelshInUnauthenticatedGuidance: Boolean = true
  val seedTimescales: Map[String,Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
  val passphraseHashKey: String = "gvBoGdgzqG1AarzF1LY0zQ=="
  val enableDataMigration: Boolean = false
  val serviceLockDuration: FiniteDuration = FiniteDuration(5, MINUTES)
  val includeAllPublishedInReviewList: Boolean = true
}

case class MockAppConfigCopyable(scratchExpiryHour: Int,
                                 scratchExpiryMinutes: Int,
                                 scratchExpiryTZ: String,
                                 archivedExpiryHours: Int,
                                 designerRole: String,
                                 factCheckerRole: String,
                                 twoEyeReviewerRole: String,
                                 fakeWelshInUnauthenticatedGuidance: Boolean,
                                 seedTimescales: Map[String,Int],
                                 passphraseHashKey: String,
                                 includeAllPublishedInReviewList: Boolean,
                                 enableDataMigration: Boolean = false,
                                 serviceLockDuration: FiniteDuration = FiniteDuration(5, MINUTES)) extends AppConfig


