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

import base.ControllerBaseSpec
import mocks.{MockAppConfig, MockAuthConnector}
import play.api.http.Status
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, ~}
import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolment, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TwoEyeReviewerAuthenticatedIdentifierActionSpec extends ControllerBaseSpec with MockAuthConnector {

  // Define simple harness class to represent controller
  class Harness(twoEyeReviewerAuthenticatedIdentifierAction: TwoEyeReviewerIdentifierAction) {

    def onPageLoad(): Action[AnyContent] = twoEyeReviewerAuthenticatedIdentifierAction { _ =>
      Results.Ok
    }
  }

  trait AuthTestData {

    val path: String = "/path"
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path)

    val enrolments: Enrolments = Enrolments(Set(Enrolment(key = "2iReviewer")))

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

    lazy val twoEyeReviewerAuthAction = new TwoEyeReviewerAuthenticatedIdentifierAction(
      mockAuthConnector,
      MockAppConfig,
      bodyParser,
      config,
      env
    )

    lazy val target = new Harness(twoEyeReviewerAuthAction)
  }

  "TwoEyeReviewerAuthenticatedIdentifierAction" should {

    "grant access if authorisation is successful" in new AuthTestData {

      val authResult = new ~(new ~(new ~(Some(Credentials("id", "type")), Some(Name(Some("name"), None))), Some("email")), enrolments)

      MockAuthConnector.authorize().returns(Future.successful(authResult))

      val result: Future[Result] = target.onPageLoad()(fakeRequest)

      status(result) shouldBe Status.OK
    }

    "deny access to user if no credentials returned" in new AuthTestData {

      val authResult = new ~(new ~(new ~(None, Some(Name(Some("name"), None))), Some("email")), enrolments)

      MockAuthConnector.authorize().returns(Future.successful(authResult))

      val result: Future[Result] = target.onPageLoad()(fakeRequest)

      status(result) shouldBe UNAUTHORIZED
    }

    "deny access to user if no name instance returned" in new AuthTestData {

      val authResult = new ~(new ~(new ~(Some(Credentials("id", "type")), None), Some("email")), enrolments)

      MockAuthConnector.authorize().returns(Future.successful(authResult))

      val result: Future[Result] = target.onPageLoad()(fakeRequest)

      status(result) shouldBe UNAUTHORIZED
    }

    "deny access to user if no name detail returned" in new AuthTestData {

      val authResult = new ~(new ~(new ~(Some(Credentials("id", "type")), Some(Name(None, None))), Some("email")), enrolments)

      MockAuthConnector.authorize().returns(Future.successful(authResult))

      val result: Future[Result] = target.onPageLoad()(fakeRequest)

      status(result) shouldBe UNAUTHORIZED
    }

    "deny access to user if no email address returned" in new AuthTestData {

      val authResult = new ~(new ~(new ~(Some(Credentials("id", "type")), Some(Name(Some("name"), None))), None), enrolments)

      MockAuthConnector.authorize().returns(Future.successful(authResult))

      val result: Future[Result] = target.onPageLoad()(fakeRequest)

      status(result) shouldBe UNAUTHORIZED
    }

    "deny access to user if no defined details returned" in new AuthTestData {

      val authResult = new ~(new ~(new ~(None, None), None), enrolments)

      MockAuthConnector.authorize().returns(Future.successful(authResult))

      val result: Future[Result] = target.onPageLoad()(fakeRequest)

      status(result) shouldBe UNAUTHORIZED
    }

    "raise unauthorized if no session record exists" in new AuthTestData {

      val error: Throwable = AuthorisationException.fromString("SessionRecordNotFound")
      MockAuthConnector.authorize().returns(Future.failed(error))

      val result: Future[Result] = target.onPageLoad()(fakeRequest)

      status(result) shouldBe UNAUTHORIZED
    }

    "raise unauthorized if authorisation fails" in new AuthTestData {

      val error: Throwable = AuthorisationException.fromString("InsufficientEnrolments")
      MockAuthConnector.authorize().returns(Future.failed(error))

      val result: Future[Result] = target.onPageLoad()(fakeRequest)

      status(result) shouldBe UNAUTHORIZED
    }

  }

}

