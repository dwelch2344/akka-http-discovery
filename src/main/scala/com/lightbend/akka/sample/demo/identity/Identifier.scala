package com.lightbend.akka.sample.demo.identity

import java.util.UUID

import akka.actor.{Actor, Props}
import com.lightbend.akka.sample.demo.identity.Identifier._
import com.lightbend.akka.sample.demo.identity.models.Identity

import scala.concurrent.Future

package object models {
  final case class Identity(id: String, name: String, username: String, customerId: String, password: String)
}


object Identifier {
  def props(): Props = Props(new Identifier())

  final case class CreateIdentity(name: String, username: String, customerId: String, password: String)
  final case class CreatedIdentity(result: Identity)

  final case class FindAny(username: String)
  final case class FoundAny(result: Seq[Identity])

  final case class FindDistinct(username: String, customerId: Option[String])
  final case class FoundDistinct(result: Identity)

  final case class Authenticate(id: String, password: String)
  final case class AuthenticationResult(success: Boolean)
}

class Identifier extends Actor {

  private val repo = List[Identity](
    Identity("1", "Dave Welch", "dave.welch", "abc123", "leetpass")
  )

  def create(name: String, username: String, customerId: String, password: String) = {
    val matches = repo.filter{ _.username.toLowerCase == username.toLowerCase }
                      .filter{ _.customerId.toLowerCase == customerId.toLowerCase }

    if( matches.lengthCompare(0) > 0 ){
      throw new IllegalStateException("Multiple matches for customerId and userId")
    }

    val result = new Identity( UUID.randomUUID.toString, name, username, customerId, password )
    sender() ! Future.successful( CreatedIdentity(result) )
  }

  def findDistinct(username: String, customerId: Option[String]) = {
    val matches = repo.filter{ u =>
      u.username.toLowerCase == username.toLowerCase && (
        customerId.isEmpty || customerId.get.equals(u.customerId)
      )
    }
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

  def authenticate(id: String, password: String) = {
    val matches = repo.filter{ u => u.id == id }
    val result: Boolean = matches match {
      case identity :: Nil => identity.password.equals(password)
      case results if( results.lengthCompare(1) > 0) => throw new RuntimeException(s"Multiple user IDs for ${id}, panic!")
      case _ => false
    }

    sender() ! AuthenticationResult(result)
  }

  def receive = {
    case FindAny(username) => findAny(username)
    case FindDistinct(username, customerId) => findDistinct(username, customerId)
    case CreateIdentity(name, username, customerId, password) => create(name, username, customerId, password)
    case Authenticate(id, password) => authenticate(id, password)
  }

}
