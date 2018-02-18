package com.lightbend.akka.sample

import com.lightbend.akka.sample.domain.customers.{Customer, CustomerRepo, DummyCustomerThing}
import org.scalamock.proxy.ProxyMockFactory
import org.scalamock.scalatest.{AbstractMockFactory, MockFactory}
import org.scalatest._

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

      "haz happy" in {

        val repo = mock[CustomerRepo]

//        (repo.getCount _).expects().returns(1)
//        (repo.save _).expects().returns(1)

        val dummy = new DummyCustomerThing(repo)



        (repo.getCount _).expects().returning(1)
        (repo.save _).expects(Customer(None, "Blah"))

        dummy.doWork()

        val count = repo.getCount()
        assert(count == 1, "Count of items was wrong")


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
