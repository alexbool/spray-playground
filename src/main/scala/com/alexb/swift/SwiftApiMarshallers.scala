package com.alexb.swift

import spray.json.DefaultJsonProtocol

private[swift] trait SwiftApiMarshallers extends DefaultJsonProtocol {
  implicit val containerFormat = jsonFormat3(Container)
}
