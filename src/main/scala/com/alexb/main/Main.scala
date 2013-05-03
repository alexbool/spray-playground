package com.alexb.main

import akka.actor.{ActorRefFactory, Actor, Props}
import akka.io.IO
import akka.util.Timeout
import scala.concurrent.duration._
import spray.can.Http
import com.alexb.calculator.{ CalculatorModule, AddCommandListener, AddCommand }
import com.alexb.orders.OrderModule
import com.alexb.statics.StaticsModule
import com.alexb.user.UserModule
import context._
import language.postfixOps
import spray.routing.RoutingSettings

object Main extends App with ActorSystemFromAppContext {

  // Initialize application context beans
  Context.initialize()

  implicit lazy val system = Context.actorSystem

  // create the service instance, supplying all required dependencies
  class SprayPlaygroundActor extends Actor with ActorSystemContext with ActorSystemConfiguration
    with OAuthdSupport with MongoFromAppContext with ElasticSearchFromAppContext with InfinispanFromAppContext
    with CalculatorModule with OrderModule with StaticsModule with UserModule {

    def actorSystem = Context.actorSystem

    val timeout = Timeout(5 seconds) // needed for `?`

    // the HttpService trait defines only one abstract member, which
    // connects the services environment to the enclosing actor or test
    def actorRefFactory: ActorRefFactory = context

    implicit val rs: RoutingSettings = RoutingSettings.default(actorRefFactory)

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

  // start a new HTTP server on seleccted port with our service actor as the handler
  IO(Http) ! Http.Bind(httpService,
      interface = system.settings.config.getString("application.host"),
      port = system.settings.config.getInt("application.port"))
}
