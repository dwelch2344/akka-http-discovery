package com.lightbend.akka.sample.routes

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.lightbend.akka.sample.actors.{Printer, UserActor}
import com.lightbend.akka.sample.demo.auth.AuthRoute
import com.lightbend.akka.sample.demo.identity.{Identifier, IdentityRoute}
import com.lightbend.akka.sample.discovery.Discovery

trait RouteProvider {
  def route : Route
}

class Routes(implicit system: ActorSystem) {

  val printer = system.actorOf(Printer.props)
  val identifier = system.actorOf(Identifier.props)

  def produce(d: Discovery): Route = {

    val auth = new AuthRoute(d).route
    val identity = new IdentityRoute(identifier).route


    // create a default "/internal" namespace for healthchecks or whatever
    val internal: Route =
      pathPrefix("internal") {
        pathEndOrSingleSlash {
          get {
            complete( (StatusCodes.OK, "Ok"))
          }
        }
      }

    // fold all the actor routes together
    Seq[Route](internal, auth, identity)
      .fold(internal) { (a, b) =>
        a ~ b
      }
  }


  def sandbox(): Route = {
    val users = system.actorOf(Props[UserActor], "usersActor")
    val u = new UserRoute(printer, users)
    val c = new ConfigRoute()

    // create a default "/internal" namespace for healthchecks or whatever
    val internal: Route =
      pathPrefix("internal") {
        pathEndOrSingleSlash {
          get {
            complete( (StatusCodes.OK, "Ok"))
          }
        }
      }

    // fold all the actor routes together
    List[RouteProvider](u, c)
      .map{ rp => rp.route }
      .fold(internal) { (a, b) =>
        a ~ b
      }
  }

}