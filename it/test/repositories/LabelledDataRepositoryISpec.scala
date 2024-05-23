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

package repositories

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, Injecting}
import data.ExampleLabelledData
import java.time.Instant
import models._

class LabelledDataRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with FutureAwaits
    with DefaultAwaitTimeout
    with GuiceOneAppPerSuite
    with BeforeAndAfterAll
    with Injecting {

  val now: Instant = Instant.ofEpochSecond(Instant.now().toEpochMilli() / 1000L, 0)
  val credId: String = "12345566"
  val user: String = "Someone"
  val email: String = s"$user@blah.com"
  val rates: LabelledData = LabelledData(Rates, ExampleLabelledData.rates, now, credId, user, email)
  val timescales: LabelledData = LabelledData(Timescales, ExampleLabelledData.timescales, now, credId, user, email)
  lazy val repository: LabelledDataRepositoryImpl = inject[LabelledDataRepositoryImpl]

  override def beforeAll(): Unit = {
    super.beforeAll()
    await(repository.save(rates.id, rates.data, rates.when, rates.credId, rates.user, rates.email))
    await(repository.save(timescales.id, timescales.data, timescales.when, timescales.credId, timescales.user, timescales.email))
  }

  "LabelledDataRepository" when {
    "Rates and Timescales records exist" when {

      "get Rates" must {
        "return the valid Rates record" in {
          await(repository.get(Rates)).fold(_ => fail(), result => {
            result.credId shouldBe credId
            result.user shouldBe user
            result.email shouldBe email
            result.when shouldBe now
          })
        }
      }

      "get Timescales" must {
        "return the valid Timescales record" in {
          await(repository.get(Timescales)).fold(_ => fail(), result => {
            result.credId shouldBe credId
            result.user shouldBe user
            result.email shouldBe email
            result.when shouldBe now
          })
        }
      }

      "save Timescales" must {
        "save an updated Timescales record" in {
          val updatedTimescales = timescales.copy(when = now.plusSeconds(30L))

          await(repository.save(Timescales, updatedTimescales.data, updatedTimescales.when, updatedTimescales.credId, updatedTimescales.user, updatedTimescales.email))
          await(repository.get(Timescales)) shouldBe Right(updatedTimescales)
        }
      }

      "save Rates" must {
        "save an updated Rates record" in {
          val updatedRates = rates.copy(when = now.plusSeconds(30L))

          await(repository.save(Rates, updatedRates.data, updatedRates.when, updatedRates.credId, updatedRates.user, updatedRates.email))
          await(repository.get(Rates)) shouldBe Right(updatedRates)
        }
      }
    }

  }
}
