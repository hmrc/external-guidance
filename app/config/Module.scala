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

package config

import core.services.{DefaultTodayProvider, TodayProvider}
import com.google.inject.AbstractModule
import controllers.actions._
import repositories._
import migrate.repositories._

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[AppConfig]).to(classOf[AppConfigImpl])
    bind(classOf[PublishedRepository]).to(classOf[PublishedRepositoryImpl])
    bind(classOf[ScratchRepository]).to(classOf[ScratchRepositoryImpl])
    bind(classOf[ApprovalRepository]).to(classOf[ApprovalRepositoryImpl])
    bind(classOf[ApprovalsRepository]).to(classOf[ApprovalsRepositoryImpl])
    bind(classOf[ArchiveRepository]).to(classOf[ArchiveRepositoryImpl])
    bind(classOf[TimescalesRepository]).to(classOf[TimescalesRepositoryImpl])
    bind(classOf[ApprovalProcessReviewRepository]).to(classOf[ApprovalProcessReviewRepositoryImpl])
    bind(classOf[AllRolesAction]).to(classOf[AllRolesAuthenticatedAction])
    bind(classOf[FactCheckerAction]).to(classOf[FactCheckerAuthenticatedAction])
    bind(classOf[TwoEyeReviewerAction]).to(classOf[TwoEyeReviewerAuthenticatedAction])
    bind(classOf[TodayProvider]).to(classOf[DefaultTodayProvider])
  }
}

