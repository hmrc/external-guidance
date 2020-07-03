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

package services

import base.UnitSpec
import models.ocelot._
import models.ocelot.stanzas.{Instruction, InstructionGroup}

import scala.util.matching.Regex.Match

class BulletPointBuilderSpec extends UnitSpec {

  def asString(elements: Seq[String]): String = elements.mkString

  def createInstructionGroup(text1: String, text2: String): InstructionGroup = {

    val phrase1: Phrase = Phrase(Vector(text1, s"$welshPrefix $text1"))
    val phrase2: Phrase = Phrase(Vector(text2, s"$welshPrefix $text2"))

    val instruction1: Instruction = Instruction(phrase1, Seq("2"), None, stack = true)
    val instruction2: Instruction = Instruction(phrase2, Seq("3"), None, stack = true)

    InstructionGroup(Seq(instruction1, instruction2))
  }

  val welshPrefix: String = "Welsh - "

  "BulletPointBuilder bold text annotation removal processing" must {

    "Manage  a blank text string" in {

      val text = ""

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe text
    }

    "Return text unchanged when no bold text present" in {

      val text: String = "Today the weather is fine"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe text
    }

    "Return bold text only when normal text is not defined" in {

      val text: String = "[bold:Important]"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Important"
    }

    "Return both normal and bold text for combination of leading text followed by bold text" in {

      val text: String = "This is [bold:Important]"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "This is Important"
    }

    "Return both normal text and bold text for combination of leading bold text followed by normal text" in {

      val text: String = "[bold:Important] do not do this"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Important do not do this"
    }

    "Return both normal and bold text for text with single embedded bold text" in {

      val text: String = "Hello from [bold:Team Ocelot] in Greenland"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Hello from Team Ocelot in Greenland"
    }

    "Return both normal and bold text with normal text embedded in bold text" in {

      val text: String = "[bold:Greetings from] our home in lovely [bold:Nova Scotia]"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Greetings from our home in lovely Nova Scotia"
    }

    "Return both normal and bold text from mixed text starting with normal text" in {

      val text: String = "Today is [bold:Wednesday 10th May] and tomorrow is [bold:Thursday 11th May]"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Today is Wednesday 10th May and tomorrow is Thursday 11th May"
    }

    "Return both normal and bold text from mixed text staring with bold text" in {

      val text: String = "[bold:Here and now] we must all [bold:try] to be calm"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Here and now we must all try to be calm"
    }
  }

  "BulletPointBuilder link text annotation removal processing" must {

    "Manage a blank text string" in {

      val text = ""

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe text
    }

    "Return text unchanged when no link text present" in {

      val text: String = "Today the weather is fine"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe text
    }

    "Return link text only when normal text is not defined" in {

      val text: String = "[link:View options:https://mydomain/options]"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "View options"
    }

    "Return both normal and link text for combination of leading text followed by link text" in {

      val text: String = "View instructions for [link:mending a broken axle:http://mechanicsAreUs/axles]"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "View instructions for mending a broken axle"
    }

    "Return both normal text and link text for combination of leading link text followed by normal text" in {

      val text: String = "[link:Click here:https://my.com/details] for information"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Click here for information"
    }

    "Return both normal and link text for text with single embedded link" in {

      val text: String = "For details [link:click here:https://info.co.uk/details] and follow the instructions shown"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "For details click here and follow the instructions shown"
    }

    "Return both normal and link text with normal text embedded in links" in {

      val text: String = "[link:Link 1 text:http://link1] and [link:link 2 text:https://link2]"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Link 1 text and link 2 text"
    }

    "Return both normal and link text from mixed text starting with normal text" in {

      val text: String = "Today is [link:Wednesday 10th May:http://my.com/calendar] and tomorrow is [link:Thursday 11th May:http://my.com/calendar]"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Today is Wednesday 10th May and tomorrow is Thursday 11th May"
    }

    "Return both normal and link text from mixed text staring with link" in {

      val text: String = "[link:Here and now:http://thisyear/today] we must all [link:try:https://explain] to be calm"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "Here and now we must all try to be calm"
    }

    "Return correct text with back to back links" in {

      val text: String = "This should [link:be interesting:https://my.com/interesting?part=2] [link:and informative:http://my.com/inform]"

      asString(BulletPointBuilder.fragmentsToDisplayAsSeq(text)) shouldBe "This should be interesting and informative"
    }

  }

  "Bullet point builder identification of bullet point list leading text" must {

    "Identify leading text in simple sentences" in {

      val text1: String = "Types of fruit you can buy: apples"
      val text2: String = "Types of fruit you can buy: oranges"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "Types of fruit you can buy:"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix Types of fruit you can buy:"
    }

    "Identify leading text in sentences starting with bold text" in {

      val text1: String = "[bold:Types of automobile] you can buy saloon"
      val text2: String = "[bold:Types of automobile] you can buy sports utility vehicle"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "[bold:Types of automobile] you can buy"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix [bold:Types of automobile] you can buy"
    }

    "Identify leading text in complex sentences" in {

      val text1: String =
        "The property allowance lets you earn up to \u00a311,000 in rental income, tax free, in each tax year. For example: renting a flat or house"
      val text2: String =
        "The property allowance lets you earn up to \u00a311,000 in rental income, tax free, in each tax year. For example: renting out a room in your home"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "The property allowance lets you earn up to \u00a311,000 in rental income, tax free, in each tax year. For example: renting"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix The property allowance lets you earn up to \u00a311,000 in rental income, tax free, in each tax year. For example: renting"
    }

    "Identify leading text is sentences where the leading text ends with bold text" in {

      // Note the final space after the bold text can be ignored

      val text1: String = "Things you might like [bold:TO DO] this very day"
      val text2: String = "Things you might like [bold:TO DO] on another day"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "Things you might like [bold:TO DO]"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix Things you might like [bold:TO DO]"
    }

    "Identify leading text in sentences where the leading text contains bold text items embedded in normal text" in {

      val text1: String = "Things [bold:to do] on sunny [bold:days] in the winter season"
      val text2: String = "Things [bold:to do] on sunny [bold:days] in the summer season"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "Things [bold:to do] on sunny [bold:days] in the"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix Things [bold:to do] on sunny [bold:days] in the"
    }

    "Identify leading text in sentences where the leading text contains normal text embedded in bold text" in {

      val text1: String = "[bold:How long] must we [bold:continue to] be [bold:stuck in] mud"
      val text2: String = "[bold:How long] must we [bold:continue to] be [bold:stuck in] snow"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "[bold:How long] must we [bold:continue to] be [bold:stuck in]"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix [bold:How long] must we [bold:continue to] be [bold:stuck in]"
    }

    "Identify leading text in simple sentences with multiple spaces between some words" in {

      val text1: String = "Types of  fruit you  can buy: apples"
      val text2: String = "Types of  fruit you  can buy: oranges"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "Types of  fruit you  can buy:"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix Types of  fruit you  can buy:"
    }

    "Identify leading text in sentences starting with bold text with multiple spaces between some of the bold words" in {

      val text1: String = "[bold:Types of  automobile] you can buy saloon"
      val text2: String = "[bold:Types of  automobile] you can buy sports utility vehicle"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "[bold:Types of  automobile] you can buy"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix [bold:Types of  automobile] you can buy"
    }

    "Identify leading text in sentences starting with link text" in {

      val text1: String = "[link:Types of automobile:http://mydomain/cars] you can buy saloon"
      val text2: String = "[link:Types of automobile:http://mydomain/cars] you can buy sports utility vehicle"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "[link:Types of automobile:http://mydomain/cars] you can buy"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix [link:Types of automobile:http://mydomain/cars] you can buy"
    }

    "Identify leading text is sentences where the leading text ends with link text" in {

      // Note the final space after the bold text can be ignored

      val text1: String = "Things you might like [link:to consider buying:https://mydomain/products?catalog=books] this very day"
      val text2: String = "Things you might like [link:to consider buying:https://mydomain/products?catalog=books] on another day"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "Things you might like [link:to consider buying:https://mydomain/products?catalog=books]"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix Things you might like [link:to consider buying:https://mydomain/products?catalog=books]"
    }

    "Identify leading text in sentences where the leading text contains link text items embedded in normal text" in {

      val text1: String = "Things to do on [link:sunny:5] days [link:at school:http://mydomain/schools] in the winter season"
      val text2: String = "Things to do on [link:sunny:5] days [link:at school:http://mydomain/schools] in the summer season"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "Things to do on [link:sunny:5] days [link:at school:http://mydomain/schools] in the"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix Things to do on [link:sunny:5] days [link:at school:http://mydomain/schools] in the"
    }

    "Identify leading text in sentences where the leading text contains normal text embedded in link text" in {

      val text1: String =
        "[link:How long:https://mydomain/duration/epochs] must we [link:continue to:2] be [link:stuck in://http://www.stuck.com/stuck] muddy lanes"
      val text2: String =
        "[link:How long:https://mydomain/duration/epochs] must we [link:continue to:2] be [link:stuck in://http://www.stuck.com/stuck] snow covered mountains"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "[link:How long:https://mydomain/duration/epochs] must we [link:continue to:2] be [link:stuck in://http://www.stuck.com/stuck]"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix [link:How long:https://mydomain/duration/epochs] must we [link:continue to:2] be [link:stuck in://http://www.stuck.com/stuck]"
    }

    "Identify leading text in sentences starting with link text with multiple spaces between some of the words" in {

      val text1: String = "[link:Types of  automobile:5] you  can buy saloon"
      val text2: String = "[link:Types of  automobile:5] you  can buy sports utility vehicle"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "[link:Types of  automobile:5] you  can buy"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix [link:Types of  automobile:5] you  can buy"
    }

    "Identify leading text in sentences starting with leading text with both links and bold text" in {

      val text1: String = "Today is a [bold:good day] to enjoy [link:motor racing:http://mydomain/motor-racing] at Silverstone"
      val text2: String = "Today is a [bold:good day] to enjoy [link:motor racing:http://mydomain/motor-racing] at Hednesford Raceway"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "Today is a [bold:good day] to enjoy [link:motor racing:http://mydomain/motor-racing] at"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix Today is a [bold:good day] to enjoy [link:motor racing:http://mydomain/motor-racing] at"
    }

    "Identify leading text in sentences where leading text and trailing text are both bold" in {

      val text1: String = "[bold:Today is the first day in ][bold:May]"
      val text2: String = "[bold:Today is the first day in ][bold:July]"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "[bold:Today is the first day in ]"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix [bold:Today is the first day in ]"
    }

    "Identify leading text in sentences where leading text and trailing text are both in links" in {

      val text1: String = "[link:Today is the first day in :https://mydomain/calendar/today][link:May:https://nydomain/calendar/may]"
      val text2: String = "[link:Today is the first day in :https://mydomain/calendar/today][link:July:https://mydomain/calendar/july]"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "[link:Today is the first day in :https://mydomain/calendar/today]"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix [link:Today is the first day in :https://mydomain/calendar/today]"
    }

    "Identify leading text where text includes a bold section followed immediately by a non-white space character" in {

      val text1: String = "You can buy the [bold:following]: apples"
      val text2: String = "You can buy the [bold:following]: oranges"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "You can buy the [bold:following]:"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix You can buy the [bold:following]:"
    }

    "Identify leading text where text includes a bold section followed immediately by a non-white space character and then further texts" in {

      val text1: String = "You can [bold:buy], things such as, various antiques"
      val text2: String = "You can [bold:buy], things such as, various trinkets"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "You can [bold:buy], things such as, various"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix You can [bold:buy], things such as, various"
    }

    "Identify leading text where text includes both bold and link placeholders immediately followed by non-whitespace characters" in {

      val text1: String = "You can [bold:buy], if you like, anything at [link:the general store:https://mydomain/store], and sell it to your friends"
      val text2: String = "You can [bold:buy], if you like, anything at [link:the general store:https://mydomain/store], and sell it to your acquaintances"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "You can [bold:buy], if you like, anything at [link:the general store:https://mydomain/store], and sell it to your"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix You can [bold:buy], if you like, anything at [link:the general store:https://mydomain/store], and sell it to your"
    }

    "Identify leading text where text includes a placeholder immediately following none-whitespace text" in {

      val text1: String = "You can buy[bold:-categories] fruit"
      val text2: String = "You can buy[bold:-categories] vegetables"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "You can buy[bold:-categories]"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix You can buy[bold:-categories]"
    }

    "Identify leading text where text includes a placeholder immediately following none-whitespace text followed by further matching text" in {

      val text1: String = "You can buy[bold:-categories] fruit and veg: potato"
      val text2: String = "You can buy[bold:-categories] fruit and veg: parsnip"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe "You can buy[bold:-categories] fruit and veg:"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe s"$welshPrefix You can buy[bold:-categories] fruit and veg:"
    }

    "Identify leading text where text includes both bold and link placeholders with leading text" in {

      val text1: String = "You can buy[bold:-categories] fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg] : potato"
      val text2: String = "You can buy[bold:-categories] fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg] : parsnip"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "You can buy[bold:-categories] fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg] :"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix You can buy[bold:-categories] fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg] :"
    }

    "Identify leading text containing both leading and trailing text with respect to place holders" in {

      val text1: String = "Today please note[bold:(Important)] we are selling[link:<link>:http://mydomain/items/fruitAndVeg] such as pears and apples"
      val text2: String = "Today please note[bold:(Important)] we are selling[link:<link>:http://mydomain/items/fruitAndVeg] such as carrots and turnips"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "Today please note[bold:(Important)] we are selling[link:<link>:http://mydomain/items/fruitAndVeg] such as"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix Today please note[bold:(Important)] we are selling[link:<link>:http://mydomain/items/fruitAndVeg] such as"
    }

    "Identify leading text where text includes both leading and trailing text for a placeholder" in {

      val text1: String = "You can buy fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg]: potato"
      val text2: String = "You can buy fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg]: parsnip"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 0) shouldBe
        "You can buy fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg]:"

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix You can buy fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg]:"
    }

    "Identify leading text where text includes both leading and trailing text for a placeholder and following text" in {

      val text1: String = "You can buy fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg], such as, potatoes"
      val text2: String = "You can buy fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg], such as, oranges"

      val instructionGroup: InstructionGroup = createInstructionGroup(text1, text2)

      BulletPointBuilder.determineMatchedLeadingText(instructionGroup, 1) shouldBe
        s"$welshPrefix You can buy fruit and vegetables[link:<link>:http://mydomain/fruitAndVeg], such as,"
    }

    "Method locateTextsAndMatchesContainingLeadingText" must {

      "Handle so far theoretical case when no text or match components are present" in {

        val text1: String = "Today is a [bold:good day] to enjoy [link:motor racing:http://mydomain/motor-racing] at Silverstone"

        val texts: List[String] = TextBuilder.placeholdersPattern.split(text1).toList
        val matches: List[Match] = TextBuilder.placeholdersPattern.findAllMatchIn(text1).toList

        // Test invocation
        val (wordsProcessed1, outputTexts1, outputMatches1) =
          BulletPointBuilder.locateTextsAndMatchesContainingLeadingText(2, List(), 0, List(), 0, texts, matches, 0)

        wordsProcessed1 shouldBe 0

        outputTexts1 shouldBe texts
        outputMatches1 shouldBe matches

        // Test invocation
        val (wordsProcessed2, outputTexts2, outputMatches2) =
          BulletPointBuilder.locateMatchesContainingLeadingText(2, List(), 0, List(), 0, texts, matches, 2)

        wordsProcessed2 shouldBe 2

        outputTexts2 shouldBe texts
        outputMatches2 shouldBe matches
      }

    }
  }

  "Method locateTextsAndMatchesContainingLeadingText" must {

    "Handle so far theoretical case when no text or match components are present" in {

      val text1: String = "Today is a [bold:good day] to enjoy [link:motor racing:http://mydomain/motor-racing] at Silverstone"

      val texts: List[String] = TextBuilder.placeholdersPattern.split(text1).toList
      val matches: List[Match] = TextBuilder.placeholdersPattern.findAllMatchIn(text1).toList

      // Test invocation
      val (wordsProcessed1, outputTexts1, outputMatches1) =
        BulletPointBuilder.locateTextsAndMatchesContainingLeadingText(2, List(), 0, List(), 0, texts, matches, 0)

      wordsProcessed1 shouldBe 0

      outputTexts1 shouldBe texts
      outputMatches1 shouldBe matches

      // Test invocation
      val (wordsProcessed2, outputTexts2, outputMatches2) =
        BulletPointBuilder.locateMatchesContainingLeadingText(2, List(), 0, List(), 0, texts, matches, 2)

      wordsProcessed2 shouldBe 2

      outputTexts2 shouldBe texts
      outputMatches2 shouldBe matches
    }
  }

  "Bullet point list instruction match testing" must {

    "Not match instructions with no similar text" in {

      val firstInstructionText: String = "Good Morning"
      val secondInstructionText: String = "Buen día"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, true)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe false
    }

    "Not match instructions with two similar leading words" in {

      val firstInstructionText: String = "Today is Wednesday"
      val secondInstructionText: String = "Today is Thursday"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, true)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe false
    }

    "Match instructions with three similar leading words" in {

      val firstInstructionText: String = "I have bought: apples"
      val secondInstructionText: String = "I have bought: oranges"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, true)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }

    "Match instructions with multiple similar leading words" in {

      val firstInstructionText: String = "The road is long and winding over there"
      val secondInstructionText: String = "The road is long and winding and here"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, true)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }

    "Not match instructions with multiple similar leading words but different spacing between the second and third words" in {

      val firstInstructionText: String = "The road   is long and winding over there"
      val secondInstructionText: String = "The road is long and winding and here"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, true)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe false
    }

    "Not match instructions with two similar leading words in bold" in {

      val firstInstructionText: String = "[bold:Today is Monday]"
      val secondInstructionText: String = "[bold:Today is Thursday]"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, true)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe false
    }

    "Match instructions with three similar leading words in bold" in {

      val firstInstructionText: String = "[bold:I have bought: apples]"
      val secondInstructionText: String = "[bold:I have bought: oranges]"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, true)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }

    "Not match instructions with three similar leading words in bold but different spacings between the first and second words" in {

      val firstInstructionText: String = "[bold:I have bought: apples]"
      val secondInstructionText: String = "[bold:I  have bought: oranges]"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, true)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe false
    }

    "Not match instructions with two similar leading words one normal text and one bold" in {

      val firstInstructionText: String = "Today [bold:is Monday]"
      val secondInstructionText: String = "Today [bold:is Thursday]"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe false
    }

    "Match instructions with three similar leading words one normal text and two bold" in {

      val firstInstructionText: String = "Today [bold:is Monday] 1st"
      val secondInstructionText: String = "Today [bold:is Monday] 2nd"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }

    "Match instructions with multiple similar leading words with multiple sets of bold words" in {

      val firstInstructionText: String = "Today is [bold:Monday and] tomorrow will [bold:be Tuesday] 4th"
      val secondInstructionText: String = "Today is [bold:Monday and] tomorrow will [bold:be Tuesday] 7th"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }

    "Not match instructions with two similar leading words in links" in {

      val firstInstructionText: String = "[link:Today is Monday:http://mydomain/test]"
      val secondInstructionText: String = "[link:Today is Thursday:http://mydomain/test]"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe false
    }

    "Match instructions with link in leading text" in {

      val firstInstructionText: String = "[link:The news this: morning:https://mydomain/news/morning] Early riser fails to get up"
      val secondInstructionText: String = "[link:The news this: afternoon:https://mydomain/news/afternoon] Lunch goes missing"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }

    "Not match instructions with link in leading text but differing spaces between the second and third words" in {

      val firstInstructionText: String = "[link:The news  this: morning:https://mydomain/news/morning] Early riser fails to get up"
      val secondInstructionText: String = "[link:The news this: afternoon:https://mydomain/news/afternoon] Lunch goes missing"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe false
    }

    "Match instructions with links in trailing text" in {

      val firstInstructionText: String = "Today I bought some [link:oranges:http://mydomain/fruits/oranges]"
      val secondInstructionText: String = "Today I bought some [link:apples:http://mydomain/fruits/apples]"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }

    "Match instructions with complex leading and trailing text" in {

      val firstInstructionText: String =
        "Today [bold: I bought] some [link:fruits:http://mydomain/fruits] at [bold:Stafford Fruit Market] see [link:Staffordshire markets:https://mydomain/markets/staffordshire]"
      val secondInstructionText: String =
        "Today [bold: I bought] some [link:fruits:http://mydomain/fruits] at [bold:Shrewsbury Market] see [link:Shropshire markets:https://mydomain/markets/shropshire]"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }

    "Match test instructions from Json prototype 1" in {

      val firstInstructionText: String =
        "The property allowance lets you earn up to \u00a311,000 in rental income, tax free, in each tax year. For example: renting a flat or house"
      val secondInstructionText: String =
        "The property allowance lets you earn up to \u00a311,000 in rental income, tax free, in each tax year. For example: renting out a room in your home"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }

    "Match test instructions from Json prototype 2" in {

      val firstInstructionText: String =
        "In some circumstances, you do not have to tell HMRC about extra income you've made. In each tax year you can earn up to £11,000, tax free, if you are: selling goods or services (trading)"
      val secondInstructionText: String =
        "In some circumstances, you do not have to tell HMRC about extra income you've made. In each tax year you can earn up to £11,000, tax free, if you are: renting land or property"

      val firstInstructionPhrase: Phrase = Phrase(firstInstructionText, "")
      val secondInstructionPhrase: Phrase = Phrase(secondInstructionText, "")

      val i1: Instruction = new Instruction(firstInstructionPhrase, Nil, None, false)
      val i2: Instruction = new Instruction(secondInstructionPhrase, Nil, None, false)

      BulletPointBuilder.matchInstructions(i1, i2) shouldBe true
    }
  }

}
