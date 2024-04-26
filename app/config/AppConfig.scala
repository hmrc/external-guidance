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
import scala.util.{Try, Success}
import scala.concurrent.duration.{Duration, MINUTES, FiniteDuration}

trait AppConfig {

  val scratchExpiryHour: Int
  val scratchExpiryMinutes: Int
  val scratchExpiryTZ: String
  val archivedExpiryHours: Int

  val designerRole: String
  val factCheckerRole: String
  val twoEyeReviewerRole: String
  val fakeWelshInUnauthenticatedGuidance: Boolean
  val seedTimescales: Map[String, Int]
  val passphraseHashKey: String
  val enableDataMigration: Boolean
  val serviceLockDuration: FiniteDuration
}

@Singleton
class AppConfigImpl @Inject() (config: Configuration, servicesConfig: ServicesConfig) extends Logging with AppConfig {

  private final def getFiniteDuration(config: Configuration, key: String): FiniteDuration =
    Try(Duration.create(config.get[String](key))) match {
      case Success(fd: FiniteDuration) => fd
      case _ =>
        logger.error(s"Unable to read configuration for FiniteDuration key $key")
        FiniteDuration(5, MINUTES)
    }

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
  lazy val serviceLockDuration: FiniteDuration = getFiniteDuration(config, "data-migration.lock-duration")
  lazy val enableDataMigration: Boolean = config.get[Boolean]("data-migration.enable")
}
