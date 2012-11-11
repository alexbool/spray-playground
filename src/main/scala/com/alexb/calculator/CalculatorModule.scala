package com.alexb.calculator

import akka.actor.{ ActorSystem, Props }
import com.alexb.main.context.ActorSystemContext

trait CalculatorModule extends CalculatorService with CalculatorServiceContext {
	this: ActorSystemContext =>
}

trait CalculatorServiceContext {
	this: ActorSystemContext =>
	
	// Creating calculator actor
	private lazy val calculatorActor = actorSystem.actorOf(
			props = Props(new CalculatorActor),
			name = "calculator-actor")
	
	def calculator = calculatorActor
}
