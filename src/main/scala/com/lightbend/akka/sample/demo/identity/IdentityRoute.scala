package com.lightbend.akka.sample.demo.identity

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.lightbend.akka.sample.demo.identity.Identifier.{Authenticate, FindDistinct, FoundDistinct, AuthenticationResult}
import com.lightbend.akka.sample.demo.identity.models.Identity
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class SearchMatch(result: Option[Identity])

class IdentityRoute(identifier: ActorRef){

  val route: Route =
    pathPrefix( "identities" ) {
      pathPrefix( "[a-zA-Z0-9\\-]+".r ) { id =>
        path( "auth" ) {
          get {
            complete((StatusCodes.NotImplemented, "Not Implemented!!"))
          } ~
          post {
            implicit val timeout: Timeout = 6.seconds
            val f = (identifier ? Authenticate(id, "leetpass")).asInstanceOf[Future[AuthenticationResult]]
            onComplete(f) {
              case Success(result) => result.success match {
                case true => complete((StatusCodes.OK, "Success"))
                case false => complete((StatusCodes.Unauthorized, "Failed"))
              }
              case Failure(ex) => complete((StatusCodes.InternalServerError, s"Error: ${ex.getMessage}"))
            }
          }
        } ~
        pathEndOrSingleSlash {
          complete( (StatusCodes.OK, "Not Implemented") )
        }
      } ~
      pathPrefix( "searches" / "byUsername" ) {
        pathEndOrSingleSlash {
          get {
            parameters('username, 'customerId.?) { (username, customerId) =>
              implicit val timeout: Timeout = 6.seconds
              val f = (identifier ? FindDistinct(username, customerId)).asInstanceOf[Future[Option[FoundDistinct]]]
              onComplete(f) {
                case Success(Some(found)) => complete((StatusCodes.OK, SearchMatch(Some(found.result))))
                case Success(None) => complete((StatusCodes.OK, SearchMatch(None))) // No need to unwrap, but for funzies we will
                case Failure(ex) => complete((StatusCodes.InternalServerError, s"Error: ${ex.getMessage}"))
              }
            }
          }
        }
      }
    }
}
