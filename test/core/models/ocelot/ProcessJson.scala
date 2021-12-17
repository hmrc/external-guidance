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

import play.api.libs.json.{JsValue, Json, JsObject}

//
// All Welsh translations are temporary placeholders and for testing purposes only
//
trait ProcessJson {

  val validOnePageJson: JsValue = Json.parse(
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
      |    "titlePhrase": 8,
      |    "processCode": "cup-of-tea"
      |  },
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
      |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
      |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
      |  ],
      |  "links": [],
      |  "timescales": {}
      |}
    """.stripMargin
  )

    val validOnePageWithTimescalesJson: JsValue = Json.parse(
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
      |    "titlePhrase": 8,
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
      |        "33"
      |      ],
      |      "stack": true
      |    },
      |    "33": {
      |      "type": "ValueStanza",
      |      "values": [
      |        {
      |          "type": "scalar",
      |          "label": "SomeLabel",
      |          "value": "[timescale:RepayReimb:days]"
      |        },
      |        {
      |          "type": "scalar",
      |          "label": "Blah",
      |          "value": "[timescale:RepayReimb:days]"
      |        }
      |      ],
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
      |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag [timescale:RepayReimb:days]?", "Welsh: Do you have a tea bag [timescale:RepayReimb:days]?"],
      |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
      |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
      |  ]
      |}
    """.stripMargin
  )

  val inValidOnePageWithTimescalesJson: JsValue = Json.parse(
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
      |    "titlePhrase": 8,
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
      |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
      |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
      |  ],
      |  "timescales" : {
      |      "RepayReimb" : "test"
      |  }
      |}
    """.stripMargin
  )

    val rawOcelotTimescalesJson: JsValue = Json.parse(
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
      |    "titlePhrase": 8,
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
      |        "33"
      |      ],
      |      "stack": true
      |    },
      |    "33": {
      |      "type": "ValueStanza",
      |      "values": [
      |        {
      |          "type": "scalar",
      |          "label": "SomeLabel",
      |          "value": "[timescale:JRSProgChaseCB:days]"
      |        },
      |        {
      |          "type": "scalar",
      |          "label": "_GuidancePassPhrase",
      |          "value": "[timescale:CHBFLCertabroad:days]"
      |        }
      |      ],
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
      |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag [timescale:JRSRefCB:days]?", "Welsh: Do you have a tea bag [timescale:JRSRefCB:days]?"],
      |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
      |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
      |  ]
      |}
    """.stripMargin
  )

  val validOnePageProcessWithPassPhrase: JsValue = Json.parse(
    """
      |{
      |  "meta": {
      |    "title": "Customer wants to make a cup of tea",
      |    "passPhrase": "A not so memorable phrase",
      |    "id": "oct90001",
      |    "ocelot": 1,
      |    "lastAuthor": "000000",
      |    "lastUpdate": 1500298931016,
      |    "version": 4,
      |    "filename": "oct90001.js",
      |    "titlePhrase": 8,
      |    "processCode": "CupOfTea"
      |  },
      |  "howto": [],
      |  "contacts": [],
      |  "links": [],
      |  "flow": {
      |    "start": {
      |      "type": "PageStanza",
      |      "url": "/feeling-bad",
      |      "next": ["33"],
      |      "stack": true
      |    },
      |    "33": {
      |      "type": "ValueStanza",
      |      "values": [
      |        {
      |          "type": "scalar",
      |          "label": "SomeLabel",
      |          "value": "43"
      |        },
      |        {
      |          "type": "scalar",
      |          "label": "_GuidancePassPhrase",
      |          "value": "A not so memorable phrase"
      |        }
      |      ],
      |      "next": [
      |        "3"
      |      ],
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
      |      "type": "InputStanza",
      |      "ipt_type": "Currency",
      |      "next": [
      |        "end"
      |      ],
      |      "name": 1,
      |      "help": 2,
      |      "label": "LabelName",
      |      "placeholder": 3,
      |      "stack": true
      |    },
      |    "end": {
      |      "type": "EndStanza"
      |    }
      |  },
      |  "phrases": [
      |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
      |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
      |  ]
      |}
    """.stripMargin
  )


  val validOnePageProcessWithProcessCodeJson: JsValue = Json.parse(
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
      |    "titlePhrase": 8,
      |    "processCode": "CupOfTea"
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
      |    "3": {
      |      "type": "InputStanza",
      |      "ipt_type": "Currency",
      |      "next": [
      |        "end"
      |      ],
      |      "name": 1,
      |      "help": 2,
      |      "label": "LabelName",
      |      "placeholder": 3,
      |      "stack": true
      |    },
      |    "end": {
      |      "type": "EndStanza"
      |    }
      |  },
      |  "phrases": [
      |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
      |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
      |  ]
      |}
    """.stripMargin
  )

  // Includes a url without leading / and a url which is just /
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
      |      "url": "feeling-bad",
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
      |      "url": "/",
      |      "next": ["5"],
      |      "stack": true
      |    },
      |    "5": {
      |      "type": "InstructionStanza",
      |      "text": 1,
      |      "next": [
      |        "6"
      |      ],
      |      "stack": true
      |    },
      |    "6": {
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
      |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"]
      |  ]
      |}
    """.stripMargin
  )

  val assortedParseErrorsJson: JsValue = Json.parse(
    """
      |{
      |  "meta": {
      |    "title": "Customer wants to make a cup of tea",
      |    "id": "oct90001",
      |    "lastAuthor": "000000",
      |    "lastUpdate": 1500298931016,
      |    "version": 4,
      |    "filename": "oct90001.js",
      |    "titlePhrase": 8,
      |    "processCode": "cup-of-tea"
      |  },
      |  "howto": [],
      |  "contacts": [],
      |  "links": [],
      |  "flow": {
      |    "start": {
      |      "type": "PageStanza",
      |      "url": "/feeling-bad",
      |      "next": ["33"],
      |      "stack": true
      |    },
      |    "33": {
      |      "type": "ValueStanza",
      |      "values": [
      |        {
      |          "type": "AnUnknownType",
      |          "label": "SomeLabel",
      |          "value": "43"
      |        }
      |      ],
      |      "next": [
      |        "34"
      |      ],
      |      "stack": true
      |    },
      |    "34": {
      |      "type": "InputStanza",
      |      "ipt_type": "UnknownInputType",
      |      "next": [
      |        "23"
      |      ],
      |      "name": 1,
      |      "help": 2,
      |      "label": "LabelName",
      |      "placeholder": 3,
      |      "stack": true
      |    },
      |    "3": {
      |      "type": "InstructionStanza",
      |      "text": 1,
      |      "link": 0,
      |      "next": [
      |        "2"
      |      ],
      |      "stack": true
      |    },
      |    "2": {
      |      "type": "UnknownStanza",
      |      "text": 0,
      |      "next": [
      |        "4"
      |      ],
      |      "stack": true
      |    },
      |    "4": {
      |      "next": [
      |        "end"
      |      ],
      |      "noteType": "UnknownType",
      |      "stack": false,
      |      "text": 59,
      |      "type": "CalloutStanza"
      |    },
      |    "5": {
      |      "next": [
      |        "end"
      |      ],
      |      "noteType": "Error",
      |      "stack": false,
      |      "text": 59
      |    },
      |    "end": {
      |      "type": "EndStanza"
      |    }
      |  },
      |  "phrases": [
      |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
      |    ["Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
      |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
      |  ],
      |  "links": [
      |   {
      |     "title": "",
      |     "window": false,
      |     "leftbar": false,
      |     "always": false,
      |     "popup": false,
      |     "id": 0
      |   }
      | ]
      |}
    """.stripMargin
  )

  val duplicateUrlsJson: JsValue = Json.parse(
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
      |    "titlePhrase": 8,
      |    "processCode": "cup-of-tea"
      |  },
      |  "howto": [],
      |  "contacts": [],
      |  "links": [],
      |  "flow": {
      |    "start": {
      |      "type": "PageStanza",
      |      "url": "/start",
      |      "next": ["1"],
      |      "stack": true
      |    },
      |    "1": {
      |      "type": "InstructionStanza",
      |      "text": 1,
      |      "next": [
      |        "2"
      |      ],
      |      "stack": true
      |    },
      |    "2": {
      |      "type": "PageStanza",
      |      "url": "/feeling-bad",
      |      "next": ["3"],
      |      "stack": true
      |    },
      |    "3": {
      |      "type": "InstructionStanza",
      |      "text": 0,
      |      "next": [
      |        "4"
      |      ],
      |      "stack": true
      |    },
      |    "4": {
      |      "type": "PageStanza",
      |      "url": "/feeling-good",
      |      "next": ["5"],
      |      "stack": true
      |    },
      |    "5": {
      |      "type": "InstructionStanza",
      |      "text": 0,
      |      "next": [
      |        "6"
      |      ],
      |      "stack": true
      |    },
      |    "6": {
      |      "type": "PageStanza",
      |      "url": "/feeling-bad",
      |      "next": ["7"],
      |      "stack": true
      |    },
      |    "7": {
      |      "type": "InstructionStanza",
      |      "text": 0,
      |      "next": [
      |        "8"
      |      ],
      |      "stack": true
      |    },
      |    "8": {
      |      "type": "PageStanza",
      |      "url": "/feeling-good",
      |      "next": ["9"],
      |      "stack": true
      |    },
      |    "9": {
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
      |    ["Ask the customer if they have a tea bag", "Welsh: Ask the customer if they have a tea bag"],
      |    ["Do you have a tea bag?", "Welsh: Do you have a tea bag?"],
      |    ["Yes - they do have a tea bag", "Welsh: Yes - they do have a tea bag"],
      |    ["No - they do not have a tea bag", "Welsh: No - they do not have a tea bag"],
      |    ["Ask the customer if they have a cup", "Welsh: Ask the customer if they have a cup"],
      |    ["Do you have a cup?", "Welsh: Do you have a cup?"],
      |    ["yes - they do have a cup ", "Welsh: yes - they do have a cup "],
      |    ["no - they don’t have a cup", "Welsh: no - they don’t have a cup"],
      |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"]
      |  ]
      |}
    """.stripMargin
  )

  // Ocelot Prototype process demonstrating range of Stanzas and range of relevant Stanza attributes
  val prototypeMetaSection: String =
    """
    |  {
    |     "id": "ext90002",
    |     "title": "Telling HMRC about extra income",
    |     "ocelot": 1,
    |     "lastAuthor": "6031631",
    |     "lastUpdate": 1579177321336,
    |     "version": 1,
    |     "filename": "ext90002.js",
    |     "processCode": "tell-hmrc"
    |  }
   """.stripMargin

  val metaSection = Json.parse(prototypeMetaSection).as[Meta]

  val prototypeFlowSection: String =
    """
      {
      "45": {
        "next": [
          "46",
          "53"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 57,
        "type": "QuestionStanza"
      },
      "98": {
        "next": [
          "99"
        ],
        "stack": true,
        "text": 65,
        "type": "InstructionStanza"
      },
      "113": {
        "next": [
          "114"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/you-need-to-tell-hmrc-rent-a-room/113"
      },
      "34": {
        "next": [
          "35"
        ],
        "stack": true,
        "text": 33,
        "type": "InstructionStanza"
      },
      "67": {
        "next": [
          "68"
        ],
        "stack": true,
        "text": 62,
        "type": "InstructionStanza"
      },
      "120": {
        "next": [
          "79"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/did-you-share-the-income"
      },
      "153": {
        "next": [
          "154"
        ],
        "stack": true,
        "text": 48,
        "type": "InstructionStanza"
      },
      "93": {
        "next": [
          "94"
        ],
        "stack": true,
        "text": 54,
        "type": "InstructionStanza"
      },
      "158": {
        "next": [
          "158a"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/somepageorother158"
      },
      "142": {
        "next": [
          "143",
          "158"
        ],
        "stack": false,
        "answers": [
          38,
          39
        ],
        "text": 26,
        "type": "QuestionStanza"
      },
      "147": {
        "next": [
          "148"
        ],
        "stack": true,
        "text": 44,
        "type": "InstructionStanza"
      },
      "12": {
        "next": [
          "13"
        ],
        "stack": true,
        "text": 12,
        "type": "InstructionStanza"
      },
      "66": {
        "next": [
          "67"
        ],
        "stack": true,
        "text": 73,
        "type": "InstructionStanza"
      },
      "89": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 51,
        "type": "InstructionStanza"
      },
      "51": {
        "next": [
          "52"
        ],
        "stack": true,
        "text": 77,
        "type": "InstructionStanza"
      },
      "124": {
        "next": [
          "125"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/did-you-share-the-income-no-income"
      },
      "84": {
        "next": [
          "85"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 59,
        "type": "CalloutStanza"
      },
      "8": {
        "next": [
          "9"
        ],
        "stack": true,
        "text": 8,
        "type": "InstructionStanza"
      },
      "73": {
        "next": [
          "74"
        ],
        "stack": true,
        "text": 31,
        "type": "InstructionStanza"
      },
      "158a": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 40,
        "type": "InstructionStanza"
      },
      "78": {
        "next": [
          "120",
          "109"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 36,
        "type": "QuestionStanza"
      },
      "19": {
        "next": [
          "20"
        ],
        "stack": true,
        "text": 18,
        "type": "InstructionStanza"
      },
      "100": {
        "next": [
          "101"
        ],
        "stack": true,
        "text": 67,
        "type": "InstructionStanza"
      },
      "23": {
        "next": [
          "24"
        ],
        "stack": true,
        "text": 22,
        "type": "InstructionStanza"
      },
      "62": {
        "next": [
          "63"
        ],
        "stack": true,
        "text": 69,
        "type": "InstructionStanza"
      },
      "135": {
        "next": [
          "136"
        ],
        "stack": true,
        "text": 45,
        "type": "InstructionStanza"
      },
      "128": {
        "next": [
          "129"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 53,
        "type": "CalloutStanza"
      },
      "4": {
        "next": [
          "5"
        ],
        "stack": true,
        "text": 4,
        "type": "InstructionStanza"
      },
      "121": {
        "next": [
          "122"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/have-you-rented-out-a-room-in-your-home-no-income"
      },
      "88": {
        "next": [
          "89"
        ],
        "stack": true,
        "text": 63,
        "type": "InstructionStanza"
      },
      "77": {
        "next": [
          "78"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/have-you-rented-out-a-room-in-your-home"
      },
      "40": {
        "next": [
          "41"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 64,
        "type": "CalloutStanza"
      },
      "110": {
        "next": [
          "111"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 58,
        "type": "CalloutStanza"
      },
      "15": {
        "next": [
          "16"
        ],
        "stack": true,
        "text": 5,
        "type": "InstructionStanza"
      },
      "11": {
        "next": [
          "12"
        ],
        "stack": true,
        "text": 11,
        "type": "InstructionStanza"
      },
      "104": {
        "next": [
          "105"
        ],
        "stack": true,
        "text": 60,
        "type": "InstructionStanza"
      },
      "90": {
        "next": [
          "91"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/do-you-want-to-use-the-rent-a-room-scheme"
      },
      "9": {
        "next": [
          "10"
        ],
        "stack": true,
        "text": 9,
        "type": "InstructionStanza"
      },
      "141": {
        "next": [
          "142"
        ],
        "noteType": "Error",
        "stack": false,
        "text": 35,
        "type": "CalloutStanza"
      },
      "139": {
        "next": [
          "140"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 37,
        "type": "CalloutStanza"
      },
      "132": {
        "next": [
          "133"
        ],
        "stack": true,
        "text": 54,
        "type": "InstructionStanza"
      },
      "44": {
        "next": [
          "45"
        ],
        "stack": true,
        "text": 45,
        "type": "InstructionStanza"
      },
      "33": {
        "next": [
          "34"
        ],
        "stack": true,
        "text": 32,
        "type": "InstructionStanza"
      },
      "117": {
        "next": [
          "118"
        ],
        "stack": true,
        "text": 62,
        "type": "InstructionStanza"
      },
      "22": {
        "next": [
          "23"
        ],
        "stack": true,
        "text": 21,
        "type": "InstructionStanza"
      },
      "56": {
        "next": [
          "57"
        ],
        "stack": true,
        "text": 61,
        "type": "InstructionStanza"
      },
      "55": {
        "next": [
          "56"
        ],
        "stack": true,
        "text": 60,
        "type": "InstructionStanza"
      },
      "26": {
        "next": [
          "27"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/have-you-made-less-than-1000"
      },
      "134": {
        "next": [
          "135"
        ],
        "stack": true,
        "text": 56,
        "type": "InstructionStanza"
      },
      "50": {
        "next": [
          "51"
        ],
        "stack": true,
        "text": 66,
        "type": "InstructionStanza"
      },
      "123": {
        "next": [
          "124",
          "138"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 36,
        "type": "QuestionStanza"
      },
      "37": {
        "next": [
          "38"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/less-than-1000/have-you-rented-out-a-room-in-your-home"
      },
      "68": {
        "next": [
          "69"
        ],
        "stack": true,
        "text": 74,
        "type": "InstructionStanza"
      },
      "61": {
        "next": [
          "62"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 68,
        "type": "CalloutStanza"
      },
      "107": {
        "next": [
          "108"
        ],
        "stack": true,
        "text": 63,
        "type": "InstructionStanza"
      },
      "13": {
        "next": [
          "14"
        ],
        "stack": true,
        "text": 13,
        "type": "InstructionStanza"
      },
      "46": {
        "next": [
          "47"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/less-than-1000/you-do-not-need-to-tell-hmrc-but-SA"
      },
      "99": {
        "next": [
          "100"
        ],
        "stack": true,
        "text": 66,
        "type": "InstructionStanza"
      },
      "24": {
        "next": [
          "25"
        ],
        "stack": true,
        "text": 5,
        "type": "InstructionStanza"
      },
      "155": {
        "next": [
          "156"
        ],
        "stack": true,
        "text": 50,
        "type": "InstructionStanza"
      },
      "94": {
        "next": [
          "95"
        ],
        "stack": true,
        "text": 56,
        "type": "InstructionStanza"
      },
      "83": {
        "next": [
          "84"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/you-need-to-tell-hmrc-rent-a-room"
      },
      "35": {
        "next": [
          "37",
          "60"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 34,
        "type": "QuestionStanza"
      },
      "16": {
        "next": [
          "17"
        ],
        "stack": true,
        "text": 15,
        "type": "InstructionStanza"
      },
      "79": {
        "next": [
          "80",
          "109"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 52,
        "type": "QuestionStanza"
      },
      "152": {
        "next": [
          "153"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 47,
        "type": "CalloutStanza"
      },
      "5": {
        "next": [
          "6"
        ],
        "stack": true,
        "text": 5,
        "type": "InstructionStanza"
      },
      "103": {
        "next": [
          "104"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 59,
        "type": "CalloutStanza"
      },
      "112": {
        "next": [
          "113",
          "90"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 26,
        "type": "QuestionStanza"
      },
      "72": {
        "next": [
          "73"
        ],
        "stack": true,
        "text": 30,
        "type": "InstructionStanza"
      },
      "10": {
        "next": [
          "11"
        ],
        "stack": true,
        "text": 10,
        "type": "InstructionStanza"
      },
      "159": {
        "next": [
          "159a"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/somepageorother"
      },
      "59": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 51,
        "type": "InstructionStanza"
      },
      "144": {
        "next": [
          "145"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 41,
        "type": "CalloutStanza"
      },
      "87": {
        "next": [
          "88"
        ],
        "stack": true,
        "text": 62,
        "type": "InstructionStanza"
      },
      "159a": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 40,
        "type": "InstructionStanza"
      },
      "48": {
        "next": [
          "49"
        ],
        "stack": true,
        "text": 75,
        "type": "InstructionStanza"
      },
      "21": {
        "next": [
          "22"
        ],
        "stack": true,
        "text": 20,
        "type": "InstructionStanza"
      },
      "116": {
        "next": [
          "117"
        ],
        "stack": true,
        "text": 61,
        "type": "InstructionStanza"
      },
      "76": {
        "next": [
          "77",
          "121"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 34,
        "type": "QuestionStanza"
      },
      "138": {
        "next": [
          "139"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/how-much-was-your-income"
      },
      "54": {
        "next": [
          "55"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 59,
        "type": "CalloutStanza"
      },
      "43": {
        "next": [
          "44"
        ],
        "stack": true,
        "text": 56,
        "type": "InstructionStanza"
      },
      "148": {
        "next": [
          "149"
        ],
        "stack": true,
        "text": 45,
        "type": "InstructionStanza"
      },
      "127": {
        "next": [
          "128"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/was-your-income-more-than-3750-no-income"
      },
      "65": {
        "next": [
          "66"
        ],
        "stack": true,
        "text": 72,
        "type": "InstructionStanza"
      },
      "71": {
        "next": [
          "72"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 29,
        "type": "CalloutStanza"
      },
      "57": {
        "next": [
          "58"
        ],
        "stack": true,
        "text": 62,
        "type": "InstructionStanza"
      },
      "108": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 51,
        "type": "InstructionStanza"
      },
      "32": {
        "next": [
          "33"
        ],
        "stack": true,
        "text": 31,
        "type": "InstructionStanza"
      },
      "80": {
        "next": [
          "81"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/was-your-income-more-than-3750"
      },
      "106": {
        "next": [
          "107"
        ],
        "stack": true,
        "text": 62,
        "type": "InstructionStanza"
      },
      "137": {
        "next": [
          "97",
          "102"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 57,
        "type": "QuestionStanza"
      },
      "82": {
        "next": [
          "83",
          "90"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 25,
        "type": "QuestionStanza"
      },
      "49": {
        "next": [
          "50"
        ],
        "stack": true,
        "text": 76,
        "type": "InstructionStanza"
      },
      "6": {
        "next": [
          "7"
        ],
        "stack": true,
        "text": 6,
        "type": "InstructionStanza"
      },
      "126": {
        "next": [
          "127",
          "159"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 52,
        "type": "QuestionStanza"
      },
      "36": {
        "next": [
          "30"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/less-than-1000/do-you-receive-any-income"
      },
      "1": {
        "next": [
          "2"
        ],
        "noteType": "Lede",
        "stack": false,
        "text": 1,
        "type": "CalloutStanza"
      },
      "39": {
        "next": [
          "40"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/less-than-1000/do-you-want-to-use-the-rent-a-room-scheme"
      },
      "140": {
        "next": [
          "141"
        ],
        "stack": true,
        "text": 25,
        "type": "InstructionStanza"
      },
      "17": {
        "next": [
          "18"
        ],
        "noteType": "SubTitle",
        "stack": false,
        "text": 16,
        "type": "CalloutStanza"
      },
      "25": {
        "next": [
          "26"
        ],
        "stack": true,
        "text": 23,
        "type": "InstructionStanza"
      },
      "60": {
        "next": [
          "61"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/less-than-1000/you-do-not-need-to-tell-hmrc"
      },
      "14": {
        "next": [
          "15"
        ],
        "stack": true,
        "text": 14,
        "type": "InstructionStanza"
      },
      "133": {
        "next": [
          "134"
        ],
        "stack": true,
        "text": 55,
        "type": "InstructionStanza"
      },
      "47": {
        "next": [
          "48"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 68,
        "type": "CalloutStanza"
      },
      "122": {
        "next": [
          "123"
        ],
        "noteType": "Error",
        "stack": false,
        "text": 35,
        "type": "CalloutStanza"
      },
      "111": {
        "next": [
          "112"
        ],
        "stack": true,
        "text": 25,
        "type": "InstructionStanza"
      },
      "102": {
        "next": [
          "103"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/you-need-to-tell-hmrc"
      },
      "31": {
        "next": [
          "32"
        ],
        "stack": true,
        "text": 30,
        "type": "InstructionStanza"
      },
      "96": {
        "next": [
          "97",
          "102"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 57,
        "type": "QuestionStanza"
      },
      "69": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 51,
        "type": "InstructionStanza"
      },
      "151": {
        "next": [
          "152"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/you-need-to-tell-hmrc-or-contact"
      },
      "95": {
        "next": [
          "96"
        ],
        "stack": true,
        "text": 45,
        "type": "InstructionStanza"
      },
      "58": {
        "next": [
          "59"
        ],
        "stack": true,
        "text": 63,
        "type": "InstructionStanza"
      },
      "145": {
        "next": [
          "146"
        ],
        "stack": true,
        "text": 42,
        "type": "InstructionStanza"
      },
      "64": {
        "next": [
          "65"
        ],
        "stack": true,
        "text": 71,
        "type": "InstructionStanza"
      },
      "53": {
        "next": [
          "54"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/less-than-1000/you-need-to-tell-hmrc"
      },
      "42": {
        "next": [
          "43"
        ],
        "stack": true,
        "text": 55,
        "type": "InstructionStanza"
      },
      "75": {
        "next": [
          "76"
        ],
        "stack": true,
        "text": 33,
        "type": "InstructionStanza"
      },
      "115": {
        "next": [
          "116"
        ],
        "stack": true,
        "text": 60,
        "type": "InstructionStanza"
      },
      "156": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 51,
        "type": "InstructionStanza"
      },
      "109": {
        "next": [
          "110"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/was-your-income-more-than-7500"
      },
      "149": {
        "next": [
          "150"
        ],
        "noteType": "Error",
        "stack": false,
        "text": 35,
        "type": "CalloutStanza"
      },
      "20": {
        "next": [
          "21"
        ],
        "stack": true,
        "text": 19,
        "type": "InstructionStanza"
      },
      "27": {
        "next": [
          "28"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 24,
        "type": "CalloutStanza"
      },
      "70": {
        "next": [
          "71"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/do-you-receive-any-income"
      },
      "2": {
        "next": [
          "3"
        ],
        "noteType": "SubTitle",
        "stack": false,
        "text": 2,
        "type": "CalloutStanza"
      },
      "86": {
        "next": [
          "87"
        ],
        "stack": true,
        "text": 61,
        "type": "InstructionStanza"
      },
      "38": {
        "next": [
          "39",
          "53"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 36,
        "type": "QuestionStanza"
      },
      "81": {
        "next": [
          "82"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 53,
        "type": "CalloutStanza"
      },
      "end": {
        "type": "EndStanza"
      },
      "160": {
        "next": [
          "1"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 0,
        "type": "CalloutStanza"
      },
      "118": {
        "next": [
          "119"
        ],
        "stack": true,
        "text": 63,
        "type": "InstructionStanza"
      },
      "92": {
        "next": [
          "93"
        ],
        "stack": true,
        "text": 54,
        "type": "InstructionStanza"
      },
      "157a": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 40,
        "type": "InstructionStanza"
      },
      "125": {
        "next": [
          "126"
        ],
        "noteType": "Error",
        "stack": false,
        "text": 35,
        "type": "CalloutStanza"
      },
      "18": {
        "next": [
          "19"
        ],
        "stack": true,
        "text": 17,
        "type": "InstructionStanza"
      },
      "101": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 51,
        "type": "InstructionStanza"
      },
      "154": {
        "next": [
          "155"
        ],
        "stack": true,
        "text": 49,
        "type": "InstructionStanza"
      },
      "30": {
        "next": [
          "31"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 29,
        "type": "CalloutStanza"
      },
      "7": {
        "next": [
          "8"
        ],
        "noteType": "SubTitle",
        "stack": false,
        "text": 7,
        "type": "CalloutStanza"
      },
      "143": {
        "next": [
          "144"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/do-you-want-to-use-the-tax-free-allowance"
      },
      "97": {
        "next": [
          "98"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/you-do-not-need-to-tell-hmrc-rent-a-room"
      },
      "130": {
        "next": [
          "113",
          "131"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 25,
        "type": "QuestionStanza"
      },
      "114": {
        "next": [
          "115"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 59,
        "type": "CalloutStanza"
      },
      "start": {
        "next": [
          "160"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/start"
      },
      "129": {
        "next": [
          "130"
        ],
        "noteType": "Error",
        "stack": false,
        "text": 35,
        "type": "CalloutStanza"
      },
      "29": {
        "next": [
          "36",
          "70"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 26,
        "type": "QuestionStanza"
      },
      "41": {
        "next": [
          "42"
        ],
        "stack": true,
        "text": 54,
        "type": "InstructionStanza"
      },
      "105": {
        "next": [
          "106"
        ],
        "stack": true,
        "text": 61,
        "type": "InstructionStanza"
      },
      "63": {
        "next": [
          "64"
        ],
        "stack": true,
        "text": 70,
        "type": "InstructionStanza"
      },
      "150": {
        "next": [
          "151",
          "157"
        ],
        "stack": false,
        "answers": [
          27,
          28
        ],
        "text": 46,
        "type": "QuestionStanza"
      },
      "3": {
        "next": [
          "4"
        ],
        "stack": true,
        "text": 3,
        "type": "InstructionStanza"
      },
      "74": {
        "next": [
          "75"
        ],
        "stack": true,
        "text": 32,
        "type": "InstructionStanza"
      },
      "91": {
        "next": [
          "92"
        ],
        "noteType": "Title",
        "stack": false,
        "text": 64,
        "type": "CalloutStanza"
      },
      "52": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 51,
        "type": "InstructionStanza"
      },
      "85": {
        "next": [
          "86"
        ],
        "stack": true,
        "text": 60,
        "type": "InstructionStanza"
      },
      "131": {
        "next": [
          "132"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/rent/1000-or-more/do-you-want-to-use-the-rent-a-room-scheme-no-income"
      },
      "28": {
        "next": [
          "29"
        ],
        "stack": true,
        "text": 25,
        "type": "InstructionStanza"
      },
      "119": {
        "next": [
          "end"
        ],
        "stack": true,
        "text": 51,
        "type": "InstructionStanza"
      },
      "136": {
        "next": [
          "137"
        ],
        "noteType": "Error",
        "stack": false,
        "text": 35,
        "type": "CalloutStanza"
      },
      "157": {
        "next": [
          "157a"
        ],
        "stack": false,
        "type": "PageStanza",
        "url": "/somepageorother157"
      },
      "146": {
        "next": [
          "147"
        ],
        "stack": true,
        "text": 43,
        "type": "InstructionStanza"
      }
    }
    """

  val prototypePhrasesSection: String =
    """
    |   [
    |     ["Telling HMRC about extra income","Welsh: Telling HMRC about extra income"],
    |     ["Check if you need to tell HMRC about extra money you've made by selling goods or services, or renting land or property.","Welsh: Check if you need to tell HMRC about extra money you've made by selling goods or services, or renting land or property."],
    |     ["Overview","Welsh: Overview"],
    |     ["In some circumstances, you do not have to tell HMRC about extra income you've made. In each tax year you can earn up to £1,000, tax free, if you are: selling goods or services (trading)","Welsh: In some circumstances, you do not have to tell HMRC about extra income you've made. In each tax year you can earn up to £1,000, tax free, if you are: selling goods or services (trading)"],
    |     ["In some circumstances, you do not have to tell HMRC about extra income you've made. In each tax year you can earn up to £1,000, tax free, if you are: renting land or property","Welsh: In some circumstances, you do not have to tell HMRC about extra income you've made. In each tax year you can earn up to £1,000, tax free, if you are: renting land or property"],
    |     ["A tax year runs from 6 April one year to 5 April the next.","Welsh: A tax year runs from 6 April one year to 5 April the next."],
    |     ["Check if you need to tell HMRC about your extra income","Welsh: Check if you need to tell HMRC about your extra income"],
    |     ["I've made extra income from selling goods or services","Welsh: I've made extra income from selling goods or services"],
    |     ["This can include selling items or offering freelance services. If you make extra money in this way, you're likely to be trading.","Welsh: This can include selling items or offering freelance services. If you make extra money in this way, you're likely to be trading."],
    |     ["Find out more about [link:how HMRC decides if you are trading or not.:https://www.youtube.com/watch?v=MYgCctGY_Ug]","Welsh: Find out more about [link:how HMRC decides if you are trading or not.:https://www.youtube.com/watch?v=MYgCctGY_Ug]"],
    |     ["If you've only sold personal possessions then you're probably not trading. You will not have to pay income tax on the money you make, but you might have to pay [link:Capital Gains Tax:https://www.gov.uk/capital-gains-tax].","Welsh: If you've only sold personal possessions then you're probably not trading. You will not have to pay income tax on the money you make, but you might have to pay [link:Capital Gains Tax:https://www.gov.uk/capital-gains-tax]."],
    |     ["[bold:The trading allowance]","[bold:Welsh: The trading allowance]"],
    |     ["The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: selling items online or face to face","Welsh: The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: selling items online or face to face"],
    |     ["The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: selling freelance services (such as gardening or babysitting)","Welsh: The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: selling freelance services (such as gardening or babysitting)"],
    |     ["The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: hiring out personal equipment (such as power tools)","Welsh: The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: hiring out personal equipment (such as power tools)"],
    |     ["Check if you need to tell HMRC about income you've made by selling goods or services","Welsh: Check if you need to tell HMRC about income you've made by selling goods or services"],
    |     ["I've made extra income from renting land or property","Welsh: I've made extra income from renting land or property"],
    |     ["Property income can include any money you earn by renting land or buildings.","Welsh: Property income can include any money you earn by renting land or buildings."],
    |     ["[bold:The property allowance]","[bold:Welsh: The property allowance]"],
    |     ["The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting a flat or house","Welsh: The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting a flat or house"],
    |     ["The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting out a room in your home","Welsh: The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting out a room in your home"],
    |     ["The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: short term holiday lets","Welsh: The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: short term holiday lets"],
    |     ["The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting out a parking space or garage","Welsh: The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting out a parking space or garage"],
    |     ["Check if you need to tell HMRC about income you've made by renting land or property","Welsh: Check if you need to tell HMRC about income you've made by renting land or property"],
    |     ["Was your income from land or property less than £1,000?[hint:Answer Yes or No!]","Welsh: Was your income from land or property less than £1,000?[hint:Welsh: Answer Yes or No!]"],
    |     ["To work this out, add up all the income you've received from your land or property. Include money received from tenants for rent, utility bills and food. Do not deduct any expenses.","Welsh: To work this out, add up all the income you've received from your land or property. Include money received from tenants for rent, utility bills and food. Do not deduct any expenses."],
    |     ["If you share the income with someone else, only include your share of the earnings.","Welsh: If you share the income with someone else, only include your share of the earnings."],
    |     ["Yes","Welsh: Yes"],
    |     ["No","Welsh: No"],
    |     ["Have you received additional trade or property income from a company or person you are connected to?","Welsh: Have you received additional trade or property income from a company or person you are connected to?"],
    |     ["This includes: a company you (or a relative) owns or controls","Welsh: This includes: a company you (or a relative) owns or controls"],
    |     ["This includes: a partnership where you (or a relative) is a partner","Welsh: This includes: a partnership where you (or a relative) is a partner"],
    |     ["This includes: your employer (or the employer of your spouse or civil partner)","Welsh: This includes: your employer (or the employer of your spouse or civil partner)"],
    |     ["Additional trade or property income includes any money you've received for providing services (such as babysitting), selling items you've bought or made, or renting property.","Welsh: Additional trade or property income includes any money you've received for providing services (such as babysitting), selling items you've bought or made, or renting property."],
    |     ["A relative includes your spouse or civil partner. It also includes your family members (excluding aunts, uncles or cousins), or their spouse or civil partner.","Welsh: A relative includes your spouse or civil partner. It also includes your family members (excluding aunts, uncles or cousins), or their spouse or civil partner."],
    |     ["Please select","Welsh: Please select"],
    |     ["Have you only rented a room in your main home?","Welsh: Have you only rented a room in your main home?"],
    |     ["How much was your income from property and/or land?","Welsh: How much was your income from property and/or land?"],
    |     ["Between £1,000 and £2,500","Welsh: Between £1,000 and £2,500"],
    |     ["More than £2,500","Welsh: More than £2,500"],
    |     ["This has not been built yet","Welsh: This has not been built yet"],
    |     ["Do you want to use the tax-free trading allowance?","Welsh: Do you want to use the tax-free trading allowance?"],
    |     ["You can choose whether to: use the £1000 tax-free property allowance. This means you will not pay tax on the first £1000 of profit","Welsh: You can choose whether to: use the £1000 tax-free property allowance. This means you will not pay tax on the first £1000 of profit"],
    |     ["You can choose whether to: deduct allowable expenses (such as the cost of maintenance and a percentage of your mortgage interest) from your land or property income. You may want to do this if you have a lot of expenses, or if you want to declare a loss.","Welsh: You can choose whether to: deduct allowable expenses (such as the cost of maintenance and a percentage of your mortgage interest) from your land or property income. You may want to do this if you have a lot of expenses, or if you want to declare a loss."],
    |     ["If you claim the £1000 tax-free property allowance, you cannot deduct allowable expenses or other allowances.","Welsh: If you claim the £1000 tax-free property allowance, you cannot deduct allowable expenses or other allowances."],
    |     ["Your choice is likely to depend on how much you've earned and how much your expenses and allowances are.","Welsh: Your choice is likely to depend on how much you've earned and how much your expenses and allowances are."],
    |     ["Find out more about [link:Tax-free allowances:https://www.gov.uk/guidance/tax-free-allowances-on-property-and-trading-income], claiming [link:allowable expenses:https://www.gov.uk/expenses-if-youre-self-employed] and [link:deducting other financial costs from your rental income:https://www.gov.uk/guidance/changes-to-tax-relief-for-residential-landlords-how-its-worked-out-including-case-studies].","Welsh: Find out more about [link:Tax-free allowances:https://www.gov.uk/guidance/tax-free-allowances-on-property-and-trading-income], claiming [link:allowable expenses:https://www.gov.uk/expenses-if-youre-self-employed] and [link:deducting other financial costs from your rental income:https://www.gov.uk/guidance/changes-to-tax-relief-for-residential-landlords-how-its-worked-out-including-case-studies]."],
    |     ["You need tell HMRC about this income","Welsh: You need tell HMRC about this income"],
    |     ["You can tell HMRC about this income on a Self Assessment tax return.","Welsh: You can tell HMRC about this income on a Self Assessment tax return."],
    |     ["If you do not normally complete a Self Assessment, you can [link:contact HMRC:https://www.gov.uk/government/organisations/hm-revenue-customs/contact/income-tax-enquiries-for-individuals-pensioners-and-employees] to discuss alternative ways of reporting this income.","Welsh: If you do not normally complete a Self Assessment, you can [link:contact HMRC:https://www.gov.uk/government/organisations/hm-revenue-customs/contact/income-tax-enquiries-for-individuals-pensioners-and-employees] to discuss alternative ways of reporting this income."],
    |     ["Find out more about [link:Self Assessment tax returns:https://www.gov.uk/self-assessment-tax-returns].","Welsh: Find out more about [link:Self Assessment tax returns:https://www.gov.uk/self-assessment-tax-returns]."],
    |     ["[link:Check if you need to tell HMRC about income you've made by selling goods or services:https://tell-hmrc-about-extra-income.herokuapp.com/version-3/sales/did-you-only-sell-personal-possessions].","Welsh: [link:Check if you need to tell HMRC about income you've made by selling goods or services:https://tell-hmrc-about-extra-income.herokuapp.com/version-3/sales/did-you-only-sell-personal-possessions]."],
    |     ["Did you share the income with anyone else?","Welsh: Did you share the income with anyone else?"],
    |     ["Was your income from renting a room/rooms more than £3,750?","Welsh: Was your income from renting a room/rooms more than £3,750?"],
    |     ["You can choose whether to: use the Rent a Room Scheme. This means you are automatically entitled to £7,500 of tax-free property income (or £3,750 if you share your property income with other people)","Welsh: You can choose whether to: use the Rent a Room Scheme. This means you are automatically entitled to £7,500 of tax-free property income (or £3,750 if you share your property income with other people)"],
    |     ["You can choose whether to: claim allowable expenses (such as the cost of maintenance and a percentage of your mortgage interest) from your rental income. This reduces the amount of tax they pay on that income. It can also be used to declare a loss.","Welsh: You can choose whether to: claim allowable expenses (such as the cost of maintenance and a percentage of your mortgage interest) from your rental income. This reduces the amount of tax they pay on that income. It can also be used to declare a loss."],
    |     ["If you use the Rent a Room Scheme, you cannot deduct allowable expenses.","Welsh: If you use the Rent a Room Scheme, you cannot deduct allowable expenses."],
    |     ["Find out more about the [link:Rent a Room Scheme:https://www.gov.uk/government/publications/rent-a-room-for-traders-hs223-self-assessment-helpsheet/hs223-rent-a-room-scheme-2019],claiming [link:allowable expenses:https://www.gov.uk/expenses-if-youre-self-employed] and [link:deducting other financial costs from your rental income:https://www.gov.uk/expenses-if-youre-self-employed].","Welsh: Find out more about the [link:Rent a Room Scheme:https://www.gov.uk/government/publications/rent-a-room-for-traders-hs223-self-assessment-helpsheet/hs223-rent-a-room-scheme-2019],claiming [link:allowable expenses:https://www.gov.uk/expenses-if-youre-self-employed] and [link:deducting other financial costs from your rental income:https://www.gov.uk/expenses-if-youre-self-employed]."],
    |     ["Was your income from renting a room/rooms more than £7,500?","Welsh: Was your income from renting a room/rooms more than £7,500?"],
    |     ["You need to tell HMRC about all your income on a Self Assessment tax return","Welsh: You need to tell HMRC about all your income on a Self Assessment tax return"],
    |     ["Self Assessment tax returns are a way of reporting your income to HMRC. You need to include the income you have received from trading on your Self Assessment tax return.","Welsh: Self Assessment tax returns are a way of reporting your income to HMRC. You need to include the income you have received from trading on your Self Assessment tax return."],
    |     ["You can deduct allowable expenses (such as the cost of maintenance and a percentage of your mortgage interest) from your rental income. This reduces the amount of tax you pay on that income. If you made a loss on renting your land or property, you may be able to offset future tax against this loss.","Welsh: You can deduct allowable expenses (such as the cost of maintenance and a percentage of your mortgage interest) from your rental income. This reduces the amount of tax you pay on that income. If you made a loss on renting your land or property, you may be able to offset future tax against this loss."],
    |     ["If you have not previously completed a Self Assessment, you will need to [link:register for Self Assessment:https://www.gov.uk/log-in-file-self-assessment-tax-return/register-if-youre-self-employed] by 5 October in the following year.","Welsh: If you have not previously completed a Self Assessment, you will need to [link:register for Self Assessment:https://www.gov.uk/log-in-file-self-assessment-tax-return/register-if-youre-self-employed] by 5 October in the following year."],
    |     ["Find out more about [link:claiming allowable expenses:https://www.gov.uk/expenses-if-youre-self-employed] and [link:deducting other financial costs from your rental income:https://www.gov.uk/guidance/changes-to-tax-relief-for-residential-landlords-how-its-worked-out-including-case-studies].","Welsh: Find out more about [link:claiming allowable expenses:https://www.gov.uk/expenses-if-youre-self-employed] and [link:deducting other financial costs from your rental income:https://www.gov.uk/guidance/changes-to-tax-relief-for-residential-landlords-how-its-worked-out-including-case-studies]."],
    |     ["Do you want to use the Rent a Room Scheme or claim allowable expenses?","Welsh: Do you want to use the Rent a Room Scheme or claim allowable expenses?"],
    |     ["If your rental income is less than £7,500 (or £3,750 if you share your income), you are automatically entitled to use the Rent a Room Scheme. This means you do not need to pay tax on this income, and you do not need to tell HMRC.","Welsh: If your rental income is less than £7,500 (or £3,750 if you share your income), you are automatically entitled to use the Rent a Room Scheme. This means you do not need to pay tax on this income, and you do not need to tell HMRC."],
    |     ["If you want to opt out of the Rent a Room Scheme in future (for example, if you want to claim allowable expenses), you must let HMRC know within one year of 31 January following the end of the tax year.","Welsh: If you want to opt out of the Rent a Room Scheme in future (for example, if you want to claim allowable expenses), you must let HMRC know within one year of 31 January following the end of the tax year."],
    |     ["Find out more about the [link:Rent a Room Scheme:https://www.gov.uk/government/publications/rent-a-room-for-traders-hs223-self-assessment-helpsheet/hs223-rent-a-room-scheme-2019].","Welsh: Find out more about the [link:Rent a Room Scheme:https://www.gov.uk/government/publications/rent-a-room-for-traders-hs223-self-assessment-helpsheet/hs223-rent-a-room-scheme-2019]."],
    |     ["You do not need to tell HMRC about your property income","Welsh: You do not need to tell HMRC about your property income"],
    |     ["You are automatically entitled to the £1000 tax-free property allowance. You do not need to report this income to HMRC.","Welsh: You are automatically entitled to the £1000 tax-free property allowance. You do not need to report this income to HMRC."],
    |     ["There are some circumstances where you may still want to tell HMRC about your income using a Self Assessment form. For example: you've made a loss and want to claim relief on a tax return","Welsh: There are some circumstances where you may still want to tell HMRC about your income using a Self Assessment form. For example: you've made a loss and want to claim relief on a tax return"],
    |     ["There are some circumstances where you may still want to tell HMRC about your income using a Self Assessment form. For example: you want to pay voluntary Class 2 National Insurance contributions to help qualify for some benefits","Welsh: There are some circumstances where you may still want to tell HMRC about your income using a Self Assessment form. For example: you want to pay voluntary Class 2 National Insurance contributions to help qualify for some benefits"],
    |     ["There are some circumstances where you may still want to tell HMRC about your income using a Self Assessment form. For example: you want to claim Tax Free Childcare for childcare costs based on your self employment income","Welsh: There are some circumstances where you may still want to tell HMRC about your income using a Self Assessment form. For example: you want to claim Tax Free Childcare for childcare costs based on your self employment income"],
    |     ["There are some circumstances where you may still want to tell HMRC about your income using a Self Assessment form. For example: you want to claim Maternity Allowance, based on your self-employment","Welsh: There are some circumstances where you may still want to tell HMRC about your income using a Self Assessment form. For example: you want to claim Maternity Allowance, based on your self-employment"],
    |     ["Find out more about [link:Tax-free allowances:https://www.gov.uk/guidance/tax-free-allowances-on-property-and-trading-income] and [link:Self Assessment tax returns:https://www.gov.uk/self-assessment-tax-returns].","Welsh: Find out more about [link:Tax-free allowances:https://www.gov.uk/guidance/tax-free-allowances-on-property-and-trading-income] and [link:Self Assessment tax returns:https://www.gov.uk/self-assessment-tax-returns]."],
    |     ["Because you have received trade or property income from a company, partnership or your employer, you need to complete a Self Assessment tax return.","Welsh: Because you have received trade or property income from a company, partnership or your employer, you need to complete a Self Assessment tax return."],
    |     ["However, if your rental income is less than £7,500 (or £3,750 if you share your income), you are automatically entitled to use the Rent a Room Scheme. This means you do not need to pay tax on this income, and you do not need to tell HMRC.","Welsh: However, if your rental income is less than £7,500 (or £3,750 if you share your income), you are automatically entitled to use the Rent a Room Scheme. This means you do not need to pay tax on this income, and you do not need to tell HMRC."],
    |     ["Find out more about the [link:Rent a Room Scheme:https://www.gov.uk/government/publications/rent-a-room-for-traders-hs223-self-assessment-helpsheet/hs223-rent-a-room-scheme-2019] and [link:Self Assessment tax returns:https://www.gov.uk/self-assessment-tax-returns].","Welsh: Find out more about the [link:Rent a Room Scheme:https://www.gov.uk/government/publications/rent-a-room-for-traders-hs223-self-assessment-helpsheet/hs223-rent-a-room-scheme-2019] and [link:Self Assessment tax returns:https://www.gov.uk/self-assessment-tax-returns]."]
    |   ]
    """.stripMargin

  // Create and add an orphaned link within the links section
  private val id = 0
  private val dest = "http://www.bbc.co.uk/news"
  private val title = "BBC News"
  private val window = false
  private val leftbar = false
  private val always = false
  private val popUp = false

  val prototypeLinksSection: String =
    s"""[
         |{
         |   "id": ${id},
         |   "dest": "${dest}",
         |   "title": "${title}",
         |   "window": ${window},
         |   "leftbar": ${leftbar},
         |   "always": ${always},
         |   "popup": ${popUp}
         |}
        ]""".stripMargin

  val prototypeJson: JsObject = Json
    .parse(
      s"""{ "meta" : ${prototypeMetaSection},
         | "flow": ${prototypeFlowSection},
         | "phrases": ${prototypePhrasesSection},
         | "links": ${prototypeLinksSection},
         | "contacts": [],
         | "howto": []}""".stripMargin
    )
    .as[JsObject]

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
      |    "processCode": "eg-auto-test"
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
      |      "Welsh: External Guidance Testing process"
      |    ],
      |    [
      |      "This process helps to automate testing of components which are generated from the external viewer.",
      |      "Welsh: This process helps to automate testing of components which are generated from the external viewer."
      |    ],
      |    [
      |      "What is External Guidance?",
      |      "Welsh: What is External Guidance?"
      |    ],
      |    [
      |      "The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: design",
      |      "Welsh: The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: design"
      |    ],
      |    [
      |      "The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: build",
      |      "Welsh: The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: build"
      |    ],
      |    [
      |      "The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: accessibility standards",
      |      "Welsh: The decision trees, produced by designers using Ocelot, that citizens can read in order to self-serve answers to their enquiries, rather than calling the contact centre. The guidance would meet the correct GOV.UK: accessibility standards"
      |    ],
      |    [
      |      "What is Ocelot?",
      |      "Welsh: What is Ocelot?"
      |    ],
      |    [
      |      "Ocelot is a content management system that has been developed to support the people working in call centres who need to advise citizens about tax processes. It works by providing a directed script of content that branches with each question.",
      |      "Welsh: Ocelot is a content management system that has been developed to support the people working in call centres who need to advise citizens about tax processes. It works by providing a directed script of content that branches with each question."
      |    ],
      |    [
      |      "It resides on the \"Stride\" network and developed by HMRC. For simple queries about your tax, you can contact [link:HM Revenue and Customs (HMRC):https://www.gov.uk/government/organisations/hm-revenue-customs/contact]",
      |      "Welsh: It resides on the \"Stride\" network and developed by HMRC. For simple queries about your tax, you can contact [link:HM Revenue and Customs (HMRC):https://www.gov.uk/government/organisations/hm-revenue-customs/contact]"
      |    ],
      |    [
      |      "To know more about different user roles in Ocelot",
      |      "Welsh: To know more about different user roles in Ocelot"
      |    ],
      |    [
      |      "Integration",
      |      "Welsh: Integration"
      |    ],
      |    [
      |      "The Ocelot design tool produces a JSON file which External guidance service needs to consume in order to render the web pages.",
      |      "Welsh: The Ocelot design tool produces a JSON file which External guidance service needs to consume in order to render the web pages."
      |    ],
      |    [
      |      "User role",
      |      "Welsh: User role"
      |    ],
      |    [
      |      "External guidance users can be assigned one, or more, of three roles Designer: designs the guidance using the Ocelot editor",
      |      "Welsh: External guidance users can be assigned one, or more, of three roles Designer: designs the guidance using the Ocelot editor"
      |    ],
      |    [
      |      "External guidance users can be assigned one, or more, of three roles Approver: reviews and approves the guidance produced by the designer",
      |      "Welsh: External guidance users can be assigned one, or more, of three roles Approver: reviews and approves the guidance produced by the designer"
      |    ],
      |    [
      |      "External guidance users can be assigned one, or more, of three roles Publisher: publishes approved guidance to the public",
      |      "Welsh: External guidance users can be assigned one, or more, of three roles Publisher: publishes approved guidance to the public"
      |    ],
      |    [
      |      "Check your understanding of different roles in Ocelot",
      |      "Welsh: Check your understanding of different roles in Ocelot"
      |    ],
      |    [
      |      "Correct answer leads forward to next question",
      |      "Welsh: Correct answer leads forward to next question"
      |    ],
      |    [
      |      "Who reviews and approves the g2uid1ance produced by the designer?",
      |      "Welsh: Who reviews and approves the g2uid1ance produced by the designer?"
      |    ],
      |    [
      |      "Users with the designer role",
      |      "Welsh: Users with the designer role"
      |    ],
      |    [
      |      "Users with the approver role",
      |      "Welsh: Users with the approver role"
      |    ],
      |    [
      |      "Do designers design guidance using the Ocelot editor?",
      |      "Welsh: Do designers design guidance using the Ocelot editor?"
      |    ],
      |    [
      |      "Yes",
      |      "Welsh: Yes"
      |    ],
      |    [
      |      "No",
      |      "Welsh: No"
      |    ],
      |    [
      |      "Oops! Wrong answer",
      |      "Welsh: Oops! Wrong answer"
      |    ],
      |    [
      |      "Sorry! You lost your place in the external guidance team. Mac will buy you a Pepsi Max to cheer you up!",
      |      "Welsh: Sorry! You lost your place in the external guidance team. Mac will buy you a Pepsi Max to cheer you up!"
      |    ],
      |    [
      |      "To know more about different users in Ocelot",
      |      "Welsh: To know more about different users in Ocelot"
      |    ],
      |    [
      |      "What is the role of publisher",
      |      "Welsh: What is the role of publisher"
      |    ],
      |    [
      |      "Yes [hint:This is hint text]",
      |      "Welsh: Yes [hint:This is hint text]"
      |    ],
      |    [
      |      "No [hint:This is hint text]",
      |      "Welsh: No [hint:This is hint text]"
      |    ],
      |    [
      |      "Congratulations",
      |      "Welsh: Congratulations"
      |    ],
      |    [
      |      "The external guidance team welcomes you! Ian will buy you a coffee.",
      |      "Welsh: The external guidance team welcomes you! Ian will buy you a coffee."
      |    ],
      |    [
      |      "Go to start of process",
      |      "Welsh: Go to start of process"
      |    ],
      |    [
      |      "You must choose one of the two options shown",
      |      "Welsh: You must choose one of the two options shown"
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

  // Do you need to tell HMRC about extra income V6 bullet point list bug example
  val prototypeExtraIncomeV6MetaSection: String =
    """
      |  {
      |     "id": "oct90002",
      |     "title": "Telling HMRC about extra income",
      |     "ocelot": 1,
      |     "lastAuthor": "7903088",
      |     "lastUpdate": 1579177321336,
      |     "version": 1,
      |     "filename": "oct90002.js",
      |     "processCode": "tell-hmrc"
      |  }
   """.stripMargin

  val prototypeExtraIncomeV6FlowSection: String =
  """
     {
      "start" : {
        "type" : "PageStanza",
        "url" : "/sales/do-you-receive-any-income-no-SA",
        "next" : [
          "1"
        ],
        "stack" : false
      },
      "1" : {
        "type" : "InstructionStanza",
        "text" : 0,
        "next" : [
          "2"
        ],
        "stack" : false
      },
      "2" : {
        "type" : "InstructionStanza",
        "text" : 1,
        "next" : [
          "3"
        ],
        "stack" : true
      },
      "3" : {
        "type" : "InstructionStanza",
        "text" : 2,
        "next" : [
          "4"
        ],
        "stack" : true
      },
      "4" : {
        "type" : "InstructionStanza",
        "text" : 3,
        "next" : [
          "end"
        ],
        "stack" : true
      },
      "end": {
      "type": "EndStanza"
      }
    }
      """

    val prototypeExtraIncomeV6PhrasesSection: String =
    """
        |[
        |      [
        |        "You've received income that you have not yet paid tax on from: a business you own or control (such as a partnership or limited company)",
        |        "Welsh: You've received income that you have not yet paid tax on from: a business you own or control (such as a partnership or limited company)"
        |      ],
        |      [
        |        "You've received income that you have not yet paid tax on from: a business a relative owns or controls",
        |        "Welsh: You've received income that you have not yet paid tax on from: a business a relative owns or controls"
        |      ],
        |      [
        |        "You've received income that you have not yet paid tax on from: your employer (for example for freelance services outside your normal contract hours)",
        |        "Welsh: You've received income that you have not yet paid tax on from: your employer (for example for freelance services outside your normal contract hours)"
        |      ],
        |      [
        |        "You've received income that you have not yet paid tax on from: the employer of your spouse or civil partner",
        |        "Welsh: You've received income that you have not yet paid tax on from: the employer of your spouse or civil partner"
        |      ]
        |    ]
        |""".stripMargin

  val prototypeExtraIncomeV6Json: JsObject = Json
    .parse(
      s"""{ "meta" : ${prototypeExtraIncomeV6MetaSection},
         | "flow": ${prototypeExtraIncomeV6FlowSection},
         | "phrases": ${prototypeExtraIncomeV6PhrasesSection},
         | "links": [],
         | "contacts": [],
         | "howto": []}""".stripMargin
    )
    .as[JsObject]

  val simpleRowStanzaProcessAsString: String =
  """{
    |  "meta": {
    |    "title": "Simple Row Stanza example",
    |    "id": "oct90005",
    |    "ocelot": 1,
    |    "lastAuthor": "000000",
    |    "lastUpdate": 1500298931016,
    |    "version": 4,
    |    "filename": "SimpleRowStanza.json",
    |    "processCode": "simple-row-stanza"
    |  },
    |  "howto": [],
    |  "contacts": [],
    |  "links": [],
    |  "flow": {
    |    "start": {
    |      "type": "PageStanza",
    |      "url":"/start",
    |      "next": ["1"],
    |      "stack": true
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
    |    "2": {
    |      "next": [ "3" ],
    |      "stack": false,
    |      "cells": [1],
    |      "type": "RowStanza"
    |    },
    |    "3": {
    |      "next": [ "4" ],
    |      "stack": true,
    |      "cells": [2, 3, 4, 5],
    |      "type": "RowStanza"
    |    },
    |     "4": {
    |      "next": [ "end" ],
    |      "stack": true,
    |      "cells": [],
    |      "type": "RowStanza"
    |    },
    |    "end": {
    |      "type": "EndStanza"
    |    }
    |  },
    |  "phrases": [
    |    ["Simple row stanza example", "Welsh: Simple row stanza example"],
    |    ["Text for single cell row stanza", "Welsh: Text for single cell row stanza"],
    |    ["Cell one text", "Welsh: Cell one text"],
    |    ["Cell two text", "Welsh: Cell two text"],
    |    ["Cell three text", "Welsh: Cell three text"],
    |    ["Cell four text", "Welsh: Cell four text"]
    |  ]
    |}
    |""".stripMargin

  val simpleRowStanzaProcessAsJson: JsObject = Json.parse(simpleRowStanzaProcessAsString).as[JsObject]
}
