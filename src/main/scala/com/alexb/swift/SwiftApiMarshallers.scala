package com.alexb.swift

import spray.json._
import spray.httpx.SprayJsonSupport

private[swift] trait SwiftApiMarshallers extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val containerFormat = jsonFormat3(Container)

  implicit def objectFormat = new RootJsonFormat[Object] {
    def read(json: JsValue) = json match {
      case x: JsObject => ???
      case _ => deserializationError("Cannot deserialize Object")
    }
    def write(obj: Object) = ???
  }
}
