package com.lightbend.akka.sample

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.lightbend.akka.sample.discovery.Discovery
import org.scalatest._
import org.springframework.cloud.consul.serviceregistry.{ConsulRegistration, ConsulRegistrationCustomizer}
import org.springframework.context.annotation.{Bean, Configuration}


@Configuration
class ExampleConfiguration {

  @Bean
  def localhostCustomizer(): ConsulRegistrationCustomizer ={
    (registration: ConsulRegistration) => {
      val s = registration.getService
      s.setAddress("localhost")
      s.setPort(8080)
    }
  }

}

class DiscoveryTest extends WordSpec with Matchers with ScalatestRouteTest {


  "Consul" can {

    "Can Resolve Services" should {

      "do ribbon" in {

        val discovery = new Discovery(classOf[ExampleConfiguration])
        var res = discovery.rest.getForObject("http://application/", classOf[String])
        println(res)

        discovery.client.getServices.forEach{ s =>
          discovery.client.getInstances(s).forEach{ si =>
            println(si)
          }
        }
      }
    }
  }
}
