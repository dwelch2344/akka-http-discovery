package com.lightbend.akka.sample

import javax.annotation.{PostConstruct, PreDestroy}

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.agent.model.NewService
import com.lightbend.akka.sample.discovery.Discovery
import com.lightbend.akka.sample.util.{EchoServer, ErrorServer}
import org.scalatest._
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.commons.util.{InetUtils, InetUtilsProperties}
import org.springframework.cloud.consul.discovery.{ConsulDiscoveryClient, ConsulDiscoveryProperties}
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


  "Ribbon" can {

    "Load Balance" should {

      "a service throwing 502s" in {

        val server1 = new EchoServer(8080)
        server1.start()

        val server2 = new ErrorServer(8081)
        server2.start()

        val server3 = new EchoServer(8082)
        server3.start()

        var reg1 = dummy("example", "localhost", 8080)
        var reg2 = dummy("example", "localhost", 8081)
        var reg3 = dummy("example", "localhost", 8082)

        val discovery = new Discovery(classOf[ExampleConfiguration])
        try {
          discovery.registry.register(reg1)
          discovery.registry.register(reg2)
          discovery.registry.register(reg3)

          var res = discovery.rest.getForObject("http://example/", classOf[String])
          println(s"Response said ${res}")


          (1 to 20).foreach{ i =>
            println(s"Request #${i}")
            discovery.rest.getForObject("http://example/", classOf[String])
          }


        }finally{
          discovery.registry.deregister(reg1)
          discovery.registry.deregister(reg2)
          discovery.registry.deregister(reg3)

          println(s"Server1: ${server1.hits}")
          println(s"Server2: ${server2.hits}")
          println(s"Server3: ${server3.hits}")

          server1.stop()
          server2.stop()
          server3.stop()

          discovery.shutdown()
        }
      }
    }
  }

  def dummy(name: String, host: String, port: Integer) : ConsulRegistration = {
    val svc = new NewService;
    svc.setAddress(host)
    svc.setPort(port)
    svc.setName(name)
    svc.setId(name + "-" + port)

    val props = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties))
    props.setRegister(false)
    props.setDeregister(false)
    props.setRegisterHealthCheck(false)
    new ConsulRegistration(svc, props)
  }
}

