package com.alexb.calculator

import spray.json._

trait CalculatorMarshallers extends DefaultJsonProtocol {

	// Not very clean, but this is the limitation of spray-json
	implicit def calculatorResultFormat = new RootJsonFormat[CalculatorResult] {
		def write(t: CalculatorResult) = t match {
			case t: SuccessResult => JsObject("success" -> JsBoolean(t.success), "result" -> JsNumber(t.result))
			case t: ErrorResult => JsObject("success" -> JsBoolean(t.success), "reason" -> JsString(t.reason))
		}
		def read(value: JsValue) = deserializationError("Unsupported operation", null)
	}
}

object CalculatorMarshallers extends CalculatorMarshallers
