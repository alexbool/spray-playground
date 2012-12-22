package com.alexb.calculator

import spray.json._

trait CalculatorMarshallers extends DefaultJsonProtocol {

  // Not very clean, but this is the limitation of spray-json
  implicit def calculatorResultFormat = new RootJsonFormat[CalculatorResult] {
    def write(t: CalculatorResult) = t match {
      case t: SuccessResult => JsObject("success" -> JsBoolean(t.success), "result" -> JsNumber(t.result))
      case t: ErrorResult   => JsObject("success" -> JsBoolean(t.success), "reason" -> JsString(t.reason))
    }
    def read(value: JsValue) = value.asJsObject.getFields("success") match {
      case Seq(JsBoolean(true)) => value.asJsObject.getFields("result") match {
        case Seq(JsNumber(result)) => SuccessResult(result.toDouble)
        case _                     => deserializationError("Unknown format")
      }
      case Seq(JsBoolean(false)) => value.asJsObject.getFields("reason") match {
        case Seq(JsString(reason)) => ErrorResult(reason)
        case _                     => deserializationError("Unknown format")
      }
      case _ => deserializationError("Unknown format")
    }
  }
}

object CalculatorMarshallers extends CalculatorMarshallers
