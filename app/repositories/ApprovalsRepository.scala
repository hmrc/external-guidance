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

package repositories

import config.AppConfig
import core.models.RequestOutcome
import core.models.errors.{DatabaseError, DuplicateKeyError, NotFoundError}
import models.{Approval, ApprovalProcessPageReview, ApprovalProcessSummary, Constants, ProcessSummary}
import play.api.Logger
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import models.ApprovalProcessMeta
import core.models.ocelot.Process
import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import core.models.MongoDateTimeFormats.zonedDateTimeFormat
import core.models.MongoDateTimeFormats.Implicits._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.result.DeleteResult
import models.ApprovalReview

 //$COVERAGE-OFF$
 
trait ApprovalsRepository {
  def createOrUpdate(process: Approval): Future[RequestOutcome[String]]
  def updateReview(id: String, version: Int, reviewType: String, updateUser: String, result: String): Future[RequestOutcome[Unit]]
  def updatePageReview(id: String, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]]
  def getById(id: String): Future[RequestOutcome[Approval]]
  def getByProcessCode(processCode: String): Future[RequestOutcome[Approval]]
  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[List[ApprovalProcessSummary]]]
  def changeStatus(id: String, status: String, user: String): Future[RequestOutcome[Unit]]
  def getTimescalesInUse(): Future[RequestOutcome[List[String]]]
  def delete(id: String): Future[RequestOutcome[Unit]]
  def processSummaries(): Future[RequestOutcome[List[ProcessSummary]]]
}

@Singleton
class ApprovalsRepositoryImpl @Inject()(component: MongoComponent)(implicit appConfig: AppConfig, ec: ExecutionContext) extends
  PlayMongoRepository[Approval](
      mongoComponent = component,
      collectionName = "approvals",
      domainFormat = Approval.format,
      indexes = Seq(IndexModel(ascending("meta.processCode"),
                               IndexOptions()
                                .name("approvals-secondary-Index-process-code")
                                .unique(true))),
      extraCodecs = Seq(Codecs.playFormatCodec(ApprovalProcessMeta.mongoFormat),
                        Codecs.playFormatCodec(ApprovalReview.format),
                        Codecs.playFormatCodec(zonedDateTimeFormat)),
      replaceIndexes = true
    )
    with ApprovalsRepository {

  val logger: Logger = Logger(getClass)
  override lazy val requiresTtlIndex = false

  def createOrUpdate(approvalProcess: Approval): Future[RequestOutcome[String]] = {
    logger.warn(s"Saving process ${approvalProcess.id} to collection $collectionName")
    val selector = equal("_id", approvalProcess.id)
    val modifier = combine(Updates.inc("version",1),
                           Updates.set("meta", Codecs.toBson(approvalProcess.meta)),
                           Updates.set("review", Codecs.toBson(approvalProcess.review)),
                           Updates.set("process", Codecs.toBson(approvalProcess.process)))

    collection
      .findOneAndUpdate(selector, modifier, FindOneAndUpdateOptions().upsert(true))
      .toFutureOption()
      .map { _ =>
        Right(approvalProcess.id)
      }
      .recover {
        case ex: MongoCommandException if ex.getErrorCode == 11000 =>
          logger.error(s"Attempt to persist approval process ${approvalProcess.id} with duplicate processCode: ${approvalProcess.meta.processCode}")
          Left(DuplicateKeyError)
        case ex =>
          logger.error(s"Attempt to persist process ${approvalProcess.id} to collection $collectionName failed with error : ${ex.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  def updateReview(id: String, version: Int, reviewType: String, updateUser: String, result: String): Future[RequestOutcome[Unit]] = {
    val modifier = combine(
      set("review.result", result),
      set("review.completionUser", updateUser),
      set("review.completionDate", Codecs.toBson(ZonedDateTime.now))
    )

    collection
      .findOneAndUpdate(equal("_id", id), modifier)
      .toFutureOption()
      .map{_.fold[RequestOutcome[Unit]](Left(NotFoundError))( _ => Right(()))}
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to update review of approval $id from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  }

  def updatePageReview(id: String, pageUrl: String, reviewType: String, reviewInfo: ApprovalProcessPageReview): Future[RequestOutcome[Unit]] = {
    val selector = and(equal("_id", id),
                       equal("meta.reviewType", reviewType),
                       equal("review.pages.pageUrl", pageUrl))
    val modifier = combine(
      Vector(set("review.pages.$.status",reviewInfo.status), set("review.pages.$.updateDate", Codecs.toBson(ZonedDateTime.now))) ++
      reviewInfo.result.fold[Vector[Bson]](Vector())(r => Vector(set("review.pages.$.result", r))) ++
      reviewInfo.updateUser.fold[Vector[Bson]](Vector())(u => Vector(set("review.pages.$.updateUser", u))): _*
    )

    collection
      .findOneAndUpdate(selector, modifier)
      .toFutureOption()
      .map{_.fold[RequestOutcome[Unit]](Left(NotFoundError))( _ => Right(()))}
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to update page review on pageUrl $pageUrl of approval $id from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  }

  //$COVERAGE-OFF$
  def getById(id: String): Future[RequestOutcome[Approval]] =
    collection
      .find(equal("_id", id))
      .headOption()
      .map {
        case None => Left(NotFoundError)
        case Some(approvalProcess) => Right(approvalProcess)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

  //$COVERAGE-OFF$
  def getByProcessCode(processCode: String): Future[RequestOutcome[Approval]] =
    collection
      .find(equal("meta.processCode", processCode))
      .headOption()
      .map {
        case None => Left(NotFoundError)
        case Some(approvalProcess) => Right(approvalProcess)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $processCode from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }

  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[List[ApprovalProcessSummary]]] = {
    val TwoEyeRestriction = equal("meta.reviewType", Constants.ReviewType2i)
    val FactCheckRestriction = equal("meta.reviewType", Constants.ReviewTypeFactCheck)

    val restrictions  = roles.flatMap {
      case appConfig.twoEyeReviewerRole => List(TwoEyeRestriction)
      case appConfig.factCheckerRole => List(FactCheckRestriction)
      case appConfig.designerRole => List(FactCheckRestriction, TwoEyeRestriction)
      case _ => Nil
    }.distinct

    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find(or(restrictions.toSeq: _*))
      .projection(fields(include("meta", "process.meta.id", "review"), excludeId()))
      .collect().toFutureOption()
      .map {
        case None => Right(Nil)
        case Some(approvals) =>
          Right(approvals.map(doc => ApprovalProcessSummary(doc.meta.id, doc.meta.title, doc.meta.dateSubmitted, doc.meta.status, doc.meta.reviewType)).toList)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve list of processes from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  }

  def changeStatus(id: String, status: String, user: String): Future[RequestOutcome[Unit]] = {

    logger.warn(s"updating status of process $id to $status to collection $collectionName")
    val selector = equal("_id", id)
    val modifier = combine(set("meta.status", status), set("meta.updateUser", user), set("meta.lastModified", Codecs.toBson(ZonedDateTime.now)))

    collection
      .findOneAndUpdate(selector, modifier)
      .toFutureOption()
      .map {
        _.fold[RequestOutcome[Unit]]{
          logger.error(s"Invalid Request - could not find process $id")
          Left(NotFoundError)
        }( _ => Right(()))
      }
      .recover {
        case error =>
          logger.error(s"Attempt to change status of process $id to collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
  }

  def getTimescalesInUse(): Future[RequestOutcome[List[String]]] =
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find(TimescalesInUseQuery)
      .collect().toFutureOption()
      .map{
        case None => Right(Nil)
        case Some(ids) => Right(ids.flatMap(pps => pps.process.validate[Process].fold(_ => Nil, p => p.timescales.keys.toList)).distinct.toList)
      }
      .recover{
        case error =>
          logger.error(s"Listing timescales used in the approval processes failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }

  def delete(id: String): Future[RequestOutcome[Unit]] =
    collection
      .deleteOne(equal("_id", id))
      .toFutureOption()
      .map {
        case Some(result: DeleteResult) if result.getDeletedCount > 0 => Right(())
        case _ =>
          logger.error(s"Attempt to delete process $id from collection approvals failed")
          Left(DatabaseError)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to delete process $id from collection approvals failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }

  def processSummaries(): Future[RequestOutcome[List[ProcessSummary]]] =
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find()
      .collect()
      .toFutureOption()
      .map{
        case None => Right(Nil)
        case Some(res) =>
          val summaries = res.map{p =>
            val process: Process = p.process.as[Process]
            ProcessSummary(
              p.id,
              p.meta.processCode,
              process.meta.version,
              process.meta.lastAuthor,
              passphraseStatus(process),
              p.meta.lastModified,
              "",
              p.meta.reviewType
            )
          }
          Right(summaries.toList)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve approval process summaries failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
 //$COVERAGE-ON$
}
