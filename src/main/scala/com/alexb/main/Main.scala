package com.alexb.main

import akka.actor.{actorRef2Scala, Actor, Props}
import akka.util.Timeout
import scala.concurrent.duration._
import spray.can.server.SprayCanHttpServerApp
import com.alexb.calculator.{ CalculatorModule, AddCommandListener, AddCommand }
import com.alexb.orders.OrderModule
import com.alexb.statics.StaticsModule
import com.alexb.user.UserModule
import context._

object Main extends App with SprayCanHttpServerApp {

  // create the service instance, supplying all required dependencies
  class SprayPlaygroundActor extends Actor with ActorSystemContext with ActorSystemConfiguration with IOBridgeContext
    with OAuthdSupport with MongoSupport with ElasticSearchSupport with InfinispanSupport
    with CalculatorModule with OrderModule with StaticsModule with UserModule {

    def actorSystem = system

    val timeout = Timeout(5 seconds) // needed for `?`

    // the HttpService trait defines only one abstract member, which
    // connects the services environment to the enclosing actor or test
    def actorRefFactory = context

    // this actor only runs our route, but you could add
    // other things here, like request stream processing
    // or timeout handling
    def receive = runRoute(calculatorRoute ~ orderRoute ~ staticsRoute ~ userRoute)
  }

  // create and start the HttpService actor running our service as well as the root actor
  val httpService = system.actorOf(
    props = Props[SprayPlaygroundActor],
    name = "service")

  ///////////////////////////////////////////////////////////////////////////
  // Subscribing AddCommandListener
  val addCommandListener = system.actorOf(Props[AddCommandListener])
  system.eventStream.subscribe(addCommandListener, classOf[AddCommand])
  ///////////////////////////////////////////////////////////////////////////

  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(httpService) ! Bind(
    interface = system.settings.config.getString("application.host"),
    port = system.settings.config.getInt("application.port"))
}
