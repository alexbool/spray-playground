package com.alexb.protobuf

import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec

class ProtobufSerializerSpec extends WordSpec with MustMatchers {

  case class Message(number: Int)
  case class Message2(text: String)
  case class Message3(number: Option[Int])
  case class Message4(numbers: Iterable[Int])
  case class Message5(msg: Message)
  case class Message6(msgs: Seq[Message])
  case class Message7(msg: Option[Message])

  "Protobuf serializer" must {
    "serialize flat messages" in {
      val serializer = Protobuf.serializer[Message]
      // https://developers.google.com/protocol-buffers/docs/encoding#simple
      serializer.serialize(Message(150)) must equal (Array(0x08, 0x96, 0x01).map(_.toByte))
      serializer.serialize(Message(0)) must equal (Array(0x08, 0x00).map(_.toByte))
    }
    "serialize messages with strings" in {
      val serializer = Protobuf.serializer[Message2]
      // https://developers.google.com/protocol-buffers/docs/encoding#types
      serializer.serialize(Message2("testing")) must equal (Array(0x0a, 0x07, 0x74, 0x65, 0x73, 0x74, 0x69, 0x6e, 0x67).map(_.toByte))
    }
    "serialize messages with optional fields" in {
      val serializer = Protobuf.serializer[Message3]
      // https://developers.google.com/protocol-buffers/docs/encoding#optional
      serializer.serialize(Message3(Some(150))) must equal (Array(0x08, 0x96, 0x01).map(_.toByte))
    }
    "serialize messages with repeated fields" in {
      val serializer = Protobuf.serializer[Message4]
      // https://developers.google.com/protocol-buffers/docs/encoding#optional
      serializer.serialize(Message4(Seq(150, 0))) must equal (Array(0x08, 0x96, 0x01, 0x08, 0x00).map(_.toByte))
    }
    "serialize embedded messages" in {
      val serializer = Protobuf.serializer[Message5]
      serializer.serialize(Message5(Message(150))) must equal (Array(0x0a, 0x03, 0x08, 0x96, 0x01).map(_.toByte))
    }
    "serialize repeated embedded messages" in {
      val serializer = Protobuf.serializer[Message6]
      serializer.serialize(Message6(Seq(Message(150)))) must equal (Array(0x0a, 0x03, 0x08, 0x96, 0x01).map(_.toByte))
    }
    "serialize optional embedded messages" in {
      val serializer = Protobuf.serializer[Message7]
      serializer.serialize(Message7(Some(Message(150)))) must equal (Array(0x0a, 0x03, 0x08, 0x96, 0x01).map(_.toByte))
      serializer.serialize(Message7(None)) must equal (Array())
    }
  }
}
