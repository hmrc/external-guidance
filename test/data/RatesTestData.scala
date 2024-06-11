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

package data

import base.UserCredentials
import models.{LabelledData, Rates, LabelledDataUpdateStatus}
import core.models.RequestOutcome
import play.api.libs.json.{JsObject, Json}
import java.time.{Instant, ZonedDateTime, ZoneId}
import models.UpdateDetails

trait RatesTestData extends UserCredentials {
  val jsonWithNoRatesTable: JsObject = Json.parse(
  """{
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
    |  ]
    |}
  """.stripMargin
  ).as[JsObject]

  val jsonWithBlankRatesTable: JsObject = Json.parse(
  """{
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
    |  "rates" : {
    |      "Legacy!higherrate!2016" : 0,
    |      "Legacy!basicrate!2022" : 0,
    |      "TaxNic!CTC!2016" : 0
    |  }
    |}
  """.stripMargin
  ).as[JsObject]

  val jsonWithUpdatedRatesTable: JsObject = Json.parse(
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
    |  "rates" : {
    |      "Legacy!higherrate!2016" : 0.4,
    |      "Legacy!basicrate!2022" : 0.2,
    |      "TaxNic!CTC!2016" : 0
    |  }
    |}
  """.stripMargin
  ).as[JsObject]
  
    val lastUpdateTime: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, ZoneId.of("UTC"))
    val lastUpdateInstant: Instant = lastUpdateTime.toInstant()
    val credId: String = credential
    val user: String = name

    val rates: Map[String, BigDecimal] = Map("Legacy!higherrate!2016" -> 0.4, "Legacy!basicrate!2022" -> 0.2, "TaxNic!CTC!2016" -> 0)
    val ratesJsonString =
      """
     |{
     |  "Legacy": {
     |      "higherrate":
     |      {
     |          "2016": 0.4,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 45
     |      },
     |      "basicrate":
     |      {
     |          "2016": 45,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 0.2
     |      }
     |  },
     |  "TaxNic": {
     |      "BasicRate":
     |      {
     |          "2016": 20,
     |          "2017": 20,
     |          "2018": 20,
     |          "2019": 20,
     |          "2020": 20,
     |          "2021": 20,
     |          "2022": 20
     |      },
     |      "CTC":
     |      {
     |          "2016": 0,
     |          "2017": 0,
     |          "2018": 0,
     |          "2019": 0,
     |          "2020": 0,
     |          "2021": 0,
     |          "2022": 0.567
     |      }
     |  }
     |}
    """.stripMargin
    val ratesJsonWithDeletionString =
      """
     |{
     |  "Legacy": {
     |      "higherrate":
     |      {
     |          "2016": 0.4,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 45
     |      },
     |      "basicrate":
     |      {
     |          "2016": 45,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 0.2
     |      }
     |  },
     |  "TaxNic": {
     |      "BasicRate":
     |      {
     |          "2019": 20,
     |          "2020": 20,
     |          "2021": 20,
     |          "2022": 20
     |      },
     |      "CTC":
     |      {
     |          "2017": 0,
     |          "2018": 0,
     |          "2019": 0,
     |          "2020": 0,
     |          "2021": 0,
     |          "2022": 0.567
     |      }
     |  }
     |}
    """.stripMargin
    val ratesJsonWithDeletionAndRetainedString =
      """
     |{
     |  "Legacy": {
     |      "higherrate":
     |      {
     |          "2016": 0.4,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 45
     |      },
     |      "basicrate":
     |      {
     |          "2016": 45,
     |          "2017": 45,
     |          "2018": 45,
     |          "2019": 45,
     |          "2020": 45,
     |          "2021": 45,
     |          "2022": 0.2
     |      }
     |  },
     |  "TaxNic": {
     |      "BasicRate":
     |      {
     |          "2019": 20,
     |          "2020": 20,
     |          "2021": 20,
     |          "2022": 20
     |      },
     |      "CTC":
     |      {
     |          "2016": 0,
     |          "2017": 0,
     |          "2018": 0,
     |          "2019": 0,
     |          "2020": 0,
     |          "2021": 0,
     |          "2022": 0.567
     |      }
     |  }
     |}
    """.stripMargin

    val ratesFourDimMap: Map[String, Map[String, Map[String, BigDecimal]]] =
      Map(
        ("Legacy" -> Map(("higherrate" -> Map(
                            ("2016" -> 0.4),
                            ("2017" -> 45),
                            ("2018" -> 45),
                            ("2019" -> 45),
                            ("2020" -> 45),
                            ("2021" -> 45),
                            ("2022" -> 45)
                          )
                         ),
                         ("basicrate" -> Map(
                            ("2016" -> 45),
                            ("2017" -> 45),
                            ("2018" -> 45),
                            ("2019" -> 45),
                            ("2020" -> 45),
                            ("2021" -> 45),
                            ("2022" -> 0.2)
                          )
                         )
                        )
      ),
      ("TaxNic" ->  Map(("BasicRate" -> Map(
                                  ("2016" -> 20),
                                  ("2017" -> 20),
                                  ("2018" -> 20),
                                  ("2019" -> 20),
                                  ("2020" -> 20),
                                  ("2021" -> 20),
                                  ("2022" -> 20)
                                )
                              ),
                              ("CTC" -> Map(
                                   ("2016" -> 0),
                                   ("2017" -> 0),
                                   ("2018" -> 0),
                                   ("2019" -> 0),
                                   ("2020" -> 0),
                                   ("2021" -> 0),
                                   ("2022" -> 0.567)
                                )
                              )
                            )
        )
      )

    val ratesTwoDimMap: Map[String, BigDecimal] = Map(
       ("Legacy!higherrate!2016" -> 0.4),
       ("Legacy!higherrate!2017" -> 45),
       ("Legacy!higherrate!2018" -> 45),
       ("Legacy!higherrate!2019" -> 45),
       ("Legacy!higherrate!2020" -> 45),
       ("Legacy!higherrate!2021" -> 45),
       ("Legacy!higherrate!2022" -> 45),
       ("Legacy!basicrate!2016" -> 45),
       ("Legacy!basicrate!2017" -> 45),
       ("Legacy!basicrate!2018" -> 45),
       ("Legacy!basicrate!2019" -> 45),
       ("Legacy!basicrate!2020" -> 45),
       ("Legacy!basicrate!2021" -> 45),
       ("Legacy!basicrate!2022" -> 0.2),
       ("TaxNic!BasicRate!2016" -> 20),
       ("TaxNic!BasicRate!2017" -> 20),
       ("TaxNic!BasicRate!2018" -> 20),
       ("TaxNic!BasicRate!2019" -> 20),
       ("TaxNic!BasicRate!2020" -> 20),
       ("TaxNic!BasicRate!2021" -> 20),
       ("TaxNic!BasicRate!2022" -> 20),
       ("TaxNic!CTC!2016" -> 0),
       ("TaxNic!CTC!2017" -> 0),
       ("TaxNic!CTC!2018" -> 0),
       ("TaxNic!CTC!2019" -> 0),
       ("TaxNic!CTC!2020" -> 0),
       ("TaxNic!CTC!2021" -> 0),
       ("TaxNic!CTC!2022" -> 0.567)
    )

    val ratesJson = Json.parse(ratesJsonString)
    val labelledData = LabelledData(Rates, ratesJson, lastUpdateTime.toInstant(), credId, user, email)
    val expected: RequestOutcome[LabelledData] = Right(labelledData)
    val labelledDataStatus = LabelledDataUpdateStatus(ratesTwoDimMap.size, Some(UpdateDetails(lastUpdateTime, credId, user, email)))

    val ratesJsonWithDeletion = Json.parse(ratesJsonWithDeletionString)
    val labelledDataWithDeletion = LabelledData(Rates, ratesJsonWithDeletion, lastUpdateTime.toInstant(), credId, user, email)
    val expectedWithDeletion: RequestOutcome[LabelledData] = Right(labelledDataWithDeletion)

    val ratesJsonWithDeletionAndRetained = Json.parse(ratesJsonWithDeletionAndRetainedString)
    val labelledDataWithDeletionAndRetained = LabelledData(Rates, ratesJsonWithDeletionAndRetained, lastUpdateTime.toInstant(), credId, user, email)
    val expectedWithDeletionAndRetained: RequestOutcome[LabelledData] = Right(labelledDataWithDeletionAndRetained)
    val expectedStatusWithDeletionAndRetained = LabelledDataUpdateStatus(1, Some(UpdateDetails(lastUpdateTime, credId, user, email, Nil)))
}
