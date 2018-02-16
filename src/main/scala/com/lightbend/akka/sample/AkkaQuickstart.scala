//#full-example
package com.lightbend.akka.sample

import java.net.InetAddress

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.google.inject.Guice
import com.lightbend.akka.sample.actors._
import com.lightbend.akka.sample.config.{FooModule, FooService}
import com.lightbend.akka.sample.discovery.{Discovery, DiscoveryThing}
import com.lightbend.akka.sample.routes.Routes
import org.springframework.cloud.commons.util.{InetUtils, InetUtilsProperties}
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties

import scala.io.StdIn


class Webserver {

  def start(args: Array[String]) {
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

    val routes = new Routes().produce()

    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

    Console.println("End")
  }

  def guice(): Unit ={
    val injector = Guice.createInjector(new FooModule)
    val foo = injector.getInstance(classOf[FooService])
    println(foo.doSomething())
  }

  def discovery(): Unit ={

    val d = new Discovery()

    d.register();
    println(s"Registered... Press Enter to continue")
    StdIn.readLine() // let it run until user presses return
    d.shutdown()
    println(s"Shutting down")
  }
}

object AkkaQuickstart extends App {
  new Webserver()
//    .start(args)
//      .guice()
      .discovery()

  println(s"Exiting")
}
