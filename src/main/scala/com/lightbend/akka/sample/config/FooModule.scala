package com.lightbend.akka.sample.config

import javax.inject.Inject

import akka.actor.ActorSystem
import com.google.inject.AbstractModule

case class MyDoSomething(theThing : String)

trait MyService {
  def doSomething(): String
}

class MyServiceImpl extends MyService {
  override def doSomething(): String = {
    System.out.println("Something has been done!")
    "Got a message"
  }
}

trait FooService{
  def doSomething(): String
}

class FooServiceImpl @Inject() ( svc : MyService ) extends FooService {
  override def doSomething(): String = {
    System.out.println("About to delegate")
    svc.doSomething()
  }
}

class FooModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ActorSystem]).toInstance(ActorSystem.apply)
    bind(classOf[MyService]).toInstance(new MyServiceImpl())
    bind(classOf[FooService]).to(classOf[FooServiceImpl])
  }
}