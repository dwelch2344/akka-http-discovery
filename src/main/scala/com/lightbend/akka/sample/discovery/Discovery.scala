package com.lightbend.akka.sample.discovery

import java.util
import java.util.{List, UUID}
import java.util.concurrent.ScheduledExecutorService
import javax.annotation.PreDestroy

import com.ecwid.consul.v1.ConsulClient
import com.lightbend.akka.sample.discovery.loadbalance.BackoffPolicyFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.config.ConfigFileApplicationListener
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.client.discovery.{DiscoveryClient, EnableDiscoveryClient}
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties
import org.springframework.cloud.consul.discovery.{ConsulDiscoveryProperties, HeartbeatProperties, TtlScheduler}
import org.springframework.cloud.consul.serviceregistry._
import org.springframework.context.{ApplicationContext, ApplicationContextInitializer, ConfigurableApplicationContext}
import org.springframework.context.annotation._
import org.springframework.web.client.RestTemplate
import org.springframework.cloud.client.loadbalancer.LoadBalancedBackOffPolicyFactory
import org.springframework.cloud.consul.binder.ConsulBinder
import org.springframework.cloud.stream.binding.BindingService
import org.springframework.context.annotation.Bean
import org.springframework.retry.backoff.BackOffPolicy
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.util.ReflectionUtils


@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableConfigurationProperties
@PropertySource(Array[String]("classpath:application.yml"))
class DiscoveryConfig {

  @Value("${PORT:8080}")
  var port :Integer = null

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

  @Bean def backOffPolciyFactory: LoadBalancedBackOffPolicyFactory = new BackoffPolicyFactory

  @Bean @Primary
  def CustomTtl(configuration: HeartbeatProperties, client: ConsulClient): TtlScheduler = new CustomTtl(configuration: HeartbeatProperties, client: ConsulClient)

  @Bean
  def kubernetesCustomizer(): ConsulRegistrationCustomizer ={
    (registration: ConsulRegistration) => {
      val s = registration.getService

      val isKubernetes = false // TODO are we in k8s / alamo?
      if( isKubernetes ){

        val address = "localhost" // TODO get kubernetes internal IP
        val id = s.getName + UUID.randomUUID() // TODO get k8s / alamo pod name

        s.setAddress(address)
        s.setId(id)
        // Add tags? maybe what region for GDPR?
      }


      s.setPort(port)
    }
  }
}

class Discovery(configs: Class[_]*) {

  private val ctx = new AnnotationConfigApplicationContext

  new ConfigFileApplicationContextInitializer().initialize(ctx)

  ctx.registerShutdownHook()
  ctx.register(classOf[DiscoveryConfig])
  configs.foreach{ cfg => ctx.register(cfg) }
  ctx.refresh()

  val rest = ctx.getBean(classOf[RestTemplate])
  val client = ctx.getBean(classOf[DiscoveryClient])
  val registry = ctx.getBean(classOf[ConsulServiceRegistry])
  val consul = ctx.getBean(classOf[ConsulClient])
  private val reg = ctx.getBean(classOf[ConsulAutoRegistration])

  println(reg)

  def getHost() = reg.getHost
  def getPort() = reg.getPort

  def register(): Unit ={
    registry.register(reg)
  }

  def deregister(): Unit ={
    registry.deregister(reg)
  }

  def shutdown(): Unit ={
    deregister()
    ctx.close()
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

// this little guy has no way to shut himself down :(  so, we'll force it here
class CustomTtl(configuration: HeartbeatProperties, client: ConsulClient) extends TtlScheduler(configuration, client) {

  @PreDestroy
  def shutdown(): Unit ={
    val f = ReflectionUtils.findField(classOf[TtlScheduler], "scheduler", classOf[TaskScheduler])
    f.setAccessible(true)
    val scheduler = f.get(this).asInstanceOf[ConcurrentTaskScheduler]
    scheduler.getConcurrentExecutor.asInstanceOf[ScheduledExecutorService].shutdown()
    println("got it")
  }

}
