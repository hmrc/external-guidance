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

package endpoints

import java.time.ZonedDateTime

import data.ExamplePayloads._
import core.models.ocelot.Process
import core.models.errors.IncompleteDataError
import models.errors.OcelotError
import models.{ApprovalProcessPageReview, ApprovalProcessStatusChange, ApprovalProcessSummary}
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSResponse
import stubs.{AuditStub, AuthStub}
import support.IntegrationSpec
import models.Constants._
import play.api.libs.json.{Json, OFormat}
import play.api.http.{ContentTypes, Status}

class PostProcessReviewISpec extends IntegrationSpec {
implicit val formats: OFormat[ApprovalProcessSummary] = Json.format[ApprovalProcessSummary]

  val statusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusComplete)

  val statusChangeCompleteJson: JsValue = Json.toJson(statusChangeInfo)
  val pageUrl: String = "/feeling-bad"

  "Calling the approval2iReviewComplete POST endpoint with a valid payload and all pages reviewed" when {

    def populateDatabase(processToSave: JsValue): String = {
      lazy val request = buildRequest("/external-guidance/approval/2i-review")

      val result = await(request.post(processToSave))
      val json = result.body[JsValue].as[JsObject]
      val id = (json \ "id").as[String]
      lazy val pageUpdateRequest = buildRequest(s"/external-guidance/approval/$id/2i-page-review$pageUrl")
      val content =
        ApprovalProcessPageReview("1", pageUrl, "Ask the customer if they have a tea bag",
          Some("Yes"), ReviewCompleteStatus, ZonedDateTime.now(), Some("user id"))
      AuditStub.audit()
      AuthStub.authorise()
      await(pageUpdateRequest.post(Json.toJson(content)))

      id
    }
    val processToSave: JsValue = simpleValidProcess

    "the requested status update is Complete" should {
      lazy val id = populateDatabase(processToSave)
      lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-review")

      "set the new status to Complete" should {

        lazy val response: WSResponse = {
          AuditStub.audit()
          AuthStub.authorise()
          await(request.post(statusChangeCompleteJson))
        }

        "return an OK status code" in {
          response.status shouldBe OK
        }

        "set the status to Complete" in {
          lazy val request = buildRequest(s"/external-guidance/approval")
          lazy val response: WSResponse = {
            AuditStub.audit()
            AuthStub.authorise()
            await(request.get())
          }
          val list: List[ApprovalProcessSummary] = response.body[JsValue].as[List[ApprovalProcessSummary]]
          val updatedEntry = list.find(p => p.id == id)
          updatedEntry shouldBe Symbol("defined")
          updatedEntry.get.status shouldBe StatusComplete
        }

      }
    }
    "the requested status update is Published" should {
      val processCode = "CupOfTea"
      lazy val id = populateDatabase(processToSave)
      lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-review")
      val statusChangePublished = ApprovalProcessStatusChange("user id", "user name", StatusPublished)
      val statusChangePublishedJson: JsValue = Json.toJson(statusChangePublished)

      lazy val response: WSResponse = {
        AuditStub.audit()
        AuthStub.authorise()
        await(request.post(statusChangePublishedJson))
      }

      "return an OK status code" in {
        response.status shouldBe OK
      }

      "set status to Published" in {
        lazy val request = buildRequest(s"/external-guidance/approval")
        lazy val response: WSResponse = {
          AuditStub.audit()
          AuthStub.authorise()
          await(request.get())
        }
        val list: List[ApprovalProcessSummary] = response.body[JsValue].as[List[ApprovalProcessSummary]]
        val updatedEntry = list.find(p => p.id == id)
        updatedEntry shouldBe Symbol("defined")
        updatedEntry.get.status shouldBe StatusPublished
      }

      "add the process to the Published collection" in {
        lazy val request = buildRequest(s"/external-guidance/published/$processCode")
        lazy val response: WSResponse = {
          AuditStub.audit()
          await(request.get())
        }
        response.status shouldBe Status.OK
        response.contentType shouldBe ContentTypes.JSON
        val publishedEntry: Process = response.body[JsValue].as[Process]
        publishedEntry.meta.id shouldBe id
      }
    }

  }

  "Calling the approval2iReviewComplete POST endpoint with a valid payload and some pages not reviewed" when {

    def populateDatabase(processToSave: JsValue): String = {
      lazy val request = buildRequest("/external-guidance/approval/2i-review")

      val result = await(request.post(processToSave))
      val json = result.body[JsValue].as[JsObject]
      (json \ "id").as[String]
    }
    val processToSave: JsValue = simpleValidProcess
    lazy val id = populateDatabase(processToSave)

    lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-review")

    "a request is made to complete the review" should {

      lazy val response: WSResponse = {
        AuditStub.audit()
        AuthStub.authorise()
        await(request.post(statusChangeCompleteJson))
      }

      "return a INCOMPLETE_ERROR status code" in {
        response.status shouldBe BAD_REQUEST
      }

      "return INCOMPLETE_DATA_ERROR in the request Body" in {
        val json = response.body[JsValue].as[JsObject]
        json shouldBe Json.toJson(OcelotError(IncompleteDataError))
      }

    }
  }

  "Calling the approval2iReviewComplete POST endpoint with an id that doesn't exist" should {

    lazy val request = buildRequest(s"/external-guidance/approval/xyzinvalid/2i-review")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.authorise()
      await(request.post(statusChangeCompleteJson))
    }

    "return a NOT_FOUND status code" in {
      response.status shouldBe NOT_FOUND
    }

  }

  "Calling the approval2iReviewComplete POST endpoint without authorization" should {

    lazy val request = buildRequest( "/external-guidance/approval/oct90001/2i-review")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.unauthorised()
      await(request.post(statusChangeCompleteJson))
    }

    "return unauthorized" in {
      response.status shouldBe UNAUTHORIZED
    }
  }

  "Calling the approval2iReviewPageComplete POST endpoint with a valid payload" should {

    def populateDatabase(processToSave: JsValue): String = {
      lazy val request = buildRequest("/external-guidance/approval/2i-review")

      val result = await(request.post(processToSave))
      val json = result.body[JsValue].as[JsObject]
      (json \ "id").as[String]
    }

    val processToSave: JsValue = simpleValidProcess
    lazy val id = populateDatabase(processToSave)
    val pageUrl = "/feeling-bad"
    lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-page-review$pageUrl")
    val content =
      ApprovalProcessPageReview("1", pageUrl, pageUrl, Some("Yes"), ReviewCompleteStatus, ZonedDateTime.now(), Some("user id"))

    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.authorise()
      await(request.post(Json.toJson(content)))
    }

    "return a NO_CONTENT status code" in {
      response.status shouldBe NO_CONTENT
    }

    "set the status to ReviewCompleteStatus" in {
      lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-page-review$pageUrl")
      lazy val response: WSResponse = {
        AuditStub.audit()
        AuthStub.authorise()
        await(request.get())
      }
      val updatedEntry: ApprovalProcessPageReview = response.body[JsValue].as[ApprovalProcessPageReview]
      updatedEntry.status shouldBe ReviewCompleteStatus
    }

  }

  "Calling the approval2iReviewPageComplete POST endpoint with an id that doesn't exist" should {

    lazy val request = buildRequest(s"/external-guidance/approval/unknownId/2i-page-review/pageUrl")
    val content =
      ApprovalProcessPageReview("1", "pageUrl", "pageUrl", Some("Success"), ReviewCompleteStatus, ZonedDateTime.now(), Some("user id"))

    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.authorise()
      await(request.post(Json.toJson(content)))
    }

    "return a NOT_FOUND status code" in {
      response.status shouldBe NOT_FOUND
    }

  }

  "Calling the approval2iReviewPageComplete POST endpoint without authorization" should {

    lazy val request = buildRequest("/external-guidance/approval/oct90001/2i-page-review/page-1")

    val content = ApprovalProcessPageReview(
      "1",
      pageUrl,
      pageUrl,
      Some("Yes"),
      ReviewCompleteStatus,
      ZonedDateTime.now(),
      Some("user id"))

    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.unauthorised()
      await(request.post(Json.toJson(content)))
    }

    "return unauthorized" in {
      response.status shouldBe UNAUTHORIZED
    }
  }

  "Calling the approvalFactCheckComplete POST endpoint with a valid payload and some pages not reviewed" when {

    def populateDatabase(processToSave: JsValue): String = {
      lazy val request = buildRequest("/external-guidance/approval/fact-check")

      val result = await(request.post(processToSave))
      val json = result.body[JsValue].as[JsObject]
      (json \ "id").as[String]
    }
    val processToSave: JsValue = simpleValidProcess
    lazy val id = populateDatabase(processToSave)

    lazy val request = buildRequest(s"/external-guidance/approval/$id/fact-check")

    "a request is made to complete the review" should {

      lazy val response: WSResponse = {
        AuditStub.audit()
        AuthStub.authorise()
        await(request.post(statusChangeCompleteJson))
      }

      "return a INCOMPLETE_ERROR status code" in {
        response.status shouldBe BAD_REQUEST
      }

      "return INCOMPLETE_DATA_ERROR in the request Body" in {
        val json = response.body[JsValue].as[JsObject]
        json shouldBe Json.toJson(OcelotError(IncompleteDataError))
      }

    }
  }

  "Calling the approvalFactCheckComplete POST endpoint with a valid payload and all pages reviewed" when {

    def populateDatabase(processToSave: JsValue): String = {
      lazy val request = buildRequest("/external-guidance/approval/fact-check")

      val result = await(request.post(processToSave))
      val json = result.body[JsValue].as[JsObject]
      val id = (json \ "id").as[String]
      lazy val pageUpdateRequest = buildRequest(s"/external-guidance/approval/$id/fact-check-page-review$pageUrl")
      val content =
        ApprovalProcessPageReview("1", pageUrl, pageUrl, Some("Yes"), ReviewCompleteStatus, ZonedDateTime.now(), Some("user id"))
      AuditStub.audit()
      AuthStub.authorise()
      await(pageUpdateRequest.post(Json.toJson(content)))

      id
    }
    val processToSave: JsValue = simpleValidProcess
    "the requested status update is Complete" should {
      lazy val id = populateDatabase(processToSave)
      lazy val request = buildRequest(s"/external-guidance/approval/$id/fact-check")

      "set the new status to Complete" should {

        lazy val response: WSResponse = {
          AuditStub.audit()
          AuthStub.authorise()
          await(request.post(statusChangeCompleteJson))
        }

        "return an OK status code" in {
          response.status shouldBe OK
        }

        "set the status to Complete" in {
          lazy val request = buildRequest(s"/external-guidance/approval")
          lazy val response: WSResponse = {
            AuditStub.audit()
            AuthStub.authorise()
            await(request.get())
          }
          val list: List[ApprovalProcessSummary] = response.body[JsValue].as[List[ApprovalProcessSummary]]
          val updatedEntry = list.find(p => p.id == id)
          updatedEntry shouldBe Symbol("defined")
          updatedEntry.get.status shouldBe StatusComplete
        }
      }
    }
  }

  "Calling the approvalFactCheckInfo GET endpoint without authorization" should {

      lazy val request = buildRequest("/external-guidance/approval/oct90001/fact-check")
      lazy val response: WSResponse = {
        AuditStub.audit()
        AuthStub.unauthorised()
        await(request.get())
      }

    "return unauthorized" in {
      response.status shouldBe UNAUTHORIZED
    }
  }

  "Calling the approvalFactCheckComplete POST endpoint without authorization" should {

    lazy val request = buildRequest("/external-guidance/approval/oct90001/fact-check")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.unauthorised()
      await(request.post(statusChangeCompleteJson))
    }

    "return unauthorized" in {
      response.status shouldBe UNAUTHORIZED
    }
  }

  "Calling the approvalFactCheckPageInfo GET endpoint without authorization" should {

    lazy val request = buildRequest( "/external-guidance/approval/oct90001/fact-check-page-review/page-1")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.unauthorised()
      await(request.get())
    }

    "return unauthorized" in {
      response.status shouldBe UNAUTHORIZED
    }
  }

  "Calling the approvalFactCheckPageComplete POST endpoint without authorization" should {
    lazy val request = buildRequest( "/external-guidance/approval/oct90001/fact-check-page-review/page-1")
    lazy val response: WSResponse = {
      AuditStub.audit()
      AuthStub.unauthorised()
      val content =
        ApprovalProcessPageReview("1", pageUrl, pageUrl, Some("Yes"), ReviewCompleteStatus, ZonedDateTime.now(), Some("user id"))
      await(request.post(Json.toJson(content)))
    }

    "return unauthorized" in {
      response.status shouldBe UNAUTHORIZED
    }
  }
}
