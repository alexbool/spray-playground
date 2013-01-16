package com.alexb.client

import org.scalatest._
import akka.actor.{ ActorSystem, Props }
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.client.HttpClient
import spray.client.pipelining._
import language.postfixOps

class SprayClientSpec extends WordSpec with MustMatchers {
  val system = ActorSystem("test")
  val httpClient = system.actorOf(
    props = Props(new HttpClient),
    name = "test-http-client")

  val pipeline = sendReceive(httpClient)(system.dispatcher)

  "Spray client" ignore {
    "download content in reasonable time" in {
      Await.result(pipeline(Get("http://173.194.32.134/" /* this is google.com's IP */)), 3 seconds)
    }
  }
}
