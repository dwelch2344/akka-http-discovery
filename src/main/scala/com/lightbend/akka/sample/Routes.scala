package com.lightbend.akka.sample

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.lightbend.akka.sample.actors.Printer
import com.lightbend.akka.sample.actors.Printer.Greeting
import com.lightbend.akka.sample.routes.{ConfigRoute, UserRoute}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn


trait RouteProvider {
  def route : Route
}

class Routes(
  implicit system: ActorSystem,
  materializer: ActorMaterializer,
  executionContext: ExecutionContextExecutor
) {

  val printer = system.actorOf(Printer.props)
//  printer ! Greeting("dummy logic!")


  def bind() {
    val u = new UserRoute(printer)
    val c = new ConfigRoute()
    val http = Http()

    import akka.http.scaladsl.server.Directives._

    val bindingFuture = http.bindAndHandle(u.route ~ c.route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }



}


//package com.lightbend.akka.sample
//
//import akka.actor.{ActorRef, ActorSystem}
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.server.Route
//import akka.stream.ActorMaterializer
//import com.lightbend.akka.sample.actors.Printer
//import com.lightbend.akka.sample.actors.Printer.Greeting
//import com.lightbend.akka.sample.routes.{ConfigRoute, UserRoute}
//
//import scala.concurrent.ExecutionContextExecutor
//import scala.io.StdIn
//
//trait RouteProvider {
//  def route : Route
//}
//
//class Routes(
//              implicit system: ActorSystem,
//              printer: ActorRef
//            ) {
//
//  //  val printer = system.actorOf(Printer.props)
//  //  printer ! Greeting("dummy logic!")
//
//
//  def bind() {
//    val u = new UserRoute()
//    val c = new ConfigRoute()
//    val http = Http()
//
//    import akka.http.scaladsl.server.Directives._
//    val routes = u.route ~ c.route
//
//    val bindingFuture = http.bindAndHandle(routes, "localhost", 8080)
//    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
//    StdIn.readLine() // let it run until user presses return
//    bindingFuture
//      .flatMap(_.unbind()) // trigger unbinding from the port
//      .onComplete(_ => system.terminate()) // and shutdown when done
//  }
//
//
//
//}
