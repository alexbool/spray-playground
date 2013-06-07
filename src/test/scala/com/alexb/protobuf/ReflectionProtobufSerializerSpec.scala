package com.alexb.protobuf

import scala.reflect.runtime.universe._

class ReflectionProtobufSerializerSpec extends ProtobufSerializerSpec {
  def createSerializer[T: TypeTag] = Protobuf.serializer[T]
  def createListSerializer[T: TypeTag] = Protobuf.serializerForList[T]
}
