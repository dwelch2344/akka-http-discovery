package com.lightbend.akka.sample.discovery

import java.util

import com.ecwid.consul.v1.ConsulClient
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties
import org.springframework.cloud.consul.discovery.{ConsulDiscoveryClient, ConsulDiscoveryProperties, HeartbeatProperties, TtlScheduler}
import org.springframework.cloud.consul.serviceregistry.{ConsulAutoRegistration, ConsulRegistrationCustomizer, ConsulServiceRegistry}
import org.springframework.context.support.GenericApplicationContext

import scala.collection.{JavaConverters, mutable}


case class MyResolver(instanceId: String, port :Integer) extends ConsulDiscoveryClient.LocalResolver{
  override def getInstanceId: String = instanceId
  override def getPort: Integer = port
}

class DiscoveryThing(
    consulHost :String,
    dp : ConsulDiscoveryProperties
  ) {

    val client :ConsulClient = new ConsulClient("127.0.0.1")

    val heartbeat = new HeartbeatProperties
    heartbeat.setTtlValue(10)
    heartbeat.setEnabled(true)

    val ttl = new TtlScheduler(heartbeat, client)


    val registry = new ConsulServiceRegistry(client, dp, ttl, heartbeat)

    val appCtx = new GenericApplicationContext
    appCtx.refresh()

    val reg = ConsulAutoRegistration.registration(
      new AutoServiceRegistrationProperties,
      dp,
      appCtx,
      new util.ArrayList[ConsulRegistrationCustomizer](),
      heartbeat
    )

    val discovery = new ConsulDiscoveryClient(client, dp, MyResolver(reg.getHost, reg.getPort))


  def register(): Unit ={
    registry.register(reg)
  }

  def deregister(): Unit ={
    registry.deregister(reg)
  }

  def getAllInstances : mutable.Buffer[ServiceInstance] = {
    JavaConverters.asScalaBuffer(discovery.getAllInstances)
  }

  def foo(): Unit ={

  }

}
