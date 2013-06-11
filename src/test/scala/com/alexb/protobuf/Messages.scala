package com.alexb.protobuf

case class Message1(number: Int)
case class Message2(text: String)
case class Message3(number: Option[Int])
case class Message4(numbers: Iterable[Int])
case class Message5(msg: Message1)
case class Message6(msgs: Seq[Message1])
case class Message7(msg: Option[Message1])