package com.alexb.protobuf

import scala.reflect.runtime.universe.TypeTag
import com.alexb.protobuf.macros.Macros

object Protobuf {
  import language.experimental.macros
  def serializer[T: TypeTag]: Serializer[T] = new ReflectionProtobufSerializer[T]
  def serializerForList[T: TypeTag]: Serializer[Iterable[T]] = new ListReflectionProtobufSerializer[T]

  def macroSerializer[T]: Serializer[T] = macro Macros.serializer[T]
  def macroSerializerForList[T]: Serializer[Iterable[T]] = macro Macros.listSerializer[T]
}
