package com.lightbend.akka.sample

import javax.annotation.{PostConstruct, PreDestroy}

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.ecwid.consul.v1.ConsulClient
import com.lightbend.akka.sample.discovery.Discovery
import org.scalatest._
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.consul.serviceregistry.{ConsulRegistration, ConsulRegistrationCustomizer}
import org.springframework.context.annotation.{Bean, Configuration, PropertySource}


@Configuration
//@PropertySource(Array[String]("classpath:application-test.yml"))
class ExampleConfiguration {

  @Value("${spring.cloud.consul.discovery.heartbeat.enabled:blah}")
  var enabled = "default"

  @Bean
  def localhostCustomizer(): ConsulRegistrationCustomizer ={
    (registration: ConsulRegistration) => {
      val s = registration.getService
      s.setAddress("localhost")
      s.setPort(8080)
    }
  }

  @PostConstruct
  def init(): Unit ={
    println("Start: " + enabled)
  }

  @PreDestroy
  def shutdown(): Unit ={
    println("stop")
  }

}

class DiscoveryTest extends WordSpec with Matchers with ScalatestRouteTest {


  "Consul" can {

    "Can Resolve Services" should {

      "do ribbon" in {
        if( true ) {

          val discovery = new Discovery(classOf[ExampleConfiguration])
          var res = discovery.rest.getForObject("http://application/", classOf[String])
          println(res)


          discovery.client.getServices.forEach { s =>
            discovery.client.getInstances(s).forEach { si =>
              println(si)
              if (si.getServiceId === "application") {
                discovery.consul.agentCheckDeregister(si.getServiceId)
                discovery.consul.agentServiceDeregister(si.getServiceId)
              }
            }
          }

          discovery.shutdown()
        }
      }
    }
  }
}
