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
import migrate.models.TimescalesUpdate
import migrate.repositories.TimescalesRepository
import uk.gov.hmrc.mongo.lock._
import repositories._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import core.models.RequestOutcome
import models.Timescales
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import core.models.errors.NotFoundError
import uk.gov.hmrc.mongo.MongoComponent
import cats.data.EitherT
import cats.implicits._
import core.models.errors.DatabaseError

trait MigrateData

@Singleton
class DataMigrationService @Inject()(
  appConfig: AppConfig,
  timescalesRespository: TimescalesRepository,
  dataRepository: LabelledDataRepository,
  lockRepository: MongoLockRepository,
  component: MongoComponent
)(implicit ec: ExecutionContext) extends Logging with MigrateData {

  val InstanceLockOut: Long = 2L
  private val lock: TimePeriodLockService = TimePeriodLockService(lockRepository, "data_migration_lock", Duration(InstanceLockOut, TimeUnit.MINUTES))

  private def migrationRequired(): Future[RequestOutcome[Boolean]] =
    dataRepository.get(Timescales).map{
      case Left(NotFoundError) => Right(true)
      case Right(_) => Right(false)
      case Left(error) =>
        logger.error(s"Failed to query data repository for Timescales, error = $error")
        Left(error)
    }

  private def migrateData(): Future[RequestOutcome[Unit]] =
    (for{
      timescales <- EitherT(timescalesData())
      _ <- EitherT(saveTimescales(timescales))
      _ <- EitherT(dropCollection("timescales"))
    } yield ()).value

  private def timescalesData(): Future[RequestOutcome[TimescalesUpdate]] =
    timescalesRespository.get(timescalesRespository.CurrentTimescalesID).map{
      case Left(error) =>
        logger.error(s"Unable to retrieve current Timescales data, error = $error")
        Left(error)
      case ok => ok
    }

  private def saveTimescales(ts: TimescalesUpdate): Future[RequestOutcome[Unit]] =
    dataRepository.save(Timescales, ts.timescales, ts.when.toInstant(), ts.credId, ts.user, ts.email).map{
      _.fold(error => {
        logger.error(s"Attempt to save to LabelledData Repository failed with error: $error")
        Left(error)
      }, _ => Right(()))
    }

  private def dropCollection(name: String): Future[RequestOutcome[Unit]] =
    component.database.getCollection(name).drop().headOption().map{outcome =>
      outcome.fold[RequestOutcome[Unit]]{
        logger.warn(s"Collection $name either does not exist or could not be dropped")
        Left(NotFoundError)
      }{_ =>
        logger.warn(s"Collection $name sucessfully dropped")
        Right(())
      }
    }.recover{
      case error: Throwable =>
        logger.error(s"Dropping collection $name returned an error. Error: $error")
        Left(DatabaseError)
    }

  lock.withRenewedLock{
    migrationRequired().flatMap{
      case Right(false) => Future.successful(logger.warn(s"Data migration not required"))
      case Right(true) =>
        logger.warn(s"Migrating timescales data to labelled data repository")
        migrateData() map {
          case Right(_) => logger.warn(s"Timescales data migration completed successfully")
          case Left(error) => logger.error(s"Data migration failed with error $error")
        }
      case _ => Future.successful(())
    }
  }
}
