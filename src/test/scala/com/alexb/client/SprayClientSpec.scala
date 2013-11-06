package com.alexb.client

import org.scalatest._
import akka.actor.ActorSystem
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.client.pipelining._
import language.postfixOps

class SprayClientSpec extends WordSpec with Matchers {
  val system = ActorSystem("test")

  val pipeline = sendReceive(system, system.dispatcher)

  "Spray client" ignore {
    "download content in reasonable time" in {
      Await.result(pipeline(Get("http://173.194.32.134/" /* this is google.com's IP */)), 3 seconds)
    }
  }
}
