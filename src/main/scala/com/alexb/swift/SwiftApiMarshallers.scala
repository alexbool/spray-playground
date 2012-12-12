package com.alexb.swift

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

private[swift] trait SwiftApiMarshallers extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val containerFormat = jsonFormat3(Container)
}
