package com.lightbend.akka.sample

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.lightbend.akka.sample.domain.customers._
import com.lightbend.akka.sample.routes.CustomersRoute
import org.scalamock.scalatest.MixedMockFactory
import org.scalatest._

import scala.concurrent.{ExecutionContext, Future}

class WebTest
    extends WordSpec
    with Matchers
    with MixedMockFactory
    with ScalatestRouteTest
    with CustomerJsonSupport {

  implicit val ec = ExecutionContext.global

  val expected = new Customers(
    List[Customer](
      new Customer(None, "jimbo@foobar.com"),
      new Customer(None, "franky@foobar.com"),
      new Customer(None, "foo@bar.com"),
      new Customer(None, "alice@FooBar.com")
    ))
  val repo = mock[CustomerRepo]
  (repo.stream _)
    .expects()
    .returning(Future[Customers] { expected })

  val svc = new CustomerService(repo)
  val route = new CustomersRoute(svc).route

  "can Stuff" should {

    "doWeb" in {

      Get("/customers/") ~> route ~> check {
//        responseAs[String] shouldEqual "Captain on the bridge!"
//        entityAs[String] should === ("blah")
        entityAs[Customers] should ===(expected)
      }

    }
  }

}
