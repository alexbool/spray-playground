package com.alexb.service

import akka.actor.Actor

case class AddCommand(a: Double, b: Double)
case class SubtractCommand(a: Double, b: Double)
case class DivideCommand(a: Double, b: Double)

class CalculatorActor extends Actor {

	protected def receive = {
		case AddCommand(a, b) => sender ! a + b
		case SubtractCommand(a, b) => sender ! a - b
		case DivideCommand(a, b) => sender ! (try { Left(a / b) } catch { case e: ArithmeticException => Right(e) })
	}
}