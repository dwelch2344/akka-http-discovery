package com.lightbend.akka.sample.actors

import akka.actor.{Actor, ActorLogging, Props}

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.util.Try

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