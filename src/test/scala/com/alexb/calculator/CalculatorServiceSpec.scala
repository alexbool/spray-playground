package com.alexb.calculator

import spray.httpx.SprayJsonSupport
import spray.testkit._
import org.scalatest._
import com.alexb.main.context.ActorSystemContext
import language.postfixOps

class CalculatorServiceSpec extends WordSpec with MustMatchers with ScalatestRouteTest
  with CalculatorMarshallers with SprayJsonSupport {

  val context = new CalculatorServiceContext with ActorSystemContext {
    def actorSystem = system
  }

  val service = context.calculatorService

	"Calculator service" must {
		"do right additions" in {
			Get("/calculator/add/35/7.2") ~> service.route ~> check {
				responseAs[CalculatorResult] must be (SuccessResult(42.2))
			}
		}
	}
}
