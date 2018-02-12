package com.lightbend.akka.sample.model

import akka.actor.{Actor, ActorRef, Props}
import com.lightbend.akka.sample._

object Greeter {
  def props(message: String, printerActor: ActorRef): Props = Props(new Greeter(message, printerActor))
  final case class WhoToGreet(who: String)
  case object Greet
}
class Greeter(message: String, printerActor: ActorRef) extends Actor {
  import Greeter._
  import Printer._

  var greeting = ""

  def receive = {
    case WhoToGreet(who) =>
      greeting = s"$message, $who"
    case Greet           =>
      //#greeter-send-message
      printerActor ! Greeting(greeting)
    //#greeter-send-message
  }
}