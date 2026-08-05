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

trait MockAppConfig extends AppConfig {

  lazy val scratchExpiryHour: Int = 23
  lazy val scratchExpiryMinutes: Int = 59
  lazy val scratchExpiryTZ: String = "Europe/London"
  lazy val archivedExpiryHours: Int = 720

  lazy val designerRole: String = "Designer"
  lazy val factCheckerRole: String = "FactChecker"
  lazy val twoEyeReviewerRole: String = "2iReviewer"
  lazy val fakeWelshInUnauthenticatedGuidance: Boolean = true
  lazy val seedTimescales: Map[String,Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
  lazy val passphraseHashKey: String = "gvBoGdgzqG1AarzF1LY0zQ=="
}


