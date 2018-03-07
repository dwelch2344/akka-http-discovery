//#full-example
package com.lightbend.akka.sample

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.google.inject.Guice
import com.lightbend.akka.sample.actors._
import com.lightbend.akka.sample.config.{FooModule, FooService}
import com.lightbend.akka.sample.discovery.Discovery
import com.lightbend.akka.sample.routes.Routes

import scala.concurrent.Await
import scala.io.StdIn

import scala.concurrent.duration._


object AkkaQuickstart extends App {
  new Webserver()
    .start(args)
  //      .guice()
}

class Webserver {

  def start(args: Array[String]) {
    implicit val system: ActorSystem = ActorSystem("core")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher


    val d = new Discovery()

    val routes = new Routes().produce(d)
    val host = d.getHost()
    val port = d.getPort()
    val service = d.getService()
    val bindingFuture = Http().bindAndHandle(routes, host, port)
    println(s"Server online at http://${host}:${port}/ and registered under ${service.getName}")

    try {
      d.register();
      println(s"Registered... Press Enter to continue")

      StdIn.readLine() // let it run until user presses return
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
    }finally{
      d.deregister()
      d.shutdown()
      Await.result(system.terminate(), 3.seconds )
    }
    Console.println("End")


  }

  def guice(): Unit ={
    val injector = Guice.createInjector(new FooModule)
    val foo = injector.getInstance(classOf[FooService])
    println(foo.doSomething())
  }


  def playground(args: Array[String]) {
    implicit val system: ActorSystem = ActorSystem("core")
    // TODO research what this does? Something to do with streams
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val printerActor: ActorRef = system.actorOf(Printer.props, "printerActor")
    val greeterActor: ActorRef = system.actorOf(Greeter.props("Howdy", printerActor), "howdyGreeter")

    // for demo
    import com.lightbend.akka.sample.actors.Greeter._
    greeterActor ! WhoToGreet("World!")
    greeterActor ! Greet

    val d = new Discovery()

    val routes = new Routes().produce(d)


    val host = d.getHost()
    val port = d.getPort()
    val bindingFuture = Http().bindAndHandle(routes, host, port)
    println(s"Server online at http://${host}:${port}/")

    try {
      d.register();
      println(s"Registered... Press Enter to continue")

      StdIn.readLine() // let it run until user presses return
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
    }finally{
      d.deregister()
    }
    Console.println("End")
  }


}
