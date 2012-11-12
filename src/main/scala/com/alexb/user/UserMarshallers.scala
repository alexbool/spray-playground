package com.alexb.user

import spray.json.DefaultJsonProtocol
import com.alexb.utils.JodaFormats

trait UserMarshallers extends DefaultJsonProtocol with JodaFormats {
  implicit val userFormat = jsonFormat3(User)
  implicit val checkResultFormat = jsonFormat1(CheckResult)
  implicit val registerUserCommandFormat = jsonFormat2(RegisterUserCommand)
}
