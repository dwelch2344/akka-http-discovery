package com.lightbend.akka.sample.demo.identity

import java.util.UUID

import akka.actor.{Actor, Props}
import com.lightbend.akka.sample.demo.identity.Identifier._
import com.lightbend.akka.sample.demo.identity.models.Identity

import scala.concurrent.Future

package object models {
  final case class Identity(id: String, name: String, username: String, customerId: String)
}


object Identifier {
  def props(): Props = Props(new Identifier())

  final case class CreateIdentity(name: String, username: String, customerId: String)
  final case class CreatedIdentity(result: Identity)

  final case class FindAny(username: String)
  final case class FoundAny(result: Seq[Identity])

  final case class FindDistinct(username: String)
  final case class FoundDistinct(result: Identity)
}

class Identifier extends Actor {

  private val repo = List[Identity](
    Identity("1", "Dave Welch", "dave.welch", "abc123")
  )

  def create(name: String, username: String, customerId: String) = {
    val matches = repo.filter{ _.username.toLowerCase == username.toLowerCase }
                      .filter{ _.customerId.toLowerCase == customerId.toLowerCase }

    if( matches.lengthCompare(0) > 0 ){
      throw new IllegalStateException("Multiple matches for customerId and userId")
    }

    val result = new Identity( UUID.randomUUID.toString, name, username, customerId )
    sender() ! Future.successful( CreatedIdentity(result) )
  }

  def findDistinct(username: String) = {
    val matches = repo.filter{ _.username.toLowerCase == username.toLowerCase }
    val result: Option[FoundDistinct] = matches match {
      case id :: Nil => Some(FoundDistinct(id))
      case _ => None
    }

    sender() ! result
  }

  def findAny(username: String) = {
    val result = repo.filter{ _.username.toLowerCase == username.toLowerCase }
    sender() ! Future.successful( FoundAny(result) )
  }

  def receive = {
    case FindAny(username) => findAny(username)
    case FindDistinct(username) => findDistinct(username)
    case CreateIdentity(name, username, customerId) => create(name, username, customerId)
  }

}
