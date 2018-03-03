package com.lightbend.akka.sample

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.lightbend.akka.sample.extension.Contextual
import org.scalatest._
import akka.pattern.ask
import akka.util.Timeout

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


      }
    }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


}



class MyCounterActor extends Actor with ActorLogging with Contextual {
  implicit val ec = ExecutionContext.global

  def receive = {
    case _ => {
      log.info("Message received")


      val t = tracer()




      val all = trace("overarching")
      Thread.sleep(1000)




      val span1 = t.newChild(all.context())
      span1.name("short-process")
      span1.start()
      all.annotate("Starting short")
      Thread.sleep(1000)
      all.annotate("ending short")
      span1.finish()


      val span2 = t.newChild(all.context())
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