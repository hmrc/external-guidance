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

package migrate.repositories

import core.models.RequestOutcome
import core.models.errors.DatabaseError
import migrate.models._
import play.api.Logger
import org.mongodb.scala._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import models.ApprovalProcessMeta
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import core.models.MongoDateTimeFormats.zonedDateTimeFormat
import org.mongodb.scala.model.Filters._


trait ApprovalRepository {
  def list(): Future[RequestOutcome[List[ApprovalProcess]]]
}

@Singleton
class ApprovalRepositoryImpl @Inject()(component: MongoComponent)(implicit ec: ExecutionContext) extends
  PlayMongoRepository[ApprovalProcess](
      mongoComponent = component,
      collectionName = "approvalProcesses",
      domainFormat = ApprovalProcess.mongoFormat,
      indexes = Seq(IndexModel(ascending("meta.processCode"),
                               IndexOptions()
                                .name("approval-secondary-Index-process-code")
                                .unique(true))),
      extraCodecs = Seq(Codecs.playFormatCodec(ApprovalProcessMeta.mongoFormat),
                        Codecs.playFormatCodec(zonedDateTimeFormat)),
      replaceIndexes = true
    )
    with ApprovalRepository {

  val logger: Logger = Logger(getClass)
  override lazy val requiresTtlIndex = false

      //$COVERAGE-OFF$

  def list(): Future[RequestOutcome[List[ApprovalProcess]]] = 
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find(nin("meta.status", List("Published", "Archived")))
      .collect()
      .toFutureOption()
      .map{
        case None => Right(Nil)
        case Some(res) => Right(res.toList)
      }
      .recover {
        case error =>
          logger.error(s"Attempt to retrieve approval list failed with error : ${error.getMessage}")
          Left(DatabaseError)
      }


 //$COVERAGE-ON$
}
