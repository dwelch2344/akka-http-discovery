package com.lightbend.akka.sample.api

import com.lightbend.akka.sample.demo.identity.SearchMatch
import com.lightbend.akka.sample.discovery.Discovery
import org.springframework.http.HttpMethod

import io.circe.generic.auto._

class ExampleApi(discovery: Discovery){

  private val IDENTITY_SVC = "app1"

  def findDistinctIdentity(username: String, customerId: Option[String]): SearchMatch ={
    val customerStr = customerId match {
      case Some(cid) => s"&customerId=${cid}"
      case _ => ""
    }
    val url = s"http://${IDENTITY_SVC}/identities/searches/byUsername?username=${username}${customerStr}"
    discovery.query(url, HttpMethod.GET, classOf[SearchMatch])
  }

  def authenticate(userId: String, password: String): String ={
    val url = s"http://${IDENTITY_SVC}/identities/${userId}/auth"
    val result = discovery.query(url, HttpMethod.POST, classOf[String], Map(password -> password ))
    result
  }

}
