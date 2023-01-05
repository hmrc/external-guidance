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

package data

import play.api.libs.json.{JsObject, Json, JsValue}

object ProcessData {

  val validId = "oct90001"

  val validOnePageJson: JsValue = Json.parse(
    """
      |{
      |  "meta": {
      |    "title": "Customer wants to make a cup of tea",
      |    "id": "oct90001",
      |    "ocelot": 1,
      |    "lastAuthor": "000000",
      |    "lastUpdate": 1500298931016,
      |    "version": 4,
      |    "filename": "oct90001.js",
      |    "processCode": "cup-of-tea"
      |  },
      |  "howto": [],
      |  "contacts": [],
      |  "links": [],
      |  "flow": {
      |    "start": {
      |      "type": "PageStanza",
      |      "url": "/feeling-bad",
      |      "next": ["3"],
      |      "stack": true
      |    },
      |    "3": {
      |      "type": "InstructionStanza",
      |      "text": 1,
      |      "next": [
      |        "2"
      |      ],
      |      "stack": true
      |    },
      |    "2": {
      |      "type": "InstructionStanza",
      |      "text": 0,
      |      "next": [
      |        "end"
      |      ],
      |      "stack": true
      |    },
      |    "end": {
      |      "type": "EndStanza"
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
      |  ]
      |}
    """.stripMargin
  )

  val invalidOnePageJson: JsValue = Json.parse(
    """
      |{
      |  "meta": {
      |    "title": "Customer wants to make a cup of tea",
      |    "id": "oct90001",
      |    "ocelot": 1,
      |    "lastAuthor": "000000",
      |    "lastUpdate": 1500298931016,
      |    "version": 4,
      |    "filename": "oct90001.js",
      |    "processCode": "cup-of-tea"
      |  },
      |  "howto": [],
      |  "contacts": [],
      |  "links": [],
      |  "flow": {
      |    "start": {
      |      "type": "PageStanza",
      |      "url": "/feeling-bad",
      |      "next": ["3"],
      |      "stack": true
      |    },
      |    "3": {
      |      "type": "InstructionStanza",
      |      "text": 1,
      |      "next": [
      |        "2"
      |      ],
      |      "stack": true
      |    },
      |    "2": {
      |      "type": "InstructionStanza",
      |      "text": 0,
      |      "next": [
      |        "4"
      |      ],
      |      "stack": true
      |    },
      |    "4": {
      |      "type": "PageStanza",
      |      "url": "/feeling-bad",
      |      "next": [
      |        "5"
      |      ],
      |      "stack": true
      |    },
      |    "5": {
      |      "type": "InstructionStanza",
      |      "text": 0,
      |      "next": [
      |        "end"
      |      ],
      |      "stack": true
      |    },
      |    "end": {
      |      "type": "EndStanza"
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
      |  ]
      |}
    """.stripMargin
  )

  val process90087Json: JsObject = Json
    .parse(
      """
        |  {
        |    "meta": {
        |      "id": "trn90087",
        |      "title": "External Guidance Automated Test Process",
        |      "ocelot": 3,
        |      "lastAuthor": "7903088",
        |      "lastUpdate": 1589467563758,
        |      "filename": "trn90087.js",
        |      "version": 6,
        |      "processCode": "eg-auto-test"
        |    },
        |    "flow": {
        |      "1": {
        |      "type": "CalloutStanza",
        |      "text": 0,
        |      "noteType": "Title",
        |      "next": [
        |      "2"
        |      ],
        |      "stack": false
        |    },
        |      "2": {
        |      "type": "CalloutStanza",
        |      "text": 1,
        |      "noteType": "Lede",
        |      "next": [
        |      "3"
        |      ],
        |      "stack": false
        |    },
        |      "3": {
        |      "type": "CalloutStanza",
        |      "text": 2,
        |      "noteType": "SubTitle",
        |      "next": [
        |      "4"
        |      ],
        |      "stack": false
        |    },
        |      "4": {
        |      "type": "InstructionStanza",
        |      "text": 3,
        |      "next": [
        |      "5"
        |      ],
        |      "stack": true
        |    },
        |      "5": {
        |      "type": "InstructionStanza",
        |      "text": 4,
        |      "next": [
        |      "6"
        |      ],
        |      "stack": true
        |    },
        |      "6": {
        |      "type": "InstructionStanza",
        |      "text": 5,
        |      "next": [
        |      "7"
        |      ],
        |      "stack": true
        |    },
        |      "7": {
        |      "type": "CalloutStanza",
        |      "text": 6,
        |      "noteType": "SubTitle",
        |      "next": [
        |      "8"
        |      ],
        |      "stack": false
        |    },
        |      "8": {
        |      "type": "InstructionStanza",
        |      "text": 7,
        |      "next": [
        |      "9"
        |      ],
        |      "stack": true
        |    },
        |      "9": {
        |      "type": "InstructionStanza",
        |      "text": 8,
        |      "next": [
        |      "10"
        |      ],
        |      "stack": true
        |    },
        |      "10": {
        |      "type": "InstructionStanza",
        |      "text": 9,
        |      "next": [
        |      "11"
        |      ],
        |      "link": 0,
        |      "stack": true
        |    },
        |      "11": {
        |      "type": "CalloutStanza",
        |      "text": 10,
        |      "noteType": "Section",
        |      "next": [
        |      "12"
        |      ],
        |      "stack": false
        |    },
        |      "12": {
        |      "type": "InstructionStanza",
        |      "text": 11,
        |      "next": [
        |      "end"
        |      ],
        |      "stack": true
        |    },
        |      "13": {
        |      "type": "ValueStanza",
        |      "values": [
        |    {
        |      "type": "scalar",
        |      "label": "PageUrl",
        |      "value": "/example-page-2"
        |    }
        |      ],
        |      "next": [
        |      "14"
        |      ],
        |      "stack": false
        |    },
        |      "14": {
        |      "type": "CalloutStanza",
        |      "text": 12,
        |      "noteType": "Title",
        |      "next": [
        |      "15"
        |      ],
        |      "stack": false
        |    },
        |      "15": {
        |      "type": "InstructionStanza",
        |      "text": 13,
        |      "next": [
        |      "16"
        |      ],
        |      "stack": true
        |    },
        |      "16": {
        |      "type": "InstructionStanza",
        |      "text": 14,
        |      "next": [
        |      "17"
        |      ],
        |      "stack": true
        |    },
        |      "17": {
        |      "type": "InstructionStanza",
        |      "text": 15,
        |      "next": [
        |      "18"
        |      ],
        |      "stack": true
        |    },
        |      "18": {
        |      "type": "InstructionStanza",
        |      "text": 16,
        |      "next": [
        |      "end"
        |      ],
        |      "link": 1,
        |      "stack": true
        |    },
        |      "19": {
        |      "type": "ValueStanza",
        |      "values": [
        |    {
        |      "type": "scalar",
        |      "label": "PageUrl",
        |      "value": "/example-page-3"
        |    }
        |      ],
        |      "next": [
        |      "20"
        |      ],
        |      "stack": false
        |    },
        |      "20": {
        |      "type": "InstructionStanza",
        |      "text": 17,
        |      "next": [
        |      "35"
        |      ],
        |      "stack": true
        |    },
        |      "21": {
        |      "type": "QuestionStanza",
        |      "text": 19,
        |      "answers": [
        |      20,
        |      21
        |      ],
        |      "next": [
        |      "25",
        |      "22"
        |      ],
        |      "stack": false
        |    },
        |      "22": {
        |      "type": "ValueStanza",
        |      "values": [
        |    {
        |      "type": "scalar",
        |      "label": "PageUrl",
        |      "value": "/example-page-4"
        |    }
        |      ],
        |      "next": [
        |      "23"
        |      ],
        |      "stack": false
        |    },
        |      "23": {
        |      "type": "InstructionStanza",
        |      "text": 17,
        |      "next": [
        |      "36"
        |      ],
        |      "stack": true
        |    },
        |      "24": {
        |      "type": "QuestionStanza",
        |      "text": 22,
        |      "answers": [
        |      23,
        |      24
        |      ],
        |      "next": [
        |      "29",
        |      "25"
        |      ],
        |      "stack": false
        |    },
        |      "25": {
        |      "type": "ValueStanza",
        |      "values": [
        |    {
        |      "type": "scalar",
        |      "label": "PageUrl",
        |      "value": "/example-page-5"
        |    }
        |      ],
        |      "next": [
        |      "26"
        |      ],
        |      "stack": false
        |    },
        |      "26": {
        |      "type": "CalloutStanza",
        |      "text": 25,
        |      "noteType": "Title",
        |      "next": [
        |      "27"
        |      ],
        |      "stack": false
        |    },
        |      "27": {
        |      "type": "InstructionStanza",
        |      "text": 26,
        |      "next": [
        |      "28"
        |      ],
        |      "stack": true
        |    },
        |      "28": {
        |      "type": "InstructionStanza",
        |      "text": 27,
        |      "next": [
        |      "end"
        |      ],
        |      "link": 2,
        |      "stack": true
        |    },
        |      "29": {
        |      "type": "ValueStanza",
        |      "values": [
        |    {
        |      "type": "scalar",
        |      "label": "PageUrl",
        |      "value": "/example-page-6"
        |    }
        |      ],
        |      "next": [
        |      "37"
        |      ],
        |      "stack": false
        |    },
        |      "30": {
        |      "type": "QuestionStanza",
        |      "text": 28,
        |      "answers": [
        |      29,
        |      30
        |      ],
        |      "next": [
        |      "31",
        |      "25"
        |      ],
        |      "stack": false
        |    },
        |      "31": {
        |      "type": "ValueStanza",
        |      "values": [
        |    {
        |      "type": "scalar",
        |      "label": "PageUrl",
        |      "value": "/example-page-7"
        |    }
        |      ],
        |      "next": [
        |      "32"
        |      ],
        |      "stack": false
        |    },
        |      "32": {
        |      "type": "CalloutStanza",
        |      "text": 31,
        |      "noteType": "Title",
        |      "next": [
        |      "33"
        |      ],
        |      "stack": false
        |    },
        |      "33": {
        |      "type": "InstructionStanza",
        |      "text": 32,
        |      "next": [
        |      "34"
        |      ],
        |      "stack": true
        |    },
        |      "34": {
        |      "type": "InstructionStanza",
        |      "text": 33,
        |      "next": [
        |      "end"
        |      ],
        |      "link": 3,
        |      "stack": true
        |    },
        |      "35": {
        |      "type": "CalloutStanza",
        |      "text": 18,
        |      "noteType": "Error",
        |      "next": [
        |      "21"
        |      ],
        |      "stack": false
        |    },
        |      "36": {
        |      "type": "CalloutStanza",
        |      "text": 18,
        |      "noteType": "Error",
        |      "next": [
        |      "24"
        |      ],
        |      "stack": false
        |    },
        |      "37": {
        |      "type": "CalloutStanza",
        |      "text": 18,
        |      "noteType": "Error",
        |      "next": [
        |      "30"
        |      ],
        |      "stack": false
        |    },
        |      "start": {
        |      "type": "ValueStanza",
        |      "values": [
        |    {
        |      "type": "scalar",
        |      "label": "PageUrl",
        |      "value": "/example-page-1"
        |    }
        |      ],
        |      "next": [
        |      "1"
        |      ],
        |      "stack": false
        |    },
        |      "end": {
        |      "type": "EndStanza"
        |    }
        |    },
        |    "phrases": [
        |    [
        |    "External Guidance Testing process",
        |    "Welsh - External Guidance Testing process"
        |    ],
        |    [
        |    "This process helps to automate testing of components which are generated from the external viewer.",
        |    "Welsh - This process helps to automate testing of components which are generated from the external viewer."
        |    ],
        |    [
        |    "What is External Guidance?",
        |    "Welsh - What is External Guidance?"
        |    ],
        |    [
        |    "The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: design",
        |    "Welsh - The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: design"
        |    ],
        |    [
        |    "The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: build",
        |    "Welsh - The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: build"
        |    ],
        |    [
        |    "The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: accessibility standards",
        |    "Welsh - The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: accessibility standards"
        |    ],
        |    [
        |    "What is Ocelot?",
        |    "Welsh - What is Ocelot?"
        |    ],
        |    [
        |    "Ocelot is a content management system that has been developed to support the people working in call centres who need to advise citizens about tax processes. It works by providing a directed script of content that branches with each question.",
        |    "Welsh - Ocelot is a content management system that has been developed to support the people working in call centres who need to advise citizens about tax processes. It works by providing a directed script of content that branches with each question."
        |    ],
        |    [
        |    "It resides on the \"Stride\" network and developed by HMRC. For simple queries about your tax, you can contact [link:HM Revenue and Customs (HMRC):https://www.gov.uk/government/organisations/hm-revenue-customs/contact]",
        |    "Welsh - It resides on the \"Stride\" network and developed by HMRC. For simple queries about your tax, you can contact [link:HM Revenue and Customs (HMRC):https://www.gov.uk/government/organisations/hm-revenue-customs/contact]"
        |    ],
        |    [
        |    "To know more about different user roles in Ocelot",
        |    "Welsh - To know more about different user roles in Ocelot"
        |    ],
        |    [
        |    "Integration",
        |    "Welsh - Integration"
        |    ],
        |    [
        |    "The Ocelot design tool produces a JSON file which External guidance service needs to consume in order to render the web pages.",
        |    "Welsh - The Ocelot design tool produces a JSON file which External guidance service needs to consume in order to render the web pages."
        |    ],
        |    [
        |    "User role",
        |    "Welsh - User role"
        |    ],
        |    [
        |    "External guidance users can be assigned one, or more, of three roles Designer: designs the guidance using the Ocelot editor",
        |    "Welsh - External guidance users can be assigned one, or more, of three roles Designer: designs the guidance using the Ocelot editor"
        |    ],
        |    [
        |    "External guidance users can be assigned one, or more, of three roles Approver: reviews and approves the guidance produced by the designer",
        |    "Welsh - External guidance users can be assigned one, or more, of three roles Approver: reviews and approves the guidance produced by the designer"
        |    ],
        |    [
        |    "External guidance users can be assigned one, or more, of three roles Publisher: publishes approved guidance to the public",
        |    "Welsh - External guidance users can be assigned one, or more, of three roles Publisher: publishes approved guidance to the public"
        |    ],
        |    [
        |    "Check your understanding of different roles in Ocelot",
        |    "Welsh - Check your understanding of different roles in Ocelot"
        |    ],
        |    [
        |    "Correct answer leads forward to next question",
        |    "Welsh - Correct answer leads forward to next question"
        |    ],
        |    [
        |    "You must choose one of the two options shown",
        |    "Welsh - You must choose one of the two options shown"
        |    ],
        |    [
        |    "Who reviews and approves the guidance produced by the designer?",
        |    "Welsh - Who reviews and approves the guidance produced by the designer?"
        |    ],
        |    [
        |    "Users with the designer role",
        |    "Welsh - Users with the designer role"
        |    ],
        |    [
        |    "Users with the approver role",
        |    "Welsh - Users with the approver role"
        |    ],
        |    [
        |    "Do designers design guidance using the Ocelot editor?",
        |    "Welsh - Do designers design guidance using the Ocelot editor?"
        |    ],
        |    [
        |    "Yes",
        |    "Welsh - Yes"
        |    ],
        |    [
        |    "No",
        |    "Welsh - No"
        |    ],
        |    [
        |    "Oops! Wrong answer",
        |    "Welsh - Oops! Wrong answer"
        |    ],
        |    [
        |    "Sorry! You lost your place in the external guidance team. Mac will buy you a Pepsi Max to cheer you up!",
        |    "Welsh - Sorry! You lost your place in the external guidance team. Mac will buy you a Pepsi Max to cheer you up!"
        |    ],
        |    [
        |    "To know more about different users in Ocelot",
        |    "Welsh - To know more about different users in Ocelot"
        |    ],
        |    [
        |    "What is the role of publisher",
        |    "Welsh - What is the role of publisher"
        |    ],
        |    [
        |    "Yes [hint:This is hint text]",
        |    "Welsh - Yes [hint:This is hint text]"
        |    ],
        |    [
        |    "No [hint:This is hint text]",
        |    "Welsh - No [hint:This is hint text]"
        |    ],
        |    [
        |    "Congratulations",
        |    "Welsh - Congratulations"
        |    ],
        |    [
        |    "The external guidance team welcomes you! Ian will buy you a coffee.",
        |    "Welsh - The external guidance team welcomes you! Ian will buy you a coffee."
        |    ],
        |    [
        |    "Go to start of process",
        |    "Welsh - Go to start of process"
        |    ]
        |    ],
        |    "contacts": [],
        |    "howto": [],
        |    "links": [
        |    {
        |      "dest": "13",
        |      "title": "Ocelot roles",
        |      "window": false,
        |      "leftbar": false,
        |      "always": false,
        |      "popup": false,
        |      "id": 0
        |    },
        |    {
        |      "dest": "19",
        |      "title": "Link to next page",
        |      "window": false,
        |      "leftbar": false,
        |      "always": false,
        |      "popup": false,
        |      "id": 1
        |    },
        |    {
        |      "dest": "19",
        |      "title": "Learn about Ocelot roles",
        |      "window": false,
        |      "leftbar": false,
        |      "always": false,
        |      "popup": false,
        |      "id": 2
        |    },
        |    {
        |      "dest": "start",
        |      "title": "Return to start of process",
        |      "window": false,
        |      "leftbar": false,
        |      "always": false,
        |      "popup": false,
        |      "id": 3
        |    }
        |    ]
        |  }
        """.stripMargin
    )
    .as[JsObject]

}
