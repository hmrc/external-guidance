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

package repositories

import config.AppConfig
import core.models.RequestOutcome
import core.models.errors.{DatabaseError, DuplicateKeyError, NotFoundError}
import models.{ApprovalProcess, ApprovalProcessSummary, Constants}
import org.bson.conversions.Bson
import org.mongodb.scala.ReadPreference
import org.mongodb.scala.model.Filters.{equal, or}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Projections.include
import org.mongodb.scala.model.Updates.{combine, inc, set}
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.Logger.logger
import play.api.libs.json.JsResultException
import repositories.formatters.ApprovalProcessFormatter
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait ApprovalRepository {
  def update(process: ApprovalProcess): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[ApprovalProcess]]
  def getByProcessCode(processCode: String): Future[RequestOutcome[ApprovalProcess]]
  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[List[ApprovalProcessSummary]]]
  def changeStatus(id: String, status: String, user: String): Future[RequestOutcome[Unit]]

  val TwoEyeRestriction: Bson = equal("meta.reviewType", Constants.ReviewType2i)
  val FactCheckRestriction: Bson = equal("meta.reviewType", Constants.ReviewTypeFactCheck)
}

@Singleton
class ApprovalRepositoryImpl @Inject() (implicit mongoComponent: MongoComponent, appConfig: AppConfig, ec: ExecutionContext)
    extends PlayMongoRepository[ApprovalProcess](
      collectionName = "approvalProcesses",
      mongoComponent = mongoComponent,
      domainFormat = ApprovalProcessFormatter.mongoFormat,
      indexes = Seq(IndexModel(
        ascending("meta.processCode"),
        IndexOptions().name("approval-secondary-Index-process-code").unique(true))
      )
    )
    with ApprovalRepository {

  def update(approvalProcess: ApprovalProcess): Future[RequestOutcome[String]] = {

    logger.info(s"Saving process ${approvalProcess.id} to collection $collectionName")

    collection.findOneAndUpdate(
      equal("_id", approvalProcess.id),
      combine(
        inc("version", 1),
        set("meta", approvalProcess.meta),
        set("process", approvalProcess.process)
      )
    )
    .toFutureOption()
    .map (_ => Right(approvalProcess.id))
    //$COVERAGE-OFF$
    .recover {
      case e: JsResultException if hasDupeKeyViolation(e) =>
        logger.error(s"Attempt to persist approval process ${approvalProcess.id} with duplicate processCode : ${approvalProcess.meta.processCode}")
        Left(DuplicateKeyError)
      case error =>
        logger.error(s"Attempt to persist process ${approvalProcess.id} to collection $collectionName failed with error : ${error.getMessage}")
        Left(DatabaseError)
    }
    //$COVERAGE-ON$
  }

  def getById(id: String): Future[RequestOutcome[ApprovalProcess]] = {

    collection.find(equal("_id", id)).toFuture()
      .map { _.headOption match {
          case Some(approvalProcess) => Right(approvalProcess)
          case None => Left(NotFoundError)
        }
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def getByProcessCode(processCode: String): Future[RequestOutcome[ApprovalProcess]] = {

    collection
      .find(equal("meta.processCode", processCode))
      .toFuture()
      .map { _.headOption match {
          case Some(approvalProcess) => Right(approvalProcess)
          case None => Left(NotFoundError)
        }
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $processCode from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[List[ApprovalProcessSummary]]] = {

    val restrictions: List[Bson] = roles.flatMap {
      case appConfig.twoEyeReviewerRole => List(TwoEyeRestriction)
      case appConfig.factCheckerRole => List(FactCheckRestriction)
      case appConfig.designerRole => List(FactCheckRestriction, TwoEyeRestriction)
      case _ => Nil
    }.distinct

    collection.withReadPreference(ReadPreference.secondaryPreferred)
      .find(or(restrictions:_*))
      .projection(include("meta", "process.meta.id"))
      .toFuture()
      .map { seq =>
        Right(seq.toList.map { doc =>
          ApprovalProcessSummary(doc.meta.id, doc.meta.title, doc.meta.dateSubmitted, doc.meta.status, doc.meta.reviewType)
        })
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve list of processes from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }

  def changeStatus(id: String, status: String, user: String): Future[RequestOutcome[Unit]] = {

    logger.info(s"updating status of process $id to $status to collection $collectionName")

    collection.findOneAndUpdate(
        filter = equal("_id", id),
        update = combine(
          set("meta.status", status),
          set("meta.updateUser", user),
          set("meta.lastModified", ZonedDateTime.now)
        )
      ).toFutureOption()
      .map {
        case Some(_) => Right(())
        case None =>
          logger.error(s"Invalid Request - could not find process $id")
          Left(NotFoundError)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to change status of process $id to collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$
  }

}
