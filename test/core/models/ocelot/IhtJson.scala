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

package core.models.ocelot

import play.api.libs.json.{JsValue, Json}

trait IhtJson {

  val ihtJsonShort: JsValue = Json.parse(
      """{
        "meta": {
          "id": "ext90010",
          "processCode": "iht-calc",
          "title": "IHT calculator",
          "ocelot": 3,
          "lastAuthor": "6031631",
          "lastUpdate": 1595504259391,
          "filename": "ext90010.js",
          "version": 1
        },
        "flow": {
          "1": {
            "type": "InstructionStanza",
            "text": 1,
            "next": [
              "2"
            ],
            "stack": true
          },
          "2": {
            "type": "InputStanza",
            "ipt_type": "Currency",
            "label": "Properties",
            "name": 16,
            "help": 17,
            "placeholder": 18,
            "next": [
              "3"
            ],
            "stack": false
          },
          "3": {
            "type": "InputStanza",
            "ipt_type": "Currency",
            "label": "Money",
            "name": 19,
            "help":20,
            "placeholder": 21,
            "next": [
              "4"
            ],
            "stack": false
          },
          "4": {
            "type": "InputStanza",
            "ipt_type": "Currency",
            "label": "Household",
            "name": 22,
            "help": 23,
            "placeholder": 24,
            "next": [
              "11"
            ],
            "stack": false
          },
          "6": {
            "type": "ValueStanza",
            "values": [
              {
                "type": "scalar",
                "label": "Properties",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "Money",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "Household",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "Motor Vehicles",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "Private pension",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "Trust",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "Foreign assets",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "Other assets",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "Mortgage_debt",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "funeral_expenses",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "other_debts",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "left to spouse",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "registered charity",
                "value": "0"
              },
              {
                "type": "scalar",
                "label": "nil rate band",
                "value": "0"
              }
            ],
            "next": [
              "1"
            ],
            "stack": false
          },
          "11": {
            "type": "InstructionStanza",
            "text": 2,
            "next": [
              "12"
            ],
            "stack": true
          },
          "12": {
            "type": "CalloutStanza",
            "text": 3,
            "noteType": "Title",
            "next": [
              "13"
            ],
            "stack": false
          },
          "13": {
            "type": "CalloutStanza",
            "text": 4,
            "noteType": "Title",
            "next": [
              "14"
            ],
            "stack": true
          },
          "14": {
            "type": "QuestionStanza",
            "text": 5,
            "answers": [
              6,
              7
            ],
            "next": [
              "150",
              "150"
            ],
            "stack": false,
            "label": "more than 100k"
          },
          "150": {
            "type": "PageStanza",
            "url": "blah",
            "next": ["15"],
            "stack": true
          },
          "15": {
            "type": "InstructionStanza",
            "text": 8,
            "next": [
              "19"
            ],
            "stack": true
          },
          "19": {
            "type": "InstructionStanza",
            "text": 9,
            "next": [
              "23"
            ],
            "stack": true
          },
          "23": {
            "type": "CalculationStanza",
            "calcs": [
              {
                "left": "[label:Properties]",
                "op": "add",
                "right": "[label:Money]",
                "label": "Value of Assets"
              },
              {
                "left": "[label:Value of Assets]",
                "op": "add",
                "right": "[label:Household]",
                "label": "Value of Assets"
              },
              {
                "left": "[label:Value of Assets]",
                "op": "add",
                "right": "[label:Motor Vehicles]",
                "label": "Value of Assets"
              },
              {
                "left": "[label:Value of Assets]",
                "op": "add",
                "right": "[label:Private pension]",
                "label": "Value of Assets"
              },
              {
                "left": "[label:Value of Assets]",
                "op": "add",
                "right": "[label:Trust]",
                "label": "Value of Assets"
              },
              {
                "left": "[label:Value of Assets]",
                "op": "add",
                "right": "[label:Foreign assets]",
                "label": "Value of Assets"
              },
              {
                "left": "[label:Value of Assets]",
                "op": "add",
                "right": "[label:Other assets]",
                "label": "Value of Assets"
              },
              {
                "left": "[label:Mortgage_debt]",
                "op": "add",
                "right": "[label:funeral_expenses]",
                "label": "Value of Debts"
              },
              {
                "left": "[label:Value of Debts]",
                "op": "add",
                "right": "[label:other_debts]",
                "label": "Value of Debts"
              },
              {
                "left": "[label:Value of Debts]",
                "op": "add",
                "right": "[label:other_debts]",
                "label": "Value of Debts"
              },
              {
                "left": "[label:left to spouse]",
                "op": "add",
                "right": "[label:registered charity]",
                "label": "Additional Info"
              },
              {
                "left": "[label:Additional Info]",
                "op": "add",
                "right": "[label:nil rate band]",
                "label": "Additional Info"
              },
              {
                "left": "[label:Value of Assets]",
                "op": "subtract",
                "right": "[label:Value of Debts]",
                "label": "IHT result"
              },
              {
                "left": "[label:IHT result]",
                "op": "subtract",
                "right": "[label:Additional Info]",
                "label": "IHT result"
              }
            ],
            "next": [
              "24"
            ],
            "stack": false
          },
          "24": {
            "type": "ChoiceStanza",
            "next": [
              "25",
              "26"
            ],
            "tests": [
              {
                "left": "[label:IHT result]",
                "test": "lessThanOrEquals",
                "right": "350000"
              }
            ],
            "stack": false
          },
          "25": {
            "type": "InstructionStanza",
            "text": 10,
            "next": [
              "27"
            ],
            "stack": true
          },
          "26": {
            "type": "InstructionStanza",
            "text": 15,
            "next": [
              "27"
            ],
            "stack": true
          },
          "27": {
            "type": "QuestionStanza",
            "text": 11,
            "answers": [
              12,
              13
            ],
            "next": [
              "280",
              "281"
            ],
            "stack": false
          },
          "280": {
            "type": "PageStanza",
            "url": "again",
            "next": ["28"],
            "stack": true
          },
          "28": {
            "type": "InstructionStanza",
            "text": 14,
            "next": [
              "end"
            ],
            "stack": true
          },
          "start": {
            "type": "PageStanza",
            "url": "start",
            "next": ["100"],
            "stack": true
          },
          "100": {
            "type": "CalloutStanza",
            "text": 0,
            "noteType": "Title",
            "next": [
              "6"
            ],
            "stack": false
          },
          "281": {
            "type": "PageStanza",
            "url": "theend",
            "next": ["end"],
            "stack": true
          },
          "end": {
            "type": "EndStanza"
          }
        },
        "phrases": [
          ["You can use exact amounts or very approximate amounts. This calculator will tell you how likely it is the estate will owe Inheritance Tax based on the amount you fill in.", "Welsh:You can use exact amounts or very approximate amounts. This calculator will tell you how likely it is the estate will owe Inheritance Tax based on the amount you fill in."],
          ["Value [bold:of Assets]", "Welsh:Value [bold:of Assets]"],
          ["Value [bold:of gifts]", "Welsh:Value [bold:of gifts]"],
          ["The 7 years are counted backwards from the date of death.", "Welsh:The 7 years are counted backwards from the date of death."],
          ["Gifts can be money, property, jewellery, vehicles, household items or anything else of value. Do not include gifts given to a spouse or civil partner who is resident in the UK.", "Welsh:Gifts can be money, property, jewellery, vehicles, household items or anything else of value. Do not include gifts given to a spouse or civil partner who is resident in the UK."],
          ["Does the value of gifts given within 7 years add up to more than £100,000?", "Welsh:Does the value of gifts given within 7 years add up to more than £100,000?"],
          ["Yes - the value of gits within 7 years is more than £100,000", "Welsh:Yes - the value of gits within 7 years is more than £100,000"],
          ["No - the value of gits within 7 years is not more than £100,000", "Welsh:No - the value of gits within 7 years is not more than £100,000"],
          ["Value [bold:of debts]", "Welsh:Value [bold:of debts]"],
          ["Additional [bold:information]", "Welsh:Additional [bold:information]"],
          ["Based on the amounts entered there will be no Inheritance tax to pay", "Welsh:Based on the amounts entered there will be no Inheritance tax to pay"],
          ["Do you want this form and result sent to your email address", "Welsh:Do you want this form and result sent to your email address"],
          ["yes - they would like it emailed", "Welsh:yes - they would like it emailed"],
          ["no - they would not like it emailed", "Welsh:no - they would not like it emailed"],
          ["I am guessing this is the email stanza that is used for Covid but never used it so just included an instruction for now with a note", "Welsh:I am guessing this is the email stanza that is used for Covid but never used it so just included an instruction for now with a note"],
          ["Based on the amounts entered there will be Inheritance tax to pay","Welsh:Based on the amounts entered there will be Inheritance tax to pay"],
          ["Properties, buildings and land", "Properties, buildings and land"],
          ["This means the value of the main home if it was owned, and any other property owned. Don’t worry about the mortgages, these are added later in the ‘Debts’ section.", "This means the value of the main home if it was owned, and any other property owned. Don’t worry about the mortgages, these are added later in the ‘Debts’ section."],
          ["£", "£"],
          ["Money", "Money"],
          ["The total of the balances in banks, building societies, National Savings, ISAs, premium bonds and safety deposit boxes.", "The total of the balances in banks, building societies, National Savings, ISAs, premium bonds and safety deposit boxes."],
          ["£", "£"],
          ["Household and personal items", "Household and personal items"],
          ["The selling price on the open market for furniture and jewellery etc.", "The selling price on the open market for furniture and jewellery etc."],
          ["£", "£"]
        ],
        "contacts": [],
        "howto": [],
        "links": []
      }""")
}