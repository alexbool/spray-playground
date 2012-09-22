package com.alexb.main

import akka.actor.{ actorRef2Scala, ActorSystem, Props }
import cc.spray.can.server.HttpServer
import cc.spray.io.pipelines.MessageHandlerDispatch
import cc.spray.io.IoWorker
import cc.spray.{ HttpService, SprayCanRootService }
import com.alexb.calculator.{ CalculatorModule, AddCommandListener, AddCommand }
import com.alexb.orders.{ OrderService, OrderActor, OrderModule }

object Main extends App {

	// we need an ActorSystem to host our application in
	val system = ActorSystem("SprayPlayground")
	
	// create the service instance, supplying all required dependencies
	val calculatorModule = new CalculatorModule(system)
	val orderModule = new OrderModule with MongoContext {
		implicit def actorSystem = system
		implicit def collection = mongoConn("spray_playground")("orders")
	}

	// create and start the HttpService actor running our service as well as the root actor
	val calculatorHttpService = system.actorOf(
		props = Props(new HttpService(calculatorModule.calculatorService)),
		name = "calculator-service")
	val orderHttpService = system.actorOf(
			props = Props(new HttpService(orderModule.orderService)),
			name = "order-service")
	val rootService = system.actorOf(
		props = Props(new SprayCanRootService(calculatorHttpService, orderHttpService)),
		name = "root-service")

	///////////////////////////////////////////////////////////////////////////
	// Subscribing AddCommandListener
	val addCommandListener = system.actorOf(Props[AddCommandListener])
	system.eventStream.subscribe(addCommandListener, classOf[AddCommand])
	///////////////////////////////////////////////////////////////////////////

	// every spray-can HttpServer (and HttpClient) needs an IoWorker for low-level network IO
	// (but several servers and/or clients can share one)
	val ioWorker = new IoWorker(system).start()

	// create and start the spray-can HttpServer, telling it that we want requests to be
	// handled by the root service actor
	val sprayCanServer = system.actorOf(
		Props(new HttpServer(ioWorker, MessageHandlerDispatch.SingletonHandler(rootService))),
		name = "http-server")

	// a running HttpServer can be bound, unbound and rebound
	// initially to need to tell it where to bind to
	sprayCanServer ! HttpServer.Bind("localhost", 8080)

	// finally we drop the main thread but hook the shutdown of
	// our IoWorker into the shutdown of the applications ActorSystem
	system.registerOnTermination {
		ioWorker.stop()
	}
}