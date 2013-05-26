package com.alexb.main

import akka.actor.Props
import akka.io.IO
import spray.can.Http
import com.alexb.calculator.{AddCommandListener, AddCommand}
import context._
import language.postfixOps

object Main extends App {

  // Initialize application context beans
  Context.initialize()

  val actorSystem = Context.actorSystem
  val config = Context.config

  ///////////////////////////////////////////////////////////////////////////
  // Subscribing AddCommandListener
  val addCommandListener = actorSystem.actorOf(Props[AddCommandListener])
  actorSystem.eventStream.subscribe(addCommandListener, classOf[AddCommand])
  ///////////////////////////////////////////////////////////////////////////

  // start a new HTTP server on selected port with our service actor as the handler
  IO(Http)(actorSystem) ! Http.Bind(Context.httpActor,
      interface = config.getString("application.host"),
      port = config.getInt("application.port"))
}
