package com.lightbend.akka.sample.util


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, pathEndOrSingleSlash, pathPrefix}
import akka.stream.ActorMaterializer

import scala.concurrent.Future


class EchoServer(port: Integer) {

  var server: Future[ServerBinding] = null
  var hits = 0


  implicit val system: ActorSystem = ActorSystem("echoServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher


  def start(): Unit ={
    server = Http().bindAndHandle(route, "localhost", port)
    println(s"Echo server started on port ${port}")
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
          println(s"Echo Server ${port} triggered")
          complete( (StatusCodes.OK, "Ok"))
        }
      }
    }

}
