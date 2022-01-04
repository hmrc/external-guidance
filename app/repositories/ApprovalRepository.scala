/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{Format, JsObject, JsResultException, Json}
import play.api.Logger
import models.ApprovalProcessMeta
import org.mongodb.scala._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._
import repositories.formatters.ApprovalProcessFormatter
import repositories.formatters.ApprovalProcessMetaFormatter
import core.models.ocelot.Process
import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait ApprovalRepository {
  def update(process: ApprovalProcess): Future[RequestOutcome[String]]
  def getById(id: String): Future[RequestOutcome[ApprovalProcess]]
  def getByProcessCode(processCode: String): Future[RequestOutcome[ApprovalProcess]]
  def approvalSummaryList(roles: List[String]): Future[RequestOutcome[List[ApprovalProcessSummary]]]
  def changeStatus(id: String, status: String, user: String): Future[RequestOutcome[Unit]]
  def getTimescalesInUse(): Future[RequestOutcome[List[String]]]
}

@Singleton
class ApprovalRepositoryImpl @Inject()(component: MongoComponent)(implicit appConfig: AppConfig, ec: ExecutionContext) extends
  PlayMongoRepository[ApprovalProcess](
      mongoComponent = component,
      collectionName = "approvalProcesses",
      domainFormat = ApprovalProcessFormatter.mongoFormat,
      indexes = Seq(IndexModel(ascending("meta.processCode"),
                               IndexOptions()
                                .name("approval-secondary-Index-process-code")
                                .unique(true))),
      extraCodecs = Seq(Codecs.playFormatCodec(ApprovalProcessMetaFormatter.mongoFormat)),
      replaceIndexes = true
    )
    with ApprovalRepository {

  val logger: Logger = Logger(getClass)
  def update(approvalProcess: ApprovalProcess): Future[RequestOutcome[String]] = {

    logger.warn(s"Saving process ${approvalProcess.id} to collection $collectionName")
    val selector = equal("_id", approvalProcess.id)
    val modifier = combine(Updates.inc("version",1),
                           Updates.set("meta", approvalProcess.meta),
                           Updates.set("process", Codecs.toBson(approvalProcess.process)))

    collection
      .findOneAndUpdate(selector, modifier, FindOneAndUpdateOptions().upsert(true))
      .toFutureOption()
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

  def getById(id: String): Future[RequestOutcome[ApprovalProcess]] =
    collection.find(equal("_id", id))
      .toFuture()
      .map {
        case Nil => Left(NotFoundError)
        case approvalProcess :: _  => Right(approvalProcess)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $id from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

  def getByProcessCode(processCode: String): Future[RequestOutcome[ApprovalProcess]] =
    collection
      .find(equal("meta.processCode", processCode))
      .toFuture()
      .map {
        case Nil => Left(NotFoundError)
        case approvalProcess :: _ => Right(approvalProcess)
      }
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve process $processCode from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$

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
      .withReadPreference(ReadPreference.primaryPreferred)
      .find(or(restrictions.toArray: _*))
      .projection(fields(include("meta", "process.meta.id"), excludeId()))
      // .cursor[ApprovalProcess]{ReadPreference.primaryPreferred}
      // .collect(maxDocs = -1, FailOnError[List[ApprovalProcess]]())
      .toFuture()
      .map { l =>
        Right(l.map(doc =>ApprovalProcessSummary(doc.meta.id, doc.meta.title, doc.meta.dateSubmitted, doc.meta.status, doc.meta.reviewType)).toList)
      }
      //.map(_ => Right(_.toList))
      //$COVERAGE-OFF$
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve list of processes from collection $collectionName failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
    //$COVERAGE-ON$
  }

  //$COVERAGE-OFF$
  def changeStatus(id: String, status: String, user: String): Future[RequestOutcome[Unit]] = {

    logger.warn(s"updating status of process $id to $status to collection $collectionName")
    val selector = equal("_id", id)
    val modifier = combine(set("meta.status", status), set("meta.updateUser", user), set("meta.lastModified", ZonedDateTime.now))

    collection
      .findOneAndUpdate(selector, modifier)
      .toFutureOption
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
    //$COVERAGE-ON$
  }

  //$COVERAGE-OFF$
  def getTimescalesInUse(): Future[RequestOutcome[List[String]]] =
    collection
      .withReadPreference(ReadPreference.primaryPreferred)
      .find(TimescalesInUseQuery)
      .toFuture()
      .map{ seq =>
        Right(seq.flatMap(pps => pps.process.validate[Process].fold(_ => Nil, p => p.timescales.keys.toList)).distinct.toList)
      }
      .recover{
        case error =>
          logger.error(s"Listing timescales used in the approval processes failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }
      //$COVERAGE-ON$

}
