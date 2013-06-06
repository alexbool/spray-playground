package com.alexb.protobuf

import com.google.protobuf.{WireFormat, CodedOutputStream}

trait FieldSerializer {
  def serialize(number: Int, value: Any, out: CodedOutputStream)
  def size(number: Int, value: Any): Int
}

object FieldSerializers {

  val IntSerializer = new FieldSerializer {
    def serialize(number: Int, value: Any, out: CodedOutputStream) {
      out.writeInt64(number, value.asInstanceOf[Int])
    }
    def size(number: Int, value: Any) = CodedOutputStream.computeInt32Size(number, value.asInstanceOf[Int])
  }

  val LongSerializer = new FieldSerializer {
    def serialize(number: Int, value: Any, out: CodedOutputStream) {
      out.writeInt64(number, value.asInstanceOf[Long])
    }
    def size(number: Int, value: Any) = CodedOutputStream.computeInt64Size(number, value.asInstanceOf[Long])
  }

  val ShortSerializer = new FieldSerializer {
    def serialize(number: Int, value: Any, out: CodedOutputStream) {
      out.writeInt64(number, value.asInstanceOf[Short])
    }
    def size(number: Int, value: Any) = CodedOutputStream.computeInt32Size(number, value.asInstanceOf[Short])
  }

  val BooleanSerializer = new FieldSerializer {
    def serialize(number: Int, value: Any, out: CodedOutputStream) {
      out.writeBool(number, value.asInstanceOf[Boolean])
    }
    def size(number: Int, value: Any) = CodedOutputStream.computeInt32Size(number, value.asInstanceOf[Short])
  }

  val FloatSerializer = new FieldSerializer {
    def serialize(number: Int, value: Any, out: CodedOutputStream) {
      out.writeFloat(number, value.asInstanceOf[Float])
    }
    def size(number: Int, value: Any) = CodedOutputStream.computeFloatSize(number, value.asInstanceOf[Float])
  }

  val DoubleSerializer = new FieldSerializer {
    def serialize(number: Int, value: Any, out: CodedOutputStream) {
      out.writeDouble(number, value.asInstanceOf[Double])
    }
    def size(number: Int, value: Any) = CodedOutputStream.computeDoubleSize(number, value.asInstanceOf[Double])
  }

  val StringSerializer = new FieldSerializer {
    def serialize(number: Int, value: Any, out: CodedOutputStream) {
      out.writeString(number, value.asInstanceOf[String])
    }
    def size(number: Int, value: Any) = CodedOutputStream.computeStringSize(number, value.asInstanceOf[String])
  }

  def optional(underlying: FieldSerializer): FieldSerializer = new FieldSerializer {
    def serialize(number: Int, value: Any, out: CodedOutputStream) {
      value.asInstanceOf[Option[_]] foreach { v =>
        underlying.serialize(number, v, out)
      }
    }

    def size(number: Int, value: Any) = value.asInstanceOf[Option[_]] match {
      case Some(v) => underlying.size(number, v)
      case None    => 0
    }
  }

  def repeated(underlying: FieldSerializer): FieldSerializer = new FieldSerializer {
    def serialize(number: Int, value: Any, out: CodedOutputStream) {
      value.asInstanceOf[Seq[_]] foreach { v =>
        underlying.serialize(number, v, out)
      }
    }

    def size(number: Int, value: Any) = value.asInstanceOf[Seq[_]].map(underlying.size(number, _)).sum
  }
}

trait MessageSerializier extends FieldSerializer {
  def serialize(value: Any, out: CodedOutputStream)
  def size(value: Any): Int

  def serialize(number: Int, value: Any, out: CodedOutputStream) {
    out.writeTag(number, WireFormat.WIRETYPE_LENGTH_DELIMITED)
    out.writeRawVarint32(size(value))
    serialize(value, out)
  }

  def size(number: Int, value: Any) = {
    val s = size(value)
    CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeRawVarint32Size(s) + s
  }
}
