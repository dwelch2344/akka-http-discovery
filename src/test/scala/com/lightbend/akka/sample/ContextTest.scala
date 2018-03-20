package com.lightbend.akka.sample

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.lightbend.akka.sample.extension.Contextual
import org.scalatest._

import scala.concurrent.duration.{Deadline, _}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.server.Directives._

class ContextTest extends WordSpec with Matchers with BeforeAndAfterAll with ScalatestRouteTest {


  val smallRoute =
    get {
      pathSingleSlash {
        val future = Future[String]{
          "Captain on the bridge!"
        }
        onComplete(future) {
          case Success(s) => complete(s"The result is: ${s}")
          case Failure(e) => complete(s"Failed: ${e.getMessage}")
        }



      } ~
        Directives.path("ping") {
          complete("PONG!")
        }
    }


  "Extension" can {

    "can Stuff" should {

      "haz happy" in {

        Get() ~> smallRoute ~> check {
          responseAs[String] shouldEqual "The result is: Captain on the bridge!"
        }

      }
    }
  }

  override def afterAll {

  }


}


class DummyActor extends Actor with ActorLogging with Contextual {
  implicit val ec = ExecutionContext.global

  def receive = {
    case _ => {
      log.info("Message received")
      sender ! "Done"
    }
  }


  def delay(dur:Deadline) = {
    Try(Await.ready(Promise().future, dur.timeLeft))
  }
}