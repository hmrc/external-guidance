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
import org.scalatest.Inspectors.forAll

class OcelotPackageSpec extends BaseSpec {

  "Date conversion" must {
    "recognise a valid date" in {
      val validDates: List[String] = List("30/04/2000", "3/12/2000", "13/4/2000", "31/3/2130")
      forAll(validDates) { validDate =>
        asDate(validDate) match {
          case Some(_) => succeed
          case _ => fail(s"Validation of $validDate failed - expected success")
        }
      }
    }

    "recognise valid dates in february" in {
      val validDates: List[String] = List("28/02/2019", "28/2/2020")
      forAll(validDates) { validDate =>
        asDate(validDate) match {
          case Some(_) => succeed
          case _ => fail(s"Validation of $validDate failed - expected success")
        }
      }
    }

    "recognise 29th Feb is valid in a leap year" in {
      val validDates: List[String] = List("29/02/2020", "29/2/2000")
      forAll(validDates) { validDate =>
        asDate(validDate) match {
          case Some(_) => succeed
          case _ => fail(s"Validation of $validDate failed - expected success")
        }
      }
    }

    "recognise 29th Feb is not valid in a non leap year" in {
      val invalidDates: List[String] = List("29/02/2021", "29/2/2100")
      forAll (invalidDates) { invalidDate =>
        asDate(invalidDate) match {
          case Some(d) => fail(s"$invalidDate returned $d when expecting None")
          case _ => succeed
        }
      }
    }

    "recognise the 31st is not valid in months that don't have 31 days" in {
      val invalidDates: List[String] = List("31/4/2000", "31/9/2000", "31/6/2000", "31/11/2036")
      forAll (invalidDates) { invalidDate =>
        asDate(invalidDate) match {
          case Some(d) => fail(s"$invalidDate returned $d when expecting None")
          case _ => succeed
        }
      }
    }

    "recognise an invalid month" in {
      val invalidDate: String = "12/13/2020"
      asDate(invalidDate) match {
        case Some(_) => fail
        case _ => succeed
      }
    }

    "recognise an invalid day" in {
      val invalidDate: String = "32/3/2000"
      asDate(invalidDate) match {
        case Some(_) => fail
        case _ => succeed
      }
    }
  }

  "Text conversion" must {
    "recognise a valid text" in {
      val validTexts: List[String] = List("Hello", "World", "030")
      forAll(validTexts) { entry =>
        asTextString(entry) match {
          case Some(_) => succeed
          case _ => fail(s"Validation of $entry failed - expected success")
        }
      }
    }
    "recognise a invalid text" in {
      asTextString("") match {
        case Some(_) => fail(s"Validation of empty string failed - expected success")
        case _ => succeed
      }
    }

    "Recognise link when text contains other placeholder" in {
      pageLinkIds("[link:Change[hint:Change the value]:45] some text [link:Change[hint:Change the value]:99]") shouldBe List("45", "99")
    }
  }

  "Int conversion" must {
    "recognise a valid number" in {
      val validNumbers: List[String] = List("30", "300", "030")
      forAll(validNumbers) { entry =>
        asInt(entry) match {
          case Some(_) => succeed
          case _ => fail(s"Validation of $entry failed - expected success")
        }
      }
    }
  }

  "Phrase interrogation" must {
    "recognise link only" in {
      isLinkOnlyPhrase(Phrase(Vector("[link:Blah:4]", "[link:Blah:4]"))) shouldBe true

      isLinkOnlyPhrase(Phrase(Vector("Blah", "Blah"))) shouldBe false
    }

    "Recognise bold only" in {
      isBoldOnlyPhrase(Phrase(Vector("[bold:Blah]", "[bold:Blah]"))) shouldBe true

      isBoldOnlyPhrase(Phrase(Vector("Blah", "Blah"))) shouldBe false
    }
  }
}
