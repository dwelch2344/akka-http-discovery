//#full-example
package com.lightbend.akka.sample

import akka.actor.{ActorRef, ActorSystem}
import com.lightbend.akka.sample.actors._

object AkkaQuickstart extends App {

  val system: ActorSystem = ActorSystem("core")
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")
  val greeter: ActorRef = system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")

  // for demo
  import com.lightbend.akka.sample.actors.Greeter._
  greeter ! WhoToGreet("World!")
  greeter ! Greet
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