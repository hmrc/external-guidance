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

package models.ocelot

import base.BaseSpec
import play.api.libs.json._
import play.api.i18n.Lang

class LabelSpec extends BaseSpec with ProcessJson {

  val englishLang: Lang = Lang("en")
  val welshLang: Lang = Lang("cy")
  val label = """{"name":"BLAH"}"""
  val labelWithSingleValue = """{"name":"BLAH","english":"39.99"}"""
  val labelWithValues = """{"name":"BLAH","english":"Hello","welsh":"Welsh, Hello"}"""

  "Label" must {
    "deserialise " in {
      Json.parse(label).as[Label] shouldBe Label("BLAH")
      Json.parse(labelWithSingleValue).as[Label] shouldBe Label("BLAH", Some("39.99"))
    }

    "serialise from Label to json" in {
      val valueLabel: Label = Label("BLAH")
      Json.toJson(valueLabel).toString shouldBe label
      val valueLabelWithValue: Label = Label("BLAH", Some("39.99"))
      Json.toJson(valueLabelWithValue).toString shouldBe labelWithSingleValue
    }

    "deserialise label with both english and welsh values" in {
      Json.parse(labelWithValues).as[Label] shouldBe Label("BLAH", Some("Hello"), Some("Welsh, Hello"))
    }

    "serialise from Label with both english and welsh values to json" in {
      val dLabelWithValues: Label = Label("BLAH", Some("Hello"), Some("Welsh, Hello"))
      Json.toJson(dLabelWithValues).toString shouldBe labelWithValues
    }

  }

  "LabelCache" must {
    "contruct single value label" in {
      val labels = LabelCache()
      val newLabels = labels.update("Name", "value")

      newLabels.updatedLabels("Name") shouldBe Label("Name", Some("value"))
    }

    "contruct muli-value label" in {
      val labels = LabelCache()
      val newLabels = labels.update("Name", "english", "welsh")

      newLabels.updatedLabels("Name") shouldBe Label("Name", Some("english"), Some("welsh"))
    }

    "Allow reference to the current value of a label" in {
      val labelsMap = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y", Some("4")), "Name" -> Label("Name", Some("Coltrane")))
      val labels = LabelCache(labelsMap)
      labels.value("X") shouldBe Some("33.5")
      labels.value("Name") shouldBe Some("Coltrane")
    }

    "Return a language specifc value of a multi-lingual label" in {
      val labelsMap = Map("Door"-> Label("Door", Some("Open"), Some("Drws")), "Name" -> Label("Name", Some("Coltrane")))
      val labels = LabelCache(labelsMap)
      labels.displayValue("Door")(englishLang) shouldBe Some("Open")
      labels.displayValue("Door")(welshLang) shouldBe Some("Drws")
      labels.displayValue("Name")(englishLang) shouldBe Some("Coltrane")
      labels.displayValue("Name")(welshLang) shouldBe Some("Coltrane")
    }

    "Allow access to the main label map" in {
      val labelsMap = Map("Door"-> Label("Door", Some("Open"), Some("Drws")), "Name" -> Label("Name", Some("Coltrane")))
      val labels = LabelCache(labelsMap)

      labels.labelMap shouldBe labelsMap
    }

    "Allow addition/update of labels with an english and welsh translation" in {
      val labels = LabelCache(Map("Door"-> Label("Door", Some("Open"), Some("Drws")), "Name" -> Label("Name", Some("Coltrane"))))

      val updatedLabels0 = labels.update("Door", "Ajar", "Dysgu")
      updatedLabels0.displayValue("Door")(englishLang) shouldBe Some("Ajar")
      updatedLabels0.displayValue("Door")(welshLang) shouldBe Some("Dysgu")

      val updatedLabels1 = updatedLabels0.update("Door", "Open", "Drws")
      updatedLabels1.displayValue("Door")(englishLang) shouldBe Some("Open")
      updatedLabels1.displayValue("Door")(welshLang) shouldBe Some("Drws")
    }

    "Construct a LabelCache from a label map and a cache of updated labels" in {
      val labelsMap = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y"), "Name" -> Label("Name", Some("Coltrane")))
      val cacheMap = Map("X"->Label("X", Some("46.5")))
      val labels = LabelCache(labelsMap, cacheMap)
      labels.value("X") shouldBe Some("46.5")
    }

    "Return an empty string if label has no assigned value" in {
      val labelsMap = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y"), "Name" -> Label("Name", Some("Coltrane")))
      val labels = LabelCache(labelsMap)
      labels.value("Y") shouldBe Some("")
    }

    "Return None if referenced label does not exist" in {
      val labelsMap = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y"), "Name" -> Label("Name", Some("Coltrane")))
      val labels = LabelCache(labelsMap)
      labels.value("Z") shouldBe None
    }

    "Allow the current value of the label to be updated" in {
      val labelsMap = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y", Some("4")), "Name" -> Label("Name", Some("Coltrane")))
      val labels = LabelCache(labelsMap)
      labels.value("X") shouldBe Some("33.5")

      val updatedLabels = labels.update("Name", "Miles")
      updatedLabels.value("Name") shouldBe Some("Miles")
    }

    "Allow a new label to be added to the cache" in {
      val labelsMap = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y", Some("4")), "Name" ->Label("Name", Some("Coltrane")))
      val labels = LabelCache(labelsMap)
      labels.value("X") shouldBe Some("33.5")

      val updatedLabels = labels.update("Location", "Here")
      updatedLabels.value("Location") shouldBe Some("Here")
    }

    "Return a map of new and updated labels on request" in {
      val labelsMap = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y", Some("4")), "Name" ->Label("Name", Some("Coltrane")))
      val labels = LabelCache(labelsMap)
      labels.value("X") shouldBe Some("33.5")

      val labels1 = labels.update("X", "49.5")
      val labels2 = labels1.update("Location", "Here")
      labels2.updatedLabels shouldBe Map("X" ->Label("X",Some("49.5")), "Location" ->Label("Location",Some("Here")))
    }

    "Flush updated labels to main store" in {
      val labelsMap = Map("X"->Label("X", Some("33.5")), "Y"->Label("Y", Some("4")), "Name" ->Label("Name", Some("Coltrane")))
      val labels = LabelCache(labelsMap)

      val labels1 = labels.update("X", "49.5")
      val labels2 = labels1.update("Location", "Here")
      labels2.updatedLabels shouldBe Map("X" ->Label("X",Some("49.5")), "Location" ->Label("Location",Some("Here")))

      val labels3 = labels2.flush
      labels3.updatedLabels shouldBe Map()
      labels3.value("X") shouldBe Some("49.5")
      labels3.value("Location") shouldBe Some("Here")
    }
  }
}
