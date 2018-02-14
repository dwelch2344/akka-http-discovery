//#full-example
package com.lightbend.akka.sample

import java.net.InetAddress
import java.util
import java.util.Base64

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.ecwid.consul.v1.ConsulClient
import org.scalatest._
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties
import org.springframework.cloud.commons.util.{InetUtils, InetUtilsProperties}
import org.springframework.cloud.consul.discovery.{ConsulDiscoveryClient, ConsulDiscoveryProperties, HeartbeatProperties, TtlScheduler}
import org.springframework.cloud.consul.serviceregistry.{ConsulAutoRegistration, ConsulRegistrationCustomizer, ConsulServiceRegistry}
import org.springframework.context.support.GenericApplicationContext

import com.lightbend.akka.sample.discovery.DiscoveryThing

case class MyResolver(instanceId: String, port :Integer) extends ConsulDiscoveryClient.LocalResolver{
  override def getInstanceId: String = instanceId
  override def getPort: Integer = port
}

class DiscoveryTest extends WordSpec with Matchers with ScalatestRouteTest {

  var client :ConsulClient = new ConsulClient("127.0.0.1")

  override def withFixture(test: NoArgTest): Outcome = {
    // seed data here?
    try test()
    finally {
      // clean up data here
    }
  }


  // Describe a scope for a subject, in this case: "A Set"
  "Consul" can { // All tests within these curly braces are about "A Set"

    "Be Configured" should {
      "register" in {

        val utils = new InetUtils(new InetUtilsProperties){
          override def findFirstNonLoopbackAddress(): InetAddress = {
            return InetAddress.getByName("127.0.0.1")
          }
        }
        val dp = new ConsulDiscoveryProperties(utils)
        dp.setPort(8080)
        dp.setHealthCheckPath("/")
        dp.setRegisterHealthCheck(true)


        val thing = new DiscoveryThing("127.0.0.1", dp)


        try {
          thing.register()
          thing.getAllInstances.foreach { i =>
            println(i.getHost, i.getPort)
            println(i)
          }
        }finally {
          Thread.sleep(120 * 1000)
          thing.deregister()
        }

      }
    }

    /*
    // Can describe nested scopes that "narrow" its outer scopes
    "Consul Client" should { // All tests within these curly braces are about "A Set (when empty)"

      "find a result" in {    // Here, 'it' refers to "A Set (when empty)". The full name
        val res = client.getKVValue("/foobar")
        val raw = res.getValue.getValue
        val decoded = new String(Base64.getDecoder.decode(raw))
        decoded should be ("hello world")
      }

      "get a client" in {
        val utils = new InetUtils(new InetUtilsProperties){
          override def findFirstNonLoopbackAddress(): InetAddress = {
            return InetAddress.getByName("127.0.0.1")
          }
        }
        val dp = new ConsulDiscoveryProperties(utils)
        dp.setPort(8080)
        dp.setHealthCheckPath("/")
        dp.setRegisterHealthCheck(true)

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

        val disc = new ConsulDiscoveryClient(client, dp, MyResolver(reg.getHost, reg.getPort))

        try {
          registry.register(reg)
          disc.getAllInstances.forEach { i =>
            println(i.getHost, i.getPort)
            println(i)
          }
        }finally {
          Thread.sleep(2000)
          disc.getAllInstances.forEach { i =>
            if( i.getServiceId !== "consul" ){
              println("De-registering" + i.getServiceId)
              registry.deregister(reg)
            }
          }
        }
      }

    }
    // */

  }

}