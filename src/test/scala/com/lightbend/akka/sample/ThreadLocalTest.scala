package com.lightbend.akka.sample

import com.lightbend.akka.sample.domain.customers.{Customer, CustomerRepo, CustomerService, Customers}
import org.scalamock.scalatest.{MixedMockFactory, MockFactory}
import org.scalatest._

//import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.DynamicVariable

import scala.concurrent.duration._

class ThreadLocalTest extends WordSpec with MockFactory with MixedMockFactory {

//
  "Domain" can {

    "can Stuff" should {

      "dummy" in {
        implicit val ec = scala.concurrent.ExecutionContext.fromExecutorService(
          new ForkJoinPoolWithDynamicVariable
        )

        ForkJoinPoolWithDynamicVariable.set("blah")

        val finalResult = Await.result(Future[Any] {
          val something = ForkJoinPoolWithDynamicVariable.get()
          println(s"Got something ${something}")
          something
        }, 3.seconds)

        println(s"Got final result ${finalResult}")
      }
    }
  }


}


import scala.concurrent.forkjoin._

class ForkJoinPoolWithDynamicVariable
  extends ForkJoinPool {

  override def execute(task: Runnable) {

    val value = ForkJoinPoolWithDynamicVariable.get()
    super.execute(new Runnable {
      override def run = {
        ForkJoinPoolWithDynamicVariable.set(value)
        task.run
      }
    })
  }
}

object ForkJoinPoolWithDynamicVariable {
  private val store = new DynamicVariable[Any]
  def set(value: Any) = {
    store.value = value
  }

  def get() = store.value
}
