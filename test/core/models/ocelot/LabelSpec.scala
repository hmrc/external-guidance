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
import play.api.libs.json._
import play.api.i18n.Lang

case class DummyLabel(name: String, english: List[String] = Nil, welsh: List[String] = Nil) extends Label

class LabelSpec extends BaseSpec with ProcessJson {

  trait Test {
    val englishLang: Lang = Lang("en")
    val welshLang: Lang = Lang("cy")
    val label = """{"name":"BLAH","english":[],"type":"scalar","welsh":[]}"""
    val labelWithSingleValue = """{"name":"BLAH","english":["39.99"],"type":"scalar","welsh":[]}"""
    val labelWithValues = """{"name":"BLAH","english":["Hello"],"type":"scalar","welsh":["Welsh: Hello"]}"""
    val listLabelWithEmptyLists = """{"name":"BLAH","english":[],"type":"list","welsh":[]}"""
    val listLabelWithSingleEntryLists = """{"name":"BLAH","english":["March"],"type":"list","welsh":["Mawrth"]}"""
    val listLabelWithSingleList = """{"name":"BLAH","english":["March,April,May,June"],"type":"list","welsh":[]}"""
    val oneEn: String = "One"
    val oneCy: String = s"Welsh: $oneEn"
    val twoEn: String = "Two"
    val twoCy: String = s"Welsh: $twoEn"
    val threeEn: String = "Three"
    val threeCy: String = s"Welsh: $threeEn"
    val phraseOne: Phrase = Phrase(oneEn, oneCy)
    val phraseTwo: Phrase = Phrase(twoEn, twoCy)
    val phraseThree: Phrase = Phrase(threeEn, threeCy)
    val oneTwo: List[Phrase] = List(phraseOne, phraseTwo)
    val oneTwoThree: List[Phrase] = oneTwo :+ phraseThree
  }

  "Label" must {
    "deserialise " in new Test {
      Json.parse(label).as[Label] shouldBe ScalarLabel("BLAH")
      Json.parse(labelWithSingleValue).as[Label] shouldBe ScalarLabel("BLAH", List("39.99"))
    }

    "serialise from Label to json" in new Test {
      val valueLabel: Label = ScalarLabel("BLAH")
      Json.toJson(valueLabel).toString shouldBe label
      val valueLabelWithValue: Label = ScalarLabel("BLAH", List("39.99"))
      Json.toJson(valueLabelWithValue).toString shouldBe labelWithSingleValue
    }

    "deserialise label with both english and welsh values" in new Test {
      Json.parse(labelWithValues).as[Label] shouldBe ScalarLabel("BLAH", List("Hello"), List("Welsh: Hello"))
    }

    "serialise from Label with both english and welsh values to json" in new Test {
      val dLabelWithValues: Label = ScalarLabel("BLAH", List("Hello"), List("Welsh: Hello"))
      Json.toJson(dLabelWithValues).toString shouldBe labelWithValues
    }

    "deserialize from list label with empty lists" in new Test {
      Json.parse(listLabelWithEmptyLists).as[Label] shouldBe ListLabel("BLAH", Nil, Nil)
    }

    "deserialize from list label with single entry lists" in new Test {
      val expectedLabel: ListLabel = ListLabel(
        "BLAH",
        List("March"),
        List("Mawrth")
      )

      Json.parse(listLabelWithSingleEntryLists).as[Label] shouldBe expectedLabel
    }

    "deserialize from list label with english list only" in new Test {
      val expectedLabel: ListLabel = ListLabel(
        "BLAH",
        List("March,April,May,June")
      )

      Json.parse(listLabelWithSingleList).as[Label] shouldBe expectedLabel
    }

    "serialize list label with empty entry lists" in new Test {

      val listLabel: Label = ListLabel("BLAH", Nil, Nil)

      Json.toJson(listLabel).toString shouldBe listLabelWithEmptyLists
    }

    "serialize list label with single entry lists" in new Test {

      val listLabel: Label = ListLabel(
        "BLAH",
        List("March"),
        List("Mawrth")
      )

      Json.toJson(listLabel).toString shouldBe listLabelWithSingleEntryLists
    }

    "serialize list label with single list" in new Test {

      val listLabel: Label = ListLabel(
        "BLAH",
        List("March,April,May,June")
      )

      Json.toJson(listLabel).toString shouldBe listLabelWithSingleList
    }

    "raise an error when an unknown label type is encountered" in new Test {

      val jsObject: JsObject = Json.parse("""{"type":"UnknownLabelType"}""").as[JsObject]

      jsObject.validate[Label] match {
        case JsSuccess(_,_) => fail("A label of unknown type should be deserialized")
        case JsError(_) => succeed
      }
    }

    "return blank string on serialization if label type is unknown" in new Test {
      val dummyLabel: Label = DummyLabel("dummy")

      Json.toJson(dummyLabel).toString() shouldBe "\"\""
    }

  }

  "LabelCache" must {
    "contruct single value scalar label" in new Test {
      val labels = LabelCache()
      val newLabels = labels.update("Name", "value")

      newLabels.updatedLabels("Name") shouldBe ScalarLabel("Name", List("value"))
    }

    "construct a single value list label" in new Test {
      val labels = LabelCache()
      val newLabels = labels.updateList("Name", List("value"))

      newLabels.updatedLabels("Name") shouldBe ListLabel("Name", List("value"))
    }

    "contruct muli-value scalar label" in new Test {
      val labels = LabelCache()
      val newLabels = labels.update("Name", "english", "welsh")

      newLabels.updatedLabels("Name") shouldBe ScalarLabel("Name", List("english"), List("welsh"))
    }

    "construct a multi-value list label" in new Test {
      val labels = LabelCache()
      val newLabels = labels.updateList("Name", List("english"), List("welsh"))

      newLabels.updatedLabels("Name") shouldBe ListLabel("Name", List("english"), List("welsh"))
    }

    "construct a single value list label from an empty english list" in new Test {
      val labels = LabelCache()
      val newLabels = labels.updateList("Name", Nil)

      newLabels.updatedLabels("Name") shouldBe ListLabel("Name", Nil)
    }

    "construct a multi-value list label from empty english and welsh lists" in new Test {
      val labels = LabelCache()
      val newLabels = labels.updateList("Name", Nil, Nil)

      newLabels.updatedLabels("Name") shouldBe ListLabel("Name", Nil, Nil)
    }

    "construct a single value list label from a multiple entry english list" in new Test {
      val labels = LabelCache()
      val newLabels = labels.updateList("Name", List("March", "April", "May"))

      newLabels.updatedLabels("Name") shouldBe ListLabel("Name", List("March" ,"April", "May"))
    }

    "construct a multi-value list label from a multiple entry english and welsh lists" in new Test {
      val labels = LabelCache()
      val newLabels = labels.updateList("Name", List("March", "April", "May"), List("Mawrth", "Ebrill", "Mai"))

      newLabels.updatedLabels("Name") shouldBe ListLabel("Name", List("March","April","May"), List("Mawrth","Ebrill","Mai"))
    }

    "update a single value scalar label" in new Test {
      val labels = LabelCache()
      val newLabels = labels.update("Name", "Hello")

      // Update labels
      val updatedLabels = newLabels.update("Name", "Goodbye")

      updatedLabels.updatedLabels("Name") shouldBe ScalarLabel("Name", List("Goodbye"))
    }

    "update a single value list label" in new Test {
      val labels = LabelCache()
      val newLabels = labels.updateList("Name", List("March","April"))

      // Update label
      val updatedLabels = newLabels.updateList("Name", List("May","June"))

      updatedLabels.updatedLabels("Name") shouldBe ListLabel("Name", List("May","June"))
    }

    "update a multi-value scalar label" in new Test {
      val labels = LabelCache()
      val newLabels = labels.update("Name", "Hello", "Helo")

      // Update label
      val updatedLabels = newLabels.update("Name", "Goodbye", "Hwyl fawr")

      updatedLabels.updatedLabels("Name") shouldBe ScalarLabel("Name", List("Goodbye"), List("Hwyl fawr"))
    }

    "update a multi-value list label" in new Test {
      val labels = LabelCache()
      val newLabels = labels.updateList("Name", List("March"), List("Mawrth"))

      // Update label
      val updatedLabels = newLabels.updateList("Name", List("April"), List("Ebrill"))

      updatedLabels.updatedLabels("Name") shouldBe ListLabel("Name", List("April"), List("Ebrill"))
    }

    "Return a blank string when a scalar label has no content" in new Test {
      val labelsMap = Map("empty" -> ScalarLabel("empty", Nil))

      val labels = LabelCache(labelsMap)

      labels.value("empty") shouldBe Some("")
    }

    "Allow reference to the current value of a scalar label" in new Test {
      val labelsMap = Map(
        "X" -> ScalarLabel("X", List("33.5")),
        "Y" -> ScalarLabel("Y", List("4")),
        "Name" -> ScalarLabel("Name", List("Coltrane"))
      )
      val labels = LabelCache(labelsMap)

      labels.value("X") shouldBe Some("33.5")
      labels.value("Y") shouldBe Some("4")
      labels.value("Name") shouldBe Some("Coltrane")
    }

    "return Nil when list label content is not defined" in new Test {
      val labelsMap = Map("emptyList" -> ListLabel("emptyList", Nil))

      val labels = LabelCache(labelsMap)

      labels.valueAsList("emptyList") shouldBe Some(Nil)
    }

    "Allow reference to the current value of a list label" in new Test {
      val labelsMap = Map(
        "a" -> ListLabel("a", List("x", "y", "z")),
        "b" -> ListLabel("b", (List("f", "g", "h"))),
        "c" -> ScalarLabel("c", List("k"))
      )

      val labels = LabelCache(labelsMap)

      labels.valueAsList("a") shouldBe Some(List("x", "y", "z"))
      labels.valueAsList("b") shouldBe Some(List("f", "g", "h"))
    }

    "Return a language specific value of a multi-lingual scalar label" in new Test {
      val labelsMap = Map(
        "Empty"-> ScalarLabel("Empty"),
        "EnglishOnly" -> ScalarLabel("EnglishOnly", List("Welcome")),
        "Door"-> ScalarLabel("Door", List("Open"), List("Drws")),
        "Name" -> ScalarLabel("Name",List("Coltrane"), List("Coltrane")))

      val labels = LabelCache(labelsMap)

      labels.displayValue("Empty")(englishLang) shouldBe Some("")
      labels.displayValue("Empty")(welshLang) shouldBe Some("")
      labels.displayValue("EnglishOnly")(englishLang) shouldBe Some("Welcome")
      labels.displayValue("EnglishOnly")(welshLang) shouldBe Some("Welcome")
      labels.displayValue("Door")(englishLang) shouldBe Some("Open")
      labels.displayValue("Door")(welshLang) shouldBe Some("Drws")
      labels.displayValue("Name")(englishLang) shouldBe Some("Coltrane")
      labels.displayValue("Name")(welshLang) shouldBe Some("Coltrane")
    }

    "Return a language specific value of a multi-value list label" in new Test {
      val labelsMap = Map(
        "one" -> ListLabel("one", Nil, Nil),
        "two" -> ListLabel("two", List("Home"), List("Hafan")),
        "three" -> ListLabel("three", List("Hello", "World"), List("Helo","Byd")),
        "four" -> ListLabel("four", List("Welcome"))
      )

      val labels = LabelCache(labelsMap)

      labels.displayValue("one")(englishLang) shouldBe Some("")
      labels.displayValue("one")(welshLang) shouldBe Some("")
      labels.displayValue("two")(englishLang) shouldBe Some("Home")
      labels.displayValue("two")(welshLang) shouldBe Some("Hafan")
      labels.displayValue("three")(englishLang) shouldBe Some("Hello,World")
      labels.displayValue("three")(welshLang) shouldBe Some("Helo,Byd")
      labels.displayValue("four")(englishLang) shouldBe Some("Welcome")
      labels.displayValue("four")(welshLang) shouldBe Some("Welcome")
    }

    "Allow access to the main label map" in new Test {
      val labelsMap = Map(
        "Door"-> ScalarLabel("Door", List("Open"), List("Drws")),
        "Name" -> ScalarLabel("Name", List("Coltrane")),
        "Colours" -> ListLabel("Colours", List("Red", "Green", "Yellow"))
      )
      val labels = LabelCache(labelsMap)

      labels.labelMap shouldBe labelsMap
    }

    "Allow addition/update of labels with an english and welsh translation" in new Test {
      val labels = LabelCache(
        Map(
          "Door"-> ScalarLabel("Door", List("Open"), List("Drws")),
          "Name" -> ScalarLabel("Name", List("Coltrane"))
        )
      )

      val updatedLabels0 = labels.update("Door", "Ajar", "Dysgu")
      updatedLabels0.displayValue("Door")(englishLang) shouldBe Some("Ajar")
      updatedLabels0.displayValue("Door")(welshLang) shouldBe Some("Dysgu")

      val updatedLabels1 = updatedLabels0.update("Door", "Open", "Drws")
      updatedLabels1.displayValue("Door")(englishLang) shouldBe Some("Open")
      updatedLabels1.displayValue("Door")(welshLang) shouldBe Some("Drws")
    }

    "Construct a LabelCache from a label map a cache of updated labels and a Flow stack" in new Test {
      val labelsMap = Map(
        "X"->ScalarLabel("X", List("33.5")),
        "Y"->ScalarLabel("Y"),
        "Name" -> ScalarLabel("Name", List("Coltrane")),
        "Colours" -> ListLabel("Colours", List("Red", "Green", "Blue"))
      )
      val cacheMap = Map(
        "X"->ScalarLabel("X", List("46.5")),
        "Colours"->ListLabel("Colours", List("Yellow", "Violet"))
      )

      val labels = LabelCache(labelsMap, cacheMap)

      labels.value("X") shouldBe Some("46.5")
      labels.valueAsList("Colours") shouldBe Some(List("Yellow", "Violet"))
      val (next, labels0) = labels.pushFlows(List("1", "2"), "3", Some("loop"), oneTwo, Map())

      next shouldBe Some("1")
      labels0.value("loop") shouldBe Some(oneEn)
      labels0.nextFlow.fold(fail("Stack should not be empty")){t =>
        val (nxt, updatedLabels) = t
        nxt shouldBe "2"
        updatedLabels.value("loop") shouldBe Some(twoEn)
        updatedLabels.displayValue("loop")(englishLang) shouldBe Some(twoEn)
        updatedLabels.displayValue("loop")(welshLang) shouldBe Some(twoCy)

      }
    }

    "Return an empty string if label has no assigned value" in new Test {
      val labelsMap = Map(
        "X"->ScalarLabel("X", List("33.5")),
        "Y"->ScalarLabel("Y"),
        "Name" -> ScalarLabel("Name", List("Coltrane")))
      val labels = LabelCache(labelsMap)
      labels.value("Y") shouldBe Some("")
    }

    "Return None if referenced scalar label does not exist" in new Test {
      val labelsMap = Map(
        "X"->ScalarLabel("X", List("33.5")),
        "Y"->ScalarLabel("Y"),
        "Name" -> ScalarLabel("Name", List("Coltrane")))
      val labels = LabelCache(labelsMap)
      labels.value("Z") shouldBe None
    }

    "Return None if referenced list label does not exist" in new Test {
      val labelMap = Map(
        "a" -> ListLabel("a", List("March"), List("Mawrth")),
        "b" -> ListLabel("b", List("May"), List("Mai"))
      )

      val labels = LabelCache(labelMap)

      labels.valueAsList("c") shouldBe None
    }

    "Return None if the accessor method value is used to access a list label" in new Test {
      val labelMap = Map("list"->ListLabel("list", List("March"), List("Mawrth")))

      val labels = LabelCache(labelMap)

      labels.value("list") shouldBe None
    }

    "Return None if the accessor method valueAsList is used to access a scalar label" in new Test {
      val labelMap = Map("scalar"->ScalarLabel("scalar"))

      val labels = LabelCache(labelMap)

      labels.valueAsList("scalar") shouldBe None
    }

    "Allow the current value of the label to be updated" in new Test {
      val labelsMap = Map(
        "X"->ScalarLabel("X", List("33.5")),
        "Y"->ScalarLabel("Y", List("4")),
        "Name" -> ScalarLabel("Name", List("Coltrane")))

      val labels = LabelCache(labelsMap)
      labels.value("X") shouldBe Some("33.5")

      val updatedLabels = labels.update("Name", "Miles")
      updatedLabels.value("Name") shouldBe Some("Miles")
    }

    "Allow new labels to be added to the cache" in new Test {
      val labelsMap = Map(
        "X"->ScalarLabel("X", List("33.5")),
        "Y"->ScalarLabel("Y", List("4")),
        "Name" ->ScalarLabel("Name", List("Coltrane")),
        "Days" -> ListLabel("Days", List("Tuesday", "Wednesday"))
      )

      val labels = LabelCache(labelsMap)
      labels.value("X") shouldBe Some("33.5")

      val updatedLabels = labels.update("Location", "Here").updateList("Months", List("February", "June"))

      updatedLabels.value("Location") shouldBe Some("Here")
      updatedLabels.valueAsList("Months") shouldBe Some(List("February", "June"))
    }

    "Return a map of new and updated labels on request" in new Test {
      val labelsMap = Map(
        "X"->ScalarLabel("X", List("33.5")),
        "Y"->ScalarLabel("Y", List("4")),
        "Name" ->ScalarLabel("Name", List("Coltrane")))

      val labels = LabelCache(labelsMap)
      labels.value("X") shouldBe Some("33.5")

      val labels1 = labels.update("X", "49.5")
      val labels2 = labels1.update("Location", "Here")
      val labels3 = labels2.updateList("Days", List("Monday","Wednesday"))
      labels3.updatedLabels shouldBe Map(
        "X"->ScalarLabel("X",List("49.5")),
        "Location"->ScalarLabel("Location",List("Here")),
        "Days"->ListLabel("Days", List("Monday", "Wednesday")))
    }

    "Flush updated labels to main store" in new Test {
      val labelsMap = Map(
        "X"->ScalarLabel("X", List("33.5")),
        "Y"->ScalarLabel("Y", List("4")),
        "Name" ->ScalarLabel("Name", List("Coltrane")))

      val labels = LabelCache(labelsMap)

      val labels1 = labels.update("X", "49.5")
      val labels2 = labels1.update("Location", "Here")
      labels2.updatedLabels shouldBe Map(
        "X" ->ScalarLabel("X",List("49.5")),
        "Location" ->ScalarLabel("Location",List("Here"))
      )

      val labels3 = labels2.flush
      labels3.updatedLabels shouldBe Map()
      labels3.value("X") shouldBe Some("49.5")
      labels3.value("Location") shouldBe Some("Here")
    }
  }

  "Labels Flow stack" must {
    "Allow adding a Flow to top of stack" in new Test {
      val (nxt, labels) = LabelCache().pushFlows(List("1","2"), "3", Some("loop"), oneTwoThree, Map())

      nxt shouldBe Some("1")

      labels.activeFlow shouldBe Some(Flow("1", Some(LabelValue("loop", phraseOne))))
      labels.flowStack.length shouldBe 3
      labels.flowStack(1) shouldBe Flow("2", Some(LabelValue("loop", phraseTwo)))
      labels.flowStack(2) shouldBe Continuation("3")
    }

    "Allow removal of Flow from top of stack" in new Test {
      val (nxt, labels) = LabelCache().pushFlows(List("1","2"), "3", Some("loop"), oneTwoThree, Map())

      nxt shouldBe Some("1")
      labels.activeFlow shouldBe Some(Flow("1", Some(LabelValue("loop", phraseOne))))
      labels.flowStack.length shouldBe 3
      labels.nextFlow.map{t =>
        val(n1, l1) = t
        n1 shouldBe "2"
        l1.activeFlow shouldBe Some(Flow("2", Some(LabelValue("loop", phraseTwo))))
        l1.flowStack.length shouldBe 2
        l1.nextFlow.map{t =>
          val(n2, l2) = t
          n2 shouldBe "3"
          l2.flowStack.length shouldBe 0
          l2.activeFlow shouldBe None

          l2.nextFlow shouldBe None
        }
      }
    }

    "Allow removal of Flow from top of stack when no label is in use" in new Test {
      val (nxt, labels) = LabelCache().pushFlows(List("1","2"), "3", None, oneTwoThree, Map())

      labels.activeFlow shouldBe Some(Flow("1", None))
      nxt shouldBe Some("1")
      labels.flowStack.length shouldBe 3
      labels.nextFlow.map{t =>
        val(n1, l1) = t
        n1 shouldBe "2"
        l1.flowStack.length shouldBe 2
        l1.activeFlow shouldBe Some(Flow("2", None))
        l1.nextFlow.map{t =>
          val(n2, l2) = t
          n2 shouldBe "3"
          l2.flowStack.length shouldBe 0
          l2.activeFlow shouldBe None
          l2.nextFlow shouldBe None
        }
      }
    }

    "Return None when the stack is empty" in new Test {
      LabelCache().nextFlow shouldBe None
      LabelCache().activeFlow shouldBe None
    }

  }
}
