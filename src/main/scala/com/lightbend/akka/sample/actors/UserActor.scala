package com.lightbend.akka.sample.actors

import akka.actor.{Actor, ActorLogging}
import spray.json.DefaultJsonProtocol._

case class CreateUser(id: Long, name: String)

class UserActor extends Actor with ActorLogging {

  implicit val format2 = jsonFormat2(CreateUser)
  var idCounter = 0;

  def receive = {
    case CreateUser(id, name) =>
      log.info(s"Greeting received (from ${sender()}): create $name with id $id")//        idCounter += 1
      Thread.sleep(1 * 1000)
      idCounter += 1
      sender() ! CreateUser(idCounter, name)

    case any =>
      throw new RuntimeException("Unhandled actor event: " + any)
  }
}