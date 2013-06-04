package com.alexb.protobuf

import scala.reflect.runtime.universe._
import java.io.OutputStream

class ReflectionProtobufSerializer[T: TypeTag] extends Serializer[T] with ProtobufUtils {
  private val rm = RootMessage[T]
  private val descriptor = descriptorFor(rm)

  def serialize(obj: T, output: OutputStream) {
    val message = dynamicMessage(obj, rm, descriptor)
    message.writeTo(output)
  }

  override def serialize(objs: Iterable[T], output: OutputStream) {
    for (obj <- objs) {
      val message = dynamicMessage(obj, rm, descriptor)
      message.writeDelimitedTo(output)
    }
  }
}
