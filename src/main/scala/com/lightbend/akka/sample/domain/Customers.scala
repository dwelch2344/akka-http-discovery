package com.lightbend.akka.sample.domain.customers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.lightbend.akka.sample.domain.{HasId, InMemoryLongIdRepository, Repository}
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContext, Future}
import spray.json.DefaultJsonProtocol._


case class Customer(var id: Option[Long], name: String) extends HasId[Long]
case class Customers(entries: List[Customer])


trait CustomerJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val ser2 = jsonFormat2(Customer)
  implicit val ser1 = jsonFormat1(Customers) // contains List[Item]
}

trait CustomerRepo extends Repository[Customer, Long]{

  // stub methods here

  def getCount(): Integer
}

class DummyCustomerRepo extends InMemoryLongIdRepository[Customer] with CustomerRepo {
  override def getCount(): Integer = this.cache.size
}


class CustomerService(repo: CustomerRepo ){

  implicit val ec = ExecutionContext.global

  def getByDomain(domain: String): Future[Customers] = {
    repo.stream().map{ all => {
        val list = all.entries
          .filter { c => c.name.toLowerCase.endsWith(s"@${domain.toLowerCase}") }
        new Customers(list)
      }
    }
  }

  def getAll(): Future[Customers] = {
    repo.stream().mapTo[Customers]
  }

  def doWork(): Unit ={
    repo.save(new Customer(None, "Blah"))
  }
}
