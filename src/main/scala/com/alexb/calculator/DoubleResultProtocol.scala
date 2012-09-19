package com.alexb.calculator

import cc.spray.json._

trait DoubleResultProtocol {

	implicit def doubleResultFormat = new RootJsonFormat[Double] {
		def write(t: Double) = JsObject("result" -> JsNumber(t))
		def read(value: JsValue) = value.asJsObject.getFields("result") match {
			case Seq(JsNumber(result)) => result.toDouble
			case x => deserializationError("Expected Double result as JsObject, but got " + x)
		}
	}

	implicit def arithmeticExceptionResultFormat = new RootJsonFormat[ArithmeticException] {
		def write(t: ArithmeticException) = JsObject("result" -> JsString("Cannot divide by zero"))
		def read(value: JsValue) = deserializationError("Unsupported operation", null)
	}

	implicit def eitherDoubleResultOrArithmeticExceptionResultFormat = new RootJsonFormat[Either[Double, ArithmeticException]] {
		def write(t: Either[Double, ArithmeticException]) = t match {
			case Left(d) => doubleResultFormat.write(d)
			case Right(d) => arithmeticExceptionResultFormat.write(d)
		}
		def read(value: JsValue) = deserializationError("Unsupported operation", null)
	}
}

object DoubleResultProtocol extends DoubleResultProtocol