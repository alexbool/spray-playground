package com.alexb.utils

import spray.json.DefaultJsonProtocol

case class ErrorDescription(description: String)

trait ErrorDescriptionMarshallers extends DefaultJsonProtocol {
  implicit val errorDescriptionFormat = jsonFormat1(ErrorDescription)
}

object ErrorDescriptionMarshallers extends ErrorDescriptionMarshallers
