package com.lightbend.akka.sample

import java.util.concurrent.{Executors, ThreadPoolExecutor, TimeUnit}

import com.lightbend.akka.sample.domain.customers.{Customer, CustomerRepo, CustomerService, Customers}
import org.scalamock.scalatest.{MixedMockFactory, MockFactory}
import org.scalatest._

import scala.concurrent.ExecutionContextExecutor

//import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.DynamicVariable

import scala.concurrent.duration._

class ThreadLocalTest extends WordSpec with MockFactory with MixedMockFactory {

//
  "Domain" can {

    "can Stuff" should {

      "dummy" in {




//        val delegate = scala.concurrent.ExecutionContext.global
        val tp = Executors.newFixedThreadPool(5)
        val delegate = scala.concurrent.ExecutionContext.fromExecutorService(
          tp
        )

        implicit val ec = new SmartExecutorService(delegate)
        LocalContext.set("blah")

        val worker = () => Future[Any] {
          Thread.sleep(2 * 1000)
          val something = LocalContext.get()
          println(s"Got something ${something} in thread ${Thread.currentThread().getId}")
          something
        }

        Await.result(worker(), 3.seconds)
        Await.result(worker(), 3.seconds)
        Await.result(worker(), 3.seconds)

        LocalContext.set("blah2")
        Await.result(worker(), 3.seconds)
        Await.result(worker(), 3.seconds)
        Await.result(worker(), 3.seconds)

//        tp.awaitTermination(1, TimeUnit.MINUTES)

        println(s"Got final result")
      }
    }
  }
}

class SmartExecutorService(delegate: ExecutionContextExecutor) extends ExecutionContextExecutor {
  override def reportFailure(cause: Throwable): Unit = {
    println(cause)
  }

  override def execute(task: Runnable) {

    val value = LocalContext.get()
    delegate.execute(new Runnable {
      override def run = {
//        LocalContext.set(value)
        task.run
      }
    })
  }
}


object LocalContext {

  private var value:Any = null

  def set(value:Any) = {
    this.value = value
  }
  def get() = value

//  private val store = new DynamicVariable[Any]
//  def set(value: Any) = {
//    store.value = value
//  }
//
//  def get() = store.value
}
