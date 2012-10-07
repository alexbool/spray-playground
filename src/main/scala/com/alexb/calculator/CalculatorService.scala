package com.alexb.calculator

import cc.spray.routing.HttpService
import cc.spray.routing.directives.PathMatchers._
import cc.spray.httpx.SprayJsonSupport
import akka.util.Timeout
import akka.actor.ActorRef
import akka.pattern.ask
import cc.spray.json.DefaultJsonProtocol

trait CalculatorService
	extends HttpService
	with SprayJsonSupport
	with DefaultJsonProtocol
	with DoubleResultProtocol {

	implicit val timeout: Timeout // needed for `?` below

	def calculator: ActorRef

	val calculatorRoute = {
		pathPrefix("calculator") { 
			path("add" / DoubleNumber / DoubleNumber) { (a, b) =>
				get {
					val cmd = AddCommand(a, b)
					actorSystem.eventStream.publish(cmd)
					complete((calculator ? cmd).mapTo[Double])
				}
			} ~ 
			path("subtract" / DoubleNumber / DoubleNumber) { (a, b) =>
				get {
					complete((calculator ? SubtractCommand(a, b)).mapTo[Double])
				}
			} ~ 
			path("divide" / DoubleNumber / DoubleNumber) { (a, b) =>
				get {
					complete((calculator ? DivideCommand(a, b)).mapTo[Either[Double, ArithmeticException]])
				}
			}
		}
	}
}
