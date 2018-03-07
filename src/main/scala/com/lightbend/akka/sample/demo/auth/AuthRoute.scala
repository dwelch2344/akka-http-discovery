package com.lightbend.akka.sample.demo.auth

import java.net.URI

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.google.common.collect.{ImmutableMap, Maps}
import com.lightbend.akka.sample.demo.identity.SearchMatch
import com.lightbend.akka.sample.discovery.Discovery
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.springframework.http.HttpMethod

class AuthRoute(discovery: Discovery){

  val rt = discovery.rest

  val route: Route =
    pathPrefix("auth" / "config" / "searches" ) {
      pathEndOrSingleSlash {
        get {
          parameters('username ? "", 'customerId ? "" ) { (username, customerId) =>
            val url = s"http://app1/identity/searches/byUsername?username=${username}"
            val obj = discovery.query(url, HttpMethod.GET, classOf[SearchMatch])
            complete((StatusCodes.OK, obj))
          }
        }
      }
    }
}
