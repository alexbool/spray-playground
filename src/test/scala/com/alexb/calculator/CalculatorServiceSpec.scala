package com.alexb.calculator

import akka.actor.{ ActorSystem, Props }
import akka.util.Timeout
import akka.util.duration._
import spray.http._
import spray.http.HttpMethods._
import spray.testkit._
import org.scalatest._

class CalculatorServiceSpec extends WordSpec with MustMatchers with CalculatorService with CalculatorServiceContext with ScalatestRouteTest {

	val timeout = Timeout(5 seconds) // needed for `?`
	def actorSystem: ActorSystem = system
	def actorRefFactory = system

	"Calculator service" must {
		"do right additions" in {
			Get("/calculator/add/35/7.2") ~> calculatorRoute ~> check {
				entityAs[String] must include ("\"result\": 42.2")
			}
		}
	}
}
