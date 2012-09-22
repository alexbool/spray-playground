package com.alexb.calculator

import akka.actor.{ ActorSystem, Props }
import akka.testkit.TestKit
import cc.spray.http._
import cc.spray.http.HttpMethods._
import cc.spray.test.SprayTest
import org.scalatest._

class CalculatorServiceSpec(system: ActorSystem) extends TestKit(system)
	with WordSpec with MustMatchers with SprayTest with CalculatorService {

	def this() = this(ActorSystem("MySpec"))
	
	implicit val calculator = actorSystem.actorOf(
		props = Props(new CalculatorActor),
		name = "calculator-actor")

	"Calculator service" must {
		"do right additions" in {
			testService(HttpRequest(GET, "/calculator/add/35/7.2")) {
				calculatorService
			}.response.content.as[String].right.get must include ("\"result\": 42.2")
		}
	}
}
