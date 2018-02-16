package com.lightbend.akka.sample.discovery.loadbalance

import org.springframework.cloud.client.loadbalancer.LoadBalancedBackOffPolicyFactory
import org.springframework.retry.backoff.{BackOffPolicy, ExponentialBackOffPolicy}

class BackoffPolicyFactory extends LoadBalancedBackOffPolicyFactory{

  private val cache = collection.mutable.Map[String, BackOffPolicy]()

  override def createBackOffPolicy(service: String): BackOffPolicy = {
    cache.getOrElseUpdate(service, new ExponentialBackOffPolicy)
  }
}
