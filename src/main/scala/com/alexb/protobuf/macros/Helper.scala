package com.alexb.protobuf.macros

import scala.reflect.macros.Context
import com.google.protobuf.{WireFormat, CodedOutputStream}

class Helper[C <: Context](val c: C) {

  import c.universe._

  /**
   * Constructs expression to write given value expression
   */
  def writePrimitive(tpe: c.Type)(out: c.Expr[CodedOutputStream], number: c.Expr[Int], value: c.Expr[Any]): c.Expr[Unit] = {
    import c.universe.definitions._
    if      (tpe =:= IntTpe)         writeInt(out, number, value.asInstanceOf[c.Expr[Int]])
    else if (tpe =:= LongTpe)        writeLong(out, number, value.asInstanceOf[c.Expr[Long]])
    else if (tpe =:= ShortTpe)       writeShort(out, number, value.asInstanceOf[c.Expr[Short]])
    else if (tpe =:= BooleanTpe)     writeBoolean(out, number, value.asInstanceOf[c.Expr[Boolean]])
    else if (tpe =:= FloatTpe)       writeFloat(out, number, value.asInstanceOf[c.Expr[Float]])
    else if (tpe =:= DoubleTpe)      writeDouble(out, number, value.asInstanceOf[c.Expr[Double]])
    else if (tpe =:= typeOf[String]) writeString(out, number, value.asInstanceOf[c.Expr[String]])
    else throw new IllegalArgumentException("Unsupported primitive type")
  }

  // Serializiers for various primitive types
  def writeInt(out: c.Expr[CodedOutputStream], number: c.Expr[Int], value: c.Expr[Int]): c.Expr[Unit] =
    reify {
      out.splice.writeInt32(number.splice, value.splice)
    }

  def writeLong(out: c.Expr[CodedOutputStream], number: c.Expr[Int], value: c.Expr[Long]): c.Expr[Unit] =
    reify {
      out.splice.writeInt64(number.splice, value.splice)
    }

  def writeShort(out: c.Expr[CodedOutputStream], number: c.Expr[Int], value: c.Expr[Short]): c.Expr[Unit] =
    reify {
      out.splice.writeInt32(number.splice, value.splice)
    }

  def writeBoolean(out: c.Expr[CodedOutputStream], number: c.Expr[Int], value: c.Expr[Boolean]): c.Expr[Unit] =
    reify {
      out.splice.writeBool(number.splice, value.splice)
    }

  def writeFloat(out: c.Expr[CodedOutputStream], number: c.Expr[Int], value: c.Expr[Float]): c.Expr[Unit] =
    reify {
      out.splice.writeFloat(number.splice, value.splice)
    }

  def writeDouble(out: c.Expr[CodedOutputStream], number: c.Expr[Int], value: c.Expr[Double]): c.Expr[Unit] =
    reify {
      out.splice.writeDouble(number.splice, value.splice)
    }

  def writeString(out: c.Expr[CodedOutputStream], number: c.Expr[Int], value: c.Expr[String]): c.Expr[Unit] =
    reify {
      out.splice.writeString(number.splice, value.splice)
    }

  def optional[T](option: c.Expr[Option[T]], body: c.Expr[T] => c.Expr[Unit]): c.Expr[Unit] = {
    val readyBody = body(reify { option.splice.get })
    reify {
      if (option.splice.isDefined) {
        readyBody.splice
      }
    }
  }

  def repeated[T](collection: c.Expr[Seq[T]], body: c.Expr[T] => c.Expr[Unit]): c.Expr[Unit] = {
    val readyBody = body(c.Expr(Ident(newTermName("msg"))))
    reify {
      for (msg <- collection.splice) {
        readyBody.splice
      }
    }
  }

  /**
   * Constructs expression to calculate size of given primitive value field
   */
  def sizeOfPrimitive(tpe: c.Type)(number: c.Expr[Int], value: c.Expr[Any]): c.Expr[Int] = {
    import c.universe.definitions._
    if      (tpe =:= IntTpe)         sizeOfInt(number, value.asInstanceOf[c.Expr[Int]])
    else if (tpe =:= LongTpe)        sizeOfLong(number, value.asInstanceOf[c.Expr[Long]])
    else if (tpe =:= ShortTpe)       sizeOfShort(number, value.asInstanceOf[c.Expr[Short]])
    else if (tpe =:= BooleanTpe)     sizeOfBoolean(number, value.asInstanceOf[c.Expr[Boolean]])
    else if (tpe =:= FloatTpe)       sizeOfFloat(number, value.asInstanceOf[c.Expr[Float]])
    else if (tpe =:= DoubleTpe)      sizeOfDouble(number, value.asInstanceOf[c.Expr[Double]])
    else if (tpe =:= typeOf[String]) sizeOfString(number, value.asInstanceOf[c.Expr[String]])
    else throw new IllegalArgumentException("Unsupported primitive type")
  }

  // Size calculators
  def sizeOfInt(number: c.Expr[Int], value: c.Expr[Int]): c.Expr[Int] =
    reify {
      CodedOutputStream.computeInt32Size(number.splice, value.splice)
    }

  def sizeOfLong(number: c.Expr[Int], value: c.Expr[Long]): c.Expr[Int] =
    reify {
      CodedOutputStream.computeInt64Size(number.splice, value.splice)
    }

  def sizeOfShort(number: c.Expr[Int], value: c.Expr[Short]): c.Expr[Int] =
    reify {
      CodedOutputStream.computeInt32Size(number.splice, value.splice)
    }

  def sizeOfBoolean(number: c.Expr[Int], value: c.Expr[Boolean]): c.Expr[Int] =
    reify {
      CodedOutputStream.computeBoolSize(number.splice, value.splice)
    }

  def sizeOfFloat(number: c.Expr[Int], value: c.Expr[Float]): c.Expr[Int] =
    reify {
      CodedOutputStream.computeFloatSize(number.splice, value.splice)
    }

  def sizeOfDouble(number: c.Expr[Int], value: c.Expr[Double]): c.Expr[Int] =
    reify {
      CodedOutputStream.computeDoubleSize(number.splice, value.splice)
    }

  def sizeOfString(number: c.Expr[Int], value: c.Expr[String]): c.Expr[Int] =
    reify {
      CodedOutputStream.computeStringSize(number.splice, value.splice)
    }

  // Misc
  def writeEmbeddedMessageTagAndSize(out: c.Expr[CodedOutputStream], number: c.Expr[Int], size: c.Expr[Int]): c.Expr[Unit] =
    reify {
      out.splice.writeTag(number.splice, WireFormat.WIRETYPE_LENGTH_DELIMITED)
      out.splice.writeRawVarint32(size.splice)
    }
}
