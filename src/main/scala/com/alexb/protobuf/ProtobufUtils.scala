package com.alexb.protobuf

import com.google.protobuf.DynamicMessage
import com.google.protobuf.Descriptors.{FileDescriptor, Descriptor}
import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, FieldDescriptorProto, DescriptorProto}
import scala.collection.JavaConversions._
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag

trait ProtobufUtils {

  def descriptorFor(msg: RootMessage): Descriptor = {
    val dps = descriptorProtos(msg)
    val fdp = FileDescriptorProto.newBuilder()
      .addAllMessageType(asJavaIterable(dps))
      .build()
    val fd = FileDescriptor.buildFrom(fdp, Array())
    fd.getMessageTypes.get(0)
  }

  private def descriptorProtos(msg: Message): Iterable[DescriptorProto] = {
    val embeddedMessages = collection.mutable.ArrayBuffer[Message]()
    val fieldNumbers = Stream from 1
    val builder = DescriptorProto.newBuilder()
    for ((field, number) <- msg.fields.zip(fieldNumbers)) {
      builder.addField(fieldDescriptorProto(number, field))
      if (field.isInstanceOf[Message]) embeddedMessages += field.asInstanceOf[Message]
    }
    val descriptorProto = builder.setName(msg.messageName).build()
    Iterable(descriptorProto) ++ embeddedMessages.flatMap(descriptorProtos(_))
  }

  private def fieldDescriptorProto(number: Int, field: Field): FieldDescriptorProto = {
    val meta = FieldMetadata(field)
    val builder = FieldDescriptorProto.newBuilder()
      .setNumber(number)
      .setName(field.fieldName)
      .setType(meta.protoType)
      .setLabel(meta.label)
    if (field.isInstanceOf[Message]) builder.setTypeName(field.asInstanceOf[Message].messageName)
    builder.build()
  }

  def dynamicMessage(obj: Any, meta: Message, descriptor: Descriptor) : DynamicMessage = {
    val m = runtimeMirror(getClass.getClassLoader)
    val im = m.reflect(obj)(ClassTag(m.runtimeClass(meta.thisType)))

    def descriptorForSubMessage(msg: Message) = descriptor.getFile.findMessageTypeByName(msg.messageName)

    def fieldValues: Seq[Option[_]] =
      meta.fields
        .map(f => (f, im.reflectMethod(f.getter)()))
        .map(metaAndValue => metaAndValue._1 match {
          case p  @ Primitive(_, _, optional)               => if (optional) metaAndValue._2.asInstanceOf[Option[_]]
                                                               else          Some(metaAndValue._2)
          case m  @ EmbeddedMessage(_, _, fields, optional) => if (optional) metaAndValue._2.asInstanceOf[Option[_]].map(v => dynamicMessage(v, m, descriptorForSubMessage(m)))
                                                               else          Some(dynamicMessage(metaAndValue._2, m, descriptorForSubMessage(m)))
          case rp @ RepeatedPrimitive(_, _)                 => Some(seqAsJavaList(metaAndValue._2.asInstanceOf[Seq[_]]))
          case rm @ RepeatedMessage(_, _, fields)           => Some(seqAsJavaList(metaAndValue._2.asInstanceOf[Seq[_]].map(v => dynamicMessage(v, rm, descriptorForSubMessage(rm)))))
        })

    val builder = DynamicMessage.newBuilder(descriptor)
    for ((fieldDescriptor, value) <- descriptor.getFields.zip(fieldValues)) {
      if (value.isDefined) builder.setField(fieldDescriptor, value.get)
    }
    builder.build()
  }
}
