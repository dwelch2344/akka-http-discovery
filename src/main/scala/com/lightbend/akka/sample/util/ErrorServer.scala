package com.lightbend.akka.sample.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, pathEndOrSingleSlash, pathPrefix}
import akka.stream.ActorMaterializer

import scala.concurrent.Future


class ErrorServer(port: Integer) {

  var server: Future[ServerBinding] = null
  var hits = 0

  implicit val system: ActorSystem = ActorSystem("errorServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher


  def start(): Unit ={
    server = Http().bindAndHandle(route, "localhost", port)
    println(s"Error server started on port ${port}")
  }

  def stop(): Unit ={
    server
      .flatMap(_.unbind()) // trigger unbinding from the port
    //    .onComplete(_ => system.terminate()) // and shutdown when done
  }

  val route =
    pathPrefix("") {
      pathEndOrSingleSlash {
        get {
          hits += 1
          Console.err.println(s"Error Server ${port} triggered")
          complete( (StatusCodes.BadGateway, "Forced Bad Gateway For Test"))
        }
      }
    }

}
