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

import base.BaseSpec
import core.models._
import org.scalatest.Inspectors.forAll
import play.api.i18n.Lang
import java.time.LocalDate

class OcelotPackageSpec extends BaseSpec with TestTimescaleDefnsDB {
  implicit val labels: Labels = new LabelCacheImpl(Map(), Map(), Nil, Map(), Map(), timescaleMap, message(Lang("en")))

  "Label name validation" must {
    "Not allow invalid characters @ £ % etc in label names" in {
      labelNameValid("Blah@") shouldBe false
      labelNameValid("Blah£") shouldBe false
      labelNameValid("Blah%") shouldBe false
      labelNameValid("Blah^") shouldBe false
      labelNameValid("Blah*") shouldBe false
      labelNameValid("Blah!") shouldBe false
      labelNameValid("Blah?") shouldBe false
      labelNameValid("Blah/") shouldBe false
      labelNameValid("Blah.") shouldBe false
      labelNameValid("Blah,") shouldBe false
      labelNameValid("Blah;") shouldBe false
      labelNameValid("Blah:") shouldBe false
      labelNameValid("Blah}") shouldBe false
      labelNameValid("Blah{") shouldBe false
      labelNameValid("Blah]") shouldBe false
      labelNameValid("Blah[") shouldBe false
      labelNameValid("Blah=") shouldBe false
      labelNameValid("Blah+") shouldBe false
    }

    "Allow spaces, but not trailing spaces" in {
      labelNameValid("Bl  ah") shouldBe true
      labelNameValid("Blah ") shouldBe false
    }

    "Allow valid characters" in {
      labelNameValid("B- _ds038sd") shouldBe true
    }
  }

  "Numeric conversion" must {
    "recognise a valid +ve int" in {
      asNumeric("56789") shouldBe Some(BigDecimal(56789))
    }
    "recognise a valid -ve int" in {
      asNumeric("-56789") shouldBe Some(BigDecimal(-56789))
    }
    "recognise a valid +ve float" in {
      asNumeric("56789.32 462734628374602") shouldBe Some(BigDecimal("56789.32462734628374602"))
    }
    "recognise a valid -ve float" in {
      asNumeric("-56789.32462734 628 374602") shouldBe Some(BigDecimal("-56789.32462734628374602"))
    }
    "return None for non-numerics" in {
      asNumeric("dhjs") shouldBe None
      asNumeric("") shouldBe None
      asNumeric("£777") shouldBe None
    }
  }

  "Currency conversion" must {
    "recognise a valid +ve int currency" in {
      asCurrency("567 89") shouldBe Some(BigDecimal("56789"))
    }
    "recognise a valid -ve int currency" in {
      asCurrency("-56789") shouldBe Some(BigDecimal(-56789.00))
    }
    "recognise a valid +ve currency with pence" in {
      asCurrency("56 789.78") shouldBe Some(BigDecimal(56789.78))
    }
    "recognise a valid -ve currency with pence" in {
      asCurrency("-56789.67") shouldBe Some(BigDecimal(-56789.67))
    }
    "recognise a valid +ve int currency with £" in {
      asCurrency("£56789") shouldBe Some(BigDecimal(56789.00))
    }
    "recognise a valid -ve int currency with £" in {
      asCurrency("-£56789") shouldBe Some(BigDecimal(-56789.00))
    }
    "recognise a valid +ve currency with pence with £" in {
      asCurrency("£56789.78") shouldBe Some(BigDecimal(56789.78))
    }
    "recognise a valid -ve currency with pence with £" in {
      asCurrency("-£56789.67") shouldBe Some(BigDecimal(-56789.67))
    }
    "return None for non-currency" in {
      asCurrency("56789.67234234") shouldBe None
      asCurrency("dhjs") shouldBe None
      asCurrency("") shouldBe None
      asCurrency("$777") shouldBe None
    }
  }

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
      forAll(invalidDates) { invalidDate =>
        asDate(invalidDate) match {
          case Some(d) => fail(s"$invalidDate returned $d when expecting None")
          case _ => succeed
        }
      }
    }

    "recognise the 31st is not valid in months that don't have 31 days" in {
      val invalidDates: List[String] = List("31/4/2000", "31/9/2000", "31/6/2000", "31/11/2036")
      forAll(invalidDates) { invalidDate =>
        asDate(invalidDate) match {
          case Some(d) => fail(s"$invalidDate returned $d when expecting None")
          case _ => succeed
        }
      }
    }

    "recognise an invalid month" in {
      val invalidDate: String = "12/13/2020"
      asDate(invalidDate) match {
        case Some(_) => fail()
        case _ => succeed
      }
    }

    "recognise an invalid day" in {
      val invalidDate: String = "32/3/2000"
      asDate(invalidDate) match {
        case Some(_) => fail()
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

  "listLength" must {
    "return None if list does not exit" in {
      listLength("NonExistent", LabelCache()) shouldBe None
    }

    "return the number of elements in a list label" in {
      val labels = LabelCache().updateList("Blah", List("one", "two", "five"))

      listLength("Blah", labels) shouldBe Some("3")
    }
  }

  "listOp" must {

    "return the number of elements in a list label" in {
      val labels = LabelCache().updateList("Blah", List("one", "two", "five"))

      listOp("Blah", Some("length"), labels) shouldBe Some("3")
    }

    "return None if list does not exit" in {
      listOp("NonExistent", Some("1"), LabelCache()) shouldBe None
    }

    "return None if list element does not exit" in {
      val labels = LabelCache().updateList("Blah", List("one", "two", "five"))
      listOp("Blah", Some("5"), labels) shouldBe None
    }

    "return first element of a list" in {
      val labels = LabelCache().updateList("Blah", List("one", "two", "five"))
      listOp("Blah", Some("first"), labels) shouldBe Some("one")
    }

    "return last element of a list" in {
      val labels = LabelCache().updateList("Blah", List("one", "two", "five"))
      listOp("Blah", Some("last"), labels) shouldBe Some("five")
    }

    "return indexed element of a list label" in {
      val labels = LabelCache().updateList("Blah", List("one", "two", "five"))

      listOp("Blah", Some("2"), labels) shouldBe Some("two")
    }
  }

  "Positive Int conversion" must {
    "recognise a valid number" in {
      List("30", "3  56", "300", "030", Int.MaxValue.toString).foreach { item =>
        asPositiveInt(item) match {
          case Some(_) => succeed
          case _ => fail(s"Validation of $item failed - expected success")
        }
      }
    }

    "not recognise invalid numbers" in {
      val tooBig: Long = 1L + Int.MaxValue
      List("A number", "", Int.MinValue.toString, "-2", "-030", "1,234,456", tooBig.toString).foreach { item =>
        asPositiveInt(item) match {
          case Some(_) => fail(s"Validation of $item failed - expected failure")
          case _ => succeed
        }
      }
    }
  }

  "Signed Int conversion" must {
    "recognise a valid number" in {
      List("30", "3  56", "1,234", "1,234,456", "-300", "030", Int.MaxValue.toString, Int.MinValue.toString).foreach { item =>
        asAnyInt(item) match {
          case Some(_) => succeed
          case _ => fail(s"Validation of $item failed - expected success")
        }
      }
    }

    "not recognise invalid numbers" in {
      val tooBig: Long = 1L + Int.MaxValue
      val tooNegative: Long = -1L + Int.MinValue
      List("A number", "", "1,234456", tooBig.toString, tooNegative.toString).foreach { item =>
        asAnyInt(item) match {
          case Some(_) => fail(s"Validation of $item failed - expected failure")
          case _ => succeed
        }
      }
    }
  }

  "List of positive Int conversion" must {
    "recognise a list of valid positive Ints" in {
      asListOfPositiveInt(s"30, 030, ${Int.MaxValue.toString}, 7003") match {
        case Some(_) => succeed
        case _ => fail(s"Validation failed - expected success")
      }
    }

    "not recognise a list containing any invalid numbers" in {
      val tooBig: Long = 1L + Int.MaxValue
      val tooNegative: Long = -1L + Int.MinValue

      List("30, 3  56, A number, 067",
        "30, -3  56, 067",
        "30, 3  56, -45, 067",
        s"30, 3  56, ${tooBig.toString}, 067",
        s"30, 3  56, ${tooNegative.toString}, 067"
      ).foreach { item =>
        asListOfPositiveInt(item) match {
          case Some(_) => fail(s"Validation of $item failed - expected failure")
          case _ => succeed
        }
      }
    }
  }

  "List of Options conversion" must {
    "Return None if passed a empty list" in {
      lOfOtoOofL(Nil) shouldBe None
    }

    "Return Some(list) if all elements defined" in {
      lOfOtoOofL(List(Some(1), Some(2), Some(5), Some(8))) shouldBe Some(List(1, 2, 5, 8))
    }

    "Return None if not all elements defined" in {
      lOfOtoOofL(List(Some(1), Some(2), None, Some(8))) shouldBe None
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

  "operandValue function" must {
    "parse date_add placeholder with literal date" in {
      operandValue("[date_add:22/9/1973:NTCReAwardManAward]")(labels) shouldBe Some("6/10/1973")
    }

    "parse date_add placeholders with label date" in {
      val labelsWithMyDate = labels.update("MyDate", "22/9/1973")
      operandValue("[date_add:MyDate:NTCReAwardManAward]")(labelsWithMyDate) shouldBe Some("6/10/1973")
    }

    "parse date label reference" in {
      val labelsWithMyDate = labels.update("MyDate", "22/9/1973")
      operandValue("[label:MyDate]")(labelsWithMyDate) shouldBe Some("22/9/1973")
    }

    "parse list place holder using a numeric index" in {
      val labels = LabelCache().updateList("L", List("6", "7", "8"))
      operandValue("[list:L:2]")(labels) shouldBe Some("7")
    }

    "parse list place holder using a label index" in {
      val labels = LabelCache().updateList("L", List("6", "7", "8"))
                               .update("Idx", "3")
      operandValue("[list:L:[label:Idx]]")(labels) shouldBe Some("8")
    }    
  }

  "Timescale functions" must {
    "dateAdd with a valid date and timescale id" in {
      dateAdd(Some("22/9/1973"), "NTCReAwardManAward", labels) shouldBe Some("6/10/1973")
    }
  }

  "Exclusive sequence exclusive option matching" must {

    "not match options where the exclusive option place holder is absent" in {
      "Other" contains ExclusivePlaceholder
    }

    "match options where the place holder is present, but no hint is defined" in {

      val optionMatch = "Other [exclusive]" contains ExclusivePlaceholder
      optionMatch shouldBe true
    }

    "match option where both place holder and hint are defined" in {

      val optionText: String = "Other [exclusive][hint:Selection of this checkbox will cause the other checkboxes to be deselected]"

      optionText contains ExclusivePlaceholder

    }

    "match option where hint text contains colon and comma" in {

      val optionText: String = "Other [exclusive][hint: Welsh: One way, or another, the other checkboxes will be deselected]"

      val optionMatch: Boolean = optionText contains ExclusivePlaceholder

      optionMatch shouldBe true
    }

  }

  "linkPattern" must {

    "correctly match a link to a stanza id" in {
      val link = "[link:Next Page:23]"
      link.matches(linkPattern) shouldBe true
    }

    "correctly match a link to a uri" in {
      val link = "[link:Next Page:https://www.google.com]"
      link.matches(linkPattern) shouldBe true
    }

    "correctly match a link to javascript code" in {
      val link = "[link:Print Page:javascript:winddow.print()]"
      link.matches(linkPattern) shouldBe true
    }

    "correctly match a link to an email address" in {
      val link = "[link:an email address:mailto:test@example.com]"
      link.matches(linkPattern) shouldBe true
    }

    "not match a link with an invalid destination" in {
      val link = "[link:Next Page:someDestination]"
      link.matches(linkPattern) shouldBe false
    }

    "match all valid link characters" in {
      val link = "[link-tab:Customs Duty waiver form:https://public-online.hmrc.gov.uk/lc/content/xfaforms/profiles/forms.html?contentRoot=repository:///Applications/Customs_C/1.0/DEMAIDD&template=DeMAid.xdp]"
      link.matches(linkPattern) shouldBe true
    }
  }

  "datePlaceholder" must {
    val date: Option[String] = Some("12/12/2021")
    val badDate: Option[String] = Some("1/2/-bad-date")

    "Ignore values not in the format of a date placeholder" in {
      val result = datePlaceholder(badDate, "dow_name")

      result shouldBe None
    }
    "correctly convert a date place holder into a year" in {
      val result = datePlaceholder(date, "year")

      result shouldBe Some("2021")
    }
    "correctly convert a date place holder into a day name" in {
      val result = datePlaceholder(date, "dow_name")

      result shouldBe Some("Sunday")
    }
    "correctly convert a date place holder into a month number" in {
      val monthNumber = datePlaceholder(date, "month")

      monthNumber shouldBe Some("12")
    }
    "correctly convert a date place holder into a month start" in {
      val monthStart = datePlaceholder(date, "month_start")

      monthStart shouldBe Some("1/12/2021")
    }
    "correctly convert a date place holder into a month end" in {
      val monthEnd = datePlaceholder(date, "month_end")

      monthEnd shouldBe Some("31/12/2021")
    }
    "correctly convert a date place holder into a month name" in {
      val monthName = datePlaceholder(date, "month_name")

      monthName shouldBe Some("December")
    }
    "correctly convert a date place holder into a day of the week number" in {
      val dayOfTheWeekNumber = datePlaceholder(date, "dow")

      dayOfTheWeekNumber shouldBe Some("7")
    }
    "correctly convert a date place holder into a day of the month" in {
      val dayOfTheMonth = datePlaceholder(date, "day")

      dayOfTheMonth shouldBe Some("12")
    }
  }

  "operandValue date placholder function using date literal" must {
    "Ignore the value if not in the format of a date placeholder" in {
      operandValue("[date:1/2/-bad-date:dow_name]")(labels) shouldBe Some("[date:1/2/-bad-date:dow_name]")
    }
    "correctly convert a date place holder into a year" in {
      operandValue("[date:12/12/2021:year]")(labels) shouldBe Some("2021")
    }
    "correctly convert a date place holder into a day name" in {
      operandValue("[date:12/12/2021:dow_name]")(labels) shouldBe Some("Sunday")
    }
    "correctly convert a date place holder into a month number" in {
      operandValue("[date:12/12/2021:month]")(labels) shouldBe Some("12")
    }
    "correctly convert a date place holder into a month start" in {
      operandValue("[date:12/12/2021:month_start]")(labels) shouldBe Some("1/12/2021")
    }
    "correctly convert a date place holder into a month end" in {
      operandValue("[date:12/12/2021:month_end]")(labels) shouldBe Some("31/12/2021")
    }
    "correctly convert a date place holder into a month name" in {
      operandValue("[date:12/12/2021:month_name]")(labels) shouldBe Some("December")
    }
    "correctly convert a date place holder into a day of the week number" in {
      operandValue("[date:12/12/2021:dow]")(labels) shouldBe Some("7")
    }
    "correctly convert a date place holder into a day of the month" in {
      operandValue("[date:12/12/2021:day]")(labels) shouldBe Some("12")
    }
  }

  "operandValue date placholder function using date label" must {
    val labelsWithMyDate = labels.update("MyDate", "12/12/2021")
    "Ignore the value if not in the format of a date placeholder" in {
      operandValue("[date:[label:AnotherDate]:dow_name]")(labelsWithMyDate) shouldBe None
    }
    "correctly convert a date place holder into a year" in {
      operandValue("[date:[label:MyDate]:year]")(labelsWithMyDate) shouldBe Some("2021")
    }
    "correctly convert a date place holder into a day name" in {
      operandValue("[date:[label:MyDate]:dow_name]")(labelsWithMyDate) shouldBe Some("Sunday")
    }
    "correctly convert a date place holder into a month number" in {
      operandValue("[date:[label:MyDate]:month]")(labelsWithMyDate) shouldBe Some("12")
    }
    "correctly convert a date place holder into a month start" in {
      operandValue("[date:[label:MyDate]:month_start]")(labelsWithMyDate) shouldBe Some("1/12/2021")
    }
    "correctly convert a date place holder into a month end" in {
      operandValue("[date:[label:MyDate]:month_end]")(labelsWithMyDate) shouldBe Some("31/12/2021")
    }
    "correctly convert a date place holder into a month name" in {
      operandValue("[date:[label:MyDate]:month_name]")(labelsWithMyDate) shouldBe Some("December")
    }
    "correctly convert a date place holder into a day of the week number" in {
      operandValue("[date:[label:MyDate]:dow]")(labelsWithMyDate) shouldBe Some("7")
    }
    "correctly convert a date place holder into a day of the month" in {
      operandValue("[date:[label:MyDate]:day]")(labelsWithMyDate) shouldBe Some("12")
    }
  }

  "Input stanza name parsing" must {

    "Support [norepeat] option" in {
      val phrase = Phrase("Some title[norepeat]","Welsh: Some title[norepeat]")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe true
      width shouldBe "10"
    }

    "Support [norepeat] option with additional whitespace" in {
      val phrase = Phrase("Some title[norepeat]  ","Welsh: Some title[norepeat]  ")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe true
      width shouldBe "10"
    }

    "Set dontRepeatName false when no [norepeat] option" in {
      val phrase = Phrase("Some title","Welsh: Some title")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe false
      width shouldBe "10"
    }

    "Set dontRepeatName false when misplaced [norepeat] option" in {
      val phrase = Phrase("Some [norepeat]title","Welsh: Some [norepeat]title")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some [norepeat]title", "Welsh: Some [norepeat]title")
      dontRepeat shouldBe false
      width shouldBe "10"
    }


    "Support [width:<Int>] option" in {
      val phrase = Phrase("Some title[width:4]","Welsh: Some title[width:4]")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe false
      width shouldBe "4"
    }

    "Support [width:<Int>] option with additional whitespace" in {
      val phrase = Phrase("Some title[width:4]  ","Welsh: Some title[width:4]  ")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe false
      width shouldBe "4"
    }

    "Set width 10 when no [width:<Int>] option" in {
      val phrase = Phrase("Some title","Welsh: Some title")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe false
      width shouldBe "10"
    }

    "Set width 10 when misplaced [width:<Int>] option" in {
      val phrase = Phrase("Some [width:4]title","Welsh: Some [width:4]title")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some [width:4]title", "Welsh: Some [width:4]title")
      dontRepeat shouldBe false
      width shouldBe "10"
    }

    "Support [norepeat][width:<Int>] options together" in {
      val phrase = Phrase("Some title[norepeat][width:4]","Welsh: Some title[norepeat][width:4]")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe true
      width shouldBe "4"
    }

    "Support [norepeat][width:<Int>] option with additional whitespace" in {
      val phrase = Phrase("Some title[norepeat][width:4]  ","Welsh: Some title[norepeat][width:4]  ")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe true
      width shouldBe "4"
    }

    "Support [width:<Int>][norepeat] options together" in {
      val phrase = Phrase("Some title[width:4][norepeat]","Welsh: Some title[width:4][norepeat]")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe true
      width shouldBe "4"
    }

    "Support [width:<Int>][norepeat] option with additional whitespace" in {
      val phrase = Phrase("Some title[width:4][norepeat]  ","Welsh: Some title[width:4][norepeat]  ")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("Some title", "Welsh: Some title")
      dontRepeat shouldBe true
      width shouldBe "4"
    }

    "Support [width:<Int>] option with no leading text" in {
      val phrase = Phrase("[width:4]","Welsh: [width:4]")
      val (p, dontRepeat, width) = fieldAndInputOptions(phrase)
      p shouldBe Phrase("", "Welsh:")
      dontRepeat shouldBe false
      width shouldBe "4"
    }

  }

  "validDate" must {
    "Detect a valid date and convert to a LocalDate" in {
      validDate("4/5/1999") shouldBe Right(LocalDate.of(1999, 5, 4))
      validDate("28/2/1999") shouldBe Right(LocalDate.of(1999, 2, 28))
      validDate("29/2/2000") shouldBe Right(LocalDate.of(2000, 2, 29))
    }

    "Fail with a list invalid field ids when date fields are of the wrong type" in {
      validDate("4/blah/1999") shouldBe Left(List(1))
      validDate("4/blah/blah") shouldBe Left(List(1, 2))
    }

    "Fail with an empty list When all date fields are of the wrong type" in {
      validDate("blah/blah/blah") shouldBe Left(Nil)
    }

    "Fail when resulting date is invalid when day month or year components do not form a valid date" in {
      validDate("4/13/1999") shouldBe Left(List(1))
      validDate("31/2/1999") shouldBe Left(List(0))
      validDate("29/2/1999") shouldBe Left(List(0))
      validDate("32/5/1999") shouldBe Left(List(0))
      validDate("12/5/19999") shouldBe Left(List(2))
    }

  }


}
