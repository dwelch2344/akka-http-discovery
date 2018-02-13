package com.lightbend.akka.sample.actors

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext, Future, Promise, blocking}
import scala.concurrent.duration._
import scala.util.Try
import akka.pattern.pipe




case class CreateUser(id: Long, name: String)
case class CreatedUser(user: CreateUser)


class UserActor extends Actor with ActorLogging {



  def delay(dur:Deadline) = {
    Try(Await.ready(Promise().future, dur.timeLeft))
  }


  import context.dispatcher

  var idCounter = 0;

  def receive = {
    case CreateUser(id, name) =>
      log.info(s"Greeting received (from ${sender()}): create $name with id $id")//        idCounter += 1
      idCounter += 1
      sender() ! CreateUser(idCounter, name)

    case any =>
      throw new RuntimeException("Unhandled actor event: " + any)
  }
}