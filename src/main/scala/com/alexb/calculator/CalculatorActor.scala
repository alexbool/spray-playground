package com.alexb.calculator

import akka.actor.Actor

class CalculatorActor extends Actor {

	def receive = {
		case AddCommand(a, b) => sender ! a + b
		case SubtractCommand(a, b) => sender ! a - b
		case DivideCommand(a, b) => sender ! (try { Left(a / b) } catch { case e: ArithmeticException => Right(e) })
	}
}
