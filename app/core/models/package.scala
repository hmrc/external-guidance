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

package core

import core.models.errors.Error
import scala.annotation.tailrec

package object models {

  type RequestOutcome[T] = Either[Error, T]

  def escapeDollarSymbol(s: String): String = s.replaceAllLiterally("$", "\\$")
  def unescapeDollarSymbol(s: String): String = s.replaceAllLiterally("\\$", "$")

   // List[Option[T]] => Option[List[T]] iff all Option[T] defined
  def lOfOtoOofL[T](l:List[Option[T]]): Option[List[T]] = {
    @tailrec
    def toOptionOfList(l:List[Option[T]], acc: List[T]): Option[List[T]] = l match {
      case Nil => Some(acc.reverse)
      case None :: _ => None
      case x :: xs => toOptionOfList(xs, x.get :: acc)
    }

    l match {
      case Nil => None
      case _ => toOptionOfList(l, Nil)
    }
  }
}
