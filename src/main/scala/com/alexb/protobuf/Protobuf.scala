package com.alexb.protobuf

import scala.reflect.runtime.universe.TypeTag

object Protobuf {
  def serializer[T: TypeTag]: Serializer[T] = new ReflectionProtobufSerializer2[T]
  def serializerForList[T: TypeTag]: Serializer[Iterable[T]] = new ListReflectionProtobufSerializer2[T]
}
