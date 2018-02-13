package com.lightbend.akka.sample.routes

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.lightbend.akka.sample.RouteProvider
import com.lightbend.akka.sample.actors.{CreateUser, CreatedUser, UserActor}
import com.lightbend.akka.sample.actors.Printer._
import scala.concurrent.duration._

import scala.concurrent.Future
import scala.util.{Failure, Success}


class UserRoute(
  printer: ActorRef,
)(implicit system: ActorSystem) extends RouteProvider {

  // TODO clean this up
  // Use these to test an actor receiving something unexpected
  final case class UnexpectedMessage(message: String)
  val useUnexpectedMessage : Boolean = false


  val route: Route =
    pathPrefix("user") {
      pathEndOrSingleSlash {
        get {
          if (useUnexpectedMessage) {
            printer ! UnexpectedMessage("This should throw an error?")
          }

          implicit val materializer = ActorMaterializer()
          implicit val executionContext = system.dispatcher

          val users = system.actorOf(Props[UserActor], "usersActor")

          implicit val timeout: Timeout = 2.seconds
          val foo = (users ? CreateUser(1, "Blah"))

          foo onComplete {
            case Success(foo) => Console.println("Got success" + foo)
            case Failure(t) => Console.println("Got failure" + t)
          }

          complete((StatusCodes.Accepted, "Get All Users"))

//          val blah : Future[Any] = users ? CreateUser(1, "Dave Welch")
//          blah onComplete {
//            case Success(posts) => complete((StatusCodes.Accepted, "Get All Users"))
//            case Failure(t) => Console.println("Failed")
//          }
//
//          complete(blah)
        } ~ post {
          parameter("bid".as[Int], "user") { (bid, user) =>
            printer ! Message("Hello User")
            complete((StatusCodes.Accepted, "create a user"))
          }
        }
      } ~
      path(IntNumber) { id =>
        complete( (StatusCodes.OK, "Got the number " + id + " and added 10 for " + (id + 10)))
      }
    }
}

