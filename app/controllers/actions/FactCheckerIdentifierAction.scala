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

package controllers.actions

import config.AppConfig
import javax.inject.Inject
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.{ExecutionContext, Future}

trait FactCheckerIdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class FactCheckerAuthenticatedIdentifierAction @Inject() (
    override val authConnector: AuthConnector,
    appConfig: AppConfig,
    val parser: BodyParsers.Default,
    val config: Configuration,
    val env: Environment
)(
    implicit val executionContext: ExecutionContext
) extends FactCheckerIdentifierAction
    with AuthorisedFunctions
    with AuthRedirects {

  val logger: Logger = Logger(getClass)

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    // Restrict access to users with fact checker role
    authorised(Enrolment(appConfig.factCheckerRole) and AuthProviders(PrivilegedApplication))
      .retrieve(Retrievals.credentials and Retrievals.name and Retrievals.email and Retrievals.authorisedEnrolments) {
        case Some(Credentials(providerId, _)) ~ Some(Name(Some(name), _)) ~ Some(email) ~ authEnrolments =>
          block(IdentifierRequest(request, providerId, name, email, authEnrolments.enrolments.map(_.key).toList))
        case _ =>
          logger.warn("Fact Checker Identifier action could not retrieve required user details in method invokeBlock")
          Future.successful(Unauthorized)
      } recover {
      case _ => Unauthorized
    }
  }
}
