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

package base

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
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
}

trait BaseSpec extends WordSpec with Matchers with ScalaFutures with TestConstants {

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
