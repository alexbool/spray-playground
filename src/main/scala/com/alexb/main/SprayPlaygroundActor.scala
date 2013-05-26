package com.alexb.main

import scala.concurrent.duration._
import akka.util.Timeout
import spray.routing.HttpServiceActor
import language.postfixOps

class SprayPlaygroundActor(routeContainers: Iterable[HttpRouteContainer])
  extends HttpServiceActor {

  val timeout: Timeout = 5 seconds // needed for `?`

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(routeContainers map(_.route) reduce(_ ~ _))
}
