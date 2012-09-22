package com.alexb.calculator

import akka.actor.{ ActorSystem, Props }

class CalculatorModule(system: ActorSystem) extends CalculatorService with CalculatorServiceContext {
	// bake your module cake here

	// create the service instance, supplying all required dependencies
	implicit def actorSystem = system
}

trait CalculatorServiceContext {
	
	implicit def actorSystem: ActorSystem
	
	// Creating calculator actor
	implicit val calculator = actorSystem.actorOf(
			props = Props(new CalculatorActor),
			name = "calculator-actor")
}
