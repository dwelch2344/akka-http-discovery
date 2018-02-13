package com.lightbend.akka.sample

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.lightbend.akka.sample.actors.{Printer, UserActor}
import com.lightbend.akka.sample.routes.{ConfigRoute, UserRoute}

trait RouteProvider {
  def route : Route
}

class Routes(implicit system: ActorSystem) {

  val printer = system.actorOf(Printer.props)

  def produce(): Route = {
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