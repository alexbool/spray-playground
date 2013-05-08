package com.alexb.swift

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

/**
 * Just for debugging StubSwiftServer
 */
object StubSwiftServerMain extends App {
  implicit val system: ActorSystem = ActorSystem("StubSwiftServerMain")

  val httpService = system.actorOf(
    props = Props[StubSwiftServer],
    name = "stub-swift-server")

  IO(Http) ! Http.Bind(httpService, interface = "localhost", port = 8080)
}
