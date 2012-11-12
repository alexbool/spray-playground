package com.alexb.statics

import spray.json.DefaultJsonProtocol

trait StaticsMarshallers extends DefaultJsonProtocol {

  implicit val cityFormat = jsonFormat2(City)
  implicit val countryFormat = jsonFormat3(Country)
}
