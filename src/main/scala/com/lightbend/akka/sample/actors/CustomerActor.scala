package com.lightbend.akka.sample.actors

import akka.actor.{Actor, ActorLogging, Props}





case class Customer(id: Long, name: String)
case class CreateCustomer(name: String)
case class GetCustomers()


object CustomerActor {
  def props(id: Integer): Props = Props(new CustomerActor(id))
}

class CustomerActor(id: Integer) extends Actor with ActorLogging{

  override def receive: Receive = {
    case CreateCustomer(name) =>

  }
}
