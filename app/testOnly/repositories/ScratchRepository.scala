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

package testOnly.repositories

import core.models.RequestOutcome
import core.models.errors.DatabaseError
import models.ScratchProcess
import org.mongodb.scala.model.Filters.equal
import play.api.Logger.logger
import repositories.formatters.ScratchProcessFormatter
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class ScratchRepository  @Inject() (mongoComponent: MongoComponent)
  extends PlayMongoRepository[ScratchProcess](
    collectionName = "scratchProcesses",
    mongoComponent = mongoComponent,
    domainFormat = ScratchProcessFormatter.mongoFormat,
    indexes = Seq()
  ) {

  def delete(id: String): Future[RequestOutcome[String]] = {

    logger.info(s"[test-only] Deleting scratch process with the ID $id")

    collection.deleteOne(equal("_id", id)).toFuture()
      .map { _ =>
        Right(id)
      }
      .recover {
        case error =>
          logger.error(s"[test-only] Failed to delete scratch process with the ID $id", error)
          Left(DatabaseError)
      }
  }
}
