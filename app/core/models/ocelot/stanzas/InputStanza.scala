/*
 * Copyright 2025 HM Revenue & Customs
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

import core.models.ocelot.stanzas.TaxCodeUtilities.retrieveTaxCodeComponents
import core.models.ocelot.{Labels, Page, Phrase, SecuredProcess, Ten, Validation, asAnyInt, asCurrency,
  asCurrencyPounds, asTextString, labelReferences, stringFromDate, taxCodeLabelPattern, taxCodePattern, validDate}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class InputStanza(
  ipt_type: InputType,
  override val next: Seq[String],
  name: Int,
  help: Option[Int],
  label: String,
  placeholder: Option[Int],
  stack: Boolean
) extends Stanza {
  override val labels: List[String] = List(label)
}

object InputStanza {

  implicit val reads: Reads[InputStanza] =
    ((JsPath \ "ipt_type").read[InputType] and
      (JsPath \ "next").read[Seq[String]](minLength[Seq[String]](1)) and
      (JsPath \ "name").read[Int] and
      (JsPath \ "help").readNullable[Int] and
      (JsPath \ "label").read[String] and
      (JsPath \ "placeholder").readNullable[Int] and
      (JsPath \ "stack").read[Boolean])(InputStanza.apply _)

  implicit val writes: OWrites[InputStanza] =
    (
      (JsPath \ "ipt_type").write[InputType] and
        (JsPath \ "next").write[Seq[String]] and
        (JsPath \ "name").write[Int] and
        (JsPath \ "help").writeNullable[Int] and
        (JsPath \ "label").write[String] and
        (JsPath \ "placeholder").writeNullable[Int] and
        (JsPath \ "stack").write[Boolean]
    )(unlift(InputStanza.unapply))

}

sealed trait Input extends DataInputStanza {
  val name: Phrase
  val help: Option[Phrase]
  val label: String
  val placeholder: Option[Phrase]
  val stack: Boolean
  val dontRepeatName: Boolean
  val width: String

  override val labelRefs: List[String] = labelReferences(name.english) ++ help.fold[List[String]](Nil)(h => labelReferences(h.english))
  override val labels: List[String] = List(label)
  def eval(value: String, page: Page, labels: Labels): (Option[String], Labels) = (next.headOption, labels.update(label, value))
}

case class NumberInput(
  override val next: Seq[String],
  name: Phrase,
  help: Option[Phrase],
  label: String,
  placeholder: Option[Phrase],
  stack: Boolean,
  dontRepeatName: Boolean = false,
  width: String = Ten
) extends Input {
  def validInput(value: String): Validation[String] = asAnyInt(value).fold[Validation[String]](Left(Nil))(v => Right(v.toString))
  override def rendered(expand: Phrase => Phrase): DataInputStanza = copy(name = expand(name), help = help.map(expand), placeholder = placeholder.map(expand))
}

case class TextInput(
  override val next: Seq[String],
  name: Phrase,
  help: Option[Phrase],
  label: String,
  placeholder: Option[Phrase],
  stack: Boolean,
  dontRepeatName: Boolean = false,
  width: String = Ten
) extends Input {
  val logger: Logger = Logger(this.getClass)
  def validInput(value: String): Validation[String] = asTextString(value).fold[Validation[String]](Left(Nil))(v => Right(v))
  override def rendered(expand: Phrase => Phrase): DataInputStanza = copy(name = expand(name), help = help.map(expand), placeholder = placeholder.map(expand))
  override def eval(value: String, page: Page, labels: Labels): (Option[String], Labels) =
    label match {
      case labelName if taxCodeLabelPattern.matches(labelName) =>
        logger.warn(s"__TaxCode_ label: $labelName received for page URL: ${page.url}")
        (next.headOption, retrieveTaxCodeComponents(labels = labels, fullTaxCode = value, label = labelName).update(name = labelName, english = value))
      case _ => super.eval(value, page, labels)
    }
}

case class PassphraseInput(
  override val next: Seq[String],
  name: Phrase,
  help: Option[Phrase],
  label: String, // IGNORED
  placeholder: Option[Phrase],
  stack: Boolean,
  dontRepeatName: Boolean = false,
  width: String = Ten
) extends Input {
  def validInput(value: String): Validation[String] = asTextString(value).fold[Validation[String]](Left(Nil))(v => Right(v))
  override def rendered(expand: Phrase => Phrase): DataInputStanza = copy(name = expand(name), help = help.map(expand), placeholder = placeholder.map(expand))
  override def eval(value: String, page: Page, labels: Labels): (Option[String], Labels) =
    (next.headOption, labels.update(SecuredProcess.PassPhraseResponseLabelName, value)
                            .update(SecuredProcess.EncryptedPassphraseResponseLabelName, labels.encrypt(value)))
}

case class CurrencyInput(
  override val next: Seq[String],
  name: Phrase,
  help: Option[Phrase],
  label: String,
  placeholder: Option[Phrase],
  stack: Boolean,
  dontRepeatName: Boolean = false,
  width: String = Ten
) extends Input {
  def validInput(value: String): Validation[String] = asCurrency(value).fold[Validation[String]](Left(Nil))(v => Right(v.toString))
  override def rendered(expand: Phrase => Phrase): DataInputStanza = copy(name = expand(name), help = help.map(expand), placeholder = placeholder.map(expand))
}

case class CurrencyPoundsOnlyInput(
  override val next: Seq[String],
  name: Phrase,
  help: Option[Phrase],
  label: String,
  placeholder: Option[Phrase],
  stack: Boolean,
  dontRepeatName: Boolean = false,
  width: String = Ten
) extends Input {
  def validInput(value: String): Validation[String] = asCurrencyPounds(value).fold[Validation[String]](Left(Nil))(v => Right(v.toString))
  override def rendered(expand: Phrase => Phrase): DataInputStanza = copy(name = expand(name), help = help.map(expand), placeholder = placeholder.map(expand))
}

case class DateInput(
  override val next: Seq[String],
  name: Phrase,
  help: Option[Phrase],
  label: String,
  placeholder: Option[Phrase],
  stack: Boolean,
  dontRepeatName: Boolean = false,
  width: String = Ten
) extends Input {
  val FieldMsgBase: String = "label"
  val FieldNames: List[String] = List("day", "month", "year")
  def validInput(value: String): Validation[String] = validDate(value).fold(err => Left(err), dte => Right(stringFromDate(dte)))
  override def rendered(expand: Phrase => Phrase): DataInputStanza = copy(name = expand(name), help = help.map(expand), placeholder = placeholder.map(expand))
}

object Input {
  def apply(stanza: InputStanza, name: Phrase, help: Option[Phrase], placeholder: Option[Phrase], dontRepeatName: Boolean = false, width: String = Ten): Input =
    stanza.ipt_type match {
      case Number => NumberInput(stanza.next, name, help, stanza.label, placeholder, stanza.stack, dontRepeatName, width)
      case Txt => TextInput(stanza.next, name, help, stanza.label, placeholder, stanza.stack, dontRepeatName, width)
      case PassphraseText => PassphraseInput(stanza.next, name, help, "", placeholder, stanza.stack, dontRepeatName, width)
      case Currency => CurrencyInput(stanza.next, name, help, stanza.label, placeholder, stanza.stack, dontRepeatName, width)
      case CurrencyPoundsOnly => CurrencyPoundsOnlyInput(stanza.next, name, help, stanza.label, placeholder, stanza.stack, dontRepeatName, width)
      case Date => DateInput(stanza.next, name, help, stanza.label, placeholder, stanza.stack, dontRepeatName, width)
    }
}

private object TaxCodeUtilities {

  private val DefaultValue: String = "BLANK"

  def retrieveTaxCodeComponents(labels: Labels,
                                fullTaxCode: String,
                                label: String): Labels = {
    val Prefix: Int = 2
    val Main: Int = 3
    val Suffix: Int = 4
    val Cumulative: Int = 5

    taxCodePattern findFirstMatchIn fullTaxCode match {
      case Some(matcher) =>
        updateLabels(labels = labels,
          label = label,
          prefix = matcher.group(Prefix),
          main = matcher.group(Main),
          suffix = matcher.group(Suffix),
          cumulative = matcher.group(Cumulative))
      case None =>
        updateLabels(labels = labels,
          label = label,
          prefix = "",
          main = DefaultValue,
          suffix = "",
          cumulative = "")
    }
  }

  private def updateLabels(labels: Labels,
                           label: String,
                           prefix: String,
                           main: String,
                           suffix: String,
                           cumulative: String): Labels = {
    labels
      .update(s"${label}_prefix", Option(prefix).getOrElse(""))
      .update(s"${label}_main", Option(main).getOrElse(DefaultValue))
      .update(s"${label}_suffix", Option(suffix).getOrElse(""))
      .update(s"${label}_cumulative", Option(cumulative).getOrElse(""))
  }
}
