package com.alexb.protobuf

import scala.reflect.runtime.universe.TypeTag

object Protobuf {
  def serializer[T: TypeTag]: Serializer[T] = new ReflectionProtobufSerializer[T]
}
