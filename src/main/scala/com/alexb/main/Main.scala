package com.alexb.main

import akka.actor.Props
import akka.io.IO
import spray.can.Http
import com.alexb.calculator.{AddCommandListener, AddCommand}
import context._
import language.postfixOps

object Main extends App with ActorSystemFromAppContext {

  // Initialize application context beans
  Context.initialize()

  // create and start the HttpService actor running our service
  val httpService = actorSystem.actorOf(
    props = Props(new SprayPlaygroundActor(Context.calculatorService, Context.orderService,
      Context.staticsService, Context.userService)),
    name = "service")

  ///////////////////////////////////////////////////////////////////////////
  // Subscribing AddCommandListener
  val addCommandListener = actorSystem.actorOf(Props[AddCommandListener])
  actorSystem.eventStream.subscribe(addCommandListener, classOf[AddCommand])
  ///////////////////////////////////////////////////////////////////////////

  // start a new HTTP server on selected port with our service actor as the handler
  IO(Http)(actorSystem) ! Http.Bind(httpService,
      interface = actorSystem.settings.config.getString("application.host"),
      port = actorSystem.settings.config.getInt("application.port"))
}
