package com.alexb.calculator

import scala.concurrent.ExecutionContext
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import akka.util.Timeout
import akka.actor.ActorRef
import akka.pattern.ask
import com.alexb.main.context.ActorSystemContext

trait CalculatorService
  extends HttpService
  with SprayJsonSupport
  with DefaultJsonProtocol
  with CalculatorMarshallers { this: ActorSystemContext =>

  implicit val timeout: Timeout // needed for `?` below
  implicit private def ec: ExecutionContext = actorSystem.dispatcher

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
