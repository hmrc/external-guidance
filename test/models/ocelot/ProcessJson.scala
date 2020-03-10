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

package models.ocelot

import play.api.libs.json.{JsValue, Json, JsObject}

//
// All Welsh translations are temporary placeholders and for testing purposes only
//
trait ProcessJson {

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
      |    "filename": "oct90001.js"
      |  },
      |  "howto": [],
      |  "contacts": [],
      |  "links": [],
      |  "flow": {
      |    "1": {
      |      "type": "ValueStanza",
      |      "values": [
      |        {
      |          "type": "scalar",
      |          "label": "PageUrl",
      |          "value": "/feeling-bad"
      |        }
      |      ],
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
    |     "filename": "ext90002.js"
    |  }
   """.stripMargin

  val metaSection = Json.parse(prototypeMetaSection).as[Meta]

  val prototypeFlowSection: String =
    """
    |   {
    |     "1": {
    |       "type": "CalloutStanza",
    |       "text": 1,
    |       "noteType": "Lede",
    |       "next": [
    |         "2"
    |       ],
    |       "stack": false
    |     },
    |     "2": {
    |       "type": "CalloutStanza",
    |       "text": 2,
    |       "noteType": "SubTitle",
    |       "next": [
    |         "3"
    |       ],
    |       "stack": false
    |     },
    |     "3": {
    |       "type": "InstructionStanza",
    |       "text": 3,
    |       "next": [
    |         "4"
    |       ],
    |       "stack": true
    |     },
    |     "4": {
    |       "type": "InstructionStanza",
    |       "text": 4,
    |       "next": [
    |         "5"
    |       ],
    |       "stack": true
    |     },
    |     "5": {
    |       "type": "InstructionStanza",
    |       "text": 5,
    |       "next": [
    |         "6"
    |       ],
    |       "stack": true
    |     },
    |     "6": {
    |       "type": "InstructionStanza",
    |       "text": 6,
    |       "next": [
    |         "7"
    |       ],
    |       "stack": true
    |     },
    |     "7": {
    |       "type": "CalloutStanza",
    |       "text": 7,
    |       "noteType": "SubTitle",
    |       "next": [
    |         "8"
    |       ],
    |       "stack": false
    |     },
    |     "8": {
    |       "type": "InstructionStanza",
    |       "text": 8,
    |       "next": [
    |         "9"
    |       ],
    |       "stack": true
    |     },
    |     "9": {
    |       "type": "InstructionStanza",
    |       "text": 9,
    |       "next": [
    |         "10"
    |       ],
    |       "stack": true
    |     },
    |     "10": {
    |       "type": "InstructionStanza",
    |       "text": 10,
    |       "next": [
    |         "11"
    |       ],
    |       "stack": true
    |     },
    |     "11": {
    |       "type": "InstructionStanza",
    |       "text": 11,
    |       "next": [
    |         "12"
    |       ],
    |       "stack": true
    |     },
    |     "12": {
    |       "type": "InstructionStanza",
    |       "text": 12,
    |       "next": [
    |         "13"
    |       ],
    |       "stack": true
    |     },
    |     "13": {
    |       "type": "InstructionStanza",
    |       "text": 13,
    |       "next": [
    |         "14"
    |       ],
    |       "stack": true
    |     },
    |     "14": {
    |       "type": "InstructionStanza",
    |       "text": 14,
    |       "next": [
    |         "15"
    |       ],
    |       "stack": true
    |     },
    |     "15": {
    |       "type": "InstructionStanza",
    |       "text": 5,
    |       "next": [
    |         "16"
    |       ],
    |       "stack": true
    |     },
    |     "16": {
    |       "type": "InstructionStanza",
    |       "text": 15,
    |       "next": [
    |         "17"
    |       ],
    |       "stack": true
    |     },
    |     "17": {
    |       "type": "CalloutStanza",
    |       "text": 16,
    |       "noteType": "SubTitle",
    |       "next": [
    |         "18"
    |       ],
    |       "stack": false
    |     },
    |     "18": {
    |       "type": "InstructionStanza",
    |       "text": 17,
    |       "next": [
    |         "19"
    |       ],
    |       "stack": true
    |     },
    |     "19": {
    |       "type": "InstructionStanza",
    |       "text": 18,
    |       "next": [
    |         "20"
    |       ],
    |       "stack": true
    |     },
    |     "20": {
    |       "type": "InstructionStanza",
    |       "text": 19,
    |       "next": [
    |         "21"
    |       ],
    |       "stack": true
    |     },
    |     "21": {
    |       "type": "InstructionStanza",
    |       "text": 20,
    |       "next": [
    |         "22"
    |       ],
    |       "stack": true
    |     },
    |     "22": {
    |       "type": "InstructionStanza",
    |       "text": 21,
    |       "next": [
    |         "23"
    |       ],
    |       "stack": true
    |     },
    |     "23": {
    |       "type": "InstructionStanza",
    |       "text": 22,
    |       "next": [
    |         "24"
    |       ],
    |       "stack": true
    |     },
    |     "24": {
    |       "type": "InstructionStanza",
    |       "text": 5,
    |       "next": [
    |         "25"
    |       ],
    |       "stack": true
    |     },
    |     "25": {
    |       "type": "InstructionStanza",
    |       "text": 23,
    |       "next": [
    |         "26"
    |       ],
    |       "stack": true
    |     },
    |     "26": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/have-you-made-less-than-1000"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageTitle",
    |           "value": "Telling HMRC about extra income"
    |         }
    |       ],
    |       "next": [
    |         "27"
    |       ],
    |       "stack": false
    |     },
    |     "27": {
    |       "type": "CalloutStanza",
    |       "text": 24,
    |       "noteType": "Title",
    |       "next": [
    |         "28"
    |       ],
    |       "stack": false
    |     },
    |     "28": {
    |       "type": "InstructionStanza",
    |       "text": 25,
    |       "next": [
    |         "29"
    |       ],
    |       "stack": true
    |     },
    |     "29": {
    |       "type": "QuestionStanza",
    |       "text": 26,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "36",
    |         "70"
    |       ],
    |       "stack": false
    |     },
    |     "30": {
    |       "type": "CalloutStanza",
    |       "text": 29,
    |       "noteType": "Title",
    |       "next": [
    |         "31"
    |       ],
    |       "stack": false
    |     },
    |     "31": {
    |       "type": "InstructionStanza",
    |       "text": 30,
    |       "next": [
    |         "32"
    |       ],
    |       "stack": true
    |     },
    |     "32": {
    |       "type": "InstructionStanza",
    |       "text": 31,
    |       "next": [
    |         "33"
    |       ],
    |       "stack": true
    |     },
    |     "33": {
    |       "type": "InstructionStanza",
    |       "text": 32,
    |       "next": [
    |         "34"
    |       ],
    |       "stack": true
    |     },
    |     "34": {
    |       "type": "InstructionStanza",
    |       "text": 33,
    |       "next": [
    |         "35"
    |       ],
    |       "stack": true
    |     },
    |     "35": {
    |       "type": "QuestionStanza",
    |       "text": 34,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "37",
    |         "60"
    |       ],
    |       "stack": false
    |     },
    |     "36": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/less-than-1000/do-you-receive-any-income"
    |         }
    |       ],
    |       "next": [
    |         "30"
    |       ],
    |       "stack": false
    |     },
    |     "37": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/less-than-1000/have-you-rented-out-a-room-in-your-home"
    |         }
    |       ],
    |       "next": [
    |         "38"
    |       ],
    |       "stack": false
    |     },
    |     "38": {
    |       "type": "QuestionStanza",
    |       "text": 36,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "39",
    |         "53"
    |       ],
    |       "stack": false
    |     },
    |     "39": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/less-than-1000/do-you-want-to-use-the-rent-a-room-scheme"
    |         }
    |       ],
    |       "next": [
    |         "40"
    |       ],
    |       "stack": false
    |     },
    |     "40": {
    |       "type": "CalloutStanza",
    |       "text": 64,
    |       "noteType": "Title",
    |       "next": [
    |         "41"
    |       ],
    |       "stack": false
    |     },
    |     "41": {
    |       "type": "InstructionStanza",
    |       "text": 54,
    |       "next": [
    |         "42"
    |       ],
    |       "stack": true
    |     },
    |     "42": {
    |       "type": "InstructionStanza",
    |       "text": 55,
    |       "next": [
    |         "43"
    |       ],
    |       "stack": true
    |     },
    |     "43": {
    |       "type": "InstructionStanza",
    |       "text": 56,
    |       "next": [
    |         "44"
    |       ],
    |       "stack": true
    |     },
    |     "44": {
    |       "type": "InstructionStanza",
    |       "text": 45,
    |       "next": [
    |         "45"
    |       ],
    |       "stack": true
    |     },
    |     "45": {
    |       "type": "QuestionStanza",
    |       "text": 57,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "46",
    |         "53"
    |       ],
    |       "stack": false
    |     },
    |     "46": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/less-than-1000/you-do-not-need-to-tell-hmrc-but-SA"
    |         }
    |       ],
    |       "next": [
    |         "47"
    |       ],
    |       "stack": false
    |     },
    |     "47": {
    |       "type": "CalloutStanza",
    |       "text": 68,
    |       "noteType": "Title",
    |       "next": [
    |         "48"
    |       ],
    |       "stack": false
    |     },
    |     "48": {
    |       "type": "InstructionStanza",
    |       "text": 75,
    |       "next": [
    |         "49"
    |       ],
    |       "stack": true
    |     },
    |     "49": {
    |       "type": "InstructionStanza",
    |       "text": 76,
    |       "next": [
    |         "50"
    |       ],
    |       "stack": true
    |     },
    |     "50": {
    |       "type": "InstructionStanza",
    |       "text": 66,
    |       "next": [
    |         "51"
    |       ],
    |       "stack": true
    |     },
    |     "51": {
    |       "type": "InstructionStanza",
    |       "text": 77,
    |       "next": [
    |         "52"
    |       ],
    |       "stack": true
    |     },
    |     "52": {
    |       "type": "InstructionStanza",
    |       "text": 51,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "53": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/less-than-1000/you-need-to-tell-hmrc"
    |         }
    |       ],
    |       "next": [
    |         "54"
    |       ],
    |       "stack": false
    |     },
    |     "54": {
    |       "type": "CalloutStanza",
    |       "text": 59,
    |       "noteType": "Title",
    |       "next": [
    |         "55"
    |       ],
    |       "stack": false
    |     },
    |     "55": {
    |       "type": "InstructionStanza",
    |       "text": 60,
    |       "next": [
    |         "56"
    |       ],
    |       "stack": true
    |     },
    |     "56": {
    |       "type": "InstructionStanza",
    |       "text": 61,
    |       "next": [
    |         "57"
    |       ],
    |       "stack": true
    |     },
    |     "57": {
    |       "type": "InstructionStanza",
    |       "text": 62,
    |       "next": [
    |         "58"
    |       ],
    |       "stack": true
    |     },
    |     "58": {
    |       "type": "InstructionStanza",
    |       "text": 63,
    |       "next": [
    |         "59"
    |       ],
    |       "stack": true
    |     },
    |     "59": {
    |       "type": "InstructionStanza",
    |       "text": 51,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "60": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/less-than-1000/you-do-not-need-to-tell-hmrc"
    |         }
    |       ],
    |       "next": [
    |         "61"
    |       ],
    |       "stack": false
    |     },
    |     "61": {
    |       "type": "CalloutStanza",
    |       "text": 68,
    |       "noteType": "Title",
    |       "next": [
    |         "62"
    |       ],
    |       "stack": false
    |     },
    |     "62": {
    |       "type": "InstructionStanza",
    |       "text": 69,
    |       "next": [
    |         "63"
    |       ],
    |       "stack": true
    |     },
    |     "63": {
    |       "type": "InstructionStanza",
    |       "text": 70,
    |       "next": [
    |         "64"
    |       ],
    |       "stack": true
    |     },
    |     "64": {
    |       "type": "InstructionStanza",
    |       "text": 71,
    |       "next": [
    |         "65"
    |       ],
    |       "stack": true
    |     },
    |     "65": {
    |       "type": "InstructionStanza",
    |       "text": 72,
    |       "next": [
    |         "66"
    |       ],
    |       "stack": true
    |     },
    |     "66": {
    |       "type": "InstructionStanza",
    |       "text": 73,
    |       "next": [
    |         "67"
    |       ],
    |       "stack": true
    |     },
    |     "67": {
    |       "type": "InstructionStanza",
    |       "text": 62,
    |       "next": [
    |         "68"
    |       ],
    |       "stack": true
    |     },
    |     "68": {
    |       "type": "InstructionStanza",
    |       "text": 74,
    |       "next": [
    |         "69"
    |       ],
    |       "stack": true
    |     },
    |     "69": {
    |       "type": "InstructionStanza",
    |       "text": 51,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "70": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/do-you-receive-any-income"
    |         }
    |       ],
    |       "next": [
    |         "71"
    |       ],
    |       "stack": false
    |     },
    |     "71": {
    |       "type": "CalloutStanza",
    |       "text": 29,
    |       "noteType": "Title",
    |       "next": [
    |         "72"
    |       ],
    |       "stack": false
    |     },
    |     "72": {
    |       "type": "InstructionStanza",
    |       "text": 30,
    |       "next": [
    |         "73"
    |       ],
    |       "stack": true
    |     },
    |     "73": {
    |       "type": "InstructionStanza",
    |       "text": 31,
    |       "next": [
    |         "74"
    |       ],
    |       "stack": true
    |     },
    |     "74": {
    |       "type": "InstructionStanza",
    |       "text": 32,
    |       "next": [
    |         "75"
    |       ],
    |       "stack": true
    |     },
    |     "75": {
    |       "type": "InstructionStanza",
    |       "text": 33,
    |       "next": [
    |         "76"
    |       ],
    |       "stack": true
    |     },
    |     "76": {
    |       "type": "QuestionStanza",
    |       "text": 34,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "77",
    |         "121"
    |       ],
    |       "stack": false
    |     },
    |     "77": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/have-you-rented-out-a-room-in-your-home"
    |         }
    |       ],
    |       "next": [
    |         "78"
    |       ],
    |       "stack": false
    |     },
    |     "78": {
    |       "type": "QuestionStanza",
    |       "text": 36,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "120",
    |         "109"
    |       ],
    |       "stack": false
    |     },
    |     "79": {
    |       "type": "QuestionStanza",
    |       "text": 52,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "80",
    |         "109"
    |       ],
    |       "stack": false
    |     },
    |     "80": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/was-your-income-more-than-3750"
    |         }
    |       ],
    |       "next": [
    |         "81"
    |       ],
    |       "stack": false
    |     },
    |     "81": {
    |       "type": "CalloutStanza",
    |       "text": 53,
    |       "noteType": "Title",
    |       "next": [
    |         "82"
    |       ],
    |       "stack": false
    |     },
    |     "82": {
    |       "type": "QuestionStanza",
    |       "text": 25,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "83",
    |         "90"
    |       ],
    |       "stack": false
    |     },
    |     "83": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/you-need-to-tell-hmrc-rent-a-room"
    |         }
    |       ],
    |       "next": [
    |         "84"
    |       ],
    |       "stack": false
    |     },
    |     "84": {
    |       "type": "CalloutStanza",
    |       "text": 59,
    |       "noteType": "Title",
    |       "next": [
    |         "85"
    |       ],
    |       "stack": false
    |     },
    |     "85": {
    |       "type": "InstructionStanza",
    |       "text": 60,
    |       "next": [
    |         "86"
    |       ],
    |       "stack": true
    |     },
    |     "86": {
    |       "type": "InstructionStanza",
    |       "text": 61,
    |       "next": [
    |         "87"
    |       ],
    |       "stack": true
    |     },
    |     "87": {
    |       "type": "InstructionStanza",
    |       "text": 62,
    |       "next": [
    |         "88"
    |       ],
    |       "stack": true
    |     },
    |     "88": {
    |       "type": "InstructionStanza",
    |       "text": 63,
    |       "next": [
    |         "89"
    |       ],
    |       "stack": true
    |     },
    |     "89": {
    |       "type": "InstructionStanza",
    |       "text": 51,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "90": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/do-you-want-to-use-the-rent-a-room-scheme"
    |         }
    |       ],
    |       "next": [
    |         "91"
    |       ],
    |       "stack": false
    |     },
    |     "91": {
    |       "type": "CalloutStanza",
    |       "text": 64,
    |       "noteType": "Title",
    |       "next": [
    |         "92"
    |       ],
    |       "stack": false
    |     },
    |     "92": {
    |       "type": "InstructionStanza",
    |       "text": 54,
    |       "next": [
    |         "93"
    |       ],
    |       "stack": true
    |     },
    |     "93": {
    |       "type": "InstructionStanza",
    |       "text": 54,
    |       "next": [
    |         "94"
    |       ],
    |       "stack": true
    |     },
    |     "94": {
    |       "type": "InstructionStanza",
    |       "text": 56,
    |       "next": [
    |         "95"
    |       ],
    |       "stack": true
    |     },
    |     "95": {
    |       "type": "InstructionStanza",
    |       "text": 45,
    |       "next": [
    |         "96"
    |       ],
    |       "stack": true
    |     },
    |     "96": {
    |       "type": "QuestionStanza",
    |       "text": 57,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "97",
    |         "102"
    |       ],
    |       "stack": false
    |     },
    |     "97": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/you-do-not-need-to-tell-hmrc-rent-a-room"
    |         }
    |       ],
    |       "next": [
    |         "98"
    |       ],
    |       "stack": false
    |     },
    |     "98": {
    |       "type": "InstructionStanza",
    |       "text": 65,
    |       "next": [
    |         "99"
    |       ],
    |       "stack": true
    |     },
    |     "99": {
    |       "type": "InstructionStanza",
    |       "text": 66,
    |       "next": [
    |         "100"
    |       ],
    |       "stack": true
    |     },
    |     "100": {
    |       "type": "InstructionStanza",
    |       "text": 67,
    |       "next": [
    |         "101"
    |       ],
    |       "stack": true
    |     },
    |     "101": {
    |       "type": "InstructionStanza",
    |       "text": 51,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "102": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/you-need-to-tell-hmrc"
    |         }
    |       ],
    |       "next": [
    |         "103"
    |       ],
    |       "stack": false
    |     },
    |     "103": {
    |       "type": "CalloutStanza",
    |       "text": 59,
    |       "noteType": "Title",
    |       "next": [
    |         "104"
    |       ],
    |       "stack": false
    |     },
    |     "104": {
    |       "type": "InstructionStanza",
    |       "text": 60,
    |       "next": [
    |         "105"
    |       ],
    |       "stack": true
    |     },
    |     "105": {
    |       "type": "InstructionStanza",
    |       "text": 61,
    |       "next": [
    |         "106"
    |       ],
    |       "stack": true
    |     },
    |     "106": {
    |       "type": "InstructionStanza",
    |       "text": 62,
    |       "next": [
    |         "107"
    |       ],
    |       "stack": true
    |     },
    |     "107": {
    |       "type": "InstructionStanza",
    |       "text": 63,
    |       "next": [
    |         "108"
    |       ],
    |       "stack": true
    |     },
    |     "108": {
    |       "type": "InstructionStanza",
    |       "text": 51,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "109": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/was-your-income-more-than-7500"
    |         }
    |       ],
    |       "next": [
    |         "110"
    |       ],
    |       "stack": false
    |     },
    |     "110": {
    |       "type": "CalloutStanza",
    |       "text": 58,
    |       "noteType": "Title",
    |       "next": [
    |         "111"
    |       ],
    |       "stack": false
    |     },
    |     "111": {
    |       "type": "InstructionStanza",
    |       "text": 25,
    |       "next": [
    |         "112"
    |       ],
    |       "stack": true
    |     },
    |     "112": {
    |       "type": "QuestionStanza",
    |       "text": 26,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "113",
    |         "90"
    |       ],
    |       "stack": false
    |     },
    |     "113": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/you-need-to-tell-hmrc-rent-a-room/113"
    |         }
    |       ],
    |       "next": [
    |         "114"
    |       ],
    |       "stack": false
    |     },
    |     "114": {
    |       "type": "CalloutStanza",
    |       "text": 59,
    |       "noteType": "Title",
    |       "next": [
    |         "115"
    |       ],
    |       "stack": false
    |     },
    |     "115": {
    |       "type": "InstructionStanza",
    |       "text": 60,
    |       "next": [
    |         "116"
    |       ],
    |       "stack": true
    |     },
    |     "116": {
    |       "type": "InstructionStanza",
    |       "text": 61,
    |       "next": [
    |         "117"
    |       ],
    |       "stack": true
    |     },
    |     "117": {
    |       "type": "InstructionStanza",
    |       "text": 62,
    |       "next": [
    |         "118"
    |       ],
    |       "stack": true
    |     },
    |     "118": {
    |       "type": "InstructionStanza",
    |       "text": 63,
    |       "next": [
    |         "119"
    |       ],
    |       "stack": true
    |     },
    |     "119": {
    |       "type": "InstructionStanza",
    |       "text": 51,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "120": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/did-you-share-the-income"
    |         }
    |       ],
    |       "next": [
    |         "79"
    |       ],
    |       "stack": false
    |     },
    |     "121": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/have-you-rented-out-a-room-in-your-home-no-income"
    |         }
    |       ],
    |       "next": [
    |         "122"
    |       ],
    |       "stack": false
    |     },
    |     "122": {
    |       "type": "CalloutStanza",
    |       "text": 35,
    |       "noteType": "Error",
    |       "next": [
    |         "123"
    |       ],
    |       "stack": false
    |     },
    |     "123": {
    |       "type": "QuestionStanza",
    |       "text": 36,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "124",
    |         "138"
    |       ],
    |       "stack": false
    |     },
    |     "124": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/did-you-share-the-income-no-income"
    |         }
    |       ],
    |       "next": [
    |         "125"
    |       ],
    |       "stack": false
    |     },
    |     "125": {
    |       "type": "CalloutStanza",
    |       "text": 35,
    |       "noteType": "Error",
    |       "next": [
    |         "126"
    |       ],
    |       "stack": false
    |     },
    |     "126": {
    |       "type": "QuestionStanza",
    |       "text": 52,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "127",
    |         "159"
    |       ],
    |       "stack": false
    |     },
    |     "127": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/was-your-income-more-than-3750-no-income"
    |         }
    |       ],
    |       "next": [
    |         "128"
    |       ],
    |       "stack": false
    |     },
    |     "128": {
    |       "type": "CalloutStanza",
    |       "text": 53,
    |       "noteType": "Title",
    |       "next": [
    |         "129"
    |       ],
    |       "stack": false
    |     },
    |     "129": {
    |       "type": "CalloutStanza",
    |       "text": 35,
    |       "noteType": "Error",
    |       "next": [
    |         "130"
    |       ],
    |       "stack": false
    |     },
    |     "130": {
    |       "type": "QuestionStanza",
    |       "text": 25,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "113",
    |         "131"
    |       ],
    |       "stack": false
    |     },
    |     "131": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/do-you-want-to-use-the-rent-a-room-scheme-no-income"
    |         }
    |       ],
    |       "next": [
    |         "132"
    |       ],
    |       "stack": false
    |     },
    |     "132": {
    |       "type": "InstructionStanza",
    |       "text": 54,
    |       "next": [
    |         "133"
    |       ],
    |       "stack": true
    |     },
    |     "133": {
    |       "type": "InstructionStanza",
    |       "text": 55,
    |       "next": [
    |         "134"
    |       ],
    |       "stack": true
    |     },
    |     "134": {
    |       "type": "InstructionStanza",
    |       "text": 56,
    |       "next": [
    |         "135"
    |       ],
    |       "stack": true
    |     },
    |     "135": {
    |       "type": "InstructionStanza",
    |       "text": 45,
    |       "next": [
    |         "136"
    |       ],
    |       "stack": true
    |     },
    |     "136": {
    |       "type": "CalloutStanza",
    |       "text": 35,
    |       "noteType": "Error",
    |       "next": [
    |         "137"
    |       ],
    |       "stack": false
    |     },
    |     "137": {
    |       "type": "QuestionStanza",
    |       "text": 57,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "97",
    |         "102"
    |       ],
    |       "stack": false
    |     },
    |     "138": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/how-much-was-your-income"
    |         }
    |       ],
    |       "next": [
    |         "139"
    |       ],
    |       "stack": false
    |     },
    |     "139": {
    |       "type": "CalloutStanza",
    |       "text": 37,
    |       "noteType": "Title",
    |       "next": [
    |         "140"
    |       ],
    |       "stack": false
    |     },
    |     "140": {
    |       "type": "InstructionStanza",
    |       "text": 25,
    |       "next": [
    |         "141"
    |       ],
    |       "stack": true
    |     },
    |     "141": {
    |       "type": "CalloutStanza",
    |       "text": 35,
    |       "noteType": "Error",
    |       "next": [
    |         "142"
    |       ],
    |       "stack": false
    |     },
    |     "142": {
    |       "type": "QuestionStanza",
    |       "text": 26,
    |       "answers": [
    |         38,
    |         39
    |       ],
    |       "next": [
    |         "143",
    |         "158"
    |       ],
    |       "stack": false
    |     },
    |     "143": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/do-you-want-to-use-the-tax-free-allowance"
    |         }
    |       ],
    |       "next": [
    |         "144"
    |       ],
    |       "stack": false
    |     },
    |     "144": {
    |       "type": "CalloutStanza",
    |       "text": 41,
    |       "noteType": "Title",
    |       "next": [
    |         "145"
    |       ],
    |       "stack": false
    |     },
    |     "145": {
    |       "type": "InstructionStanza",
    |       "text": 42,
    |       "next": [
    |         "146"
    |       ],
    |       "stack": true
    |     },
    |     "146": {
    |       "type": "InstructionStanza",
    |       "text": 43,
    |       "next": [
    |         "147"
    |       ],
    |       "stack": true
    |     },
    |     "147": {
    |       "type": "InstructionStanza",
    |       "text": 44,
    |       "next": [
    |         "148"
    |       ],
    |       "stack": true
    |     },
    |     "148": {
    |       "type": "InstructionStanza",
    |       "text": 45,
    |       "next": [
    |         "149"
    |       ],
    |       "stack": true
    |     },
    |     "149": {
    |       "type": "CalloutStanza",
    |       "text": 35,
    |       "noteType": "Error",
    |       "next": [
    |         "150"
    |       ],
    |       "stack": false
    |     },
    |     "150": {
    |       "type": "QuestionStanza",
    |       "text": 46,
    |       "answers": [
    |         27,
    |         28
    |       ],
    |       "next": [
    |         "151",
    |         "157"
    |       ],
    |       "stack": false
    |     },
    |     "151": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageName",
    |           "value": "Telling HMRC about extra income"
    |         },
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/rent/1000-or-more/you-need-to-tell-hmrc-or-contact"
    |         }
    |       ],
    |       "next": [
    |         "152"
    |       ],
    |       "stack": false
    |     },
    |     "152": {
    |       "type": "CalloutStanza",
    |       "text": 47,
    |       "noteType": "Title",
    |       "next": [
    |         "153"
    |       ],
    |       "stack": false
    |     },
    |     "153": {
    |       "type": "InstructionStanza",
    |       "text": 48,
    |       "next": [
    |         "154"
    |       ],
    |       "stack": true
    |     },
    |     "154": {
    |       "type": "InstructionStanza",
    |       "text": 49,
    |       "next": [
    |         "155"
    |       ],
    |       "stack": true
    |     },
    |     "155": {
    |       "type": "InstructionStanza",
    |       "text": 50,
    |       "next": [
    |         "156"
    |       ],
    |       "stack": true
    |     },
    |     "156": {
    |       "type": "InstructionStanza",
    |       "text": 51,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "157": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/somepageorother157"
    |         }
    |       ],
    |       "next": [
    |         "157a"
    |       ],
    |       "stack": false
    |     },
    |     "157a": {
    |       "type": "InstructionStanza",
    |       "text": 40,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "158": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/somepageorother158"
    |         }
    |       ],
    |       "next": [
    |         "158a"
    |       ],
    |       "stack": false
    |     },
    |     "158a": {
    |       "type": "InstructionStanza",
    |       "text": 40,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "159": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/somepageorother"
    |         }
    |       ],
    |       "next": [
    |         "159a"
    |       ],
    |       "stack": false
    |     },
    |     "159a": {
    |       "type": "InstructionStanza",
    |       "text": 40,
    |       "next": [
    |         "end"
    |       ],
    |       "stack": true
    |     },
    |     "160": {
    |       "type": "CalloutStanza",
    |       "text": 0,
    |       "noteType": "Title",
    |       "next": [
    |         "1"
    |       ],
    |       "stack": false
    |     },
    |     "start": {
    |       "type": "ValueStanza",
    |       "values": [
    |         {
    |           "type": "scalar",
    |           "label": "PageUrl",
    |           "value": "/"
    |         }
    |       ],
    |       "next": [
    |         "160"
    |       ],
    |       "stack": false
    |     },
    |     "end": {
    |       "type": "EndStanza"
    |     }
    |   }
    """.stripMargin

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
    |     ["[bold:The trading allowance]","[bold:Welsh, The trading allowance]"],
    |     ["The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: selling items online or face to face","Welsh: The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: selling items online or face to face"],
    |     ["The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: selling freelance services (such as gardening or babysitting)","Welsh: The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: selling freelance services (such as gardening or babysitting)"],
    |     ["The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: hiring out personal equipment (such as power tools)","Welsh: The trading allowance lets you earn up to £1,000 from any trading, casual or miscellaneous income, tax free, in each tax year. For example: hiring out personal equipment (such as power tools)"],
    |     ["Check if you need to tell HMRC about income you've made by selling goods or services","Welsh: Check if you need to tell HMRC about income you've made by selling goods or services"],
    |     ["I've made extra income from renting land or property","Welsh: I've made extra income from renting land or property"],
    |     ["Property income can include any money you earn by renting land or buildings.","Welsh: Property income can include any money you earn by renting land or buildings."],
    |     ["[bold:The property allowance]","[bold:Welsh, The property allowance]"],
    |     ["The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting a flat or house","Welsh: The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting a flat or house"],
    |     ["The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting out a room in your home","Welsh: The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting out a room in your home"],
    |     ["The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: short term holiday lets","Welsh: The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: short term holiday lets"],
    |     ["The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting out a parking space or garage","Welsh: The property allowance lets you earn up to £1,000 in rental income, tax free, in each tax year. For example: renting out a parking space or garage"],
    |     ["Check if you need to tell HMRC about income you've made by renting land or property","Welsh: Check if you need to tell HMRC about income you've made by renting land or property"],
    |     ["Was your income from land or property less than £1,000?","Welsh: Was your income from land or property less than £1,000?"],
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

  val prototypeJson: JsObject = Json.parse(
    s"""{ "meta" : ${prototypeMetaSection},
         | "flow": ${prototypeFlowSection},
         | "phrases": ${prototypePhrasesSection},
         | "links": ${prototypeLinksSection},
         | "contacts": [],
         | "howto": []}""".stripMargin
  ).as[JsObject]

}
