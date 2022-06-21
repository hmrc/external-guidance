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

package core.models.ocelot.stanzas

import base.BaseSpec
import core.models.ocelot._

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.math.BigDecimal.RoundingMode
import core.models.ocelot.errors.{UnsupportedOperationError, RuntimeError}

class OperationsSpec extends BaseSpec {
  val aNumber: BigDecimal = BigDecimal(32.6)
  val aDate: LocalDate = LocalDate.of(2018, 2, 20)
  val aString: String = "Some text"
  val aList: List[String] = List("One")
  val otherList: List[String] = List("One", "Two", "London")

  def result(res: Either[RuntimeError, Labels]): Labels = res match {
    case Right(labels) => labels
    case _ => LabelCache()
  }

  "Operand" must {
    "detect a Time period operand" in {
      Operand("31day", LabelCache()) shouldBe Some(TimePeriodOperand(TimePeriod(31, Day)))
    }
    "detect a date operand" in {
      Operand("1/12/1999", LabelCache()) shouldBe Some(DateOperand(LocalDate.of(1999, 12, 1)))
    }
    "detect a numeric operand" in {
      Operand("1999.7", LabelCache()) shouldBe Some(NumericOperand(BigDecimal(1999.7)))
    }
    "detect a string operand" in {
      Operand("Hello", LabelCache()) shouldBe Some(StringOperand("Hello"))
    }
    "detect a string collection operand" in {
      val labels: Labels = LabelCache().updateList("AList", List("Hello", "my", "name"))
      Operand("[label:AList]", labels) shouldBe Some(StringCollection(List("Hello", "my", "name")))
    }
    "not detect collection label from a literal" in {
      Operand.collection("hello", LabelCache()) shouldBe None
    }
  }

  "AddOperation" must {
    "correctly add a time period to a date" in {
      val labels = result(AddOperation(stringFromDate(aDate), "4day", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("24/2/2018")
    }
    "correctly sum two numbers" in {
      val labels = result(AddOperation("32.76", "65.2", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("97.96")
    }
    "correctly sum two strings" in {
      val labels = result(AddOperation("Hello", "Universe", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("HelloUniverse")
    }
    "fail to sum date and date" in {
      val labels = result(AddOperation(stringFromDate(aDate), stringFromDate(aDate), "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe None
    }
    "correctly sum date and string" in {
      val labels = result(AddOperation(stringFromDate(aDate), "-Universe", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("20/2/2018-Universe")
    }
    "correctly sum date and a numeric as a TimePeriod" in {
      val labels = result(AddOperation(stringFromDate(aDate), "3", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("23/2/2018")
    }
    "correctly sum string and date" in {
      val labels = result(AddOperation("Hello-", stringFromDate(aDate), "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("Hello-20/2/2018")
    }
    "correctly append an element to a list" in {
      val labels: Labels = LabelCache().updateList("AList", List("Hello", "my", "name"))

      val updatedLabels = result(AddOperation("[label:AList]", "is", "Answer").eval(labels))

      updatedLabels.valueAsList("Answer") shouldBe Some(List("Hello", "my", "name", "is"))
    }
    "correctly prepend an element to a list" in {
      val labels: Labels = LabelCache().updateList("AList", List("Hello", "my", "name"))

      val updatedLabels = result(AddOperation("is", "[label:AList]", "Answer").eval(labels))

      updatedLabels.valueAsList("Answer") shouldBe Some(List("is", "Hello", "my", "name"))
    }
  }

  "SubtractOperation" must {
    "correctly subtract a time period from a date" in {
      val labels = result(SubtractOperation(stringFromDate(aDate), "4day", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("16/2/2018")
    }
    "correctly subtract a numeric as a TimePeriod in days from a date" in {
      val labels = result(SubtractOperation(stringFromDate(aDate), "3", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("17/2/2018")
    }
    "correctly subtract two numbers" in {
      val labels = result(SubtractOperation("32", "65", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("-33")
    }
    "fail to subtract date and date" in {
      val labels = result(SubtractOperation(stringFromDate(aDate), stringFromDate(aDate), "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some(aDate.until(aDate, ChronoUnit.DAYS).toString)
    }
    "correctly remove an element from a list" in {
      val labels: Labels = LabelCache().updateList("AList", List("Hello", "my", "name"))

      val updatedLabels = result(SubtractOperation("[label:AList]", "my", "Answer").eval(labels))

      updatedLabels.valueAsList("Answer") shouldBe Some(List("Hello", "name"))
    }
  }
  "MultiplyOperation" must {
    "correctly multiply two numbers" in {
      val labels = result(MultiplyOperation("32", "6", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("192")
    }
  }
  "DivideOperation" must {
    "correctly divide two numbers" in {
      val labels = result(DivideOperation("32", "4", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("8")
    }
  }
  "CeilingOperation" must {
    "correctly ceil two numbers" in {
      val labels = result(CeilingOperation("32.33", "1", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("32.4")
    }
  }
  "FloorOperation" must {
    "correctly floor two numbers" in {
      val labels = result(FloorOperation("32.67", "1", "Answer").eval(LabelCache()))

      labels.value("Answer") shouldBe Some("32.6")
    }
  }

  "Addition Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      AddOperation("", "", "").evalNumericOp(aNumber, aNumber) shouldBe Right(s"${aNumber + aNumber}")
    }
    "Evaluate two dates correctly or return None" in {
      AddOperation("", "", "").evalDateOp(aDate, aDate) shouldBe Left(UnsupportedOperationError("AddOperation", "2018-02-20", "2018-02-20", "", ""))
    }
    "Evaluate two strings correctly or return None" in {
      AddOperation("", "", "").evalStringOp(aString, aString) shouldBe Right(aString + aString)
    }
    "Evaluate list and string correctly or return None" in {
      AddOperation("", "", "").evalCollectionScalarOp(aList, aString) shouldBe Right(aList :+ aString)
    }
    "Evaluate string and list correctly or return None" in {
      AddOperation("", "", "").evalScalarCollectionOp(aString, aList) shouldBe Right(aString :: aList)
    }
    "Evaluate list and list correctly or return None" in {
      AddOperation("", "", "").evalCollectionCollectionOp(otherList, aList) shouldBe Right(otherList ::: aList)
    }
  }

  "Subtraction Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      SubtractOperation("", "", "").evalNumericOp(aNumber, aNumber) shouldBe Right(s"${aNumber - aNumber}")
    }
    "Evaluate two dates correctly or return None" in {
      SubtractOperation("", "", "").evalDateOp(aDate, aDate) shouldBe Right(aDate.until(aDate, ChronoUnit.DAYS).toString)
    }
    "Evaluate two strings correctly or return None" in {
      SubtractOperation("", "", "").evalStringOp(aString, aString) shouldBe Left(UnsupportedOperationError("SubtractOperation", aString, aString, "", ""))
    }
    "Evaluate list and string correctly or return None" in {
      SubtractOperation("", "", "").evalCollectionScalarOp(aList, aString) shouldBe Right(aList.filterNot(_ == aString))
    }
    "Evaluate string and list correctly or return None" in {
      SubtractOperation("", "", "").evalScalarCollectionOp(aString, aList) shouldBe Left(UnsupportedOperationError("SubtractOperation", aString, aList.toString, "", ""))
    }
    "Evaluate list and list correctly or return None" in {
      SubtractOperation("", "", "").evalCollectionCollectionOp(otherList, aList) shouldBe Right(otherList.filterNot(aList.contains(_)))
    }
  }

  "Multiply Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      MultiplyOperation("", "", "").evalNumericOp(aNumber, aNumber) shouldBe Right(s"${aNumber * aNumber}")
    }
    "Evaluate two dates correctly or return None" in {
      MultiplyOperation("", "", "").evalDateOp(aDate, aDate) shouldBe Left(UnsupportedOperationError("MultiplyOperation", aDate.toString, aDate.toString, "",""))
    }
    "Evaluate two strings correctly or return None" in {
      MultiplyOperation("", "", "").evalStringOp(aString, aString) shouldBe Left(UnsupportedOperationError("MultiplyOperation", aString, aString, "", ""))
    }
    "Evaluate list and string correctly or return None" in {
      MultiplyOperation("", "", "").evalCollectionScalarOp(aList, aString) shouldBe Left(UnsupportedOperationError("MultiplyOperation", aList.toString, aString, "", ""))
    }
    "Evaluate string and list correctly or return None" in {
      MultiplyOperation("", "", "").evalScalarCollectionOp(aString, aList) shouldBe Left(UnsupportedOperationError("MultiplyOperation", aString, aList.toString, "", ""))
    }
    "Evaluate list and list correctly or return None" in {
      MultiplyOperation("", "", "").evalCollectionCollectionOp(otherList, aList) shouldBe Left(UnsupportedOperationError("MultiplyOperation", otherList.toString, aList.toString, "", ""))
    }
  }

  "Divide Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      DivideOperation("", "", "").evalNumericOp(aNumber, aNumber) shouldBe Right(s"${aNumber / aNumber}")
    }
    "Evaluate two dates correctly or return None" in {
      DivideOperation("", "", "").evalDateOp(aDate, aDate) shouldBe Left(UnsupportedOperationError("DivideOperation", aDate.toString, aDate.toString, "", ""))
    }
    "Evaluate two strings correctly or return None" in {
      DivideOperation("", "", "").evalStringOp(aString, aString) shouldBe Left(UnsupportedOperationError("DivideOperation", aString, aString, "", ""))
    }
    "Evaluate list and string correctly or return None" in {
      DivideOperation("", "", "").evalCollectionScalarOp(aList, aString) shouldBe Left(UnsupportedOperationError("DivideOperation", aList.toString, aString, "", ""))
    }
    "Evaluate string and list correctly or return None" in {
      DivideOperation("", "", "").evalScalarCollectionOp(aString, aList) shouldBe Left(UnsupportedOperationError("DivideOperation", aString, aList.toString, "", ""))
    }
    "Evaluate list and list correctly or return None" in {
      DivideOperation("", "", "").evalCollectionCollectionOp(otherList, aList) shouldBe Left(UnsupportedOperationError("DivideOperation", otherList.toString, aList.toString, "", ""))
    }
  }

  "Ceiling Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      CeilingOperation("", "", "").evalNumericOp(aNumber, aNumber) shouldBe Right(s"${aNumber.setScale(aNumber.toInt, RoundingMode.CEILING).bigDecimal.toPlainString}")
    }
    "Evaluate two dates correctly or return None" in {
      CeilingOperation("", "", "").evalDateOp(aDate, aDate) shouldBe Left(UnsupportedOperationError("CeilingOperation", aDate.toString, aDate.toString, "", ""))
    }
    "Evaluate two strings correctly or return None" in {
      CeilingOperation("", "", "").evalStringOp(aString, aString) shouldBe Left(UnsupportedOperationError("CeilingOperation", aString, aString, "", ""))
    }
    "Evaluate list and string correctly or return None" in {
      CeilingOperation("", "", "").evalCollectionScalarOp(aList, aString) shouldBe Left(UnsupportedOperationError("CeilingOperation", aList.toString, aString, "", ""))
    }
    "Evaluate string and list correctly or return None" in {
      CeilingOperation("", "", "").evalScalarCollectionOp(aString, aList) shouldBe Left(UnsupportedOperationError("CeilingOperation", aString, aList.toString, "", ""))
    }
    "Evaluate list and list correctly or return None" in {
      CeilingOperation("", "", "").evalCollectionCollectionOp(otherList, aList) shouldBe Left(UnsupportedOperationError("CeilingOperation", otherList.toString, aList.toString, "", ""))
    }
  }

  "Floor Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      FloorOperation("", "", "").evalNumericOp(aNumber, aNumber) shouldBe Right(s"${aNumber.setScale(aNumber.toInt, RoundingMode.FLOOR).bigDecimal.toPlainString}")
    }
    "Evaluate two dates correctly or return None" in {
      FloorOperation("", "", "").evalDateOp(aDate, aDate) shouldBe Left(UnsupportedOperationError("FloorOperation", aDate.toString, aDate.toString, "", ""))
    }
    "Evaluate two strings correctly or return None" in {
      FloorOperation("", "", "").evalStringOp(aString, aString) shouldBe Left(UnsupportedOperationError("FloorOperation", aString, aString, "", ""))
    }
    "Evaluate list and string correctly or return None" in {
      FloorOperation("", "", "").evalCollectionScalarOp(aList, aString) shouldBe Left(UnsupportedOperationError("FloorOperation", aList.toString, aString, "", ""))
    }
    "Evaluate string and list correctly or return None" in {
      FloorOperation("", "", "").evalScalarCollectionOp(aString, aList) shouldBe Left(UnsupportedOperationError("FloorOperation", aString, aList.toString, "", ""))
    }
    "Evaluate list and list correctly or return None" in {
      FloorOperation("", "", "").evalCollectionCollectionOp(otherList, aList) shouldBe Left(UnsupportedOperationError("FloorOperation", otherList.toString, aList.toString, "", ""))
    }
  }
}
