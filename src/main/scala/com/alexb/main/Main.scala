package com.alexb.main

import akka.actor.{ActorRefFactory, Actor, Props}
import akka.io.IO
import akka.util.Timeout
import scala.concurrent.duration._
import spray.can.Http
import com.alexb.calculator.{CalculatorService, AddCommandListener, AddCommand}
import com.alexb.orders.OrderService
import com.alexb.statics.StaticsService
import com.alexb.user.UserModule
import context._
import language.postfixOps

object Main extends App with ActorSystemFromAppContext {

  // Initialize application context beans
  Context.initialize()

  // create the service instance, supplying all required dependencies
  class SprayPlaygroundActor(calculatorService: CalculatorService, orderService: OrderService,
                             staticsService: StaticsService)
    extends Actor with ActorSystemFromAppContext with ActorSystemConfiguration
    with MongoFromAppContext with ElasticSearchFromAppContext with InfinispanFromAppContext
    with UserModule {

    val timeout = Timeout(5 seconds) // needed for `?`

    // the HttpService trait defines only one abstract member, which
    // connects the services environment to the enclosing actor or test
    def actorRefFactory: ActorRefFactory = context

    // this actor only runs our route, but you could add
    // other things here, like request stream processing
    // or timeout handling
    def receive = runRoute(calculatorService.calculatorRoute ~
      orderService.orderRoute ~
      staticsService.staticsRoute ~
      userRoute)
  }

  // create and start the HttpService actor running our service as well as the root actor
  val httpService = actorSystem.actorOf(
    props = Props(new SprayPlaygroundActor(Context.calculatorService, Context.orderService, Context.staticsService)),
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
