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

import play.api.libs.json.{JsValue, Json}

trait SequenceJson {
  val seqJson: JsValue = Json.parse(
    """{
      "meta": {
        "links": [],
        "id": "ext90054",
        "title": "Sequence",
        "processCode": "seq",
        "ocelot": 5,
        "titlePhrase": 0,
        "lastAuthor": "7903085",
        "lastUpdate": 1613486711724,
        "version": 1,
        "filename": "ext90054.js"
      },
      "flow": {
        "1": {
          "type": "SequenceStanza",
          "text": 1,
          "next": [
            "4",
            "6",
            "8",
            "88",
            "2"
          ],
          "options": [
            2,
            3,
            4,
            5
          ],
          "stack": false,
          "label": "Choice"
        },
        "2": {
          "type": "PageStanza",
          "url": "/done",
          "next": [
            "3"
          ],
          "stack": false
        },
        "3": {
          "type": "InstructionStanza",
          "text": 9,
          "next": [
            "end"
          ],
          "stack": true
        },
        "4": {
          "type": "PageStanza",
          "url": "/one",
          "next": [
            "5"
          ],
          "stack": false
        },
        "5": {
          "type": "InstructionStanza",
          "text": 8,
          "next": [
            "end"
          ],
          "stack": true
        },
        "6": {
          "type": "PageStanza",
          "url": "/second",
          "next": [
            "7"
          ],
          "stack": false
        },
        "7": {
          "type": "InstructionStanza",
          "text": 7,
          "next": [
            "end"
          ],
          "stack": true
        },
        "8": {
          "type": "PageStanza",
          "url": "/third",
          "next": [
            "9"
          ],
          "stack": false
        },
        "88": {
          "type": "PageStanza",
          "url": "/fourth",
          "next": [
            "9"
          ],
          "stack": false
        },
        "9": {
          "type": "InstructionStanza",
          "text": 6,
          "next": [
            "end"
          ],
          "stack": true
        },
        "start": {
          "type": "PageStanza",
          "url": "/start",
          "next": [
            "1"
          ],
          "stack": false
        },
        "end": {
          "type": "EndStanza"
        }
      },
      "phrases": [
        ["Sequence", "Welsh: Sequence"],
        ["First Second and/or Third", "Welsh: First Second and/or Third"],
        ["First", "Welsh: First"],
        ["Second", "Welsh: Second"],
        ["Third", "Welsh: Third"],
        ["Fourth", "Welsh: Fourth"],
        ["The third", "Welsh: The third"],
        ["The second", "Welsh: The second"],
        ["The first one", "Welsh: The first one"],
        ["All done", "Welsh: All done"]
      ],
      "contacts": [],
      "howto": [],
      "links": []
    }""")

  val seqInputJson: JsValue = Json.parse(
    """
    {
      "meta": {
        "links": [],
        "id": "ext90054a",
        "title": "Sequence with in flow input",
        "processCode": "seq-inp",
        "ocelot": 5,
        "titlePhrase": 0,
        "lastAuthor": "7903085",
        "lastUpdate": 1613755336860,
        "version": 2,
        "filename": "ext90054a.js"
      },
      "flow": {
        "1": {
          "type": "SequenceStanza",
          "text": 1,
          "next": [
            "4",
            "6",
            "8",
            "88",
            "2"
          ],
          "options": [
            2,
            3,
            4,
            5
          ],
          "stack": false,
          "label": "Choice"
        },
        "2": {
          "type": "PageStanza",
          "url": "/done",
          "next": [
            "3"
          ],
          "stack": false
        },
        "3": {
          "type": "InstructionStanza",
          "text": 6,
          "next": [
            "end"
          ],
          "stack": true
        },
        "4": {
          "type": "PageStanza",
          "url": "/one",
          "next": [
            "5"
          ],
          "stack": false
        },
        "5": {
          "type": "InstructionStanza",
          "text": 7,
          "next": [
            "10"
          ],
          "stack": true
        },
        "6": {
          "type": "ValueStanza",
          "values": [
            {
              "type": "scalar",
              "label": "SecondSeqChoice",
              "value": "[label:Choice]"
            }
          ],
          "next": [
            "end"
          ],
          "stack": false
        },
        "7": {
          "type": "InstructionStanza",
          "text": 11,
          "next": [
            "end"
          ],
          "stack": true
        },
        "8": {
          "type": "PageStanza",
          "url": "/third",
          "next": [
            "9"
          ],
          "stack": false
        },
        "88": {
          "type": "PageStanza",
          "url": "/fourth",
          "next": [
            "9"
          ],
          "stack": false
        },
        "9": {
          "type": "InputStanza",
          "ipt_type": "Text",
          "name": 12,
          "help": 13,
          "label": "FlowInput",
          "next": [
            "end"
          ],
          "stack": false
        },
        "10": {
          "type": "QuestionStanza",
          "text": 8,
          "answers": [
            9,
            10
          ],
          "next": [
            "100",
            "100"
          ],
          "stack": false,
          "label": "YesNo"
        },
        "100": {
          "type": "ChoiceStanza",
          "tests": [
            {
              "left": "2",
              "test": "equals",
              "right": "3"
            }],
          "next": [
            "end",
            "end"
          ],
          "stack": true
        },
        "start": {
          "type": "PageStanza",
          "url": "/start",
          "next": [
            "1"
          ],
          "stack": false
        },
        "end": {
          "type": "EndStanza"
        }
      },
      "phrases": [
        ["Sequence", "Welsh: Sequence"],
        ["First Second and/or Third", "Welsh: First Second and/or Third"],
        ["First", "Welsh: First"],
        ["Second", "Welsh: Second"],
        ["Third", "Welsh: Third"],
        ["Fourth", "Welsh: Fourth"],
        ["All done YesNo = [label:YesNo], Choice_seq = [label:Choice_seq], SecondSeqChoice = [label:SecondSeqChoice], FlowInput = [label:FlowInput]", "Welsh: All done YesNo = [label:YesNo], Choice_seq = [label:Choice_seq], SecondSeqChoice = [label:SecondSeqChoice], FlowInput = [label:FlowInput]"],
        ["The first one", "Welsh: The first one"],
        ["Well which??", "Welsh: Well which??"],
        ["Yes", "Welsh: Yes"],
        ["No", "Welsh: No"],
        ["The second", "Welsh: The second"],
        ["A value related to loop label [label:Choice]", "Welsh: A value related to loop label [label:Choice]"],
        ["None", "Welsh: None"]
      ],
      "contacts": [],
      "howto": [],
      "links": []
    }
    """)

  val nestedSeqJson: JsValue = Json.parse(
    """
    {
      "meta": {
        "links": [],
        "id": "ext90058",
        "title": "Nested Sequence",
        "processCode": "seq",
        "ocelot": 5,
        "titlePhrase": 0,
        "lastAuthor": "7903085",
        "lastUpdate": 1613998348223,
        "version": 1,
        "filename": "ext90058.js"
      },
      "flow": {
        "1": {
          "type": "SequenceStanza",
          "text": 1,
          "next": [
            "4",
            "6",
            "8",
            "88",
            "2"
          ],
          "options": [
            2,
            3,
            4,
            5
          ],
          "stack": false,
          "label": "Choice"
        },
        "2": {
          "type": "PageStanza",
          "url": "/done",
          "next": [
            "3"
          ],
          "stack": false
        },
        "3": {
          "type": "InstructionStanza",
          "text": 6,
          "next": [
            "end"
          ],
          "stack": true
        },
        "4": {
          "type": "PageStanza",
          "url": "/one",
          "next": [
            "5"
          ],
          "stack": false
        },
        "5": {
          "type": "InstructionStanza",
          "text": 7,
          "next": [
            "10"
          ],
          "stack": true
        },
        "6": {
          "type": "ValueStanza",
          "values": [
            {
              "type": "scalar",
              "label": "SecondSeqChoice",
              "value": "[label:Choice]"
            }
          ],
          "next": [
            "end"
          ],
          "stack": false
        },
        "8": {
          "type": "PageStanza",
          "url": "/third",
          "next": [
            "9"
          ],
          "stack": false
        },
        "88": {
          "type": "PageStanza",
          "url": "/fourth",
          "next": [
            "9"
          ],
          "stack": false
        },
        "9": {
          "type": "SequenceStanza",
          "text": 0,
          "next": [
            "12",
            "16",
            "11"
          ],
          "options": [
            11,
            12
          ],
          "stack": false,
          "label": "Nested"
        },
        "10": {
          "type": "QuestionStanza",
          "text": 8,
          "answers": [
            9,
            10
          ],
          "next": [
            "end",
            "end"
          ],
          "stack": false,
          "label": "YesNo"
        },
        "11": {
          "type": "ValueStanza",
          "values": [
            {
              "type": "scalar",
              "label": "NestedSeqComplete",
              "value": "Yes"
            }
          ],
          "next": [
            "end"
          ],
          "stack": false
        },
        "12": {
          "type": "PageStanza",
          "url": "/nested-monday",
          "next": [
            "13"
          ],
          "stack": false
        },
        "13": {
          "type": "QuestionStanza",
          "text": 13,
          "answers": [
            9,
            10
          ],
          "next": [
            "14",
            "15"
          ],
          "stack": false
        },
        "14": {
          "type": "ValueStanza",
          "values": [
            {
              "type": "scalar",
              "label": "MondaySet",
              "value": "Yes"
            }
          ],
          "next": [
            "end"
          ],
          "stack": false
        },
        "15": {
          "type": "ValueStanza",
          "values": [
            {
              "type": "scalar",
              "label": "TuesdaySet",
              "value": "No"
            }
          ],
          "next": [
            "end"
          ],
          "stack": false
        },
        "16": {
          "type": "CalculationStanza",
          "calcs": [
            {
              "left": "[label:Choice]",
              "op": "add",
              "right": "[label:Nested]",
              "label": "SumLabel"
            }
          ],
          "next": [
            "end"
          ],
          "stack": false
        },
        "start": {
          "type": "PageStanza",
          "url": "/start",
          "next": [
            "1"
          ],
          "stack": false
        },
        "end": {
          "type": "EndStanza"
        }
      },
      "phrases": [
        ["Nested Sequence", "Welsh: Nested Sequence"],
        ["First Second and/or Third", "Welsh: First Second and/or Third"],
        ["First", "Welsh: First"],
        ["Second", "Welsh: Second"],
        ["Third", "Welsh: Third"],
        ["Fourth", "Welsh: Fourth"],
        ["All done YesNo = [label:YesNo], Choice_seq = [label:Choice_seq], SecondSeqChoice = [label:SecondSeqChoice], SumLabel= [label:SumLabel]", "Welsh: All done YesNo = [label:YesNo], Choice_seq = [label:Choice_seq], SecondSeqChoice = [label:SecondSeqChoice], SumLabel= [label:SumLabel]"],
        ["The first one", "Welsh: The first one"],
        ["Well which??", "Welsh: Well which??"],
        ["Yes", "Welsh: Yes"],
        ["No", "Welsh: No"],
        ["Monday", "Welsh: Monday"],
        ["Tuesday", "Welsh: Tuesday"],
        ["Down in nested sequence ??", "Welsh: Down in nested sequence ??"]
      ],
      "contacts": [],
      "howto": [],
      "links": []
    }"""
  )

val nestedSeqWithFlowInputJson: JsValue = Json.parse(
    """
    {
      "meta": {
        "links": [],
        "id": "ext90054a",
        "title": "Sequence with in flow input",
        "processCode": "seq-inp",
        "ocelot": 5,
        "titlePhrase": 0,
        "lastAuthor": "7903085",
        "lastUpdate": 1613755336860,
        "version": 2,
        "filename": "ext90054a.js"
      },
      "flow": {
        "1": {
          "type": "SequenceStanza",
          "text": 1,
          "next": [
            "4",
            "6",
            "8",
            "8",
            "2"
          ],
          "options": [
            2,
            3,
            4,
            5
          ],
          "stack": false,
          "label": "Choice"
        },
        "2": {
          "type": "PageStanza",
          "url": "/done",
          "next": [
            "3"
          ],
          "stack": false
        },
        "3": {
          "type": "InstructionStanza",
          "text": 6,
          "next": [
            "end"
          ],
          "stack": true
        },
        "4": {
          "type": "PageStanza",
          "url": "/one",
          "next": [
            "5"
          ],
          "stack": false
        },
        "5": {
          "type": "InstructionStanza",
          "text": 7,
          "next": [
            "10"
          ],
          "stack": true
        },
        "6": {
          "type": "ValueStanza",
          "values": [
            {
              "type": "scalar",
              "label": "SecondSeqChoice",
              "value": "Loop value = [label:Choice]"
            }
          ],
          "next": [
            "end"
          ],
          "stack": false
        },
        "7": {
          "type": "InstructionStanza",
          "text": 11,
          "next": [
            "end"
          ],
          "stack": true
        },
        "8": {
          "type": "PageStanza",
          "url": "/third",
          "next": [
            "9"
          ],
          "stack": false
        },
        "9": {
          "type": "InputStanza",
          "ipt_type": "Text",
          "name": 12,
          "help": 13,
          "label": "FlowInput",
          "next": [
            "end"
          ],
          "stack": false
        },
        "10": {
          "type": "QuestionStanza",
          "text": 8,
          "answers": [
            9,
            10
          ],
          "next": [
            "end",
            "end"
          ],
          "stack": false,
          "label": "YesNo"
        },
        "start": {
          "type": "PageStanza",
          "url": "/start",
          "next": [
            "1"
          ],
          "stack": false
        },
        "end": {
          "type": "EndStanza"
        }
      },
      "phrases": [
        ["Sequence", "Welsh: Sequence"],
        ["First Second and/or Third", "Welsh: First Second and/or Third"],
        ["First", "Welsh: First"],
        ["Second", "Welsh: Second"],
        ["Third", "Welsh: Third"],
        ["Fourth", "Welsh: Fourth"],
        ["All done YesNo = [label:YesNo], Choice_seq = [label:Choice_seq], SecondSeqChoice = [label:SecondSeqChoice], FlowInput = [label:FlowInput]", "Welsh: All done YesNo = [label:YesNo], Choice_seq = [label:Choice_seq], SecondSeqChoice = [label:SecondSeqChoice], FlowInput = [label:FlowInput]"],
        ["The first one", "Welsh: The first one"],
        ["Well which??", "Welsh: Well which??"],
        ["Yes", "Welsh: Yes"],
        ["No", "Welsh: No"],
        ["The second", "Welsh: The second"],
        ["A value related to loop label [label:Choice]", "Welsh: A value related to loop label [label:Choice]"],
        ["None", "Welsh: None"]
      ],
      "contacts": [],
      "howto": [],
      "links": []
    }
    """)
}
