package com.lightbend.akka.sample

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.lightbend.akka.sample.domain.customers.{Customer, DummyCustomerRepo}
import org.scalatest._


class MockTest extends WordSpec with Matchers with ScalatestRouteTest {


  "Domain" can {

    "can Stuff" should {

      "haz happy" in {

        val repo = new DummyCustomerRepo

        repo.save(new Customer(None, "Bill"))
        repo.save(new Customer(None, "Ted"))
        repo.save(new Customer(None, "Rufus"))

        repo.toString
      }
    }
  }

}

