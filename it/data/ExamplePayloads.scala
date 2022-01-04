/*
 * Copyright 2022 HM Revenue & Customs
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

package data

import java.time.{LocalDate, ZoneOffset}
import java.util.UUID

import models.{ApprovalProcessStatusChange, PageReview, ProcessReview}
import play.api.libs.json.{JsObject, JsValue, Json}
import models.Constants._

object ExamplePayloads {

  val simpleValidProcessString: String =
    """
      |{
      |  "meta": {
      |    "id": "oct90001",
      |    "title": "Customer wants to make a cup of tea",
      |    "ocelot": 1,
      |    "lastAuthor": "000000",
      |    "lastUpdate": 1500298931016,
      |    "version": 4,
      |    "filename": "oct90001.js",
      |    "processCode": "CupOfTea"
      |  },
      |  "flow": {
      |    "34": {
      |      "next": [
      |        "end"
      |      ],
      |      "stack": true,
      |      "link": 3,
      |      "text": 3,
      |      "type": "InstructionStanza"
      |    },
      |    "33": {
      |      "next": [
      |        "34"
      |      ],
      |      "stack": true,
      |      "text": 2,
      |      "type": "InstructionStanza"
      |    },
      |    "32": {
      |      "next": [
      |        "33"
      |      ],
      |      "noteType": "Title",
      |      "stack": false,
      |      "text": 1,
      |      "type": "CalloutStanza"
      |    },
      |    "end": {
      |      "type": "EndStanza"
      |    },
      |    "start": {
      |      "next": [
      |        "32"
      |      ],
      |      "stack": false,
      |      "type": "PageStanza",
      |      "url": "/feeling-bad"
      |    }
      |  },
      |  "phrases": [
      |    ["Ask the customer if they have a tea bag", "Welsh, Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh, Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh, Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh, No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh, Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh, Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh, yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh, no - they don’t have a cup"]
      |  ],
      |  "links": [
      |    {
      |      "id": 0,
      |      "dest": "13",
      |      "title": "Ocelot roles",
      |      "window": false
      |    },
      |    {
      |      "id": 1,
      |      "dest": "19",
      |      "title": "Link to next page",
      |      "window": false
      |    },
      |    {
      |      "id": 2,
      |      "dest": "19",
      |      "title": "Learn about Ocelot roles",
      |      "window": false
      |    },
      |    {
      |      "id": 3,
      |      "dest": "start",
      |      "title": "Return to start of process",
      |      "window": false
      |    }
      |  ]
      |}
    """.stripMargin

  val simpleValidProcess: JsValue = Json.parse(simpleValidProcessString)

  val dateSubmitted: LocalDate = LocalDate.of(2020, 3, 3)
  val submittedDateInMilliseconds: Long = dateSubmitted.atStartOfDay(ZoneOffset.UTC).toInstant.toEpochMilli

  val validApprovalProcessJson: JsValue = Json.parse(
    s"""
      |{
      |  "meta" : {
      |    "id" : "oct90001",
      |    "title" : "This is the title",
      |    "status" : "$StatusSubmittedFor2iReview",
      |    "dateSubmitted" : {"$$date": $submittedDateInMilliseconds}
      |  },
      |  "process" : $simpleValidProcessString
      |}
      """.stripMargin
  )

  val expectedApprovalProcessJson: JsValue = Json.parse(
    s"""
      |{
      |  "_id" : "oct90001",
      |  "meta" : {
      |    "id" : "oct90001",
      |    "title" : "This is the title",
      |    "status" : "$StatusSubmitted",
      |    "dateSubmitted" : {"$$date": $submittedDateInMilliseconds},
      |    "reviewType" : "$ReviewType2i"
      |  },
      |  "process" : $simpleValidProcessString
      |}
      """.stripMargin
  )

  val validId = "oct90001"

  val processReviewInfo: ProcessReview =
    ProcessReview(
      UUID.randomUUID(),
      validId,
      1,
      ReviewType2i,
      "Telling HMRC about extra income",
      LocalDate.of(2020, 5, 10),
      List(
        PageReview("id1", "how did you earn extra income", "how-did-you-earn-extra-income", InitialPageReviewStatus, None),
        PageReview("id2", "did you only sell personal possessions", "sold-goods-or-services/did-you-only-sell-personal-possessions", InitialPageReviewStatus, None),
        PageReview("id3", "have you made a profit of 6000 ormore", "sold-goods-or-services/have-you-made-a-profit-of-6000-or-more", InitialPageReviewStatus, None),
        PageReview("id4", "have you made 1000 or more", "sold-goods-or-services/have-you-made-1000-or-more", InitialPageReviewStatus, None),
        PageReview("id5", "have you made 1000 or more", "sold-goods-or-services/you-do-not-need-to-tell-hmrc", InitialPageReviewStatus, None),
        PageReview("id6", "do you receive any income", "rent-a-property/do-you-receive-any-income", InitialPageReviewStatus, None),
        PageReview("id7", "have you rented out a room", "rent-a-property/have-you-rented-out-a-room", InitialPageReviewStatus, None)
      )
    )

  val statusChangeInfo: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user id", "user name", StatusSubmitted)

  val statusChangeJson: JsValue = Json.toJson(statusChangeInfo)

  val invalidStatusChangeJson: JsObject = Json.obj()

  val processWithCallouts: String =
    """
      |{
      |    "meta": {
      |    "id": "trn90099",
      |    "title": "External Guidance Automated Test Process Fact Check1",
      |    "ocelot": 3,
      |    "lastAuthor": "7903088",
      |    "lastUpdate": 1591002966651,
      |    "version": 7,
      |    "filename": "trn90088.js",
      |    "processCode": "this-is-the-process-code"
      |  },
      |  "flow": {
      |    "34": {
      |      "next": [
      |        "end"
      |      ],
      |      "stack": true,
      |      "link": 3,
      |      "text": 32,
      |      "type": "InstructionStanza"
      |    },
      |    "12": {
      |      "next": [
      |        "end"
      |      ],
      |      "stack": true,
      |      "text": 11,
      |      "type": "InstructionStanza"
      |    },
      |    "8": {
      |      "next": [
      |        "9"
      |      ],
      |      "stack": true,
      |      "text": 7,
      |      "type": "InstructionStanza"
      |    },
      |    "19": {
      |      "next": [
      |        "20"
      |      ],
      |      "stack": false,
      |      "type": "PageStanza",
      |      "url": "/example-page-3"
      |    },
      |    "23": {
      |      "next": [
      |        "36"
      |      ],
      |      "stack": true,
      |      "text": 17,
      |      "type": "InstructionStanza"
      |    },
      |    "4": {
      |      "next": [
      |        "5"
      |      ],
      |      "stack": true,
      |      "text": 3,
      |      "type": "InstructionStanza"
      |    },
      |    "15": {
      |      "next": [
      |        "16"
      |      ],
      |      "stack": true,
      |      "text": 13,
      |      "type": "InstructionStanza"
      |    },
      |    "11": {
      |      "next": [
      |        "12"
      |      ],
      |      "noteType": "Section",
      |      "stack": false,
      |      "text": 10,
      |      "type": "CalloutStanza"
      |    },
      |    "9": {
      |      "next": [
      |        "10"
      |      ],
      |      "stack": true,
      |      "text": 8,
      |      "type": "InstructionStanza"
      |    },
      |    "33": {
      |      "next": [
      |        "34"
      |      ],
      |      "stack": true,
      |      "text": 31,
      |      "type": "InstructionStanza"
      |    },
      |    "22": {
      |      "next": [
      |        "23"
      |      ],
      |      "stack": false,
      |      "type": "PageStanza",
      |      "url": "/example-page-4"
      |    },
      |    "26": {
      |      "next": [
      |        "27"
      |      ],
      |      "noteType": "Title",
      |      "stack": false,
      |      "text": 24,
      |      "type": "CalloutStanza"
      |    },
      |    "37": {
      |      "next": [
      |        "30"
      |      ],
      |      "noteType": "Error",
      |      "stack": false,
      |      "text": 33,
      |      "type": "CalloutStanza"
      |    },
      |    "13": {
      |      "next": [
      |        "14"
      |      ],
      |      "stack": false,
      |      "type": "PageStanza",
      |      "url": "/example-page-2"
      |    },
      |    "24": {
      |      "next": [
      |        "29",
      |        "25"
      |      ],
      |      "stack": false,
      |      "answers": [
      |        22,
      |        23
      |      ],
      |      "text": 21,
      |      "type": "QuestionStanza"
      |    },
      |    "35": {
      |      "next": [
      |        "21"
      |      ],
      |      "noteType": "Error",
      |      "stack": false,
      |      "text": 33,
      |      "type": "CalloutStanza"
      |    },
      |    "16": {
      |      "next": [
      |        "17"
      |      ],
      |      "stack": true,
      |      "text": 14,
      |      "type": "InstructionStanza"
      |    },
      |    "5": {
      |      "next": [
      |        "6"
      |      ],
      |      "stack": true,
      |      "text": 4,
      |      "type": "InstructionStanza"
      |    },
      |    "10": {
      |      "next": [
      |        "11"
      |      ],
      |      "stack": true,
      |      "link": 0,
      |      "text": 9,
      |      "type": "InstructionStanza"
      |    },
      |    "21": {
      |      "next": [
      |        "25",
      |        "22"
      |      ],
      |      "stack": false,
      |      "answers": [
      |        19,
      |        20
      |      ],
      |      "text": 18,
      |      "type": "QuestionStanza"
      |    },
      |    "32": {
      |      "next": [
      |        "33"
      |      ],
      |      "noteType": "Title",
      |      "stack": false,
      |      "text": 30,
      |      "type": "CalloutStanza"
      |    },
      |    "6": {
      |      "next": [
      |        "7"
      |      ],
      |      "stack": true,
      |      "text": 5,
      |      "type": "InstructionStanza"
      |    },
      |    "36": {
      |      "next": [
      |        "24"
      |      ],
      |      "noteType": "Error",
      |      "stack": false,
      |      "text": 33,
      |      "type": "CalloutStanza"
      |    },
      |    "1": {
      |      "next": [
      |        "2"
      |      ],
      |      "noteType": "Title",
      |      "stack": false,
      |      "text": 0,
      |      "type": "CalloutStanza"
      |    },
      |    "17": {
      |      "next": [
      |        "18"
      |      ],
      |      "stack": true,
      |      "text": 15,
      |      "type": "InstructionStanza"
      |    },
      |    "25": {
      |      "next": [
      |        "26"
      |      ],
      |      "stack": false,
      |      "type": "PageStanza",
      |      "url": "/example-page-5"
      |    },
      |    "14": {
      |      "next": [
      |        "15"
      |      ],
      |      "noteType": "Title",
      |      "stack": false,
      |      "text": 12,
      |      "type": "CalloutStanza"
      |    },
      |    "31": {
      |      "next": [
      |        "32"
      |      ],
      |      "stack": false,
      |      "type": "PageStanza",
      |      "url": "/example-page-7"
      |    },
      |    "20": {
      |      "next": [
      |        "35"
      |      ],
      |      "stack": true,
      |      "text": 17,
      |      "type": "InstructionStanza"
      |    },
      |    "27": {
      |      "next": [
      |        "28"
      |      ],
      |      "stack": true,
      |      "text": 25,
      |      "type": "InstructionStanza"
      |    },
      |    "2": {
      |      "next": [
      |        "3"
      |      ],
      |      "noteType": "Lede",
      |      "stack": false,
      |      "text": 1,
      |      "type": "CalloutStanza"
      |    },
      |    "end": {
      |      "type": "EndStanza"
      |    },
      |    "18": {
      |      "next": [
      |        "end"
      |      ],
      |      "stack": true,
      |      "link": 1,
      |      "text": 16,
      |      "type": "InstructionStanza"
      |    },
      |    "30": {
      |      "next": [
      |        "31",
      |        "25"
      |      ],
      |      "stack": false,
      |      "answers": [
      |        28,
      |        29
      |      ],
      |      "text": 27,
      |      "type": "QuestionStanza"
      |    },
      |    "7": {
      |      "next": [
      |        "8"
      |      ],
      |      "noteType": "SubTitle",
      |      "stack": false,
      |      "text": 6,
      |      "type": "CalloutStanza"
      |    },
      |    "start": {
      |      "next": [
      |        "1"
      |      ],
      |      "stack": false,
      |      "type": "PageStanza",
      |      "url": "/example-page-1"
      |    },
      |    "29": {
      |      "next": [
      |        "37"
      |      ],
      |      "stack": false,
      |      "type": "PageStanza",
      |      "url": "/example-page-6"
      |    },
      |    "3": {
      |      "next": [
      |        "4"
      |      ],
      |      "noteType": "SubTitle",
      |      "stack": false,
      |      "text": 2,
      |      "type": "CalloutStanza"
      |    },
      |    "28": {
      |      "next": [
      |        "end"
      |      ],
      |      "stack": true,
      |      "link": 2,
      |      "text": 26,
      |      "type": "InstructionStanza"
      |    }
      |  },
      |  "phrases": [
      |    [
      |      "External Guidance Testing process",
      |      "Welsh, External Guidance Testing process"
      |    ],
      |    [
      |      "This process helps to automate testing of components which are generated from the external viewer.",
      |      "Welsh, This process helps to automate testing of components which are generated from the external viewer."
      |    ],
      |    [
      |      "What is External Guidance?",
      |      "Welsh, What is External Guidance?"
      |    ],
      |    [
      |      "The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: design",
      |      "Welsh, The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: design"
      |    ],
      |    [
      |      "The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: build",
      |      "Welsh, The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: build"
      |    ],
      |    [
      |      "The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: accessibility standards",
      |      "Welsh, The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: accessibility standards"
      |    ],
      |    [
      |      "What is Ocelot?",
      |      "Welsh, What is Ocelot?"
      |    ],
      |    [
      |      "Ocelot is a content management system that has been developed to support the people working in call centres who need to advise citizens about tax processes. It works by providing a directed script of content that branches with each question.",
      |      "Welsh, Ocelot is a content management system that has been developed to support the people working in call centres who need to advise citizens about tax processes. It works by providing a directed script of content that branches with each question."
      |    ],
      |    [
      |      "It resides on the \"Stride\" network and developed by HMRC. For simple queries about your tax, you can contact [link:HM Revenue and Customs (HMRC):https://www.gov.uk/government/organisations/hm-revenue-customs/contact]",
      |      "Welsh, It resides on the \"Stride\" network and developed by HMRC. For simple queries about your tax, you can contact [link:HM Revenue and Customs (HMRC):https://www.gov.uk/government/organisations/hm-revenue-customs/contact]"
      |    ],
      |    [
      |      "To know more about different user roles in Ocelot",
      |      "Welsh, To know more about different user roles in Ocelot"
      |    ],
      |    [
      |      "Integration",
      |      "Welsh, Integration"
      |    ],
      |    [
      |      "The Ocelot design tool produces a JSON file which External guidance service needs to consume in order to render the web pages.",
      |      "Welsh, The Ocelot design tool produces a JSON file which External guidance service needs to consume in order to render the web pages."
      |    ],
      |    [
      |      "User role",
      |      "Welsh, User role"
      |    ],
      |    [
      |      "External guidance users can be assigned one, or more, of three roles Designer: designs the guidance using the Ocelot editor",
      |      "Welsh, External guidance users can be assigned one, or more, of three roles Designer: designs the guidance using the Ocelot editor"
      |    ],
      |    [
      |      "External guidance users can be assigned one, or more, of three roles Approver: reviews and approves the guidance produced by the designer",
      |      "Welsh, External guidance users can be assigned one, or more, of three roles Approver: reviews and approves the guidance produced by the designer"
      |    ],
      |    [
      |      "External guidance users can be assigned one, or more, of three roles Publisher: publishes approved guidance to the public",
      |      "Welsh, External guidance users can be assigned one, or more, of three roles Publisher: publishes approved guidance to the public"
      |    ],
      |    [
      |      "Check your understanding of different roles in Ocelot",
      |      "Welsh, Check your understanding of different roles in Ocelot"
      |    ],
      |    [
      |      "Correct answer leads forward to next question",
      |      "Welsh, Correct answer leads forward to next question"
      |    ],
      |    [
      |      "Who reviews and approves the g2uid1ance produced by the designer?",
      |      "Welsh, Who reviews and approves the g2uid1ance produced by the designer?"
      |    ],
      |    [
      |      "Users with the designer role",
      |      "Welsh, Users with the designer role"
      |    ],
      |    [
      |      "Users with the approver role",
      |      "Welsh, Users with the approver role"
      |    ],
      |    [
      |      "Do designers design guidance using the Ocelot editor?",
      |      "Welsh, Do designers design guidance using the Ocelot editor?"
      |    ],
      |    [
      |      "Yes",
      |      "Welsh, Yes"
      |    ],
      |    [
      |      "No",
      |      "Welsh, No"
      |    ],
      |    [
      |      "Oops! Wrong answer",
      |      "Welsh, Oops! Wrong answer"
      |    ],
      |    [
      |      "Sorry! You lost your place in the external guidance team. Mac will buy you a Pepsi Max to cheer you up!",
      |      "Welsh, Sorry! You lost your place in the external guidance team. Mac will buy you a Pepsi Max to cheer you up!"
      |    ],
      |    [
      |      "To know more about different users in Ocelot",
      |      "Welsh, To know more about different users in Ocelot"
      |    ],
      |    [
      |      "What is the role of publisher",
      |      "Welsh, What is the role of publisher"
      |    ],
      |    [
      |      "Yes [hint:This is hint text]",
      |      "Welsh, Yes [hint:This is hint text]"
      |    ],
      |    [
      |      "No [hint:This is hint text]",
      |      "Welsh, No [hint:This is hint text]"
      |    ],
      |    [
      |      "Congratulations",
      |      "Welsh, Congratulations"
      |    ],
      |    [
      |      "The external guidance team welcomes you! Ian will buy you a coffee.",
      |      "Welsh, The external guidance team welcomes you! Ian will buy you a coffee."
      |    ],
      |    [
      |      "Go to start of process",
      |      "Welsh, Go to start of process"
      |    ],
      |    [
      |      "You must choose one of the two options shown",
      |      "Welsh, You must choose one of the two options shown"
      |    ]
      |  ],
      |  "links": [
      |    {
      |      "id": 0,
      |      "dest": "13",
      |      "title": "Ocelot roles",
      |      "window": false
      |    },
      |    {
      |      "id": 1,
      |      "dest": "19",
      |      "title": "Link to next page",
      |      "window": false
      |    },
      |    {
      |      "id": 2,
      |      "dest": "19",
      |      "title": "Learn about Ocelot roles",
      |      "window": false
      |    },
      |    {
      |      "id": 3,
      |      "dest": "start",
      |      "title": "Return to start of process",
      |      "window": false
      |    }
      |  ]
      |}
      |""".stripMargin

  val validProcessWithCallouts: JsValue = Json.parse(processWithCallouts)

}
