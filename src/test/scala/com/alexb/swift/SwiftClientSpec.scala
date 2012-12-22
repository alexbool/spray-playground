package com.alexb.swift

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random
import spray.can.server.SprayCanHttpServerApp
import scala.io.Source

class SwiftClientSpec extends WordSpec with MustMatchers with SprayCanHttpServerApp {
  val timeout = 20 seconds
  implicit val askTimeout = Timeout(timeout)

  val port = Stream.continually(Random.nextInt(65536)).find(_ > 49152).get
  val server = newHttpServer(system.actorOf(Props(new MockSwiftServer)))
  Await.ready(server ? Bind("localhost", port), timeout)

  val client = system.actorOf(Props(new SwiftClient(SwiftCredentials("some_account", "some_auth_key"),
    "localhost", port)))

  "Swift client" must {
    "create containers" in {
      Await.result(client ? CreateContainer("new_container"), timeout) must be (CreateContainerResult(true, false))
    }
    "create duplicate containers" in {
      Await.result(client ? CreateContainer("new_container"), timeout) must be (CreateContainerResult(true, true))
    }
    "list containers" in {
      Await.result(client ? ListContainers, timeout) must be (Seq(Container("new_container", 0, 0)))
    }
    "list objects" in {
      Await.result(client ? ListObjects("new_conatiner"), timeout) must be (Seq())
    }
    "put objects" in {
      val bytes = Source.fromFile("src/test/resources/sample.png")(scala.io.Codec.ISO8859).map(_.toByte).toArray
      Await.result(client ? PutObject("new_conatiner", "sample.png", bytes), timeout) must be (PutObjectResult(true))
    }
  }
}
