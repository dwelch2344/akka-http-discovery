package com.lightbend.akka.sample.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.lightbend.akka.sample.actors.CreateUser
import com.lightbend.akka.sample.actors.Printer._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Success, Try}


class UserRoute(
  printer: ActorRef,
  users: ActorRef,
) extends RouteProvider {

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

          implicit val timeout: Timeout = 6.seconds
          val foo: Future[CreateUser] = (users ? CreateUser(1, "Blah")).mapTo[CreateUser]

          onComplete(foo) {
            case success : Success[CreateUser] => {
              val item = success.get
              complete( (StatusCodes.Accepted, item.toString ) )
            }
            case t : Try[CreateUser] => complete( (StatusCodes.Accepted, "Error: " + t.toString()) )
          }
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

