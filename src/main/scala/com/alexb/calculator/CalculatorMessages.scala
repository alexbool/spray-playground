package com.alexb.calculator

case class AddCommand(a: Double, b: Double)
case class SubtractCommand(a: Double, b: Double)
case class DivideCommand(a: Double, b: Double)

private[calculator] class CalculatorResult(val success: Boolean)
case class SuccessResult(result: Double) extends CalculatorResult(true)
case class ErrorResult(reason: String) extends CalculatorResult(false)
