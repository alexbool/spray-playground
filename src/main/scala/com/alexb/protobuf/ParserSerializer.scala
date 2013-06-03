package com.alexb.protobuf

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream}

trait Serializer[Message] {
  def serialize(obj: Message, output: OutputStream)

  def serialize(obj: Message): Array[Byte] = {
    val output = new ByteArrayOutputStream
    try {
      serialize(obj, output)
      output.toByteArray
    } finally {
      output.close()
    }
  }

  def serialize(objs: Iterable[Message], output: OutputStream) {
    for (obj <- objs) serialize(obj, output)
  }

  def serialize(objs: Iterable[Message]): Array[Byte] = {
    val output = new ByteArrayOutputStream
    try {
      for (obj <- objs) serialize(obj, output)
      output.toByteArray
    } finally {
      output.close()
    }
  }
}

trait Parser[Message] {

  /**
   * Parses one object from the given input stream
   *
   * @param input Input stream with serialized data to be parsed
   * @return Some(object) when parse is successful, None when input stream has ended
   */
  protected def parseInternal(input: InputStream): Option[Message]

  def parse(input: InputStream): Message = parseInternal(input).get

  def parse(data: Array[Byte]): Message = {
    val input = new ByteArrayInputStream(data)
    try {
      parse(input)
    } finally {
      input.close()
    }
  }

  def parseList(input: InputStream): Seq[Message] = {
    var result = Seq[Message]()
    var objO: Option[Message] = None
    do {
      objO = parseInternal(input)
      result = result ++ objO
    } while (objO.isDefined)
    result
  }

  def parseList(data: Array[Byte]): Seq[Message] = {
    val input = new ByteArrayInputStream(data)
    try {
      parseList(input)
    } finally {
      input.close()
    }
  }
}

trait ParserSerializer[T] extends Parser[T] with Serializer[T]
