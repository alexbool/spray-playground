package com.alexb.client

import akka.actor.ActorSystem
import spray.routing.SimpleRoutingApp

object SprayServerMain extends App with SimpleRoutingApp {

  implicit val system: ActorSystem = ActorSystem("SprayServer")

  val route =
    get { ctx =>
      ctx.complete("OK\n")
    }

  startServer("localhost", 8080)(route)
}
