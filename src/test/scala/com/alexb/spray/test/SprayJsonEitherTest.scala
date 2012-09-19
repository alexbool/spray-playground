package com.alexb.spray.test

import cc.spray.json._
import org.scalatest._

case class SuccessResponse(result: Double)
case class ErrorResponse(code: Int, message: String)

object ResponseJsonProtocol extends DefaultJsonProtocol {
	implicit val successFormat = jsonFormat1(SuccessResponse)
	implicit val errorFormat = jsonFormat2(ErrorResponse)

	implicit def eitherSuccessOrErrorFormat = new RootJsonFormat[Either[SuccessResponse, ErrorResponse]] {
		def write(t: Either[SuccessResponse, ErrorResponse]) = t match {
			case Left(d) => successFormat.write(d)
			case Right(d) => errorFormat.write(d)
		}
		def read(value: JsValue) = deserializationError("Unsupported operation", null)
	}
}

class SprayJsonEitherTest extends FlatSpec with ShouldMatchers {
	import ResponseJsonProtocol._

	"spray-json" should "serialize Either[A, B] values" in {
		val a: Either[SuccessResponse, ErrorResponse] = Left(SuccessResponse(2.0))
		val b: Either[SuccessResponse, ErrorResponse] = Right(ErrorResponse(1, "An error occured"))

		val aJson = a.toJson.compactPrint
		val bJson = b.toJson.compactPrint

		aJson should include("\"result\":2.0")
		bJson should include("\"code\":1")
		bJson should include("\"message\":\"An error occured\"")
	}
}