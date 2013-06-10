package com.alexb.protobuf.macros

import com.alexb.protobuf.Serializer
import java.io.OutputStream
import com.google.protobuf.CodedOutputStream

trait MacroSerializerBase[T] extends Serializer[T] {
  def serialize(obj: T, output: OutputStream) {
    val cos = CodedOutputStream.newInstance(output)
    serialize(obj, cos)
    cos.flush()
  }

  protected def serialize(obj: T, output: CodedOutputStream)
}
