package com.lightbend.akka.sample.actors

import akka.actor.{Actor, ActorLogging, Props}


case class Customer(id: Long, name: String)
case class CreateCustomer(name: String)
case class GetCustomers()


class CustomerActor extends Actor with ActorLogging{

  def props(): Props = Props(new CustomerActor)


  override def receive: Receive = {
    case CreateCustomer(name) =>

  }
}
