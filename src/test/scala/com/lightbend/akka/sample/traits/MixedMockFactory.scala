package com.lightbend.akka.sample.traits

import org.scalamock.proxy.ProxyMockFactory
import org.scalamock.scalatest.AbstractMockFactory
import org.scalatest.Suite

import scala.reflect.ClassTag

trait MixedMockFactory extends AbstractMockFactory {
  this: Suite =>

  object Proxy extends ProxyMockFactory {
    import org.scalamock.proxy._
    def mock[T: ClassTag]: T with Mock = super.mock[T]
    def stub[T: ClassTag]: T with Stub = super.stub[T]
  }
}