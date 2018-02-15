//#full-example
package com.lightbend.akka.sample

import java.net.InetAddress

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.ecwid.consul.v1.ConsulClient
import com.netflix.client.config.IClientConfig
import com.netflix.loadbalancer.{Server, ServerList}
import org.scalatest._
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.commons.util.{InetUtils, InetUtilsProperties}
import org.springframework.cloud.consul.config.{ConsulConfigAutoConfiguration, ConsulConfigProperties}
import org.springframework.cloud.consul.discovery._
import org.springframework.cloud.consul.discovery.configclient.ConsulConfigServerAutoConfiguration
import org.springframework.cloud.consul.serviceregistry.{ConsulAutoServiceRegistrationAutoConfiguration, ConsulServiceRegistryAutoConfiguration}
import org.springframework.cloud.netflix.ribbon.{RibbonAutoConfiguration, RibbonClientConfiguration}
import org.springframework.context.annotation.{AnnotationConfigApplicationContext, Configuration, PropertySource}
import org.springframework.web.client.RestTemplate

import org.springframework.context.annotation.Bean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer


case class MyResolver(instanceId: String, port :Integer) extends ConsulDiscoveryClient.LocalResolver{
  override def getInstanceId: String = instanceId
  override def getPort: Integer = port
}


@Configuration
@EnableDiscoveryClient
@EnableAutoConfiguration
@EnableConfigurationProperties
@PropertySource(Array[String]("classpath:application.yml"))
class ConfigClass {

  @Value("${redis.client.name:failedover}")
  def foo:String = null

  @Bean def propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer

//  @Bean
  def getConsulProperties(): ConsulDiscoveryProperties ={
    val utils = new InetUtils(new InetUtilsProperties){
      override def findFirstNonLoopbackAddress(): InetAddress = {
        return InetAddress.getByName("127.0.0.1")
      }
    }

    println("GOT " + foo)

    val dp = new ConsulDiscoveryProperties(utils)
    dp.setPort(8080)
    dp.setHealthCheckPath("/")
    dp.setRegisterHealthCheck(true)
    dp
  }

  @Bean def propertyConfigInDev = new PropertySourcesPlaceholderConfigurer

//  @Bean
//  def consulClient() :ConsulClient = {
//    new ConsulClient("127.0.0.1")
//  }
//
//  @Bean
//  def consulConfigProperties() :ConsulConfigProperties  = {
//    return new ConsulConfigProperties()
//  }
//
  @Bean
  @LoadBalanced
  def restTemplate() :RestTemplate = new RestTemplate()
//
//
//  @Bean
//  def ribbonServerList(config: IClientConfig, properties: ConsulDiscoveryProperties): ServerList[Server] = {
//    val serverList = new ConsulServerList(consulClient(), properties)
//    serverList.initWithNiwsConfig(config)
//    serverList.asInstanceOf[ServerList[Server]]
//  }

}

class DiscoveryTest extends WordSpec with Matchers with ScalatestRouteTest {



  val utils = new InetUtils(new InetUtilsProperties){
    override def findFirstNonLoopbackAddress(): InetAddress = {
      return InetAddress.getByName("127.0.0.1")
    }
  }
  val dp = new ConsulDiscoveryProperties(utils)
  dp.setPort(8080)
  dp.setHealthCheckPath("/")
  dp.setRegisterHealthCheck(true)



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

      "do ribbon" in {
        val ctx = new AnnotationConfigApplicationContext


        Seq[Class[_]](
          classOf[RibbonAutoConfiguration],
          classOf[RibbonClientConfiguration],
          classOf[ConsulDiscoveryClientConfiguration],
          classOf[ConsulRibbonClientConfiguration],
          classOf[RibbonConsulAutoConfiguration],
          classOf[ConsulConfigAutoConfiguration],
//          classOf[ConsulConfigServerAutoConfiguration],
          classOf[ConsulAutoServiceRegistrationAutoConfiguration],
          classOf[ConsulServiceRegistryAutoConfiguration],
          classOf[ConfigClass]
        ).foreach{ c =>
          ctx.register(c)
        }

//        ctx.register(classOf[ConfigClass])
        ctx.refresh()



        def rt = ctx.getBean(classOf[RestTemplate])
        var res = rt.getForObject("http://application/", classOf[String])
        println(res)

      }

//      "register" in {
//        val thing = new DiscoveryThing("127.0.0.1", dp)
//        try {
//          thing.register()
//          thing.getAllInstances.foreach { i =>
//            println(i.getHost, i.getPort)
//            println(i)
//          }
//        }finally {
//          Thread.sleep(120 * 1000)
//          thing.deregister()
//        }
//      }
    }

  }

}
