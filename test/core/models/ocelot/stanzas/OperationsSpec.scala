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

package core.models.ocelot.stanzas

import base.BaseSpec
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.math.BigDecimal.RoundingMode

import core.models.ocelot._

class OperationsSpec extends BaseSpec {
  val aNumber: BigDecimal = BigDecimal(32.6)
  val aDate: LocalDate = LocalDate.of(2018,2,1)
  val aString: String = "Some text"
  val aList: List[String] = List("One")
  val otherList: List[String] = List("One", "Two", "London")

  "Operand" must {
    "detect a date operand" in {
      Operand("1/12/1999", LabelCache()) shouldBe Some(DateOperand(LocalDate.of(1999,12,1)))
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
    "correctly sum two numbers" in {
      val labels = AddOperation("32.76", "65.2", "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some("97.96")
    }
    "correctly sum two strings" in {
      val labels = AddOperation("Hello", "Universe", "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some("HelloUniverse")
    }
    "fail to sum date and date" in {
      val labels = AddOperation(stringFromDate(aDate), stringFromDate(aDate), "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe None
    }
    "correctly sum date and string" in {
      val labels = AddOperation(stringFromDate(aDate), "-Universe", "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some("1/2/2018-Universe")
    }
    "correctly sum string and date" in {
      val labels = AddOperation("Hello-", stringFromDate(aDate), "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some("Hello-1/2/2018")
    }
    "correctly append an element to a list" in {
      val labels: Labels = LabelCache().updateList("AList", List("Hello", "my", "name"))

      val updatedLabels = AddOperation("[label:AList]", "is", "Answer").eval(labels)

      updatedLabels.valueAsList("Answer") shouldBe Some(List("Hello", "my", "name", "is"))
    }
    "correctly prepend an element to a list" in {
      val labels: Labels = LabelCache().updateList("AList", List("Hello", "my", "name"))

      val updatedLabels = AddOperation("is", "[label:AList]", "Answer").eval(labels)

      updatedLabels.valueAsList("Answer") shouldBe Some(List("is", "Hello", "my", "name"))
    }
  }

  "SubtractOperation" must {
    "correctly subtract two numbers" in {
      val labels = SubtractOperation("32", "65", "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some("-33")
    }
    "fail to subtract date and date" in {
      val labels = SubtractOperation(stringFromDate(aDate), stringFromDate(aDate), "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some(aDate.until(aDate, ChronoUnit.DAYS).toString)
    }
    "correctly remove an element from a list" in {
      val labels: Labels = LabelCache().updateList("AList", List("Hello", "my", "name"))

      val updatedLabels = SubtractOperation("[label:AList]", "my", "Answer").eval(labels)

      updatedLabels.valueAsList("Answer") shouldBe Some(List("Hello", "name"))
    }
  }
  "MultiplyOperation" must {
    "correctly multiply two numbers" in {
      val labels = MultiplyOperation("32", "6", "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some("192")
    }
  }
  "DivideOperation" must {
    "correctly divide two numbers" in {
      val labels = DivideOperation("32", "4", "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some("8")
    }
  }
  "CeilingOperation" must {
    "correctly ceil two numbers" in {
      val labels = CeilingOperation("32.33", "1", "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some("32.4")
    }
  }
  "FloorOperation" must {
    "correctly floor two numbers" in {
      val labels = FloorOperation("32.67", "1", "Answer").eval(LabelCache())

      labels.value("Answer") shouldBe Some("32.6")
    }
  }


  "Addition Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      AddOperation("","","").evalNumericOp(aNumber, aNumber) shouldBe Some(s"${aNumber + aNumber}")
    }
    "Evaluate two dates correctly or return None" in {
      AddOperation("","","").evalDateOp(aDate, aDate) shouldBe None
    }
    "Evaluate two strings correctly or return None" in {
      AddOperation("","","").evalStringOp(aString, aString) shouldBe Some(aString + aString)
    }
    "Evaluate list and string correctly or return None" in {
      AddOperation("","","").evalCollectionScalarOp(aList, aString) shouldBe Some(aList :+ aString)
    }
    "Evaluate string and list correctly or return None" in {
      AddOperation("","","").evalScalarCollectionOp(aString, aList) shouldBe Some(aString :: aList)
    }
    "Evaluate list and list correctly or return None" in {
      AddOperation("","","").evalCollectionCollectionOp(otherList, aList) shouldBe Some(otherList ::: aList)
    }
  }

  "Subtraction Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      SubtractOperation("","","").evalNumericOp(aNumber, aNumber) shouldBe Some(s"${aNumber - aNumber}")
    }
    "Evaluate two dates correctly or return None" in {
      SubtractOperation("","","").evalDateOp(aDate, aDate) shouldBe Some(aDate.until(aDate, ChronoUnit.DAYS).toString)
    }
    "Evaluate two strings correctly or return None" in {
      SubtractOperation("","","").evalStringOp(aString, aString) shouldBe None
    }
    "Evaluate list and string correctly or return None" in {
      SubtractOperation("","","").evalCollectionScalarOp(aList, aString) shouldBe Some(aList.filterNot(_ == aString))
    }
    "Evaluate string and list correctly or return None" in {
      SubtractOperation("","","").evalScalarCollectionOp(aString, aList) shouldBe None
    }
    "Evaluate list and list correctly or return None" in {
      SubtractOperation("","","").evalCollectionCollectionOp(otherList, aList) shouldBe Some(otherList.filterNot(aList.contains(_)))
    }
  }

  "Multiply Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      MultiplyOperation("","","").evalNumericOp(aNumber, aNumber) shouldBe Some(s"${aNumber * aNumber}")
    }
    "Evaluate two dates correctly or return None" in {
      MultiplyOperation("","","").evalDateOp(aDate, aDate) shouldBe None
    }
    "Evaluate two strings correctly or return None" in {
      MultiplyOperation("","","").evalStringOp(aString, aString) shouldBe None
    }
    "Evaluate list and string correctly or return None" in {
      MultiplyOperation("","","").evalCollectionScalarOp(aList, aString) shouldBe None
    }
    "Evaluate string and list correctly or return None" in {
      MultiplyOperation("","","").evalScalarCollectionOp(aString, aList) shouldBe None
    }
    "Evaluate list and list correctly or return None" in {
      MultiplyOperation("","","").evalCollectionCollectionOp(otherList, aList) shouldBe None
    }
  }

  "Divide Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      DivideOperation("","","").evalNumericOp(aNumber, aNumber) shouldBe Some(s"${aNumber / aNumber}")
    }
    "Evaluate two dates correctly or return None" in {
      DivideOperation("","","").evalDateOp(aDate, aDate) shouldBe None
    }
    "Evaluate two strings correctly or return None" in {
      DivideOperation("","","").evalStringOp(aString, aString) shouldBe None
    }
    "Evaluate list and string correctly or return None" in {
      DivideOperation("","","").evalCollectionScalarOp(aList, aString) shouldBe None
    }
    "Evaluate string and list correctly or return None" in {
      DivideOperation("","","").evalScalarCollectionOp(aString, aList) shouldBe None
    }
    "Evaluate list and list correctly or return None" in {
      DivideOperation("","","").evalCollectionCollectionOp(otherList, aList) shouldBe None
    }
  }

  "Ceiling Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      CeilingOperation("","","").evalNumericOp(aNumber, aNumber) shouldBe Some(s"${aNumber.setScale(aNumber.toInt, RoundingMode.CEILING).bigDecimal.toPlainString}")
    }
    "Evaluate two dates correctly or return None" in {
      CeilingOperation("","","").evalDateOp(aDate, aDate) shouldBe None
    }
    "Evaluate two strings correctly or return None" in {
      CeilingOperation("","","").evalStringOp(aString, aString) shouldBe None
    }
    "Evaluate list and string correctly or return None" in {
      CeilingOperation("","","").evalCollectionScalarOp(aList, aString) shouldBe None
    }
    "Evaluate string and list correctly or return None" in {
      CeilingOperation("","","").evalScalarCollectionOp(aString, aList) shouldBe None
    }
    "Evaluate list and list correctly or return None" in {
      CeilingOperation("","","").evalCollectionCollectionOp(otherList, aList) shouldBe None
    }
  }

  "Floor Operation evaluation overrides" must {
    "Evaluate two numbers correctly or return None" in {
      FloorOperation("","","").evalNumericOp(aNumber, aNumber) shouldBe Some(s"${aNumber.setScale(aNumber.toInt, RoundingMode.FLOOR).bigDecimal.toPlainString}")
    }
    "Evaluate two dates correctly or return None" in {
      FloorOperation("","","").evalDateOp(aDate, aDate) shouldBe None
    }
    "Evaluate two strings correctly or return None" in {
      FloorOperation("","","").evalStringOp(aString, aString) shouldBe None
    }
    "Evaluate list and string correctly or return None" in {
      FloorOperation("","","").evalCollectionScalarOp(aList, aString) shouldBe None
    }
    "Evaluate string and list correctly or return None" in {
      FloorOperation("","","").evalScalarCollectionOp(aString, aList) shouldBe None
    }
    "Evaluate list and list correctly or return None" in {
      FloorOperation("","","").evalCollectionCollectionOp(otherList, aList) shouldBe None
    }
  }
}
