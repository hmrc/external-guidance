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

package core.models.ocelot.stanzas

import base.BaseSpec
import play.api.libs.json._

import core.models.ocelot.Process

class RowStanzaSpec extends BaseSpec {

  def getRowStanzaWithMultipleCellsAsJsValue(): JsValue = Json.parse(
    s"""|{
        | "type": "RowStanza",
        | "cells": [10, 11, 12, 13, 14, 15],
        | "next": [ "10" ],
        | "stack": false
        |}""".stripMargin
  )

  val validRowStanzaWithMultipleCells: JsObject = getRowStanzaWithMultipleCellsAsJsValue().as[JsObject]

  trait Test {

    def getRowStanzaWithZeroCellsAsJsValue(): JsValue = Json.parse(
      s"""|{
          | "type": "RowStanza",
          | "cells": [],
          | "next": [ "10" ],
          | "stack": false
          |}""".stripMargin
    )

    def getRowStanzaWithSingleCellAsJsValue(): JsValue = Json.parse(
      s"""|{
          | "type": "RowStanza",
          | "cells": [1],
          | "next": [ "10" ],
          | "stack": true
          |}""".stripMargin
    )

    def getRowStanzaWithZeroEntryNextSequenceAsJsValue(): JsValue = Json.parse(
      s"""|{
          | "type": "RowStanza",
          | "cells": [10, 11, 12, 13, 14, 15],
          | "next": [],
          | "stack": false
          |}""".stripMargin
    )

    val validRowStanzaWithZeroCells: JsObject = getRowStanzaWithZeroCellsAsJsValue().as[JsObject]
    val validRowStanzaWithSingleCell: JsObject = getRowStanzaWithSingleCellAsJsValue().as[JsObject]

    val invalidRowStanzaWithZeroEntryNextSequence: JsObject = getRowStanzaWithZeroEntryNextSequenceAsJsValue().as[JsObject]

    val nextSequence: Seq[String] = Seq("10")

    val singleCellSequence: Seq[Int] = Seq(1)
    val multipleCellSequence: Seq[Int] = Seq(ten, eleven, twelve, thirteen, fourteen, fifteen)

    val expectedRowStanzaWithZeroCells: RowStanza = RowStanza(Nil, nextSequence, stack = false)
    val expectedRowStanzaWithSingleCell: RowStanza = RowStanza(singleCellSequence, nextSequence, stack = true)
    val expectedRowStanzaWithMultipleCells: RowStanza = RowStanza(multipleCellSequence, nextSequence, stack = false)

    val onePageJsonWithValidRowStanza: JsValue = Json.parse(
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
        |      "next": ["2"],
        |      "stack": true
        |    },
        |    "3": {
        |      "next": [ "end" ],
        |      "stack": false,
        |      "cells": [9, 10],
        |      "type": "RowStanza"
        |    },
        |    "2": {
        |      "type": "InstructionStanza",
        |      "text": 0,
        |      "next": [
        |        "3"
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
        |    ["Customer wants to make a cup of tea", "Welsh: Customer wants to make a cup of tea"],
        |    ["Cell A Text", "Welsh: Cell A Text"],
        |    ["Cell B Text", "Welsh: Cell B Text"]
        |  ]
        |}
    """.stripMargin
    )
  }

  "Reading a valid row stanza" should {

    "manage a row stanza with zero cells" in new Test {

      validRowStanzaWithZeroCells.validate[RowStanza] match {
        case JsSuccess(rowStanza, _) => rowStanza shouldBe expectedRowStanzaWithZeroCells
        case e: JsError => fail( "Unable to parse row stanza with zero cells")
      }
    }

    "manage a row stanza with a single cell" in new Test {
      validRowStanzaWithSingleCell.validate[RowStanza] match {
        case JsSuccess(rowStanza, _) => rowStanza shouldBe expectedRowStanzaWithSingleCell
        case e: JsError => fail( "Unable to parse row stanza with single cell")
      }
    }

    "manage a row stanza with multiple cells" in new Test {
      validRowStanzaWithMultipleCells.validate[RowStanza] match {
        case JsSuccess(rowStanza, _) => rowStanza shouldBe expectedRowStanzaWithMultipleCells
        case e: JsError => fail("Unable to parse rowe stanza with multiple cells")
      }
    }

  }

  "Reading an invalid row stanza" should {

    "raise an error if the next array is empty" in new Test {

      invalidRowStanzaWithZeroEntryNextSequence.validate[RowStanza] match {
        case JsError(errTuple :: _) => errTuple match {
          case (_, err +: _) if err.messages.head == "error.minLength" => succeed
          case _ => fail("Unexpected error tuple raised parsing invalid row stanza")
        }
        case JsError(_) => fail("Unexpected error raised parsing invalid row stanza")
        case JsSuccess(rowStanza, _) => fail( "A row stanza with zero next entries should be invalid")
      }

    }

    /** Test for missing properties in Json object representing row stanza */
    missingJsObjectAttrTests[RowStanza](validRowStanzaWithMultipleCells, List("type"))

    /** Test for properties of the wrong type in json object representing row stanza */
    incorrectPropertyTypeJsObjectAttrTests[RowStanza](validRowStanzaWithMultipleCells, List("type"))
  }

  "Writing instances of RowStanza to JSON" should {

    "write a zero cell row stanza" in new Test {

      val expectedRowStanzaWithZeroCellsAsString = getRowStanzaWithZeroCellsAsJsValue().toString()

      val stanza: Stanza = expectedRowStanzaWithZeroCells
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedRowStanzaWithZeroCellsAsString
    }

    "write a single cell row stanza" in new Test {

      val expectedRowStanzaWithSingleCellAsString = getRowStanzaWithSingleCellAsJsValue().toString()

      val stanza: Stanza = expectedRowStanzaWithSingleCell
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedRowStanzaWithSingleCellAsString
    }

    "write a multiple cell row stanza" in new Test {

      val expectedRowStanzaWithMultipleCellsAsString = getRowStanzaWithMultipleCellsAsJsValue().toString()

      val stanza: Stanza = expectedRowStanzaWithMultipleCells
      val actualResult: String = Json.toJson(stanza).toString()

      actualResult shouldBe expectedRowStanzaWithMultipleCellsAsString
    }

  }

  "Page building" should {

    "be able to parse a process containing the definition of a row stanza" in new Test {

      onePageJsonWithValidRowStanza.as[JsObject].validate[Process] match {
        case JsSuccess(process, _) => {
          process.flow.get("3") match {
            case Some(stanza) => stanza match {
              case r: RowStanza => r shouldBe RowStanza( Seq(nine, ten), Seq( "end"), stack = false)
              case _ => fail( "Stanza with identifier of row stanza is not a row stanza")
            }
            case _ => fail( "Stanza with identifier of row stanza not found in process")
          }
        }
        case e: JsError => fail( s"Parsing process with row stanza raised error $e")
      }

    }
  }

}
