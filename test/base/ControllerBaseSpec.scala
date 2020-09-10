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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.Injector
import play.api.mvc.BodyParsers
import play.api.{Configuration, Environment}

trait ControllerBaseSpec extends BaseSpec with GuiceOneAppPerSuite {

  val credential: String = "7010010"
  val name: String = "George Hudson"
  val email: String = "ghudson@emailworld.com"

  lazy val injector: Injector = app.injector

  lazy val config = app.injector.instanceOf[Configuration]
  lazy val env = app.injector.instanceOf[Environment]
  lazy val bodyParser = app.injector.instanceOf[BodyParsers.Default]
}