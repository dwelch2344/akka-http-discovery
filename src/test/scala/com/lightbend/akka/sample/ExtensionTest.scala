package com.lightbend.akka.sample

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.lightbend.akka.sample.extension.Contextual
import org.scalatest._
import akka.pattern.ask
import akka.util.Timeout
import com.lightbend.akka.sample.util.mdc.MDCPropagatingExecutionContext
import org.slf4j.MDC

import scala.concurrent.duration.{Deadline, Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.Try

class ExtensionTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {




  "Extension" can {

    "can Stuff" should {

      "haz happy" in {


        implicit val timeout = Timeout(15 seconds)
        val echo = system.actorOf(Props[MyCounterActor])
        val future = echo ? "hello world"


        Await.result(future, timeout.duration)
//        expectMsg("Done")
        println("Yay")

        Thread.sleep(2 * 1000)

      }

      "haz context" in {

        val log = system.log
        val key = "requestID"
        implicit val ec = MDCPropagatingExecutionContext.Implicits.global

        val fn = (name: String, token: String) => Future[Any] {
          MDC.put(key, token)
          Future[Any] {
            val id = MDC.get(key)
            log.info(s" [${name}] = ${id}")
          }
        }

        val f1 = fn("a", "1")
        val f2 = fn("b", "2")

        val result = for {
          r1 <- f1
          r2 <- f2
        } yield(r1, r2)

        println( Await.result(result, 10.seconds))

      }
    }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


}



class MyCounterActor extends Actor with ActorLogging with Contextual {
  implicit val ec = MDCPropagatingExecutionContext.Implicits.global

  def receive = {
    case _ => {
      log.info("Message received")




      val all = trace("overarching")
      Thread.sleep(1000)




      val span1 = tracer.newChild(all.context())
      span1.name("short-process")
      span1.start()
      all.annotate("Starting short")
      Thread.sleep(1000)
      all.annotate("ending short")
      span1.finish()


      val span2 = tracer.newChild(all.context())
      span2.name("long-process")
      span2.start()
      all.annotate("starting long")
      Thread.sleep(3 * 1000)
      all.annotate("ending long")
      span2.finish()


      all.finish()

      flush()


      log.info("Message Finished")

      sender ! "Done"
    }
  }


  def delay(dur:Deadline) = {
    Try(Await.ready(Promise().future, dur.timeLeft))
  }
}