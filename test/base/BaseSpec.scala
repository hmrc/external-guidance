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

package base

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import play.api.i18n.Lang

trait EnglishLanguage {
  implicit val lang: Lang = Lang("en")
}

trait WelshLanguage {
  implicit val lang: Lang = Lang("cy")
}

trait TestConstants {

  val zero: Int = 0
  val one: Int = 1
  val two: Int = 2
  val three: Int = 3
  val four: Int = 4
  val five: Int = 5
  val six: Int = 6
  val seven: Int = 7
  val eight: Int = 8
  val nine = 9
  val ten = 10
  val eleven = 11
  val twelve = 12
  val thirteen = 13
  val fourteen = 14
  val fifteen = 15

  val oneHundred: Int = 100

  val enmessages: Map[String, String] =
    Map("month.display.value.1" -> "January",
        "month.display.value.2" -> "February",
        "month.display.value.3" -> "March",
        "month.display.value.4" -> "April",
        "month.display.value.5" -> "May",
        "month.display.value.6" -> "June",
        "month.display.value.7" -> "July",
        "month.display.value.8" -> "August",
        "month.display.value.9" -> "September",
        "month.display.value.10" -> "October",
        "month.display.value.11" -> "November",
        "month.display.value.12" -> "December",
        "day.display.value.1" -> "Monday",
        "day.display.value.2" -> "Tuesday",
        "day.display.value.3" -> "Wednesday",
        "day.display.value.4" -> "Thursday",
        "day.display.value.5" -> "Friday",
        "day.display.value.6" -> "Saturday",
        "day.display.value.7" -> "Sunday"
      )

  val cymessages: Map[String, String] =
    Map("month.display.value.1" -> "Ionawr",
        "month.display.value.2" -> "Chwefror",
        "month.display.value.3" -> "Mawrth",
        "month.display.value.4" -> "Ebrill",
        "month.display.value.5" -> "Mai",
        "month.display.value.6" -> "Mehefin",
        "month.display.value.7" -> "Gorffennaf",
        "month.display.value.8" -> "Awst",
        "month.display.value.9" -> "Medi",
        "month.display.value.10" -> "Hydref",
        "month.display.value.11" -> "Tachwedd",
        "month.display.value.12" -> "Rhagfyr",
        "day.display.value.1" -> "Dydd Llun",
        "day.display.value.2" -> "Dydd Mawrth",
        "day.display.value.3" -> "Dydd Mercher",
        "day.display.value.4" -> "Dydd Iau",
        "day.display.value.5" -> "Dydd Gwener",
        "day.display.value.6" -> "Dydd Sadwrn",
        "day.display.value.7" -> "Dydd Sul"
      )

  def message(lang: Lang)(id: String, param: Any*): String =
    lang.code match {
      case "en" => enmessages(id)
      case "cy" => cymessages(id)
    }
}

trait BaseSpec extends AnyWordSpec with Matchers with ScalaFutures with TestConstants {

  def missingJsObjectAttrTests[T](jsObject: JsObject, attrsToIgnore: List[String] = Nil)(implicit objectReads: Reads[T]): Unit =
    jsObject.keys.filterNot(attrsToIgnore.contains(_)).foreach { attributeName =>
      s"return error when json is missing attribute $attributeName" in {
        val invalidJson = jsObject - attributeName
        invalidJson.validate[T] match {
          case JsSuccess(_, _) => fail(s"Object incorrectly created when attribute $attributeName missing")
          case JsError(_) => succeed
        }
      }
    }

  def incorrectPropertyTypeJsObjectAttrTests[T](jsObject: JsObject, attrsToIgnore: List[String] = Nil)(implicit objectReads: Reads[T]): Unit = {

    jsObject.keys.filterNot(attrsToIgnore.contains(_)).foreach { attributeName =>
      val attributeValue = jsObject.value(attributeName)

      // Create a new JsValue of incorrect type
      val invalidJsValue = attributeValue match {
        case _: JsString => JsNumber(BigDecimal(0))
        case _ => JsString("This attribute should not be a string")
      }

      // Invalidate JsObject
      val invalidJsObject: JsObject = (jsObject - attributeName) ++
        Json.obj(attributeName -> invalidJsValue)

      // Apply test
      s"reading a value of incorrect type for $attributeName" should {

        "Raise an exception" in {

          invalidJsObject.validate[T] match {
            case JsSuccess(_, _) => fail(s"Should not be able to parse Json object when attribute $attributeName is of the wrong type")
            case JsError(_) => succeed
          }
        }
      }

    }

  }

  def removeSpacesAndNewLines(s: String): String = {
    s.replaceAll("\n","" ).replaceAll(" ", "")
  }
}
