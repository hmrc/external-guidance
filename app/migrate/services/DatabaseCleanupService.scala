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

import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.mongo._
import scala.concurrent.{ExecutionContext, Future}
import cats.data.OptionT
import cats.implicits._

@Singleton
class DatabaseCleanupService @Inject() (component: MongoComponent)(implicit ec: ExecutionContext, val appConfig: AppConfig) extends Logging {
  private def dropCollection(name: String): Future[Option[Unit]] =
    component.database.getCollection(name).drop().headOption().map{outcome =>
      outcome.fold(logger.warn(s"Collection $name either does not exist or could not be dropped"))(_ => logger.warn(s"Collection $name sucessfully dropped"))
      outcome
    }.recover{
      case error =>
        logger.error(s"Dropping collection $name returned an error. Error: $error")
        None
    }

  logger.warn(s"Database cleanup service")

  for{
    _ <- OptionT(dropCollection("approvalProcesses"))
    _ <- OptionT(dropCollection("approvalProcessReviews"))
    _ <- OptionT(dropCollection("locks"))
  } yield {
    logger.warn(s"Database cleanup complete")
  }
}
