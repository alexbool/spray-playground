package com.alexb.calculator

import akka.actor.{ ActorSystem, Props }

class CalculatorModule(system: ActorSystem) extends CalculatorService {

	// create the service instance, supplying all required dependencies
	implicit def actorSystem = system
	// bake your module cake here

	// Creating calculator actor
	implicit val calculator = actorSystem.actorOf(
		props = Props(new CalculatorActor),
		name = "calculator-actor")
}