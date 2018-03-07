package com.lightbend.akka.sample.demo.auth

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.lightbend.akka.sample.api.ExampleApi
import com.lightbend.akka.sample.discovery.Discovery
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

class AuthRoute(discovery: Discovery){

  val api = new ExampleApi(discovery)

  val route: Route =
    pathPrefix("auth" / "config" / "searches" ) {
      pathEndOrSingleSlash {
        get {
          parameters('username ? "", 'customerId.? ) { (username, customerId) =>
            val result = api.findDistinctIdentity(username, customerId)
            result.result match {
              case Some(id) =>
                api.authenticate(id.id, "blah")
                complete((StatusCodes.OK, result))
              case None => complete((StatusCodes.OK, result))
            }
          }
        }
      }
    }
}