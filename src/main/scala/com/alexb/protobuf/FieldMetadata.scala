package com.alexb.protobuf

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.{Label, Type => ProtoType}
import scala.reflect.runtime.universe._
import scala.reflect.runtime.universe.definitions._

class FieldMetadata private (val protoType: ProtoType, val label: Label)

object FieldMetadata {
  def apply(field: Field) = fieldMetadata(field)
  private def apply(protoType: ProtoType, label: Label) = new FieldMetadata(protoType, label)

  private def fieldMetadata(field: Field): FieldMetadata = field match {
    case Primitive(getter, optional)     => FieldMetadata(fieldTypeForPrimitive(getter.returnType), if (optional) Label.LABEL_OPTIONAL else Label.LABEL_REQUIRED)
    case EmbeddedMessage(_, _, optional) => FieldMetadata(ProtoType.TYPE_MESSAGE, if (optional) Label.LABEL_OPTIONAL else Label.LABEL_REQUIRED)
    case RepeatedPrimitive(getter)       => FieldMetadata(fieldTypeForPrimitive(typeArguments(getter.returnType).head), Label.LABEL_REPEATED)
    case RepeatedMessage(_, _)           => FieldMetadata(ProtoType.TYPE_MESSAGE, Label.LABEL_REPEATED)
  }

  private def fieldTypeForPrimitive(fieldType: Type): ProtoType = fieldType match {
    case IntTpe     => ProtoType.TYPE_INT32
    case LongTpe    => ProtoType.TYPE_INT64
    case BooleanTpe => ProtoType.TYPE_BOOL
    case FloatTpe   => ProtoType.TYPE_FLOAT
    case DoubleTpe  => ProtoType.TYPE_DOUBLE
    case _          => protoTypeForObject(fieldType)
  }

  private def protoTypeForObject(fieldType: Type): ProtoType =
    if      (fieldType =:= typeOf[String])    ProtoType.TYPE_STRING
    else if (fieldType <:< typeOf[Option[_]]) fieldTypeForPrimitive(typeArguments(fieldType).head)
    else                                      throw new IllegalArgumentException("Unsupported primitive type")

  private def typeArguments(tpe: Type) = tpe match {
    case TypeRef(_, _, args) => args
  }
}
