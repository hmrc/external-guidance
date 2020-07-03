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

package models.ui

import base.UnitSpec
import play.api.i18n.Lang

class UIComponentsSpec extends UnitSpec {

  val four: Int = 4
  val five: Int = 5
  val six: Int = 6

  val h1English: String = "Heading level 1 text"
  val h1Welsh: String = "Welsh heading level 1 text"

  val h2English: String = "Heading level 2 text"
  val h2Welsh: String = "Welsh heading level 2 text"

  val h3English: String = "Heading level 3 text"
  val h3Welsh: String = "Welsh heading level 3 text"

  val h4English: String = "Heading level 4 text"
  val h4Welsh: String = "Welsh heading level 4 text"

  val engLeadingText: String = "Leading text"
  val welLeadingText: String = "Welsh leading text"

  val engBulletPointOneText = "Bullet point 1"
  val welBulletPointOneText = "Welsh bullet point 1"

  val engBulletPointTwoText = "Bullet point 2"
  val welBulletPointTwoText = "Welsh bullet point 2"

  val englishLang: Lang = Lang("en")
  val welshLang: Lang = Lang("cy")

  "UIComponents" must {

    "return appropriate lang text from Text and Text given Lang welsh" in {
      val welsh = "Welsh, Hello my name is ...."
      val english = "Hello my name is ...."

      implicit val lang: Lang = Lang("cy")

      Text(english, welsh).value shouldBe Seq(Words(welsh))
    }

    "return appropriate lang text from Text and Text given Lang english" in {
      val welsh = "Welsh, Hello my name is ...."
      val english = "Hello my name is ...."
      implicit val lang: Lang = Lang("en")

      Text(english, welsh).value shouldBe Seq(Words(english))
    }

    "return appropriate lang text from Text and Text given Lang unknown" in {
      val welsh = "Welsh, Hello my name is ...."
      val english = "Hello my name is ...."
      implicit val lang: Lang = Lang("jp")

      Text(english, welsh).value shouldBe Seq(Words(english))
    }

    "use text components with an overriding implementation of the method toString" in {
      val english = "Example text"
      val welsh = "Welsh example text"

      Text(english, welsh).toString shouldBe s"[$english:$welsh]"
    }

    "support isEmpty within Text" in {
      Text().isEmpty(Lang("en")) shouldBe true
      Text().isEmpty(Lang("cy")) shouldBe true

      Text("", "").isEmpty(Lang("en")) shouldBe false

      Text("", "").english.forall(_.isEmpty) shouldBe true
      Text("", "").welsh.forall(_.isEmpty) shouldBe true
    }

    "use Link components with an implementation of the toString method for use in debugging" in {

      val englishLinkText = "English link text"
      val welshLinkText = "Welsh link text"

      val destination = "http://my.com/page"

      val linkEn: Link = Link(destination, englishLinkText)
      val linkCy: Link = Link(destination, welshLinkText)

      linkEn.toString shouldBe s"[link:$englishLinkText:$destination:false]"
      linkCy.toString shouldBe s"[link:$welshLinkText:$destination:false]"
    }

    "use Link components which correctly support isEmpty" in {
      Link("/secondpage", "").isEmpty shouldBe true
      Link("/secondpage", "Hello").isEmpty shouldBe false
    }

    "use Link components which correctly support toString" in {
      val en: String = "Hello"
      Link("4", en).toString shouldBe s"[link:$en:4:false]"
    }

  }

}
