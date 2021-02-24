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

package core.models.ocelot

import base.BaseSpec
import play.api.libs.json._
import core.models.ocelot.stanzas.{ValueStanza, Value, ScalarType}

class FlowSpec extends BaseSpec {

  val labelValueJson: JsValue = Json.parse(
    s"""|{
        |    "name": "LabelName",
        |    "value": "A value"
        |}""".stripMargin
  )

  val invalidLabelValueJson: JsValue = Json.parse(
    s"""|{
        |    "namef": "LabelName",
        |    "value": "A value"
        |}""".stripMargin
  )

    val flowStageJson: JsValue = Json.parse(
    s"""|{
        |    "next": "1",
        |    "labelValue": {
        |     "name": "LabelName",
        |     "value": "A value"
        |    }
        |}""".stripMargin
  )

  val invalidFlowStageJson: JsValue = Json.parse(
    s"""|{
        |    "nextf": "1",
        |    "labelValue": {
        |     "name": "LabelName",
        |     "value": "A value"
        |    }
        |}""".stripMargin
  )


  val flowJson: JsValue = Json.parse(
    s"""|{
        |    "next": "1",
        |    "labelValue": {
        |     "name": "LabelName",
        |     "value": "A value"
        |    }
        |}""".stripMargin
  )

  val invalidflowStageJson: JsValue = Json.parse(
    s"""|{
        |    "type": "other",
        |    "next": "1",
        |    "labelValue": {
        |     "name": "LabelName",
        |     "value": "A value"
        |    }
        |}""".stripMargin
  )

  val flowStageflowJson: JsValue = Json.parse(
    s"""|{
        |    "type": "flow",
        |    "next": "1",
        |    "labelValue": {
        |     "name": "LabelName",
        |     "value": "A value"
        |    }
        |}""".stripMargin
  )

  val invalidFlowJson: JsValue = Json.parse(
    s"""|{
        |    "nextf": "1",
        |    "labelValue": {
        |     "name": "LabelName",
        |     "value": "A value"
        |    }
        |}""".stripMargin
  )

  val continuationJson: JsValue = Json.parse(
    s"""{
        |  "next": "1",
        |  "stanzas": [
        |    {
        |      "key": "1",
        |      "stanza": {
        |        "next": [
        |          "11"
        |        ],
        |        "stack": false,
        |        "values": [
        |          {
        |            "type": "scalar",
        |            "label": "labelName",
        |            "value": "23"
        |          }
        |        ],
        |        "type": "ValueStanza"
        |      }
        |    }
        |  ]
        }""".stripMargin
  )

  val flowStageContinuationJson: JsValue = Json.parse(
    s"""{
        |  "type": "cont",
        |  "next": "1",
        |  "stanzas": [
        |    {
        |      "key": "1",
        |      "stanza": {
        |        "next": [
        |          "11"
        |        ],
        |        "stack": false,
        |        "values": [
        |          {
        |            "type": "scalar",
        |            "label": "labelName",
        |            "value": "23"
        |          }
        |        ],
        |        "type": "ValueStanza"
        |      }
        |    }
        |  ]
        }""".stripMargin
  )

  val invalidContinuationJson: JsValue = Json.parse(
    s"""{
        |  "nextf": "1",
        |  "stanzas": [
        |    {
        |      "key": "1",
        |      "stanza": {
        |        "type": "ValueStanza",
        |        "values": [
        |          {
        |            "type": "scalar",
        |            "label": "labelName",
        |            "value": "23"
        |          }
        |        ],
        |        "next": [
        |          "11"
        |        ],
        |        "stack": false
        |      }
        |    }
        |  ]
        }""".stripMargin
  )

  val keyedStanzaJson: JsValue = Json.parse(
    s"""|{
        |  "key": "1",
        |  "stanza": {
        |    "next": [
        |      "11"
        |    ],
        |    "stack": false,
        |    "values": [
        |      {
        |        "type": "scalar",
        |        "label": "labelName",
        |        "value": "23"
        |      }
        |    ],
        |    "type": "ValueStanza"
        |  }
        |}""".stripMargin
  )

  val invalidKeyedStanzaJson: JsValue = Json.parse(
    s"""|{
        |  "keyf": "1",
        |  "stanza": {
        |    "next": [
        |      "11"
        |    ],
        |    "stack": false,
        |    "values": [
        |      {
        |        "type": "scalar",
        |        "label": "labelName",
        |        "value": "23"
        |      }
        |    ],
        |    "type": "ValueStanza"
        |  }
        |}""".stripMargin
  )

  val expectedLabelValue: LabelValue = LabelValue("LabelName", Some("A value"))
  val expectedFlow: Flow = Flow("1", Some(expectedLabelValue))
  val expectedContinuation: Continuation = Continuation("1", List(KeyedStanza("1", ValueStanza(List(Value(ScalarType, "labelName", "23")), Seq("11"), false))))
  val expectedKeyedStanza: KeyedStanza = KeyedStanza("1", ValueStanza(List(Value(ScalarType, "labelName", "23")), Seq("11"), false))

  "Reading an invalid FlowStage" should {
    "Generate a JsonValidationError" in {

      invalidflowStageJson.validate[FlowStage] match {
        case JsSuccess(value, _) => fail("Should generate a JsonValidationError")
        case JsError(error) => succeed
      }
    }
  }

  "Reading valid Flow JSON" should {
    "create a Flow" in {
      flowJson.as[Flow] shouldBe expectedFlow

      flowStageflowJson.as[FlowStage] shouldBe expectedFlow
    }
  }

  "Reading invalid Flow JSON" should {
    "generate a JsError" in {
      invalidFlowJson.validate[Flow] match {
        case JsError(_) => succeed
        case _ => fail("An instance of Flow should not be created next is missing")
      }
    }
  }

  "serialise Flow to json" in {
    Json.toJson(expectedFlow) shouldBe flowJson
  }

  "serialise FlowStage Flow to json" in {
    val flowStage: FlowStage = expectedFlow
    Json.toJson(flowStage) shouldBe flowStageflowJson
  }

  "Reading valid Continuation JSON" should {
    "create a Flow" in {
      continuationJson.as[Continuation] shouldBe expectedContinuation

      flowStageContinuationJson.as[FlowStage] shouldBe expectedContinuation
    }
  }

  "Reading invalid Continuation JSON" should {
    "generate a JsError" in {
      invalidContinuationJson.validate[Continuation] match {
        case JsError(_) => succeed
        case _ => fail("An instance of Continuation should not be created next is missing")
      }
    }
  }

  "serialise Continuation to json" in {
    Json.toJson(expectedContinuation) shouldBe continuationJson
  }

  "serialise FlowStage Continuation to json" in {
    val flowStage: FlowStage = expectedContinuation
    Json.toJson(flowStage) shouldBe flowStageContinuationJson
  }

  "Reading valid KeyedStanza JSON" should {
    "create a KeyedStanza" in {
      keyedStanzaJson.as[KeyedStanza] shouldBe expectedKeyedStanza
    }
  }

  "Reading invalid KeyedStanza JSON" should {
    "generate a JsError" in {
      invalidKeyedStanzaJson.validate[KeyedStanza] match {
        case JsError(_) => succeed
        case _ => fail("An instance of KeyedStanza should not be created next is missing")
      }
    }
  }

  "serialise KeyedStanza to json" in {
    Json.toJson(expectedKeyedStanza) shouldBe keyedStanzaJson
  }

  "Reading valid LabelValue JSON" should {
    "create a LabelValue" in {
      labelValueJson.as[LabelValue] shouldBe expectedLabelValue
    }
  }

  "Reading invalid LabelValue JSON" should {
    "generate a JsError" in {
      invalidLabelValueJson.validate[LabelValue] match {
        case JsError(_) => succeed
        case _ => fail("An instance of LabelValue should not be created next is missing")
      }
    }
  }

  "serialise LabelValue to json" in {
    Json.toJson(expectedLabelValue) shouldBe labelValueJson
  }

}
