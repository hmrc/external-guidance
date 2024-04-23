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
import core.models.errors._

trait MigrateData

@Singleton
class DataMigrationService @Inject()(
  serviceLock: ServiceLock,
  appConfig: AppConfig,
  approvalRespository: ApprovalRepository,
  approvalReviewRepository: ApprovalProcessReviewRepository,
  approvals: ApprovalsRepository
)(implicit ec: ExecutionContext) extends Logging with MigrateData {


  private def migrationRequired(): Future[RequestOutcome[Boolean]] =
    approvals.processSummaries().map{
      case Right(Nil) => Right(true)
      case Right(_) => Right(false)
      case Left(err) => Left(err)
    }

  private def startupDataMigration(): Unit = {
    logger.info(s"Startup data migration")
    serviceLock.lock("DataMigration").map{lockOption =>
      lockOption.map{lock =>
        migrationRequired().flatMap{
          case Right(true) => 
            logger.warn(s"Data Migration: Started at ${ZonedDateTime.now}")
            migrateData()
          case Right(_) =>
            logger.warn(s"Data Migration: Not required")
            Future.successful(Right(()))
          case Left(err) =>
            logger.error(s"Unable to determine whether data migration is possible or necessary")
            Future.successful(Left(err))
        }.map{ _ =>
          serviceLock.unlock(lock.owner).map(_ => logger.warn(s"Data Migration: Finished at ${ZonedDateTime.now}"))
        }
      }
    }
  }

  private def createApproval(ap: ApprovalProcess, ar: ApprovalProcessReview): Approval = 
    Approval(
      ap.id,
      ap.meta,
      ApprovalReview(
        ar.pages,
        ar.lastUpdated,
        ar.result,
        ar.completionDate,
        ar.completionUser
      ),
      ap.process,
      ap.version
    )

  private def migrateData(): Future[RequestOutcome[Unit]] =
    approvalRespository.list().flatMap{
      case Left(err) => Future.successful(Left(err))
      case Right(approvals) =>
        logger.warn(s"Found ${approvals.length} ApprovalProcess records to migrate")
        Future.sequence(approvals.map{approval =>
          approvalReviewRepository.getByIdVersionAndType(approval.id, approval.version, approval.meta.reviewType).map{
            case Left(err) => 
              logger.error(s"Failed ($err) to find review for ${approval.id}, ${approval.version}, ${approval.meta.reviewType}")
              Left(err)
            case Right(review) => 
              logger.warn(s"Found matching review for Approval with ${approval.id}, ${approval.version} and ${approval.meta.reviewType}")
              Right(createApproval(approval, review))
          }
        }).map{
          case results if results.forall(_.isRight) => 
            logger.warn(s"Created ${results.length} Approvalrecords during migration")
            Right(())
          case results =>
            logger.error(s"Failed to create all Approvalrecords during migration ${results.filter(_.isLeft)}")
            Left(NotFoundError)
        }
    }

  startupDataMigration()
}
