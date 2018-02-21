package com.lightbend.akka.sample.domain

import com.lightbend.akka.sample.domain.customers.{Customer, Customers}
import com.lightbend.akka.sample.domain.types.{Loader, Saver, Streamer}

import scala.concurrent.{ExecutionContext, Future}



trait HasId[ID]{
  var id: Option[ID]
}

trait Repository[E <: HasId[ID], ID]{
  def save(e: E): Future[E]
  def load(id: ID): Future[Option[E]]
  def stream(): Future[Customers]
}


// easy infrastructure


package object types {
  type Saver[E] = E => Future[E]
  type Loader[E, ID] = ID => Future[Option[E]]
  type Streamer[E, ID] = () => Future[Customers]
}




class RepositoryImpl[E <: HasId[ID], ID](
  saver: Option[Saver[E]],
  loader: Loader[E, ID],
  streamer:  Streamer[E, ID]
) extends Repository[E, ID] {
//class Repository[E, ID](saver: Saver[E], loader: Loader[E, ID], streamer:  Streamer[E, ID]) {

  def save(e: E): Future[E] = saver.get.apply(e)
  def load(id: ID): Future[Option[E]] = loader.apply(id)
  def stream(): Future[Customers] = streamer.apply

}





class InMemoryLongIdRepository[E <: HasId[Long]] extends Repository[E, Long]{

  val cache : collection.mutable.Map[Long, E] = collection.mutable.Map[Long, E]()
  var id = 0L

  implicit val ec = ExecutionContext.global

  override def save(e: E): Future[E] =
    Future[E] {
      e.id match {
        case None => {
          e.id = Some(id)
          id += 1
        }
      }
      cache.put(e.id.get, e)
      e
    }

  override def load(id: Long): Future[Option[E]] =
    Future[Option[E]] {
      cache.get(id)
    }

  override def stream(): Future[Customers] =
    Future[Customers]{
      val list = cache.toSeq.map{ e => e._2 }.toList.asInstanceOf[List[Customer]]
      new Customers(list)
    }
}