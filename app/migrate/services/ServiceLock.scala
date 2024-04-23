/*
 * Copyright 2023 HM Revenue & Customs
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

package migrate.services

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import config.AppConfig
import uk.gov.hmrc.mongo.lock._
import play.api.Logging
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait ServiceLock {
  def lock(taskName: String): Future[Option[Lock]]
  def renew(taskName: String): Future[Unit]
  def unlock(taskName: String): Future[Unit]
}

@Singleton
class ServiceLockImpl @Inject()(repo: MongoLockRepository, appConfig: AppConfig)(implicit ec: ExecutionContext) extends ServiceLock with Logging {
  private val forceReleaseAfter: Duration = Duration(appConfig.serviceLockDuration.toMillis, TimeUnit.MILLISECONDS)
  private val lockId: String = "external_guidance_lock"

  logger.warn(s"Service Lock period: $forceReleaseAfter")

  def lock(taskName: String): Future[Option[Lock]] = repo.takeLock(lockId, taskName, forceReleaseAfter)
  def renew(taskName: String): Future[Unit] = repo.refreshExpiry(lockId, taskName, forceReleaseAfter).map(_ => ())
  def unlock(taskName: String): Future[Unit] = repo.releaseLock(lockId, taskName)
}
