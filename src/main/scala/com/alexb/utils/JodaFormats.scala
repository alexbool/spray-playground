package com.alexb.utils

import org.joda.time.Instant
import org.joda.time.format.ISODateTimeFormat
import org.scala_tools.time.Imports._
import spray.json._

trait JodaFormats extends DefaultJsonProtocol {

  private val timeZone = DateTimeZone.UTC

  implicit def instantFormat = new JsonFormat[Instant] {
    def write(instant: Instant) = JsString(instant.toDateTime(timeZone).toString(ISODateTimeFormat.dateTimeNoMillis))
    def read(json: JsValue) = json match {
      case JsString(str) => Instant.parse(str)
      case _             => deserializationError("Unknown Instant format")
    }
  }
}

object JodaForamts extends JodaFormats
