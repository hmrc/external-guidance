/*
 * Copyright 2021 HM Revenue & Customs
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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {

  val scratchExpiryHour: Int
  val scratchExpiryMinutes: Int
  val scratchExpiryTZ: String

  val designerRole: String
  val factCheckerRole: String
  val twoEyeReviewerRole: String
  val addMissingWelshToUnauthenticatedGuidance: Boolean
  val passPhrasePagePrompt: String
}

@Singleton
class AppConfigImpl @Inject() (config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {

  lazy val scratchExpiryHour = servicesConfig.getInt("mongodb.scratchExpiryHour")
  lazy val scratchExpiryMinutes = servicesConfig.getInt("mongodb.scratchExpiryMinutes")
  lazy val scratchExpiryTZ = servicesConfig.getString("mongodb.scratchExpiryTZ")

  lazy val designerRole = servicesConfig.getString("strideAuth.roles.designer")
  lazy val factCheckerRole = servicesConfig.getString("strideAuth.roles.factChecker")
  lazy val twoEyeReviewerRole = servicesConfig.getString("strideAuth.roles.twoEyeReviewer")
  lazy val addMissingWelshToUnauthenticatedGuidance: Boolean =
    config.getOptional[Boolean]("addMissingWelshToUnauthenticatedGuidance").fold(false)(answer => answer)
  lazy val passPhrasePagePrompt = servicesConfig.getString("pass-phrase-page-prompt")
}
