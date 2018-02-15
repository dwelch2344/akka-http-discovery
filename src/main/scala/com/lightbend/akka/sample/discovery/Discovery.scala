package com.lightbend.akka.sample.discovery

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.client.discovery.{DiscoveryClient, EnableDiscoveryClient}
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.consul.serviceregistry.{ConsulAutoRegistration, ConsulServiceRegistry}
import org.springframework.context.annotation.{AnnotationConfigApplicationContext, Bean, Configuration, PropertySource}
import org.springframework.web.client.RestTemplate


@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableConfigurationProperties
@PropertySource(Array[String]("classpath:application.yml"))
private class DiscoveryConfig {

  @Bean
  @LoadBalanced
  def restTemplate() :RestTemplate = new RestTemplate()

}

class Discovery(configs: Class[_]*) {

  private val ctx = new AnnotationConfigApplicationContext
  ctx.register(classOf[DiscoveryConfig])
  configs.foreach{ cfg => ctx.register(cfg) }
  ctx.refresh()

  val rest = ctx.getBean(classOf[RestTemplate])
  val client = ctx.getBean(classOf[DiscoveryClient])
  val registry = ctx.getBean(classOf[ConsulServiceRegistry])
  private val reg = ctx.getBean(classOf[ConsulAutoRegistration])

  println(reg)


  def register(): Unit ={
    registry.register(reg)
  }

  def deregister(): Unit ={
    registry.deregister(reg)
  }
}
