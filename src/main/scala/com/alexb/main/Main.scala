package com.alexb.main

import akka.actor.{ actorRef2Scala, Actor, ActorSystem, Props }
import cc.spray.can.server.HttpServer
import cc.spray.io.pipelining.SingletonHandler
import cc.spray.io.IOBridge
import cc.spray.routing.HttpService
import akka.util.Timeout
import akka.util.duration._
import com.alexb.calculator.{ CalculatorModule, AddCommandListener, AddCommand }
import com.alexb.orders.{ OrderService, OrderActor, OrderModule }

object Main extends App {

	// we need an ActorSystem to host our application in
	val system = ActorSystem("SprayPlayground")
	
	// create the service instance, supplying all required dependencies
	// val calculatorModule = new CalculatorModule
	val service = new Actor with CalculatorModule with OrderModule with MongoContext {
		def actorSystem = system
		
		val timeout = Timeout(5 seconds) // needed for `?`
		
		// the HttpService trait defines only one abstract member, which
		// connects the services environment to the enclosing actor or test
		def actorRefFactory = context

		// this actor only runs our route, but you could add
		// other things here, like request stream processing
		// or timeout handling
	 	def receive = runRoute(calculatorRoute)
		implicit def collection = mongoConn("spray_playground")("orders")
	}

	// create and start the HttpService actor running our service as well as the root actor
	val httpService = system.actorOf(
		props = Props(service),
		name = "service")

	///////////////////////////////////////////////////////////////////////////
	// Subscribing AddCommandListener
	val addCommandListener = system.actorOf(Props[AddCommandListener])
	system.eventStream.subscribe(addCommandListener, classOf[AddCommand])
	///////////////////////////////////////////////////////////////////////////

	// every spray-can HttpServer (and HttpClient) needs an IOBridge for low-level network IO
	// (but several servers and/or clients can share one)
	val ioBridge = new IOBridge(system).start()

	// create and start the spray-can HttpServer, telling it that we want requests to be
	// handled by the root service actor
	val sprayCanServer = system.actorOf(
		Props(new HttpServer(ioBridge, SingletonHandler(httpService))),
		name = "http-server")

	// a running HttpServer can be bound, unbound and rebound
	// initially to need to tell it where to bind to
	sprayCanServer ! HttpServer.Bind(
			system.settings.config.getString("application.host"),
			system.settings.config.getInt("application.port"))

	// finally we drop the main thread but hook the shutdown of
	// our IOBridge into the shutdown of the applications ActorSystem
	system.registerOnTermination {
		ioBridge.stop()
	}
}