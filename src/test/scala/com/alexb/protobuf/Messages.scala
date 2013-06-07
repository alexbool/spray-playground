package com.alexb.protobuf

case class Message(number: Int)
case class Message2(text: String)
case class Message3(number: Option[Int])
case class Message4(numbers: Iterable[Int])
case class Message5(msg: Message)
case class Message6(msgs: Seq[Message])
case class Message7(msg: Option[Message])
