package com.alexb.swift

import spray.json._
import spray.httpx.SprayJsonSupport
import org.joda.time.Instant

private[swift] trait SwiftApiMarshallers extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val containerFormat = jsonFormat3(Container)

  implicit def objectFormat = new RootJsonFormat[ObjectMetadata] {
    def read(json: JsValue) =
      json.asJsObject.getFields("name", "hash", "bytes", "content_type", "last_modified") match {
        case Seq(JsString(name), JsString(hash), JsNumber(bytes), JsString(contentType), JsString(lastModified)) =>
          ObjectMetadata(name, hash, bytes.toLong, contentType, new Instant(lastModified))
        case _ => deserializationError("Cannot deserialize ObjectMetadata")
    }
    def write(obj: ObjectMetadata) = serializationError("Serialization is not implemented")
  }
}
