package com.lightbend.akka.sample.demo.customers

import java.util.UUID

import akka.actor.{Actor, Props}
import com.lightbend.akka.sample.demo.customers.Customers._
import com.lightbend.akka.sample.demo.customers.models.Customer

import scala.concurrent.Future

package object models {
  final case class Customer(id: String, name: String, customerId: String, federationUrl: String, federationInternal: Boolean)
}


object Customers {
  def props(): Props = Props(new Customers())

  final case class CreateCustomer(name: String, customerId: String, federationUrl: String, federationInternal: Boolean)
  final case class CreatedCustomer(result: Customer)

  final case class FindAny(username: String)
  final case class FoundAny(result: Seq[Customer])

  final case class FindDistinct(username: String)
  final case class FoundDistinct(result: Option[Customer])
}

class Customers extends Actor {

  private val repo = List[Customer]()

  def create(name: String, customerId: String, federationUrl: String, federationInternal: Boolean) = {
    val matches = repo.filter{ _.customerId.toLowerCase == customerId.toLowerCase }

    if( matches.lengthCompare(0) > 0 ){
      throw new IllegalStateException("Multiple matches for customerId")
    }

    val result = new Customer( UUID.randomUUID.toString, name, customerId, federationUrl, federationInternal )
    sender() ! Future.successful( CreatedCustomer(result) )
  }

  def findDistinct(customerId: String) = {
    val matches = repo.filter{ _.customerId.toLowerCase == customerId.toLowerCase }
    val result: Option[Customer] = matches match {
      case id :: Nil => Some(id)
      case _ => None
    }

    sender() ! Future.successful(FoundDistinct(result))
  }

  def findAny(customerId: String) = {
    val result = repo.filter{ _.customerId.toLowerCase == customerId.toLowerCase }
    sender() ! Future.successful( FoundAny(result) )
  }

  def receive = {
    case FindAny(username) => findAny(username)
    case FindDistinct(username) => findDistinct(username)
    case CreateCustomer(name, customerId, federationUrl, federationInternal) => create(name, customerId, federationUrl, federationInternal)
  }

}
