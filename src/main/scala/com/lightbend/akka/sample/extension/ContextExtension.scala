package com.lightbend.akka.sample.extension

import akka.actor.{Actor, ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import brave._
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.okhttp3.OkHttpSender


trait Contextual { self : Actor =>
  val tracer = ContextExtension(context.system).tracer
  def trace(name: String) = ContextExtension(context.system).tracer.newTrace().name(name).start()
  def flush() = {
    ContextExtensionImpl.spanReporter.flush()
  }
}

class ContextExtensionImpl extends Extension {
  // Tracing exposes objects you might need, most importantly the tracer
  val tracer = ContextExtensionImpl.tracer
}

object ContextExtensionImpl {

  val sender = OkHttpSender.create("http://127.0.0.1:9411/api/v2/spans")
  val spanReporter = AsyncReporter.builder(sender).queuedMaxSpans(1).build

  // Create a tracing component with the service name you want to see in Zipkin.
  val tracing = Tracing.newBuilder.localServiceName("my-service").spanReporter(spanReporter).build

  // Tracing exposes objects you might need, most importantly the tracer
  val tracer = tracing.tracer
}




object ContextExtension
  extends ExtensionId[ContextExtensionImpl]
    with ExtensionIdProvider {

  //The lookup method is required by ExtensionIdProvider,
  // so we return ourselves here, this allows us
  // to configure our extension to be loaded when
  // the ActorSystem starts up
  override def lookup = ContextExtension

  //This method will be called by Akka
  // to instantiate our Extension
  override def createExtension(system: ExtendedActorSystem) = new ContextExtensionImpl

  /**
    * Java API: retrieve the Count extension for the given system.
    */
  override def get(system: ActorSystem): ContextExtensionImpl = super.get(system)
}