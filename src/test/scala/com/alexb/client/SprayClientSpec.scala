package com.alexb.client

import org.scalatest._
import akka.actor.{ ActorSystem, Props }
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.io.IOBridge
import spray.can.client.HttpClient
import spray.client.HttpConduit
import spray.client.HttpConduit._

class SprayClientSpec extends WordSpec with MustMatchers {
  val system = ActorSystem("test")
  val ioBridge = new IOBridge(system).start()
  val httpClient = system.actorOf(
    props = Props(new HttpClient(ioBridge)),
    name = "test-http-client")
  val conduit = system.actorOf(
    props = Props(new HttpConduit(httpClient, "173.194.32.134" /* this is google.com's IP */, 80)),
    name = "test-http-conduit"
  )
  val pipeline = sendReceive(conduit)

  "Spray client" ignore {
    "download content in reasonable time" in {
      Await.result(pipeline(Get("/")), 3 seconds)
    }
  }
}
