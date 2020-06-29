/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDateTime

import data.ExamplePayloads._
import models.errors.IncompleteDataError
import models.ocelot.Process
import models.{ApprovalProcessPageReview, ApprovalProcessStatusChange, ApprovalProcessSummary}
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSResponse
import stubs.AuditStub
import support.IntegrationSpec
import utils.Constants._

class PostProcessReviewISpec extends IntegrationSpec {

  val statusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusWithDesignerForUpdate)

  val statusChangeWithDesignerJson: JsValue = Json.toJson(statusChangeInfo)
  val pageUrl: String = "/feeling-bad"

  "Calling the approval2iReviewComplete POST endpoint with a valid payload and all pages reviewed" when {

    def populateDatabase(processToSave: JsValue): String = {
      lazy val request = buildRequest("/external-guidance/approval/2i-review")

      val result = await(request.post(processToSave))
      val json = result.body[JsValue].as[JsObject]
      val id = (json \ "id").as[String]
      lazy val pageUpdateRequest = buildRequest(s"/external-guidance/approval/$id/2i-page-review$pageUrl")
      val content = ApprovalProcessPageReview("1", pageUrl, Some("Yes"), ReviewCompleteStatus, Some("A basic comment"), LocalDateTime.now(), Some("User1"))
      await(pageUpdateRequest.post(Json.toJson(content)))

      id
    }
    val processToSave: JsValue = simpleValidProcess

    "the requested status update is With DesignerForUpdate" should {
      lazy val id = populateDatabase(processToSave)
      lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-review")

      "set the new status to With Designer For Update" should {

        lazy val response: WSResponse = {
          AuditStub.audit()
          await(request.post(statusChangeWithDesignerJson))
        }

        "return an OK status code" in {
          response.status shouldBe OK
        }

        "set the status to WithDesignerForUpdate" in {
          lazy val request = buildRequest(s"/external-guidance/approval")
          lazy val response: WSResponse = {
            AuditStub.audit()
            await(request.get())
          }
          val list: List[ApprovalProcessSummary] = response.body[JsValue].as[List[ApprovalProcessSummary]]
          val updatedEntry = list.find(p => p.id == id)
          updatedEntry shouldBe 'defined
          updatedEntry.get.status shouldBe StatusWithDesignerForUpdate
        }

      }
    }
    "the requested status update is Published" should {
      lazy val id = populateDatabase(processToSave)
      lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-review")
      val statusChangePublished = ApprovalProcessStatusChange("user id", "user name", StatusPublished)
      val statusChangePublishedJson: JsValue = Json.toJson(statusChangePublished)

      lazy val response: WSResponse = {
        AuditStub.audit()
        await(request.post(statusChangePublishedJson))
      }

      "return an OK status code" in {
        response.status shouldBe OK
      }

      "set status to Published" in {
        lazy val request = buildRequest(s"/external-guidance/approval")
        lazy val response: WSResponse = {
          AuditStub.audit()
          await(request.get())
        }
        val list: List[ApprovalProcessSummary] = response.body[JsValue].as[List[ApprovalProcessSummary]]
        val updatedEntry = list.find(p => p.id == id)
        updatedEntry shouldBe 'defined
        updatedEntry.get.status shouldBe StatusPublished
      }

      "add the process to the Published collection" in {
        lazy val request = buildRequest(s"/external-guidance/published/$id")
        lazy val response: WSResponse = {
          AuditStub.audit()
          await(request.get())
        }
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
        await(request.post(statusChangeWithDesignerJson))
      }

      "return a INCOMPLETE_ERROR status code" in {
        response.status shouldBe BAD_REQUEST
      }

      "return INCOMPLETE_DATA_ERROR in the request Body" in {
        val json = response.body[JsValue].as[JsObject]
        json shouldBe Json.toJson(IncompleteDataError)
      }

    }
  }

  "Calling the approval2iReviewComplete POST endpoint with an id that doesn't exist" should {

    lazy val request = buildRequest(s"/external-guidance/approval/xyzinvalid/2i-review")
    lazy val response: WSResponse = {
      AuditStub.audit()
      await(request.post(statusChangeWithDesignerJson))
    }

    "return a NOT_FOUND status code" in {
      response.status shouldBe NOT_FOUND
    }

  }

  "Calling the approval2iReviewPageComplete POST endpoint with a valid payload" should {

    def populateDatabase(processToSave: JsValue): String = {
      lazy val request = buildRequest("/external-guidance/approval")

      val result = await(request.post(processToSave))
      val json = result.body[JsValue].as[JsObject]
      (json \ "id").as[String]
    }

    val processToSave: JsValue = simpleValidProcess
    lazy val id = populateDatabase(processToSave)
    val pageUrl = "/feeling-bad"
    lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-page-review$pageUrl")
    val content = ApprovalProcessPageReview("1", pageUrl, Some("Yes"), ReviewCompleteStatus, Some("A basic comment"), LocalDateTime.now(), Some("User1"))

    lazy val response: WSResponse = {
      AuditStub.audit()
      await(request.post(Json.toJson(content)))
    }

    "return a NO_CONTENT status code" in {
      response.status shouldBe NO_CONTENT
    }

    "set the status to ReviewCompleteStatus" in {
      lazy val request = buildRequest(s"/external-guidance/approval/$id/2i-page-review$pageUrl")
      lazy val response: WSResponse = {
        AuditStub.audit()
        await(request.get())
      }
      val updatedEntry: ApprovalProcessPageReview = response.body[JsValue].as[ApprovalProcessPageReview]
      updatedEntry.status shouldBe ReviewCompleteStatus
    }

  }

  "Calling the approval2iReviewPageComplete POST endpoint with an id that doesn't exist" should {

    lazy val request = buildRequest(s"/external-guidance/approval/unknownId/2i-page-review/pageUrl")
    val content = ApprovalProcessPageReview("1", "pageUrl", Some("Success"), ReviewCompleteStatus, Some("A basic comment"), LocalDateTime.now(), Some("User1"))

    lazy val response: WSResponse = {
      AuditStub.audit()
      await(request.post(Json.toJson(content)))
    }

    "return a NOT_FOUND status code" in {
      response.status shouldBe NOT_FOUND
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
        await(request.post(statusChangeWithDesignerJson))
      }

      "return a INCOMPLETE_ERROR status code" in {
        response.status shouldBe BAD_REQUEST
      }

      "return INCOMPLETE_DATA_ERROR in the request Body" in {
        val json = response.body[JsValue].as[JsObject]
        json shouldBe Json.toJson(IncompleteDataError)
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
      val content = ApprovalProcessPageReview("1", pageUrl, Some("Yes"), ReviewCompleteStatus, Some("A basic comment"), LocalDateTime.now(), Some("User1"))
      await(pageUpdateRequest.post(Json.toJson(content)))

      id
    }
    val processToSave: JsValue = simpleValidProcess
    "the requested status update is With DesignerForUpdate" should {
      lazy val id = populateDatabase(processToSave)
      lazy val request = buildRequest(s"/external-guidance/approval/$id/fact-check")

      "set the new status to With Designer For Update" should {

        lazy val response: WSResponse = {
          AuditStub.audit()
          await(request.post(statusChangeWithDesignerJson))
        }

        "return an OK status code" in {
          response.status shouldBe OK
        }

        "set the status to WithDesignerForUpdate" in {
          lazy val request = buildRequest(s"/external-guidance/approval")
          lazy val response: WSResponse = {
            AuditStub.audit()
            await(request.get())
          }
          val list: List[ApprovalProcessSummary] = response.body[JsValue].as[List[ApprovalProcessSummary]]
          val updatedEntry = list.find(p => p.id == id)
          updatedEntry shouldBe 'defined
          updatedEntry.get.status shouldBe StatusWithDesignerForUpdate
        }
      }
    }
  }
}
