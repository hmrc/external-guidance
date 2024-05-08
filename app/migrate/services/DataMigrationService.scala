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
import models._
import migrate.models._
import migrate.repositories._
import play.api.libs.json._
import repositories._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import core.models.RequestOutcome
import core.models.ocelot.Process
import core.models.errors.{InvalidProcessError, NotFoundError}

trait MigrateData

@Singleton
class DataMigrationService @Inject()(
  serviceLock: ServiceLock,
  appConfig: AppConfig,
  approvalRespository: ApprovalRepository,
  publishedRepository: PublishedRepository,
  approvalsRepository: ApprovalsRepository
)(implicit ec: ExecutionContext) extends Logging with MigrateData {
  val processIds: List[String] =
    List(
      "ext90193",
      "ext90195",
      "ext90228",
      "ext90209",
      "ext90184",
      "ext90179",
      "ext90203",
      "ext90107",
      "ext90064",
      "ext90005",
      "ext90111",
      "ext90224"
    )

  private def approval(pid: String): Future[RequestOutcome[ApprovalProcess]] =
    approvalRespository.byId(pid)

  private def published(pid: String): Future[RequestOutcome[PublishedProcess]] =
    publishedRepository.getById(pid)

  private def toProcess(jsObj: JsObject): Option[Process] =
    jsObj.validate[Process].fold(_ => None, process => Some(process))

  def dataCheck2(): Future[List[RequestOutcome[Unit]]] =
    Future.sequence(processIds.map{ pid =>
      approval(pid).flatMap{
        case Right(app) =>
          toProcess(app.process).fold[Future[RequestOutcome[Unit]]](Future.successful(Left(NotFoundError))){appProcess =>
            published(pid).flatMap{
              case Right(pub) =>
                toProcess(pub.process).fold[Future[RequestOutcome[Unit]]]{
                  logger.warn(s"Unable to parse published process $pid")
                  Future.successful(Left(InvalidProcessError))
                }{ pubProcess =>
                  logger.warn(s"Process $pid, App version:${appProcess.meta.version}, stat:${app.meta.status}, sub:${app.meta.dateSubmitted}" +
                              s" mod:${app.meta.lastModified}, Pub version ${pubProcess.meta.version}, pub:${pub.datePublished}")
                  Future.successful(Right(()))
                }
              case Left(err) =>
                logger.warn(s"Process $pid, App version:${appProcess.meta.version}, stat:${app.meta.status}, mod:${app.meta.lastModified}")
                logger.warn(s"Unable to find published process $pid")
                Future.successful(Left(NotFoundError))
            }
          }
        case Left(err) => Future.successful(Left(err))
      }
    })

  if (appConfig.enableDataMigration) {
    logger.warn(s"Start-up lock claim")
    serviceLock.lock("Start-up").map{lockOption =>
      lockOption.fold{
        logger.warn(s"Start-up lock already taken")
      }{lock => {
        logger.warn(s"Starting Data check")
        dataCheck2()
        }.map(_ => {
          logger.warn(s"Data check complete")
          serviceLock.unlock(lock.owner)
        }
        )
      }
    }
  }
}
