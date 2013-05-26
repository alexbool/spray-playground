package com.alexb.calculator

import scala.concurrent.ExecutionContext
import spray.routing.Directives
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import akka.util.Timeout
import akka.actor.ActorRef
import akka.pattern.ask
import akka.event.EventStream
import com.alexb.main.HttpRouteContainer

class CalculatorService(calculator: ActorRef, eventStream: EventStream)(implicit ec: ExecutionContext, timeout: Timeout)
  extends Directives with SprayJsonSupport with DefaultJsonProtocol with CalculatorMarshallers with HttpRouteContainer {

  val route =
    pathPrefix("calculator") {
      get {
        path("add" / DoubleNumber / DoubleNumber) { (a, b) =>
          val cmd = AddCommand(a, b)
          eventStream.publish(cmd)
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
