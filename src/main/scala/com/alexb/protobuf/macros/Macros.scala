package com.alexb.protobuf.macros

import scala.reflect.macros.Context
import com.alexb.protobuf._
import com.google.protobuf.CodedOutputStream

object Macros {

  def serializer[T: c.WeakTypeTag](c: Context): c.Expr[Serializer[T]] = {
    import c.universe._

    val tt = implicitly[c.WeakTypeTag[T]]
    val helper = new Helper[c.type](c)
    val rm: helper.mm.RootMessage = helper.mm.apply(tt.tpe)

    val fs = helper.serializeMessage(rm, c.Expr[T](Ident(newTermName("obj"))), c.Expr[CodedOutputStream](Ident(newTermName("output"))))
    println(fs)

    val resultingSerializer = reify {
      new MacroSerializerBase[T] {
        protected def serialize(obj: T, output: CodedOutputStream) {
          fs.splice
        }
      }
    }
    resultingSerializer
  }

  def listSerializer[T: c.WeakTypeTag](c: Context): c.Expr[Serializer[Iterable[T]]] = {
    import c.universe._

    val tt = implicitly[c.WeakTypeTag[T]]
    val helper = new Helper[c.type](c)
    val rm: helper.mm.RootMessage = helper.mm.apply(tt.tpe)

    val fs = helper.serializeMessage(rm, c.Expr[T](Ident(newTermName("obj"))), c.Expr[CodedOutputStream](Ident(newTermName("output"))))
    val ms = helper.messageSize(rm, c.Expr[T](Ident(newTermName("obj"))))
    println(fs)
    println(ms)

    val resultingSerializer = reify {
      new ListMacroSerializerBase[T] {
        protected def serialize(obj: T, output: CodedOutputStream) {
          fs.splice
        }

        protected def size(obj: T): Int = ms.splice
      }
    }
    resultingSerializer
  }
}
