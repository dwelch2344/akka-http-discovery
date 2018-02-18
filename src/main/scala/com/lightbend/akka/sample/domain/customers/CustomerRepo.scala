package com.lightbend.akka.sample.domain.customers

import com.lightbend.akka.sample.domain.{HasId, InMemoryLongIdRepository, Repository}



case class Customer(var id: Option[Long], name: String) extends HasId[Long]

trait CustomerRepo extends Repository[Customer, Long]{
  // stub methods here

  def getCount(): Integer
}

class DummyCustomerRepo extends InMemoryLongIdRepository[Customer] with CustomerRepo {
  override def getCount(): Integer = this.cache.size
}


class DummyCustomerThing(repo: CustomerRepo ){

  def doWork(): Unit ={
    repo.save(new Customer(None, "Blah"))
  }
}
