package com.alexb.main

import spray.routing.Route

trait HttpRouteContainer {
  def route: Route
}
