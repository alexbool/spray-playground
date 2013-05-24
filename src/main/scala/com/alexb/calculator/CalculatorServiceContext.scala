package com.alexb.calculator

import akka.actor.Props
import com.alexb.main.context.ActorSystemContext
import scala.concurrent.duration._
import language.postfixOps

trait CalculatorServiceContext {
  this: ActorSystemContext =>

  private lazy val calculator = actorSystem.actorOf(
    props = Props(new CalculatorActor),
    name = "calculator")

  lazy val calculatorService =
    new CalculatorService(calculator, actorSystem.eventStream)(actorSystem.dispatcher, 5 seconds)
}
