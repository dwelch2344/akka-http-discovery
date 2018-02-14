package com.lightbend.akka.sample.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.duration._


class ConfigRoute()  extends RouteProvider{
  val route: Route =
    path("auction") {
      put {
        parameter("bid".as[Int], "user") { (bid, user) =>
          // place a bid, fire-and-forget
//          auction ! Bid(user, bid)
          complete((StatusCodes.Accepted, "bid placed"))
        }
      } ~
        get {
          implicit val timeout: Timeout = 5.seconds

          // query the actor for the current auction state
//          val bids: Future[Bids] = (auction ? GetBids).mapTo[Bids]
//          complete(bids)
          complete((StatusCodes.Accepted, "Something happened"))
        }
    }
}

