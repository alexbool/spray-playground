package com.alexb.utils

import org.scalatest._
import org.joda.time.Instant
import spray.json.{JsString, JsNumber}

class JodaFormatsSpec extends WordSpec with Matchers with JodaFormats {

	"Joda JsonFormatter" should {
		"serialize Instants" in {
			val instant = new Instant(1351368713660L)
			instantFormat.write(instant) should equal(JsString("2012-10-27T20:11:53Z"))
		}
		"deserialize Instants" in {
			instantFormat.read(JsString("2012-10-27T20:11:53Z")) should equal(new Instant(1351368713000L))
			evaluating { instantFormat.read(JsNumber(10)) } should produce [Exception]
		}
	}
}
