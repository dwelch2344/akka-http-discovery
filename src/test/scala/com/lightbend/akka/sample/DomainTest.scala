package com.lightbend.akka.sample

import com.lightbend.akka.sample.domain.customers.{Customer, CustomerRepo, CustomerService}
import org.scalamock.proxy.ProxyMockFactory
import org.scalamock.scalatest.{AbstractMockFactory, MockFactory}
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.reflect.ClassTag


trait MixedMockFactory extends AbstractMockFactory {
  this: Suite =>

  object Proxy extends ProxyMockFactory {
    import org.scalamock.proxy._
    def mock[T: ClassTag]: T with Mock = super.mock[T]
    def stub[T: ClassTag]: T with Stub = super.stub[T]
  }
}

class DomainTest extends WordSpec with MockFactory with MixedMockFactory {
//class DomainTest extends FlatSpec with Matchers with ProxyMockFactory {

//
  "Domain" can {

    "can Stuff" should {

      "dummy" in {
        val repo = mock[CustomerRepo]
        (repo.getCount _).expects().returning(1)
        (repo.save _).expects(Customer(None, "Blah"))


        val dummy = new CustomerService(repo)
        dummy.doWork()
        val count = repo.getCount()
        assert(count == 1, "Count of items was wrong")
      }

      "dummy2" in {
        val repo = mock[CustomerRepo]

        implicit val ec = ExecutionContext.global

        (repo.stream _).expects().returning(Future[Seq[Customer]]{
          Seq[Customer](
            new Customer(None, "jimbo@foobar.com"),
            new Customer(None, "franky@foobar.com"),
            new Customer(None, "foo@bar.com"),
            new Customer(None, "alice@FooBar.com")
          )
        })

        val svc = new CustomerService(repo)
        val matches = Await.result(svc.getByDomain("foobar.com"), 2 seconds)
        assert(matches.size == 3, "Count of matches was wrong")
      }
    }
  }

//  "Case class" should "be mocked" in {
//    val foo = mock[CustomerRepo]
//    foo 'getCount returning (0)
////    (foo.getCount _).expects().returning(5)
////    foo.getCount() should be(5)
//  }



}
