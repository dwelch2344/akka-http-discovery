package com.lightbend.akka.sample.domain.customers

import com.lightbend.akka.sample.domain.{HasId, InMemoryLongIdRepository, Repository}

import scala.concurrent.{ExecutionContext, Future}



case class Customer(var id: Option[Long], name: String) extends HasId[Long]

trait CustomerRepo extends Repository[Customer, Long]{
  // stub methods here

  def getCount(): Integer
}

class DummyCustomerRepo extends InMemoryLongIdRepository[Customer] with CustomerRepo {
  override def getCount(): Integer = this.cache.size
}


class CustomerService(repo: CustomerRepo ){

  implicit val ec = ExecutionContext.global

  def getByDomain(domain: String): Future[Seq[Customer]] = {
    repo.stream().map{ all =>
      all.filter{ c => c.name.toLowerCase.endsWith(s"@${domain.toLowerCase}")}
    }
  }

  def doWork(): Unit ={
    repo.save(new Customer(None, "Blah"))
  }
}
