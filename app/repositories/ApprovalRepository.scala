/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.ZonedDateTime

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.errors.{DatabaseError, DuplicateKeyError, NotFoundError}
import models.{ApprovalProcess, ApprovalProcessSummary, RequestOutcome}
import play.api.libs.json.{Format, JsObject, JsResultException, Json, JsonValidationError}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.ReadPreference
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.ImplicitBSONHandlers._
import repositories.formatters.ApprovalProcessFormatter
import repositories.formatters.ApprovalProcessMetaFormatter._
import uk.gov.hmrc.mongo.ReactiveRepository
import utils.Constants

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait ApprovalRepository {
  def update(process: ApprovalProcess): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[ApprovalProcess]]
  def getByProcessCode(processCode: String): Future[RequestOutcome[ApprovalProcess]]
  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[List[ApprovalProcessSummary]]]
  def changeStatus(id: String, status: String, user: String): Future[RequestOutcome[Unit]]

  val TwoEyeRestriction: JsObject = Json.obj("meta.reviewType" -> Constants.ReviewType2i)
  val FactCheckRestriction: JsObject = Json.obj("meta.reviewType" -> Constants.ReviewTypeFactCheck)
}

@Singleton
class ApprovalRepositoryImpl @Inject() (implicit mongoComponent: ReactiveMongoComponent, appConfig: AppConfig, ec: ExecutionContext)
    extends ReactiveRepository[ApprovalProcess, String](
      collectionName = "approvalProcesses",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = ApprovalProcessFormatter.mongoFormat,
      idFormat = implicitly[Format[String]]
    )
    with ApprovalRepository {

  private def processCodeIndexName = "approval-secondary-Index-process-code"

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] =
    // If current configuration includes an update to the unique attribute of the processCode index, drop the current index to allow its re-creation
    collection.indexesManager.list().flatMap { indexes =>
      indexes
        .filter(idx =>
          idx.name == Some(processCodeIndexName) && !idx.unique
        )
        .map { i =>
          logger.warn(s"Dropping $processCodeIndexName ready for re-creation, due to configured unique change")
          collection.indexesManager.drop(processCodeIndexName).map(ret => logger.info(s"Drop of $processCodeIndexName index returned $ret"))
        }

      super.ensureIndexes
    }

  override def indexes: Seq[Index] = Seq(
    Index(
      key = Seq("meta.processCode" -> IndexType.Ascending),
      name = Some(processCodeIndexName),
      unique = true
    )
  )

  def update(approvalProcess: ApprovalProcess): Future[RequestOutcome[String]] = {

    logger.info(s"Saving process ${approvalProcess.id} to collection $collectionName")
    val selector = Json.obj("_id" -> approvalProcess.id)
    val metaJson = Json.toJson(approvalProcess.meta)
    val modifier = Json.obj("$inc" -> Json.obj("version" -> 1), "$set" -> Json.obj("meta" -> metaJson, "process" -> approvalProcess.process))

    this
      .findAndUpdate(selector, modifier, upsert = true)
      .map { _ =>
        Right(approvalProcess.id)
      }
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

    findById(id)
      .map {
        case Some(approvalProcess) => Right(approvalProcess)
        case None => Left(NotFoundError)
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

    val selector = Json.obj("meta.processCode" -> processCode)
    collection
      .find[JsObject, JsObject](selector)
      .one[ApprovalProcess]
      .map {
        case Some(approvalProcess) => Right(approvalProcess)
        case None => Left(NotFoundError)
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

    val restrictions: List[JsObject] = roles.flatMap {
      case appConfig.twoEyeReviewerRole => List(TwoEyeRestriction)
      case appConfig.factCheckerRole => List(FactCheckRestriction)
      case appConfig.designerRole => List(FactCheckRestriction, TwoEyeRestriction)
      case _ => Nil
    }.distinct

    val selector = Json.obj("$or" -> restrictions)
    val projection = Some(Json.obj("meta" -> 1, "process.meta.id" -> 1))

    collection
      .find(
        selector,
        projection
      )
      .cursor[ApprovalProcess](ReadPreference.primaryPreferred)
      .collect(maxDocs = -1, FailOnError[List[ApprovalProcess]]())
      .map {
        _.map { doc =>
          ApprovalProcessSummary(doc.meta.id, doc.meta.title, doc.meta.dateSubmitted, doc.meta.status, doc.meta.reviewType)
        }
      }
      .map(list => Right(list))
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
    val selector = Json.obj("_id" -> id)
    val modifier = Json.obj("$set" -> Json.obj("meta.status" -> status, "meta.updateUser" -> user, "meta.lastModified" ->ZonedDateTime.now))

    this
      .findAndUpdate(selector, modifier)
      .map { result =>
        if (result.result[ApprovalProcess].isDefined) {
          Right(())
        } else {
          logger.error(s"Invalid Request - could not find process $id")
          Left(NotFoundError)
        }
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
