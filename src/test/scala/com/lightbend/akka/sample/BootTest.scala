package com.lightbend.akka.sample

import javax.inject.Inject

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.ecwid.consul.v1.ConsulClient
import com.netflix.client.config.{DefaultClientConfigImpl, IClientConfig}
import com.netflix.loadbalancer.{Server, ServerList}
import org.scalatest._
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.consul.discovery._
import org.springframework.cloud.consul.serviceregistry.{ConsulAutoServiceRegistrationAutoConfiguration, ConsulServiceRegistryAutoConfiguration}
import org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration
import org.springframework.context.annotation._
import org.springframework.web.client.RestTemplate

@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableConfigurationProperties
@PropertySource(Array[String]("classpath:application.yml"))
class ExampleConfig {

  @Bean
  @LoadBalanced
  def restTemplate() :RestTemplate = new RestTemplate()

}

//@Configuration
////class CustomRibbonClientConfiguration extends RibbonClientConfiguration {
//class CustomRibbonClientConfiguration extends ConsulRibbonClientConfiguration {
//
//  @Inject
//  var client: ConsulClient = null;
//
//  @Bean @Primary
//  override def ribbonServerList(config: IClientConfig, properties: ConsulDiscoveryProperties): ServerList[Server] = {
//    val serverList = new ConsulServerList(client, properties)
//    serverList.initWithNiwsConfig(config)
//    serverList.asInstanceOf[ServerList[Server]]
//  }
//
//}

class BootTest extends WordSpec with Matchers with ScalatestRouteTest {


  override def withFixture(test: NoArgTest): Outcome = {
    // seed data here?
    try test()
    finally {
      // clean up data here
    }
  }


  "Consul" can {

    "Be Configured" should {

      "do ribbon" in {
        val ctx = new AnnotationConfigApplicationContext

        //          classOf[ConsulConfigServerAutoConfiguration],

        Seq[Class[_]](
          classOf[ExampleConfig],
          classOf[ConsulRibbonClientConfiguration],
//          classOf[RibbonAutoConfiguration],
          classOf[RibbonClientConfiguration],
          classOf[ConsulDiscoveryClientConfiguration],
          classOf[RibbonConsulAutoConfiguration],
//          classOf[ConsulConfigAutoConfiguration],
          classOf[ConsulAutoServiceRegistrationAutoConfiguration],
          classOf[ConsulServiceRegistryAutoConfiguration]
        ).foreach{ c =>
          ctx.register(c)
        }

        ctx.refresh()



        def rt = ctx.getBean(classOf[RestTemplate])
        var res = rt.getForObject("http://application/", classOf[String])
        println(res)
      }
    }
  }
}
