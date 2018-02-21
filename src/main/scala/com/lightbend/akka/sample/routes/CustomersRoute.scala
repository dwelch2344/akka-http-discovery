package com.lightbend.akka.sample.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.lightbend.akka.sample.domain.customers._


class CustomersRoute(
//  printer: ActorRef,
//  users: ActorRef,
  customers: CustomerService
) extends RouteProvider  with Directives with CustomerJsonSupport {


  val route: Route =
    pathPrefix("customers") {
      pathEndOrSingleSlash {
        get {
          onSuccess(customers.getAll()) { c =>
            complete((StatusCodes.OK, c))
          }
          //      } ~
          //      post {
          //          parameter("bid".as[Int], "user") { (bid, user) =>
          //            complete((StatusCodes.Accepted, "create a user"))
          //          }
          //        }
          //      } ~
          //      path(IntNumber) { id =>
          //        complete( (StatusCodes.OK, "Got the number " + id + " and added 10 for " + (id + 10)))
        } ~
          path("/searches/") {
            path("byDomain") {
              get {
                parameters('domain, 'limit.as[Int]) { (domain, _) =>
                  onSuccess(customers.getByDomain(domain)) { c =>
                    complete((StatusCodes.OK, c ))
                  }
                }
              }
            }
          }
      }
    }
}

