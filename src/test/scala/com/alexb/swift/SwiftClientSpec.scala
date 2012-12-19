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

class SwiftClientSpec extends WordSpec with MustMatchers with SprayCanHttpServerApp {
  val timeout = 20 seconds
  implicit val askTimeout = Timeout(timeout)

  val port = Stream.continually(Random.nextInt(65536)).find(_ > 49152).get
  val server = newHttpServer(system.actorOf(Props(new MockSwiftServer)))
  Await.ready(server ? Bind("localhost", port), timeout)

  val client = system.actorOf(Props(new SwiftClient("localhost", port, false,
    SwiftCredentials("some_account", "some_auth_key"))))

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
  }
}
