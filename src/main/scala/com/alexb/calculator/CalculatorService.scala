package com.alexb.calculator

import spray.routing.HttpService
import spray.httpx.SprayJsonSupport
import akka.util.Timeout
import akka.actor.ActorRef
import akka.pattern.ask
import spray.json.DefaultJsonProtocol

trait CalculatorService
  extends HttpService
  with SprayJsonSupport
  with DefaultJsonProtocol
  with CalculatorMarshallers {

  implicit val timeout: Timeout // needed for `?` below

  def calculator: ActorRef

  val calculatorRoute =
    pathPrefix("calculator") {
      get {
        path("add" / DoubleNumber / DoubleNumber) { (a, b) =>
          val cmd = AddCommand(a, b)
          actorSystem.eventStream.publish(cmd)
          complete((calculator ? cmd).mapTo[CalculatorResult])
        } ~
        path("subtract" / DoubleNumber / DoubleNumber) { (a, b) =>
          complete((calculator ? SubtractCommand(a, b)).mapTo[CalculatorResult])
        } ~
        path("divide" / DoubleNumber / DoubleNumber) { (a, b) =>
          complete((calculator ? DivideCommand(a, b)).mapTo[CalculatorResult])
        }
      }
    }
}
