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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {

  lazy val scratchExpiryHour: Int
  lazy val scratchExpiryMinutes: Int
  lazy val scratchExpiryTZ: String
  lazy val archivedExpiryHours: Int

  lazy val designerRole: String
  lazy val factCheckerRole: String
  lazy val twoEyeReviewerRole: String
  lazy val fakeWelshInUnauthenticatedGuidance: Boolean
  lazy val seedTimescales: Map[String, Int]
  lazy val passphraseHashKey: String
}

@Singleton
class AppConfigImpl @Inject() (config: Configuration, servicesConfig: ServicesConfig) extends Logging with AppConfig {
  lazy val scratchExpiryHour: Int = servicesConfig.getInt("mongodb.scratchExpiryHour")
  lazy val scratchExpiryMinutes: Int = servicesConfig.getInt("mongodb.scratchExpiryMinutes")
  lazy val scratchExpiryTZ: String = servicesConfig.getString("mongodb.scratchExpiryTZ")
  lazy val archivedExpiryHours: Int = servicesConfig.getInt("mongodb.archivedExpiryHours")

  lazy val designerRole: String = servicesConfig.getString("strideAuth.roles.designer")
  lazy val factCheckerRole: String = servicesConfig.getString("strideAuth.roles.factChecker")
  lazy val twoEyeReviewerRole: String = servicesConfig.getString("strideAuth.roles.twoEyeReviewer")
  lazy val fakeWelshInUnauthenticatedGuidance: Boolean = config.getOptional[Boolean]("welsh-guidance-text.fake-when-unauthenticated").getOrElse(false)
  lazy val seedTimescales: Map[String, Int] = config.get[Map[String, Int]]("seed-timescales")
  lazy val passphraseHashKey: String = config.get[String]("passphrase-hashkey")
}
