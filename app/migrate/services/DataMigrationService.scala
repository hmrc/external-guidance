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

package migrate.services

import play.api.Logging
import config.AppConfig
import migrate.repositories.TimescalesRepository
import uk.gov.hmrc.mongo.lock._
import repositories._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import models.Timescales
import core.models.RequestOutcome
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import core.models.errors.NotFoundError

trait MigrateData

@Singleton
class DataMigrationService @Inject()(
  appConfig: AppConfig,
  timescalesRespository: TimescalesRepository,
  labelledDataRepository: LabelledDataRepository,
  lockRepository: MongoLockRepository
)(implicit ec: ExecutionContext) extends Logging with MigrateData {

  val InstanceLockOut: Long = 3L
  private val lock: TimePeriodLockService = TimePeriodLockService(lockRepository, "data_migration_lock", Duration(InstanceLockOut, TimeUnit.MINUTES))

  private def migrationRequired(): Future[RequestOutcome[Boolean]] =
    labelledDataRepository.get(Timescales).map{
      case Left(NotFoundError) => Right(true)
      case Right(_) => Right(false)
      case Left(err) => Left(err)
    }

  private def migrateData(): Future[RequestOutcome[Boolean]] =
    migrationRequired().flatMap{
      case Right(false) => Future.successful(Right(false))
      case Right(true) =>
        logger.warn(s"Migrating Timescales data to LabelledData Repository")
        timescalesRespository.get(timescalesRespository.CurrentTimescalesID).flatMap{
          case Right(timescalesUpdate) =>
            labelledDataRepository.save(
                                    Timescales,
                                    timescalesUpdate.timescales,
                                    timescalesUpdate.when.toInstant(),
                                    timescalesUpdate.credId,
                                    timescalesUpdate.user,
                                    timescalesUpdate.email
                                  ).map{
              case Right(labelledData) =>
                logger.warn(s"Timescales data stored")
                Right(true)
              case Left(err) =>
                logger.error(s"Attempt to save to LabelledData Repository failed with error: $err")
                Left(err)
            }
          case Left(err) =>
            logger.error(s"Unable to retrieve current Timescales data, error = $err")
            Future.successful(Left(err))
        }
      case Left(err) => Future.successful(Left(err))
    }

    logger.warn(s"Data migration lock claim")
    lock.withRenewedLock(migrateData()).map{
      case None => logger.warn(s"Data migration lock already taken")
      case Some(Right(true)) => logger.warn("Timescale data migration complete")
      case Some(Right(false)) => logger.warn("Timescale data migration not required")
      case Some(Left(err)) => logger.error(s"Migration failed with error $err")
    }
}
