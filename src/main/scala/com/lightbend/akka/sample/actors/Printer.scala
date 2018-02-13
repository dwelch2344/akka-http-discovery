package com.lightbend.akka.sample.actors

import akka.actor.{Actor, ActorLogging, Props}

object Printer {
  def props: Props = Props[Printer]
  final case class Greeting(greeting: String)
  final case class Message(message: String)

}




class Printer extends Actor with ActorLogging {
  import Printer._

  def receive = {
    case Greeting(greeting) =>
      log.info(s"Greeting received (from ${sender()}): $greeting")
    case Message(message) =>
      log.info(s"Message received (from ${sender()}): $message")


    case any =>
      throw new RuntimeException("Unhandled actor event: " + any)
  }
}