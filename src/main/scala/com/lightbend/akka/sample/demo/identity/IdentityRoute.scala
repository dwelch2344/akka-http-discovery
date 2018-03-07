package com.lightbend.akka.sample.demo.identity

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.lightbend.akka.sample.demo.identity.Identifier.{FindDistinct, FoundDistinct}
import com.lightbend.akka.sample.demo.identity.models.Identity

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

case class SearchMatch(result: Option[Identity])

class IdentityRoute(identifier: ActorRef){

  val route: Route =
    pathPrefix("identity" / "searches" / "byUsername" ) {
      pathEndOrSingleSlash {
        get {
          parameters('username) { (username) =>
            implicit val timeout: Timeout = 6.seconds
            val f = (identifier ? FindDistinct(username)).asInstanceOf[Future[Option[FoundDistinct]]]
            onComplete(f) {
              case Success(Some(found)) => complete((StatusCodes.OK, SearchMatch(Some(found.result)) ))
              case Success(None) => complete((StatusCodes.OK, SearchMatch(None))) // No need to unwrap, but for funzies we will
              case Failure(ex) => complete((StatusCodes.InternalServerError, s"Error: ${ex.getMessage}"))
            }
          }
        }
      }
    }
}
