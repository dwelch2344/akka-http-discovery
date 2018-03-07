package com.lightbend.akka.sample.discovery

import java.io.IOException
import java.lang.reflect.Type
import java.util
import java.util.UUID
import java.util.concurrent.ScheduledExecutorService
import javax.annotation.PreDestroy

import com.ecwid.consul.v1.ConsulClient
import com.lightbend.akka.sample.discovery.loadbalance.BackoffPolicyFactory
import io.circe.Decoder
import io.circe.parser.decode
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.config.ConfigFileApplicationListener
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.client.discovery.{DiscoveryClient, EnableDiscoveryClient}
import org.springframework.cloud.client.loadbalancer.{LoadBalanced, LoadBalancedBackOffPolicyFactory}
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties
import org.springframework.cloud.consul.discovery.{ConsulDiscoveryProperties, HeartbeatProperties, TtlScheduler}
import org.springframework.cloud.consul.serviceregistry._
import org.springframework.context.annotation.{Bean, _}
import org.springframework.context.{ApplicationContext, ApplicationContextInitializer, ConfigurableApplicationContext}
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.converter.{GenericHttpMessageConverter, HttpMessageConverter}
import org.springframework.http.{HttpMethod, MediaType}
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.util.ReflectionUtils
import org.springframework.web.client.{HttpMessageConverterExtractor, RequestCallback, RestTemplate}

import scala.collection.JavaConverters._


@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableConfigurationProperties
@PropertySource(Array[String]("classpath:application.yml"))
class DiscoveryConfig {

  @Value("${SD_SERVICE:#{null}}")
  var serviceDiscoveryService: String = null

  @Value("${SD_HOST:#{null}}")
  var serviceDiscoveryHost: String = null

  @Value("${app.port}")
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



      val id = s.getName + UUID.randomUUID() // TODO get k8s / alamo pod name
      if( serviceDiscoveryHost != null ){
        s.setAddress(serviceDiscoveryHost)
      }

      if( serviceDiscoveryService != null ){
        s.setName(serviceDiscoveryService)
      }

      s.setId(id)
      // Add tags? maybe what region for GDPR?

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
  def getService() = reg.getService

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

  def query[T: Decoder](url: String, method: HttpMethod, clazz: Class[T]): T  = {
    query(url, method, clazz, Map.empty)
  }

  def query[T: Decoder](url: String, method: HttpMethod, clazz: Class[T], params: Map[String, Any]): T = {
    val requestCallback = new AcceptHeaderRequestCallback(classOf[String], asScalaBuffer(rest.getMessageConverters) )
    val responseExtractor = new HttpMessageConverterExtractor[String](classOf[String], rest.getMessageConverters)
    val raw = rest.execute(url, method, requestCallback, responseExtractor, mapAsJavaMap(params) )

    val decoded = decode[T](raw)
    decoded match {
      case Right(result) => result
      case Left(ex:Throwable) => throw new RuntimeException(s"Failed Making API Request: ${ex.getMessage}", ex)
    }
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


private class AcceptHeaderRequestCallback (val responseType: Type, converters: Seq[HttpMessageConverter[_]]) extends RequestCallback {
  @throws[IOException]
  override def doWithRequest(request: ClientHttpRequest): Unit = {
    if (this.responseType != null) {

      var responseClass: Class[_] = responseType match {
        case clazz: Class[_] => clazz
        case _ => null
      }
      val allSupportedMediaTypes = new util.ArrayList[MediaType]

      converters.foreach { converter =>
        if (responseClass != null) {
          if ( converter.canRead( responseClass, null) ){
            allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter))
          }else converter match {
            case genericConverter: GenericHttpMessageConverter[_] =>
              if (genericConverter.canRead(this.responseType, null, null)) {
                allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter))
              }
            case _ =>
          }
        }
      }
      if (!allSupportedMediaTypes.isEmpty) {
        MediaType.sortBySpecificity(allSupportedMediaTypes)
//        if (logger.isDebugEnabled){
//          logger.debug("Setting request Accept header to " + allSupportedMediaTypes)
//        }
        request.getHeaders.setAccept(allSupportedMediaTypes)
      }
    }
  }

  private def getSupportedMediaTypes(messageConverter: HttpMessageConverter[_]) = {
    var supportedMediaTypes = messageConverter.getSupportedMediaTypes
    val result = new util.ArrayList[MediaType](supportedMediaTypes.size)

    asScalaBuffer(supportedMediaTypes).foreach{ smt =>
      var supportedMediaType = smt
      if (supportedMediaType.getCharset != null) {
        supportedMediaType = new MediaType(supportedMediaType.getType, supportedMediaType.getSubtype)
      }
      result.add(supportedMediaType)
    }
    result
  }
}