package com.alexb.calculator

import akka.actor.{ ActorSystem, Props }

trait CalculatorModule extends CalculatorService with CalculatorServiceContext {
	
}

trait CalculatorServiceContext {
	
	implicit def actorSystem: ActorSystem
	
	// Creating calculator actor
	private lazy val calculatorActor = actorSystem.actorOf(
			props = Props(new CalculatorActor),
			name = "calculator-actor")
	
	implicit def calculator = calculatorActor
}
