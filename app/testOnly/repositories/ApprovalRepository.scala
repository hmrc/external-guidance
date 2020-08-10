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

package testOnly.repositories

import javax.inject.{Inject, Singleton}
import models.errors.DatabaseError
import models.{ApprovalProcess, RequestOutcome}
import play.api.libs.json.Format
import play.modules.reactivemongo.ReactiveMongoComponent
import repositories.formatters.ApprovalProcessFormatter
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApprovalRepository @Inject() (mongoComponent: ReactiveMongoComponent)
    extends ReactiveRepository[ApprovalProcess, String](
      collectionName = "approvalProcesses",
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = ApprovalProcessFormatter.mongoFormat,
      idFormat = implicitly[Format[String]]
    ) {

  def delete(id: String): Future[RequestOutcome[String]] = {

    logger.info(s"[test-only] Deleting approval process with the ID $id")

    removeById(id)
      .map { _ =>
        Right(id)
      }
      .recover {
        case error =>
          logger.error(s"[test-only] Failed to delete approval process with the ID $id", error)
          Left(DatabaseError)
      }
  }

}
