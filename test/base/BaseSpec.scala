/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json._

trait BaseSpec extends WordSpec with MustMatchers with ScalaFutures {

  def missingJsObjectAttrTests[T](jsObject: JsObject, attrsToIgnore: List[String] = Nil)(implicit objectReads: Reads[T]): Unit =
    jsObject.keys.filterNot(attrsToIgnore.contains(_)).foreach { attributeName =>
      s"throw exception when json is missing attribute $attributeName" in {
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
}
