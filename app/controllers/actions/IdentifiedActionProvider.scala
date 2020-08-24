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

package controllers.actions

import config.AppConfig
import javax.inject.Inject
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import play.api.Logger
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import scala.concurrent.{ExecutionContext, Future}

trait IdentifiedActionProvider {
  def apply(constraint: Predicate): IdentifiedAction
}

trait IdentifiedAction extends ActionBuilder[IdentifierRequest, AnyContent]

class PrivilegedIdentifiedActionProvider @Inject() (
  appConfig: AppConfig,
  bodyParser: BodyParsers.Default,
  override val authConnector: AuthConnector,
  val config: Configuration,
  val env: Environment
)(implicit val ec: ExecutionContext) extends IdentifiedActionProvider with AuthorisedFunctions with AuthRedirects {

  val logger = Logger(getClass())

  def apply(constraint: Predicate): IdentifiedAction =
    new IdentifiedAction {
      val parser = bodyParser
      val executionContext: ExecutionContext = ec

      override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] =
        invokeBlockWithConstraint(constraint)(request, block)
    }

  def invokeBlockWithConstraint[A](constraint: Predicate)(request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, None)

    authorised(constraint and AuthProviders(PrivilegedApplication))
      .retrieve(Retrievals.credentials and Retrievals.name and Retrievals.email and Retrievals.authorisedEnrolments) {
        case Some(Credentials(providerId, _)) ~ Some(Name(Some(name), _)) ~ Some(email) ~ authEnrolments =>
          block(IdentifierRequest(request, providerId, name, email, authEnrolments.enrolments.map(_.key).toList))
        case _ =>
          logger.warn("Identifier action could not retrieve required user details in method invokeBlock")
          Future.successful(Unauthorized)
      } recover {
      case err =>
        logger.error(s"FAILED Authorise/Retrievals with $err")
        Unauthorized
    }
  }

}