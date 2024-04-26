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

import cats.data.EitherT
import cats.implicits._
import java.time.ZonedDateTime
import play.api.Logging
import config.AppConfig
import models._
import migrate.models._
import migrate.repositories._
import repositories._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import models.Approval
import core.models.RequestOutcome

trait MigrateData

@Singleton
class DataMigrationService @Inject()(
  serviceLock: ServiceLock,
  appConfig: AppConfig,
  approvalRespository: ApprovalRepository,
  approvalReviewRepository: ApprovalProcessReviewRepository,
  approvalsRepository: ApprovalsRepository
)(implicit ec: ExecutionContext) extends Logging with MigrateData {

  private def migrationRequired(): Future[RequestOutcome[Boolean]] =
    approvalsRepository.processSummaries().map{
      case Right(Nil) => Right(true)
      case Right(_) => Right(false)
      case Left(err) => Left(err)
    }

  private def createApproval(ap: ApprovalProcess, ar: ApprovalProcessReview): Future[RequestOutcome[String]] = {
    val approval = Approval(ap.id, ap.meta,
                            ApprovalReview(ar.pages, ar.lastUpdated, ar.result, ar.completionDate, ar.completionUser),
                            ap.process, ap.version)
    approvalsRepository.createOrUpdate(approval).map{
      case Right(id) => Right(id)
      case Left(err) =>
        logger.error(s"Unable to create new approval for process ${ap.id}, error = $err")
        Left(err)
    }
  }

  private def review(id: String, version: Int, reviewType: String): Future[RequestOutcome[ApprovalProcessReview]] =
    approvalReviewRepository.getByIdVersionAndType(id, version, reviewType).map{
      case Right(review) => Right(review)
      case Left(err) =>
        logger.error(s"No review found for process $id, version $version, reviewType $reviewType")
        Left(err)
     }

  private def migrateData(): Future[RequestOutcome[Unit]] =
    approvalRespository.list().flatMap{
      case Left(err) =>
        logger.error(s"Unable to retrieve list of AprovalProcess, error = $err")
        Future.successful(Left(err))
      case Right(approvals) =>
        approvals.map{app =>
          for{
            review <- EitherT(review(app.id, app.version, app.meta.reviewType))
            result <- EitherT(createApproval(app, review))
          } yield result
        }.traverse(_.value).map{outcomes =>
          val successes: List[String] = outcomes.collect{case Right(id) => id}
          val failureCount: Int = outcomes.collect{case Left(_) => 1}.toList.length
          logger.warn(s"Following processes migrated sucessfully: ${successes.mkString(", ")}")
          logger.warn(s"${failureCount} processes failed to migrate (See log)")
          Right(())
        }
    }

  if (appConfig.enableDataMigration) {
    logger.warn(s"Data migration lock claim")
    serviceLock.lock("DataMigration").map{lockOption =>
      lockOption.fold{
        logger.warn(s"Migration lock already taken")
      }{lock =>
        logger.warn(s"Starting Data migration check")
        migrationRequired().flatMap{
          case Right(true) =>
            logger.warn(s"Data Migration: Started at ${ZonedDateTime.now}")
            migrateData().map{_ =>
              logger.warn(s"Data Migration: Finished at ${ZonedDateTime.now}")
              Right(())
            }
          case Right(_) =>
            logger.warn(s"Data Migration: Not required")
            Future.successful(Right(()))
          case Left(err) =>
            logger.error(s"Unable to determine whether data migration is possible or necessary")
            Future.successful(Left(err))
        }.map(_ => serviceLock.unlock(lock.owner))
      }
    }
  }
}
