package com.alexb.calculator

import akka.actor.Actor

class CalculatorActor extends Actor {

	def receive = {
		case AddCommand(a, b) => sender ! SuccessResult(a + b)
		case SubtractCommand(a, b) => sender ! SuccessResult(a - b)
		case DivideCommand(a, b) => sender ! (if (b != 0) SuccessResult(a / b) else ErrorResult("Division by zero"))
	}
}
