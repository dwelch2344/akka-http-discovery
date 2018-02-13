//#full-example
package com.lightbend.akka.sample

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import com.lightbend.akka.sample.actors._


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

    val routes = new Routes()
    routes.bind()

    Console.println("End")
  }
}

object AkkaQuickstart extends App {

  new Webserver().start(args)
}



/*
//#main-class
object AkkaQuickstart extends App {
  import Greeter._

  // Create the 'helloAkka' actor system
  val system: ActorSystem = ActorSystem("helloAkka")

  //#create-actors
  // Create the printer actor
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

  // Create the 'greeter' actors
  val howdyGreeter: ActorRef =
    system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
  val helloGreeter: ActorRef =
    system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
  val goodDayGreeter: ActorRef =
    system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")
  //#create-actors

  //#main-send-messages
  howdyGreeter ! WhoToGreet("Akka")
  howdyGreeter ! Greet

  howdyGreeter ! WhoToGreet("Lightbend")
  howdyGreeter ! Greet

  helloGreeter ! WhoToGreet("Scala")
  helloGreeter ! Greet

  goodDayGreeter ! WhoToGreet("Play")
  goodDayGreeter ! Greet
}
*/