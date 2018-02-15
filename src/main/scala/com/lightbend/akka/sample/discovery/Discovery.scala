package com.lightbend.akka.sample.discovery

import java.util
import java.util.List

import com.ecwid.consul.v1.ConsulClient
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.config.ConfigFileApplicationListener
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.client.discovery.{DiscoveryClient, EnableDiscoveryClient}
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties
import org.springframework.cloud.consul.discovery.{ConsulDiscoveryProperties, HeartbeatProperties}
import org.springframework.cloud.consul.serviceregistry.{ConsulAutoRegistration, ConsulAutoServiceRegistration, ConsulRegistrationCustomizer, ConsulServiceRegistry}
import org.springframework.context.{ApplicationContext, ApplicationContextInitializer, ConfigurableApplicationContext}
import org.springframework.context.annotation._
import org.springframework.web.client.RestTemplate
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableConfigurationProperties
@PropertySource(Array[String]("classpath:application.yml"))
class DiscoveryConfig {

  @Bean
  @LoadBalanced
  def restTemplate() :RestTemplate = new RestTemplate()

  @Bean
  def autoServiceRegistrationProperties: AutoServiceRegistrationProperties = new AutoServiceRegistrationProperties

  @Bean
  def consulAutoServiceRegistration(registry: ConsulServiceRegistry, autoServiceRegistrationProperties: AutoServiceRegistrationProperties, properties: ConsulDiscoveryProperties, consulRegistration: ConsulAutoRegistration) = new ConsulAutoServiceRegistration(registry, autoServiceRegistrationProperties, properties, consulRegistration)

  @Bean
  def consulRegistration(autoServiceRegistrationProperties: AutoServiceRegistrationProperties, properties: ConsulDiscoveryProperties, applicationContext: ApplicationContext, registrationCustomizers: ObjectProvider[util.List[ConsulRegistrationCustomizer]], heartbeatProperties: HeartbeatProperties): ConsulAutoRegistration = ConsulAutoRegistration.registration(autoServiceRegistrationProperties, properties, applicationContext, registrationCustomizers.getIfAvailable, heartbeatProperties)

  @Bean
  def YamlPropertiesFactoryBean() : YamlPropertiesFactoryBean = new YamlPropertiesFactoryBean

}

class Discovery(configs: Class[_]*) {

  private val ctx = new AnnotationConfigApplicationContext

  new ConfigFileApplicationContextInitializer().initialize(ctx)

  ctx.register(classOf[DiscoveryConfig])
  configs.foreach{ cfg => ctx.register(cfg) }
  ctx.refresh()

  val rest = ctx.getBean(classOf[RestTemplate])
  val client = ctx.getBean(classOf[DiscoveryClient])
  val registry = ctx.getBean(classOf[ConsulServiceRegistry])
  val consul = ctx.getBean(classOf[ConsulClient])
  private val reg = ctx.getBean(classOf[ConsulAutoRegistration])

  println(reg)


  def register(): Unit ={
    registry.register(reg)
  }

  def deregister(): Unit ={
    registry.deregister(reg)
  }

  def shutdown(): Unit ={
    deregister()
    ctx.destroy()
  }
}


class ConfigFileApplicationContextInitializer() extends ApplicationContextInitializer[ConfigurableApplicationContext] {
  override def initialize(applicationContext: ConfigurableApplicationContext): Unit = {
    new ConfigFileApplicationListener() {
      def apply(): Unit = {
        this.addPropertySources(applicationContext.getEnvironment, applicationContext)
        this.addPostProcessors(applicationContext)
      }
    }.apply()
  }
}
